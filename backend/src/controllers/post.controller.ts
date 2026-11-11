import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { getCache, setCache, delCache, delCacheByPattern, CACHE_TTL } from '../config/redis';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { parsePagination, parsePositiveInt } from '../utils/http';
import { AuthRequest } from '../middlewares/auth.middleware';
import { formatUser, attachPostFlags } from './user.controller';
import { handleUploadedFile } from './upload.controller';

export const POST_INCLUDE = {
  author: {
    select: {
      id: true,
      username: true,
      fullName: true,
      avatarUrl: true,
      bio: true,
      role: true,
      _count: {
        select: {
          followers: true,
          following: true,
          posts: { where: { status: 'Active' as const } },
        },
      },
    },
  },
  images: true,
  _count: { select: { likes: true, comments: true, shares: true } },
};

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

// ── GET /posts ────────────────────────────

export async function getNewsfeed(req: AuthRequest, res: Response): Promise<void> {
  const { page, limit, skip } = parsePagination(req.query, 10, 100);
  const sort = (req.query.sort as string) || 'latest';

  const orderBy: any =
    sort === 'trending'
      ? [{ likes: { _count: 'desc' } }, { createdAt: 'desc' }]
      : { createdAt: 'desc' };

  const where: any = { status: 'Active' };

  if (req.query.hasVoucher === 'true') {
    where.voucherCode = { not: null };
  }

  if (sort === 'following') {
    if (!req.user) {
      paginatedResponse(res, [], { page, limit, total: 0 });
      return;
    }
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

  const formatted = await attachPostFlags(posts.map(formatPost), req.user?.userId);
  paginatedResponse(res, formatted, { page, limit, total });
}

// ── POST /posts ───────────────────────────

export async function createPost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { content, images, locationUrl, voucherCode, hashtags } = req.body;

  const post = await prisma.post.create({
    data: {
      authorId: userId,
      content,
      locationUrl: locationUrl || null,
      voucherCode: voucherCode || null,
      hashtags: hashtags || [],
      images: images
        ? { create: images.map((img: any) => ({ imageUrl: img.imageUrl, displayOrder: img.displayOrder ?? 0 })) }
        : undefined,
    },
    include: POST_INCLUDE,
  });

  await Promise.all([delCacheByPattern('newsfeed:*'), delCache(`user:${userId}`)]);
  successResponse(res, { ...formatPost(post), isSaved: false, isLiked: false }, 'Post created', 201);
}

// ── GET /posts/:postId ────────────────────

export async function getPostById(req: AuthRequest, res: Response): Promise<void> {
  const postId = parsePositiveInt(req.params.postId);
  if (!postId) {
    errorResponse(res, 'Invalid post ID', 400);
    return;
  }

  const cacheKey = `post:${postId}`;
  let basePost = await getCache<ReturnType<typeof formatPost>>(cacheKey);

  if (!basePost) {
    const post = await prisma.post.findFirst({
      where: { id: postId, status: 'Active' },
      include: POST_INCLUDE,
    });
    if (!post) {
      errorResponse(res, 'Post not found', 404);
      return;
    }
    basePost = formatPost(post);
    await setCache(cacheKey, basePost, CACHE_TTL.POST_DETAIL);
  }

  const [enriched] = await attachPostFlags([basePost], req.user?.userId);
  successResponse(res, enriched);
}

// ── PUT /posts/:postId ────────────────────

export async function updatePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parsePositiveInt(req.params.postId);
  if (!postId) {
    errorResponse(res, 'Invalid post ID', 400);
    return;
  }
  const { content, images, locationUrl, voucherCode, hashtags, status } = req.body;

  const post = await prisma.post.findFirst({
    where: { id: postId, status: { in: ['Active', 'Hidden'] } },
  });
  if (!post) {
    errorResponse(res, 'Post not found', 404);
    return;
  }
  if (post.authorId !== userId && req.user!.role !== 'Admin') {
    errorResponse(res, 'Not authorized to edit this post', 403);
    return;
  }

  if (Array.isArray(images)) {
    await prisma.postImage.deleteMany({ where: { postId } });
  }

  const updated = await prisma.post.update({
    where: { id: postId },
    data: {
      content,
      locationUrl: locationUrl === '' ? null : locationUrl,
      voucherCode: voucherCode === '' ? null : voucherCode,
      hashtags: hashtags ?? undefined,
      status: status ?? undefined,
      images: Array.isArray(images)
        ? { create: images.map((img: any) => ({ imageUrl: img.imageUrl, displayOrder: img.displayOrder ?? 0 })) }
        : undefined,
    },
    include: POST_INCLUDE,
  });

  await Promise.all([
    delCache(`post:${postId}`),
    delCache(`user:${post.authorId}`),
    delCacheByPattern('newsfeed:*'),
  ]);
  const [enriched] = await attachPostFlags([formatPost(updated)], userId);
  successResponse(res, enriched, 'Post updated');
}

