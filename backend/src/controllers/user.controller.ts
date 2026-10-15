import { Response } from 'express';
import { prisma } from '../config/prisma';
import { getCache, setCache, delCache, CACHE_TTL } from '../config/redis';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';

const USER_PUBLIC_SELECT = {
  id: true, username: true, fullName: true, avatarUrl: true, bio: true, role: true,
  _count: { select: { followers: true, following: true } },
};

export function formatUser(user: any) {
  const { id, _count, ...rest } = user;
  return {
    userId: id,
    ...rest,
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
  const userId = req.user!.userId;

  const user = await prisma.user.findUnique({
    where: { id: userId },
    include: { _count: { select: { followers: true, following: true } } },
  });
  if (!user) { errorResponse(res, 'User not found', 404); return; }

  successResponse(res, formatUserPrivate(user));
}

// ── PUT /users/me ─────────────────────────

export async function updateMe(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { fullName, bio, gender, dateOfBirth, avatarUrl } = req.body;

  const user = await prisma.user.update({
    where: { id: userId },
    data: {
      fullName,
      bio,
      gender,
      dateOfBirth: dateOfBirth ? new Date(dateOfBirth) : undefined,
      avatarUrl,
    },
    include: { _count: { select: { followers: true, following: true } } },
  });

  await delCache(`user:${userId}`);
  successResponse(res, formatUserPrivate(user), 'Profile updated');
}

// ── GET /users/:userId ────────────────────

export async function getUserById(req: AuthRequest, res: Response): Promise<void> {
  const userId = parseInt(req.params.userId);

  const cacheKey = `user:${userId}`;
  const cached = await getCache(cacheKey);
  if (cached) { successResponse(res, cached); return; }

  const user = await prisma.user.findUnique({
    where: { id: userId },
    select: USER_PUBLIC_SELECT,
  });
  if (!user) { errorResponse(res, 'User not found', 404); return; }

  const formatted = formatUser(user);
  await setCache(cacheKey, formatted, CACHE_TTL.USER_PROFILE);
  successResponse(res, formatted);
}

// ── GET /users/:userId/posts ──────────────

export async function getUserPosts(req: AuthRequest, res: Response): Promise<void> {
  const userId = parseInt(req.params.userId);
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 10;
  const skip = (page - 1) * limit;

  const [posts, total] = await Promise.all([
    prisma.post.findMany({
      where: { authorId: userId, status: 'Active' },
      include: {
        author: { select: USER_PUBLIC_SELECT },
        images: true,
        _count: { select: { likes: true, comments: true, shares: true } },
      },
      orderBy: { createdAt: 'desc' },
      skip,
      take: limit,
    }),
    prisma.post.count({ where: { authorId: userId, status: 'Active' } }),
  ]);

  paginatedResponse(res, posts.map(formatPost), { page, limit, total });
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
