import { Response } from 'express';
import { prisma } from '../config/prisma';
import { redis, getCache, setCache, delCache, delCacheByPattern, CACHE_TTL } from '../config/redis';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { parsePagination, parsePositiveInt } from '../utils/http';
import { AuthRequest } from '../middlewares/auth.middleware';
import { getExpiresIn } from '../utils/auth';
import { toRelativeUploadPath } from './upload.controller';
import { areUsersBlocked, canAccessPrivacy } from '../utils/privacy';
import crypto from 'crypto';

const USER_PUBLIC_SELECT = {
  id: true,
  username: true,
  fullName: true,
  avatarUrl: true,
  bio: true,
  city: true,
  role: true,
  _count: {
    select: {
      followers: true,
      following: true,
      posts: { where: { status: 'Active' as const } },
    },
  },
};

export const DEFAULT_AVATAR_URL = '/uploads/images/default-avatar.jpg';

export function resolveAvatarUrl(avatarUrl?: string | null): string {
  const trimmed = typeof avatarUrl === 'string' ? avatarUrl.trim() : '';
  return trimmed || DEFAULT_AVATAR_URL;
}

export function formatUser(user: any) {
  const { id, username, fullName, avatarUrl, bio, city, role, _count, preferences } = user;
  const prefNames = preferences
    ? preferences.map((up: any) => (up.preference ? up.preference.name : up.name || up)).filter(Boolean)
    : [];
  return {
    userId: id,
    username,
    fullName,
    avatarUrl: resolveAvatarUrl(avatarUrl),
    bio,
    city: city ?? null,
    role,
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
    allergies: Array.isArray(user.allergies) ? user.allergies : [],
    accountStatus: user.accountStatus,
    isGoogleAccount: !user.passwordHash,
    createdAt: user.createdAt,
  };
}

// ── GET /users/me ─────────────────────────

export async function getMe(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;

  const user = await prisma.user.findUnique({
    where: { id: userId },
    include: {
      _count: {
        select: {
          followers: true,
          following: true,
          posts: { where: { status: 'Active' } },
        },
      },
      preferences: { include: { preference: true } },
    },
  });

  if (!user) {
    errorResponse(res, 'User not found', 404);
    return;
  }

  successResponse(res, formatUserPrivate(user));
}

// ── PUT /users/me ─────────────────────────

export async function updateMe(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { username, fullName, bio, city, gender, dateOfBirth, avatarUrl, preferences } = req.body;

  let parsedDob: Date | undefined;
  if (dateOfBirth !== undefined && dateOfBirth !== null && dateOfBirth !== '') {
    parsedDob = new Date(dateOfBirth);
    if (Number.isNaN(parsedDob.getTime())) {
      errorResponse(res, 'Ngày sinh không hợp lệ', 400);
      return;
    }
  }

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
        data: prefIds.map((preferenceId) => ({ userId, preferenceId })),
        skipDuplicates: true,
      });
    }
  }

  const normalizedCity =
    city === undefined
      ? undefined
      : typeof city === 'string' && city.trim()
        ? city.trim()
        : null;
  const sanitizedAvatar =
    avatarUrl === undefined
      ? undefined
      : avatarUrl === '' || avatarUrl === null
        ? DEFAULT_AVATAR_URL
        : toRelativeUploadPath(avatarUrl);

  const user = await prisma.user.update({
    where: { id: userId },
    data: {
      username: username || undefined,
      fullName,
      bio,
      city: normalizedCity,
      gender,
      dateOfBirth: parsedDob,
      avatarUrl: sanitizedAvatar,
    },
    include: {
      _count: {
        select: {
          followers: true,
          following: true,
          posts: { where: { status: 'Active' } },
        },
      },
      preferences: { include: { preference: true } },
    },
  });

  await delCache(`user:${userId}`);
  successResponse(res, formatUserPrivate(user), 'Profile updated');
}

// ── GET /users/:userId ────────────────────

