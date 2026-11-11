import { Response } from 'express';
import { prisma } from '../config/prisma';
import { redis, getCache, setCache, delCache, CACHE_TTL } from '../config/redis';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';
import { getExpiresIn, verifyAccessToken } from '../utils/auth';

const USER_PUBLIC_SELECT = {
  id: true, username: true, fullName: true, avatarUrl: true, bio: true, role: true,
  _count: { select: { followers: true, following: true, posts: true } },
};

// Explicit whitelist for the owner's own profile: never select passwordHash / refreshToken.
const USER_PRIVATE_SELECT = {
  ...USER_PUBLIC_SELECT,
  email: true, gender: true, dateOfBirth: true, accountStatus: true, createdAt: true,
  preferences: { include: { preference: true } },
};

/** Resolves the caller's id from a real, verifiable access token (ignores the demo fallback in the
 *  auth middleware). Sends 401 and returns null when there is none. */
function requireTokenUserId(req: AuthRequest, res: Response): number | null {
  const header = req.headers.authorization;
  const token = header?.startsWith('Bearer ') ? header.split(' ')[1] : '';
  try {
    return verifyAccessToken(token).userId;
  } catch {
    errorResponse(res, 'Invalid or expired token', 401);
    return null;
  }
}

export function formatUser(user: any) {
  // passwordHash / refreshToken are dropped defensively in case a caller passes a full User row.
  const { id, _count, preferences, passwordHash, refreshToken, ...rest } = user;
  const prefNames = preferences
    ? preferences.map((up: any) => (up.preference ? up.preference.name : up.name || up)).filter(Boolean)
    : [];
  return {
    userId: id,
    ...rest,
    preferences: prefNames,
    postsCount: _count?.posts ?? 0,
    followersCount: _count?.followers ?? 0,
    followingCount: _count?.following ?? 0,
  };
}

export function formatUserPrivate(user: any) {
  return {
    ...formatUser(user),
    email: user.email,
    gender: user.gender,
    dateOfBirth: user.dateOfBirth,
    accountStatus: user.accountStatus,
    createdAt: user.createdAt,
  };
}

// ── GET /users/me ─────────────────────────

export async function getMe(req: AuthRequest, res: Response): Promise<void> {
  const userId = requireTokenUserId(req, res);
  if (userId === null) return;

  const user = await prisma.user.findUnique({
    where: { id: userId },
    select: USER_PRIVATE_SELECT,
  });

  if (!user) {
    errorResponse(res, 'User not found', 404);
    return;
  }

  successResponse(res, formatUserPrivate(user));
}

// ── PUT /users/me ─────────────────────────

export async function updateMe(req: AuthRequest, res: Response): Promise<void> {
  const userId = requireTokenUserId(req, res);
  if (userId === null) return;
  const { username, fullName, bio, gender, dateOfBirth, avatarUrl, preferences } = req.body;

  if (username) {
    const existing = await prisma.user.findFirst({
      where: { username, NOT: { id: userId } },
    });
    if (existing) {
      errorResponse(res, 'Tên người dùng này đã được sử dụng', 409);
      return;
    }
  }

  if (Array.isArray(preferences)) {
    const prefIds: number[] = [];
    for (const pName of preferences) {
      if (!pName || typeof pName !== 'string') continue;
      const cleanName = pName.trim();
      if (!cleanName) continue;
      let pref = await prisma.foodPreference.findFirst({ where: { name: cleanName } });
      if (!pref) {
        pref = await prisma.foodPreference.create({ data: { name: cleanName } });
      }
      prefIds.push(pref.id);
    }
    await prisma.userPreference.deleteMany({ where: { userId } });
    if (prefIds.length > 0) {
      await prisma.userPreference.createMany({
        data: prefIds.map(preferenceId => ({ userId, preferenceId })),
        skipDuplicates: true,
      });
    }
  }

  const user = await prisma.user.update({
    where: { id: userId },
    data: {
      username: username || undefined,
      fullName,
      bio,
      gender,
      dateOfBirth: dateOfBirth ? new Date(dateOfBirth) : undefined,
      avatarUrl,
    },
    select: USER_PRIVATE_SELECT,
  });

  await delCache(`user:${userId}`);
  successResponse(res, formatUserPrivate(user), 'Profile updated');
}

// ── GET /users/:userId ────────────────────