// ── DELETE /posts/:postId ─────────────────

export async function deletePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parsePositiveInt(req.params.postId);
  if (!postId) {
    errorResponse(res, 'Invalid post ID', 400);
    return;
  }

  const post = await prisma.post.findFirst({ where: { id: postId } });
  if (!post) {
    errorResponse(res, 'Post not found', 404);
    return;
  }
  if (post.authorId !== userId && req.user!.role !== 'Admin') {
    errorResponse(res, 'Not authorized', 403);
    return;
  }

  await prisma.post.update({ where: { id: postId }, data: { status: 'Deleted' } });
  await Promise.all([
    delCache(`post:${postId}`),
    delCache(`user:${post.authorId}`),
    delCacheByPattern('newsfeed:*'),
  ]);
  successResponse(res, null, 'Post deleted');
}

// ── POST /posts/:postId/likes ─────────────

export async function likePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parsePositiveInt(req.params.postId);
  if (!postId) {
    errorResponse(res, 'Invalid post ID', 400);
    return;
  }

  const post = await prisma.post.findFirst({ where: { id: postId, status: 'Active' } });
  if (!post) {
    errorResponse(res, 'Post not found', 404);
    return;
  }

  try {
    await prisma.postLike.create({ data: { userId, postId } });
    if (post.authorId !== userId) {
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
  const postId = parsePositiveInt(req.params.postId);
  if (!postId) {
    errorResponse(res, 'Invalid post ID', 400);
    return;
  }

  await prisma.postLike.deleteMany({ where: { userId, postId } });
  successResponse(res, null, 'Unliked successfully');
}

// ── GET /posts/:postId/likes ──────────────

export async function getPostLikes(req: Request, res: Response): Promise<void> {
  const postId = parsePositiveInt(req.params.postId);
  if (!postId) {
    errorResponse(res, 'Invalid post ID', 400);
    return;
  }
  const { page, limit, skip } = parsePagination(req.query, 20, 100);

  const [likes, total] = await Promise.all([
    prisma.postLike.findMany({
      where: { postId },
      include: { user: { select: { id: true, username: true, fullName: true, avatarUrl: true, bio: true, role: true, _count: { select: { followers: true, following: true } } } } },
      skip,
      take: limit,
    }),
    prisma.postLike.count({ where: { postId } }),
  ]);

  paginatedResponse(res, likes.map((l) => formatUser(l.user)), { page, limit, total });
}

// ── POST /posts/:postId/shares ────────────

export async function sharePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parsePositiveInt(req.params.postId);
  if (!postId) {
    errorResponse(res, 'Invalid post ID', 400);
    return;
  }

  const post = await prisma.post.findFirst({ where: { id: postId, status: 'Active' }, select: { id: true } });
  if (!post) {
    errorResponse(res, 'Post not found', 404);
    return;
  }

  await prisma.postShare.upsert({
    where: { userId_postId: { userId, postId } },
    update: {},
    create: { userId, postId },
  });
  successResponse(res, { postId, shared: true }, 'Shared successfully');
}

// ── POST /posts/upload-media ──────────────

export async function uploadMedia(req: AuthRequest, res: Response): Promise<void> {
  if (!req.file) {
    errorResponse(res, 'Vui lòng chọn tệp ảnh hoặc video để tải lên (key field: "file")', 400);
    return;
  }
  const data = handleUploadedFile(req, req.file);
  successResponse(res, data, 'Tải lên thành công');
}