export async function getUserById(req: AuthRequest, res: Response): Promise<void> {
  const userId = parsePositiveInt(req.params.userId);
  if (!userId) {
    errorResponse(res, 'Invalid user ID', 400);
    return;
  }

  const cacheKey = `user:${userId}`;
  const viewerId = req.user?.userId;

  const liveMeta = await prisma.user.findUnique({
    where: { id: userId },
    select: {
      accountStatus: true,
      privacySetting: { select: { profileInfo: true } },
    },
  });
  if (!liveMeta || liveMeta.accountStatus !== 'Active') {
    await delCache(cacheKey);
    errorResponse(res, 'User not found', 404);
    return;
  }
  if (viewerId && viewerId !== userId && (await areUsersBlocked(userId, viewerId))) {
    errorResponse(res, 'User not found', 404);
    return;
  }

  let baseProfile = await getCache<ReturnType<typeof formatUser>>(cacheKey);

  if (!baseProfile) {
    const user = await prisma.user.findUnique({
      where: { id: userId, accountStatus: 'Active' },
      select: USER_PUBLIC_SELECT,
    });
    if (!user) {
      errorResponse(res, 'User not found', 404);
      return;
    }
    baseProfile = formatUser(user);
    await setCache(cacheKey, baseProfile, CACHE_TTL.USER_PROFILE);
  }

  let isFollowing = false;
  if (viewerId && viewerId !== userId) {
    const follow = await prisma.follow.findUnique({
      where: {
        followerId_followingId: {
          followerId: viewerId,
          followingId: userId,
        },
      },
      select: { followerId: true },
    });
    isFollowing = Boolean(follow);
  }

  const canViewInfo = await canAccessPrivacy(liveMeta.privacySetting?.profileInfo, userId, viewerId);
  const profileData = canViewInfo
    ? { ...baseProfile, profileRestricted: false }
    : { ...baseProfile, bio: null, city: null, preferences: [], profileRestricted: true };

  successResponse(res, { ...profileData, isFollowing });
}

// ── GET /users/:userId/posts ──────────────

export async function getUserPosts(req: AuthRequest, res: Response): Promise<void> {
  const isMe = req.params.userId === 'me' || !req.params.userId;
  let targetUserId: number | null = null;
  if (isMe) {
    if (!req.user?.userId) {
      errorResponse(res, 'Authentication required', 401);
      return;
    }
    targetUserId = req.user.userId;
  } else {
    targetUserId = parsePositiveInt(req.params.userId);
    if (!targetUserId) {
      errorResponse(res, 'Invalid user ID', 400);
      return;
    }
  }

  const viewerId = req.user?.userId;
  if (!isMe && viewerId !== targetUserId) {
    const targetUser = await prisma.user.findUnique({
      where: { id: targetUserId },
      select: {
        accountStatus: true,
        privacySetting: { select: { newPosts: true } },
      },
    });
    if (!targetUser || targetUser.accountStatus !== 'Active') {
      errorResponse(res, 'User not found', 404);
      return;
    }
    if (viewerId && (await areUsersBlocked(targetUserId, viewerId))) {
      errorResponse(res, 'User not found', 404);
      return;
    }
    if (!(await canAccessPrivacy(targetUser.privacySetting?.newPosts, targetUserId, viewerId))) {
      errorResponse(res, 'Người dùng đã giới hạn quyền xem bài viết', 403);
      return;
    }
  }

  const { page, limit, skip } = parsePagination(req.query, 10, 100);
  const requestedStatus = typeof req.query.status === 'string' ? req.query.status : 'Active';
  const postStatus: 'Active' | 'Hidden' = isMe && (requestedStatus === 'Active' || requestedStatus === 'Hidden')
    ? requestedStatus
    : 'Active';

  const postWhere = {
    authorId: targetUserId,
    status: postStatus,
    author: { accountStatus: 'Active' as const },
  };

  const [posts, total] = await Promise.all([
    prisma.post.findMany({
      where: postWhere,
      include: {
        author: { select: USER_PUBLIC_SELECT },
        images: true,
        _count: {
          select: {
            likes: true,
            comments: { where: { author: { accountStatus: 'Active' } } },
            shares: true,
          },
        },
      },
      orderBy: { createdAt: 'desc' },
      skip,
      take: limit,
    }),
    prisma.post.count({ where: postWhere }),
  ]);

  const formatted = await attachPostFlags(posts.map(formatPost), req.user?.userId);
  paginatedResponse(res, formatted, { page, limit, total });
}