export async function getUserById(req: AuthRequest, res: Response): Promise<void> {
  const userId = parseInt(req.params.userId);
  // Non-numeric ids (e.g. "user_linhdan") used to reach Prisma as NaN and crash the request.
  if (!/^\d+$/.test(req.params.userId) || userId <= 0) {
    errorResponse(res, 'Invalid userId', 400);
    return;
  }

  const cacheKey = `user:${userId}`;
  let formatted: any = await getCache(cacheKey);
  if (!formatted) {
    const user = await prisma.user.findUnique({
      where: { id: userId },
      select: USER_PUBLIC_SELECT,
    });
    if (!user) { errorResponse(res, 'User not found', 404); return; }
    formatted = formatUser(user);
    await setCache(cacheKey, formatted, CACHE_TTL.USER_PROFILE);
  }

  // isFollowing is viewer-specific, so it is computed per request and never cached.
  let isFollowing = false;
  const header = req.headers.authorization;
  if (header?.startsWith('Bearer ')) {
    try {
      const viewerId = verifyAccessToken(header.split(' ')[1]).userId;
      if (viewerId !== userId) {
        const follow = await prisma.follow.findUnique({
          where: { followerId_followingId: { followerId: viewerId, followingId: userId } },
        });
        isFollowing = !!follow;
      }
    } catch { /* guest / invalid token: not following */ }
  }

  successResponse(res, { ...formatted, isFollowing });
}

// ── GET /users/:userId/posts ──────────────

export async function getUserPosts(req: AuthRequest, res: Response): Promise<void> {
  const isMe = req.params.userId === 'me' || !req.params.userId;
  let userId: number;
  if (isMe) {
    const me = requireTokenUserId(req, res);
    if (me === null) return;
    userId = me;
  } else {
    userId = parseInt(req.params.userId);
    if (!Number.isInteger(userId) || userId <= 0) {
      errorResponse(res, 'Invalid userId', 400);
      return;
    }
  }
  const page = Math.max(parseInt(req.query.page as string) || 1, 1);
  const limit = Math.min(Math.max(parseInt(req.query.limit as string) || 10, 1), 50);
  const skip = (page - 1) * limit;

  // Only this user's own posts — an empty profile must stay empty.
  const where = { authorId: userId, status: 'Active' as const };
  const [posts, total] = await Promise.all([
    prisma.post.findMany({
      where,
      include: {
        author: { select: USER_PUBLIC_SELECT },
        images: true,
        _count: { select: { likes: true, comments: true, shares: true } },
      },
      orderBy: { createdAt: 'desc' },
      skip,
      take: limit,
    }),
    prisma.post.count({ where }),
  ]);

  const formatted = await attachSavedFlags(posts.map(formatPost), req.user?.userId);
  paginatedResponse(res, formatted, { page, limit, total });
}

/** Adds `isSaved` to each formatted post for the signed-in user (false for guests). */
export async function attachSavedFlags<T extends { postId: number }>(
  posts: T[],
  userId?: number,
): Promise<(T & { isSaved: boolean })[]> {
  if (!userId || posts.length === 0) return posts.map((p) => ({ ...p, isSaved: false }));
  const saved = await prisma.savedPost.findMany({
    where: { userId, postId: { in: posts.map((p) => p.postId) } },
    select: { postId: true },
  });
  const savedIds = new Set(saved.map((s) => s.postId));
  return posts.map((p) => ({ ...p, isSaved: savedIds.has(p.postId) }));
}

export function formatPost(post: any) {
  const { id, _count, author, ...rest } = post;
  return {
    postId: id,
    author: author ? formatUser(author) : undefined,
    ...rest,
    likesCount: _count?.likes ?? 0,
    commentsCount: _count?.comments ?? 0,
    sharesCount: _count?.shares ?? 0,
  };
}

// ── Privacy & Blocked/Restricted Accounts ─────────

const DEFAULT_PRIVACY = {
  newPosts: 'friends',
  profileInfo: 'only_me',
  followingList: 'friends',
  findEmail: 'no_one',
  friendRequests: 'friends_of_friends',
  messages: 'friends',
  comments: 'friends',
};

const DEFAULT_BLOCKED = [
  { id: 1, name: 'Minh Anh', initials: 'MA', status: 'Đã chặn' },
  { id: 2, name: 'Hoàng Nam', initials: 'HN', status: 'Đã chặn' },
  { id: 3, name: 'Thanh Trúc', initials: 'TT', status: 'Đã chặn' },
];

const DEFAULT_RESTRICTED = [
  { id: 101, name: 'Ngọc Linh', initials: 'NL', status: 'Đã hạn chế' },
];

export async function getPrivacySettings(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId || 1;
  const raw = await redis.get(`user:${userId}:privacy`);

  let privacy = DEFAULT_PRIVACY;
  if (raw) {
    try {
      privacy = { ...DEFAULT_PRIVACY, ...JSON.parse(raw) };
    } catch {
      privacy = DEFAULT_PRIVACY;
    }
  } else {
    await redis.set(`user:${userId}:privacy`, JSON.stringify(DEFAULT_PRIVACY));
  }

  res.status(200).json({
    success: true,
    data: privacy,
    message: 'Lấy cài đặt quyền riêng tư thành công',
  });
}

