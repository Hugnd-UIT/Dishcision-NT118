import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { getCache, setCache, CACHE_TTL } from '../config/redis';
import { successResponse, paginatedResponse } from '../utils/response';
import { formatUser } from './user.controller';

const AUTHOR_SELECT = {
  id: true, username: true, fullName: true, avatarUrl: true,
  bio: true, role: true, _count: { select: { followers: true, following: true } },
};

function formatPost(post: any) {
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

// ── GET /search/food ──────────────────────

export async function searchFood(req: Request, res: Response): Promise<void> {
  const { keyword, category, priceMin, priceMax, location, page = '1', limit = '10' } = req.query;
  const p = parseInt(page as string);
  const l = parseInt(limit as string);
  const skip = (p - 1) * l;

  const where: any = { status: 'Active' };
  if (keyword) {
    where.OR = [
      { content: { contains: keyword as string, mode: 'insensitive' } },
      { hashtags: { has: keyword as string } },
    ];
  }
  if (location) where.locationUrl = { contains: location as string, mode: 'insensitive' };

  const [posts, total] = await Promise.all([
    prisma.post.findMany({
      where,
      include: { author: { select: AUTHOR_SELECT }, images: true, _count: { select: { likes: true, comments: true, shares: true } } },
      orderBy: { createdAt: 'desc' },
      skip, take: l,
    }),
    prisma.post.count({ where }),
  ]);

  paginatedResponse(res, posts.map(formatPost), { page: p, limit: l, total });
}

// ── GET /search/accounts ──────────────────

export async function searchAccounts(req: Request, res: Response): Promise<void> {
  const { keyword, page = '1', limit = '10' } = req.query;
  const p = parseInt(page as string);
  const l = parseInt(limit as string);
  const skip = (p - 1) * l;

  const where: any = {};
  if (keyword) {
    where.OR = [
      { username: { contains: keyword as string, mode: 'insensitive' } },
      { fullName: { contains: keyword as string, mode: 'insensitive' } },
    ];
  }

  const [users, total] = await Promise.all([
    prisma.user.findMany({
      where: { ...where, accountStatus: 'Active' },
      select: AUTHOR_SELECT,
      skip, take: l,
    }),
    prisma.user.count({ where: { ...where, accountStatus: 'Active' } }),
  ]);

  paginatedResponse(res, users.map(formatUser), { page: p, limit: l, total });
}

// ── GET /search/trending ──────────────────

export async function getTrending(_req: Request, res: Response): Promise<void> {
  const cacheKey = 'search:trending';
  const cached = await getCache(cacheKey);
  if (cached) { successResponse(res, cached); return; }

  // Get most used hashtags from recent posts
  const posts = await prisma.post.findMany({
    where: { status: 'Active', createdAt: { gte: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000) } },
    select: { hashtags: true },
  });

  const tagCount: Record<string, number> = {};
  posts.forEach((p) => {
    p.hashtags.forEach((tag) => {
      tagCount[tag] = (tagCount[tag] || 0) + 1;
    });
  });

  const trendingHashtags = Object.entries(tagCount)
    .sort(([, a], [, b]) => b - a)
    .slice(0, 10)
    .map(([tag, postCount]) => ({ tag, postCount }));

  const trendingDishes = ['Banh mi', 'Bun dau mam tom', 'Com tam', 'Pho bo', 'Bun cha'];

  const result = { trendingHashtags, trendingDishes };
  await setCache(cacheKey, result, CACHE_TTL.TRENDING);
  successResponse(res, result);
}