/** Adds `isSaved` and `isLiked` to each formatted post for the signed-in user. */
export async function attachPostFlags<T extends { postId: number }>(
  posts: T[],
  userId?: number,
): Promise<(T & { isSaved: boolean; isLiked: boolean })[]> {
  if (!userId || posts.length === 0) {
    return posts.map((p) => ({ ...p, isSaved: false, isLiked: false }));
  }
  const postIds = posts.map((p) => p.postId);
  const [saved, liked] = await Promise.all([
    prisma.savedPost.findMany({
      where: { userId, postId: { in: postIds } },
      select: { postId: true },
    }),
    prisma.postLike.findMany({
      where: { userId, postId: { in: postIds } },
      select: { postId: true },
    }),
  ]);
  const savedIds = new Set(saved.map((s) => s.postId));
  const likedIds = new Set(liked.map((l) => l.postId));
  return posts.map((p) => ({
    ...p,
    isSaved: savedIds.has(p.postId),
    isLiked: likedIds.has(p.postId),
  }));
}

export const attachSavedFlags = attachPostFlags;

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

const PRIVACY_KEYS = new Set([
  'newPosts',
  'profileInfo',
  'followingList',
  'findEmail',
  'friendRequests',
  'messages',
  'comments',
]);

function formatPrivacy(row: any) {
  return {
    newPosts: row.newPosts,
    profileInfo: row.profileInfo,
    followingList: row.followingList,
    findEmail: row.findEmail,
    friendRequests: row.friendRequests,
    messages: row.messages,
    comments: row.comments,
  };
}