export async function updatePrivacySettings(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId || 1;
  const updates = req.body;

  const raw = await redis.get(`user:${userId}:privacy`);
  let privacy = DEFAULT_PRIVACY;
  if (raw) {
    try {
      privacy = { ...DEFAULT_PRIVACY, ...JSON.parse(raw) };
    } catch {
      privacy = DEFAULT_PRIVACY;
    }
  }

  const updatedPrivacy = { ...privacy, ...updates };
  await redis.set(`user:${userId}:privacy`, JSON.stringify(updatedPrivacy));

  res.status(200).json({
    success: true,
    data: updatedPrivacy,
    message: 'Cập nhật quyền riêng tư thành công',
  });
}

export async function getBlockedAccounts(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId || 1;
  const raw = await redis.get(`user:${userId}:blocked`);

  let blocked = DEFAULT_BLOCKED;
  if (raw) {
    try {
      blocked = JSON.parse(raw);
    } catch {
      blocked = DEFAULT_BLOCKED;
    }
  } else {
    await redis.set(`user:${userId}:blocked`, JSON.stringify(DEFAULT_BLOCKED));
  }

  res.status(200).json({
    success: true,
    data: blocked,
    message: 'Lấy danh sách đã chặn thành công',
  });
}

export async function unblockAccount(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId || 1;
  const { accountId } = req.body;

  const raw = await redis.get(`user:${userId}:blocked`);
  let blocked = DEFAULT_BLOCKED;
  if (raw) {
    try {
      blocked = JSON.parse(raw);
    } catch {
      blocked = DEFAULT_BLOCKED;
    }
  }

  blocked = blocked.filter((item: any) => item.id !== accountId && String(item.id) !== String(accountId));
  await redis.set(`user:${userId}:blocked`, JSON.stringify(blocked));

  res.status(200).json({
    success: true,
    data: blocked,
    message: 'Đã bỏ chặn tài khoản thành công',
  });
}

export async function getRestrictedAccounts(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId || 1;
  const raw = await redis.get(`user:${userId}:restricted`);

  let restricted = DEFAULT_RESTRICTED;
  if (raw) {
    try {
      restricted = JSON.parse(raw);
    } catch {
      restricted = DEFAULT_RESTRICTED;
    }
  } else {
    await redis.set(`user:${userId}:restricted`, JSON.stringify(DEFAULT_RESTRICTED));
  }

  res.status(200).json({
    success: true,
    data: restricted,
    message: 'Lấy danh sách bị hạn chế thành công',
  });
}

export async function unrestrictAccount(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId || 1;
  const { accountId } = req.body;

  const raw = await redis.get(`user:${userId}:restricted`);
  let restricted = DEFAULT_RESTRICTED;
  if (raw) {
    try {
      restricted = JSON.parse(raw);
    } catch {
      restricted = DEFAULT_RESTRICTED;
    }
  }

  restricted = restricted.filter((item: any) => item.id !== accountId && String(item.id) !== String(accountId));
  await redis.set(`user:${userId}:restricted`, JSON.stringify(restricted));

  res.status(200).json({
    success: true,
    data: restricted,
    message: 'Đã bỏ hạn chế tài khoản thành công',
  });
}


// ── Delete own account ───────────────────
// Tries a hard delete; if related rows block it (FK constraints), the account is deactivated instead
// so it can no longer sign in. Either way the current token is revoked.
export async function deleteMe(req: AuthRequest, res: Response): Promise<void> {
  // Never trust the demo fallback in `authenticate` for an irreversible action:
  // require a real, verifiable access token and take the user id from it.
  const token = req.headers.authorization?.startsWith('Bearer ') ? req.headers.authorization.split(' ')[1] : '';
  let userId: number;
  try {
    userId = verifyAccessToken(token).userId;
  } catch {
    errorResponse(res, 'Invalid or expired token', 401);
    return;
  }

  let mode: 'deleted' | 'deactivated' = 'deleted';
  try {
    await prisma.user.delete({ where: { id: userId } });
  } catch {
    mode = 'deactivated';
    await prisma.user.update({
      where: { id: userId },
      data: { accountStatus: 'Suspended', refreshToken: null },
    });
  }

  await redis.set(`blacklist:${token}`, '1', 'EX', getExpiresIn());
  const keys = ['privacy', 'blocked', 'restricted', 'sessions', '2fa_enabled'].map((k) => `user:${userId}:${k}`);
  await redis.del(...keys);

  successResponse(res, { mode }, mode === 'deleted' ? 'Tài khoản đã được xóa' : 'Tài khoản đã bị vô hiệu hóa');
}
