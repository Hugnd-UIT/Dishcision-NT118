import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { getCache, setCache, delCache, delCacheByPattern, CACHE_TTL } from '../config/redis';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';
import { formatUser } from './user.controller';

export const POST_INCLUDE = {
  author: {
    select: {
      id: true, username: true, fullName: true, avatarUrl: true,
      bio: true, role: true, _count: { select: { followers: true, following: true } },
    },
  },
  images: true,
  _count: { select: { likes: true, comments: true, shares: true } },
};

export function formatPost(post: any, userId?: number) {
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

// ── GET /posts ────────────────────────────

export async function getNewsfeed(req: AuthRequest, res: Response): Promise<void> {
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 10;
  const sort = (req.query.sort as string) || 'latest';
  const skip = (page - 1) * limit;

  const orderBy: any =
    sort === 'trending'
      ? [{ likes: { _count: 'desc' } }, { createdAt: 'desc' }]
      : { createdAt: 'desc' };

  const where: any = { status: 'Active' };

  // Following feed: only posts from users the current user follows
  if (sort === 'following' && req.user) {
    const follows = await prisma.follow.findMany({
      where: { followerId: req.user.userId },
      select: { followingId: true },
    });
    where.authorId = { in: follows.map((f) => f.followingId) };
  }

  const [posts, total] = await Promise.all([
    prisma.post.findMany({ where, include: POST_INCLUDE, orderBy, skip, take: limit }),
    prisma.post.count({ where }),
  ]);

  paginatedResponse(res, posts.map(formatPost), { page, limit, total });
}

// ── POST /posts ───────────────────────────

export async function createPost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { content, images, locationUrl, voucherCode, hashtags } = req.body;

  const post = await prisma.post.create({
    data: {
      authorId: userId,
      content,
      locationUrl,
      voucherCode,
      hashtags: hashtags || [],
      images: images
        ? { create: images.map((img: any) => ({ imageUrl: img.imageUrl, displayOrder: img.displayOrder ?? 0 })) }
        : undefined,
    },
    include: POST_INCLUDE,
  });

  await delCacheByPattern('newsfeed:*');
  successResponse(res, formatPost(post), 'Post created', 201);
}

// ── GET /posts/:postId ────────────────────

export async function getPostById(req: Request, res: Response): Promise<void> {
  const postId = parseInt(req.params.postId);

  const cacheKey = `post:${postId}`;
  const cached = await getCache(cacheKey);
  if (cached) { successResponse(res, cached); return; }

  const post = await prisma.post.findFirst({
    where: { id: postId, status: 'Active' },
    include: POST_INCLUDE,
  });
  if (!post) { errorResponse(res, 'Post not found', 404); return; }

  const formatted = formatPost(post);
  await setCache(cacheKey, formatted, CACHE_TTL.POST_DETAIL);
  successResponse(res, formatted);
}

// ── PUT /posts/:postId ────────────────────

export async function updatePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parseInt(req.params.postId);
  const { content, images, locationUrl, voucherCode, hashtags } = req.body;

  const post = await prisma.post.findFirst({ where: { id: postId, status: 'Active' } });
  if (!post) { errorResponse(res, 'Post not found', 404); return; }
  if (post.authorId !== userId && req.user!.role !== 'Admin') {
    errorResponse(res, 'Not authorized to edit this post', 403); return;
  }

  // Replace images
  if (images) {
    await prisma.postImage.deleteMany({ where: { postId } });
  }

  const updated = await prisma.post.update({
    where: { id: postId },
    data: {
      content,
      locationUrl,
      voucherCode,
      hashtags: hashtags || undefined,
      images: images
        ? { create: images.map((img: any) => ({ imageUrl: img.imageUrl, displayOrder: img.displayOrder ?? 0 })) }
        : undefined,
    },
    include: POST_INCLUDE,
  });

  await delCache(`post:${postId}`);
  successResponse(res, formatPost(updated), 'Post updated');
}

// ── DELETE /posts/:postId ─────────────────

export async function deletePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parseInt(req.params.postId);

  const post = await prisma.post.findFirst({ where: { id: postId } });
  if (!post) { errorResponse(res, 'Post not found', 404); return; }
  if (post.authorId !== userId && req.user!.role !== 'Admin') {
    errorResponse(res, 'Not authorized', 403); return;
  }

  await prisma.post.update({ where: { id: postId }, data: { status: 'Deleted' } });
  await delCache(`post:${postId}`);
  successResponse(res, null, 'Post deleted');
}

// ── POST /posts/:postId/likes ─────────────

export async function likePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parseInt(req.params.postId);

  try {
    await prisma.postLike.create({ data: { userId, postId } });
    // Create notification for post author
    const post = await prisma.post.findUnique({ where: { id: postId } });
    if (post && post.authorId !== userId) {
      const liker = await prisma.user.findUnique({ where: { id: userId }, select: { username: true } });
      await prisma.notification.create({
        data: { userId: post.authorId, type: 'Like', message: `${liker?.username} liked your post`, relatedPostId: postId },
      });
    }
    successResponse(res, null, 'Liked successfully');
  } catch {
    errorResponse(res, 'Already liked', 409);
  }
}

// ── DELETE /posts/:postId/likes ───────────

export async function unlikePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parseInt(req.params.postId);
  await prisma.postLike.deleteMany({ where: { userId, postId } });
  successResponse(res, null, 'Unliked successfully');
}

// ── GET /posts/:postId/likes ──────────────

export async function getPostLikes(req: Request, res: Response): Promise<void> {
  const postId = parseInt(req.params.postId);
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 20;
  const skip = (page - 1) * limit;

  const [likes, total] = await Promise.all([
    prisma.postLike.findMany({
      where: { postId },
      include: { user: { select: { id: true, username: true, fullName: true, avatarUrl: true, bio: true, role: true, _count: { select: { followers: true, following: true } } } } },
      skip, take: limit,
    }),
    prisma.postLike.count({ where: { postId } }),
  ]);

  paginatedResponse(res, likes.map((l) => formatUser(l.user)), { page, limit, total });
}

// ── POST /posts/:postId/shares ────────────

export async function sharePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parseInt(req.params.postId);

  try {
    await prisma.postShare.create({ data: { userId, postId } });
    successResponse(res, null, 'Shared successfully');
  } catch {
    errorResponse(res, 'Already shared', 409);
  }
}

import { handleUploadedFile } from './upload.controller';

// ── POST /posts/upload-media ──────────────

export async function uploadMedia(req: AuthRequest, res: Response): Promise<void> {
  try {
    if (req.file) {
      const data = handleUploadedFile(req, req.file);
      successResponse(res, data, 'Tải lên thành công');
      return;
    }
    successResponse(res, { url: `/uploads/images/img-${Date.now()}.jpg` }, 'Upload successful (mock)');
  } catch (error: any) {
    errorResponse(res, error.message || 'Lỗi khi tải lên', 500);
  }
}