function getInitials(name: string): string {
  const parts = String(name || 'U').trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return 'U';
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

async function fetchBlockedList(userId: number) {
  const rows = await prisma.blockedAccount.findMany({
    where: { userId },
    include: {
      blockedUser: {
        select: { id: true, username: true, fullName: true, avatarUrl: true },
      },
    },
    orderBy: { createdAt: 'desc' },
  });
  return rows.map((r) => ({
    id: r.blockedUser.id,
    userId: r.blockedUser.id,
    username: r.blockedUser.username,
    name: r.blockedUser.fullName,
    avatarUrl: resolveAvatarUrl(r.blockedUser.avatarUrl),
    initials: getInitials(r.blockedUser.fullName),
    status: 'Đã chặn',
  }));
}

async function fetchRestrictedList(userId: number) {
  const rows = await prisma.restrictedAccount.findMany({
    where: { userId },
    include: {
      restrictedUser: {
        select: { id: true, username: true, fullName: true, avatarUrl: true },
      },
    },
    orderBy: { createdAt: 'desc' },
  });
  return rows.map((r) => ({
    id: r.restrictedUser.id,
    userId: r.restrictedUser.id,
    username: r.restrictedUser.username,
    name: r.restrictedUser.fullName,
    avatarUrl: resolveAvatarUrl(r.restrictedUser.avatarUrl),
    initials: getInitials(r.restrictedUser.fullName),
    status: 'Đã hạn chế',
  }));
}

export async function getPrivacySettings(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const row = await prisma.userPrivacy.upsert({
    where: { userId },
    create: { userId },
    update: {},
  });

  res.status(200).json({
    success: true,
    data: formatPrivacy(row),
    message: 'Lấy cài đặt quyền riêng tư thành công',
  });
}

export async function updatePrivacySettings(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const updates: Record<string, string> = {};
  for (const [k, v] of Object.entries(req.body || {})) {
    if (PRIVACY_KEYS.has(k) && typeof v === 'string' && v.trim()) {
      updates[k] = v.trim();
    }
  }

  const row = await prisma.userPrivacy.upsert({
    where: { userId },
    create: { userId, ...updates },
    update: updates,
  });

  await Promise.all([delCache(`user:${userId}`), delCacheByPattern('newsfeed:*')]);

  res.status(200).json({
    success: true,
    data: formatPrivacy(row),
    message: 'Cập nhật quyền riêng tư thành công',
  });
}

export async function getBlockedAccounts(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const blocked = await fetchBlockedList(userId);

  res.status(200).json({
    success: true,
    data: blocked,
    message: 'Lấy danh sách đã chặn thành công',
  });
}

export async function blockAccount(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const targetId = parsePositiveInt(req.body?.accountId ?? req.body?.targetUserId ?? req.body?.userId);
  if (!targetId || targetId === userId) {
    errorResponse(res, 'ID tài khoản không hợp lệ', 400);
    return;
  }

  const target = await prisma.user.findUnique({ where: { id: targetId }, select: { id: true } });
  if (!target) {
    errorResponse(res, 'Không tìm thấy tài khoản', 404);
    return;
  }

  await prisma.blockedAccount.upsert({
    where: { userId_blockedUserId: { userId, blockedUserId: targetId } },
    create: { userId, blockedUserId: targetId },
    update: {},
  });

  await Promise.all([delCache(`user:${userId}`), delCache(`user:${targetId}`)]);

  const blocked = await fetchBlockedList(userId);
  res.status(200).json({
    success: true,
    data: blocked,
    message: 'Đã chặn tài khoản thành công',
  });
}

export async function unblockAccount(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const targetId = parsePositiveInt(req.body?.accountId ?? req.body?.targetUserId ?? req.body?.userId);
  if (!targetId) {
    errorResponse(res, 'ID tài khoản không hợp lệ', 400);
    return;
  }

  await prisma.blockedAccount.deleteMany({
    where: { userId, blockedUserId: targetId },
  });

  await Promise.all([delCache(`user:${userId}`), delCache(`user:${targetId}`)]);

  const blocked = await fetchBlockedList(userId);
  res.status(200).json({
    success: true,
    data: blocked,
    message: 'Đã bỏ chặn tài khoản thành công',
  });
}

export async function getRestrictedAccounts(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const restricted = await fetchRestrictedList(userId);

  res.status(200).json({
    success: true,
    data: restricted,
    message: 'Lấy danh sách bị hạn chế thành công',
  });
}

export async function restrictAccount(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const targetId = parsePositiveInt(req.body?.accountId ?? req.body?.targetUserId ?? req.body?.userId);
  if (!targetId || targetId === userId) {
    errorResponse(res, 'ID tài khoản không hợp lệ', 400);
    return;
  }

  const target = await prisma.user.findUnique({ where: { id: targetId }, select: { id: true } });
  if (!target) {
    errorResponse(res, 'Không tìm thấy tài khoản', 404);
    return;
  }

  await prisma.restrictedAccount.upsert({
    where: { userId_restrictedUserId: { userId, restrictedUserId: targetId } },
    create: { userId, restrictedUserId: targetId },
    update: {},
  });

  const restricted = await fetchRestrictedList(userId);
  res.status(200).json({
    success: true,
    data: restricted,
    message: 'Đã hạn chế tài khoản thành công',
  });
}

export async function unrestrictAccount(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const targetId = parsePositiveInt(req.body?.accountId ?? req.body?.targetUserId ?? req.body?.userId);
  if (!targetId) {
    errorResponse(res, 'ID tài khoản không hợp lệ', 400);
    return;
  }

  await prisma.restrictedAccount.deleteMany({
    where: { userId, restrictedUserId: targetId },
  });

  const restricted = await fetchRestrictedList(userId);
  res.status(200).json({
    success: true,
    data: restricted,
    message: 'Đã bỏ hạn chế tài khoản thành công',
  });
}


export async function getMyActivity(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const typeFilter = typeof req.query.type === 'string' ? req.query.type.trim() : '';
  const daysParam = parsePositiveInt(typeof req.query.days === 'string' ? req.query.days : undefined);
  const since = daysParam ? new Date(Date.now() - daysParam * 24 * 60 * 60 * 1000) : undefined;
  const dateFilter = since ? { gte: since } : undefined;

  const [posts, comments, likes, reviews, saved] = await Promise.all([
    !typeFilter || typeFilter === 'post'
      ? prisma.post.findMany({
          where: { authorId: userId, status: { in: ['Active', 'Hidden'] }, ...(dateFilter ? { createdAt: dateFilter } : {}) },
          select: { id: true, content: true, locationUrl: true, status: true, createdAt: true },
          orderBy: { createdAt: 'desc' },
          take: 30,
        })
      : Promise.resolve([]),
    !typeFilter || typeFilter === 'comment'
      ? prisma.comment.findMany({
          where: { authorId: userId, ...(dateFilter ? { createdAt: dateFilter } : {}) },
          include: { post: { select: { id: true, content: true } } },
          orderBy: { createdAt: 'desc' },
          take: 30,
        })
      : Promise.resolve([]),
    !typeFilter || typeFilter === 'like'
      ? prisma.postLike.findMany({
          where: { userId, ...(dateFilter ? { createdAt: dateFilter } : {}) },
          include: { post: { select: { id: true, content: true } } },
          orderBy: { createdAt: 'desc' },
          take: 30,
        })
      : Promise.resolve([]),
    !typeFilter || typeFilter === 'review'
      ? prisma.review.findMany({
          where: { authorId: userId, ...(dateFilter ? { createdAt: dateFilter } : {}) },
          select: { id: true, restaurantName: true, rating: true, content: true, createdAt: true },
          orderBy: { createdAt: 'desc' },
          take: 30,
        })
      : Promise.resolve([]),
    !typeFilter || typeFilter === 'save'
      ? prisma.savedPost.findMany({
          where: { userId, ...(dateFilter ? { createdAt: dateFilter } : {}) },
          include: { post: { select: { id: true, content: true } } },
          orderBy: { createdAt: 'desc' },
          take: 30,
        })
      : Promise.resolve([]),
  ]);

  const items: any[] = [];
  for (const p of posts) {
    items.push({
      id: `post-${p.id}`,
      type: 'post',
      targetId: p.id,
      title: p.status === 'Hidden' ? 'Bạn đã lưu trữ một bài viết' : 'Bạn đã đăng một bài viết',
      snippet: p.content,
      createdAt: p.createdAt,
    });
  }
  for (const c of comments) {
    items.push({
      id: `comment-${c.id}`,
      type: 'comment',
      targetId: c.postId,
      title: 'Bạn đã bình luận',
      snippet: `“${c.content}”`,
      createdAt: c.createdAt,
    });
  }
  for (const l of likes) {
    items.push({
      id: `like-${l.postId}`,
      type: 'like',
      targetId: l.postId,
      title: 'Bạn đã thích một bài viết',
      snippet: l.post?.content || '',
      createdAt: l.createdAt,
    });
  }
  for (const r of reviews) {
    items.push({
      id: `review-${r.id}`,
      type: 'review',
      targetId: r.id,
      title: `Bạn đã đánh giá ${r.restaurantName} (${r.rating}★)`,
      snippet: r.content,
      createdAt: r.createdAt,
    });
  }
  for (const s of saved) {
    items.push({
      id: `save-${s.postId}`,
      type: 'save',
      targetId: s.postId,
      title: 'Bạn đã lưu một bài viết',
      snippet: s.post?.content || '',
      createdAt: s.createdAt,
    });
  }

  items.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  successResponse(res, items.slice(0, 60));
}

// ── Delete own account ───────────────────
// Tries a hard delete; if related rows block it (FK constraints), the account is deactivated instead
// so it can no longer sign in. Either way the current token is revoked.
export async function deleteMe(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const token = req.headers.authorization?.slice(7).trim() || '';
  const sessions = await prisma.userSession.findMany({
    where: { userId },
    select: { tokenHash: true },
  });

  let mode: 'deleted' | 'deactivated' = 'deleted';
  try {
    await prisma.user.delete({ where: { id: userId } });
  } catch {
    mode = 'deactivated';
    await prisma.$transaction([
      prisma.user.update({
        where: { id: userId },
        data: { accountStatus: 'Suspended', refreshToken: null },
      }),
      prisma.userSession.deleteMany({ where: { userId } }),
    ]);
  }

  const ttl = getExpiresIn();
  const blacklistOps: Promise<any>[] = [];
  if (token) {
    const currentHash = crypto.createHash('sha256').update(token).digest('hex');
    blacklistOps.push(
      redis.set(`blacklist:${token}`, '1', 'EX', ttl),
      redis.set(`blacklist_hash:${currentHash}`, '1', 'EX', ttl)
    );
  }
  for (const s of sessions) {
    if (s.tokenHash) {
      blacklistOps.push(redis.set(`blacklist_hash:${s.tokenHash}`, '1', 'EX', ttl));
    }
  }
  const keys = ['privacy', 'blocked', 'restricted', 'sessions', '2fa_enabled'].map((k) => `user:${userId}:${k}`);
  blacklistOps.push(
    redis.del(...keys),
    delCache(`user:${userId}`),
    delCache('search:trending'),
    delCacheByPattern('post:*'),
    delCacheByPattern('newsfeed:*')
  );
  await Promise.all(blacklistOps);

  successResponse(res, { mode }, mode === 'deleted' ? 'Tài khoản đã được xóa' : 'Tài khoản đã bị vô hiệu hóa');
}
