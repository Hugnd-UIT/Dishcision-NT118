import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { getCache, setCache, CACHE_TTL } from '../config/redis';
import { successResponse, paginatedResponse } from '../utils/response';
import { parsePagination } from '../utils/http';
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
  const { keyword, location } = req.query;
  const { page: p, limit: l, skip } = parsePagination(req.query, 10, 100);

  const where: any = { status: 'Active' };
  if (keyword) {
    const kw = String(keyword).trim();
    where.OR = [
      { content: { contains: kw, mode: 'insensitive' } },
      { hashtags: { has: kw } },
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
  const { keyword } = req.query;
  const { page: p, limit: l, skip } = parsePagination(req.query, 10, 100);

  const where: any = {};
  if (keyword) {
    const kw = String(keyword).trim();
    where.OR = [
      { username: { contains: kw, mode: 'insensitive' } },
      { fullName: { contains: kw, mode: 'insensitive' } },
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

// ── GET /search/dishes ────────────────────

export async function searchDishes(req: Request, res: Response): Promise<void> {
  const keyword = typeof req.query.keyword === 'string' ? req.query.keyword.trim() : '';
  const { page: p, limit: l, skip } = parsePagination(req.query, 20, 100);

  const where: any = {};
  if (keyword) {
    where.OR = [
      { name: { contains: keyword, mode: 'insensitive' } },
      { category: { contains: keyword, mode: 'insensitive' } },
      { description: { contains: keyword, mode: 'insensitive' } },
      { storeUser: { fullName: { contains: keyword, mode: 'insensitive' } } },
    ];
  }

  const [dishes, total] = await Promise.all([
    prisma.dish.findMany({
      where,
      include: {
        storeUser: {
          select: { id: true, fullName: true, storeProfile: { select: { address: true } } },
        },
      },
      orderBy: [{ rating: 'desc' }, { reviewsCount: 'desc' }],
      skip,
      take: l,
    }),
    prisma.dish.count({ where }),
  ]);

  const formatted = dishes.map((d) => ({
    id: d.id,
    name: d.name,
    price: d.price,
    formattedPrice: d.formattedPrice,
    description: d.description,
    imageUrl: d.imageUrl,
    category: d.category,
    rating: d.rating,
    reviewsCount: d.reviewsCount,
    storeId: d.storeUserId,
    storeName: d.storeUser.fullName,
    storeAddress: d.storeUser.storeProfile?.address || '',
  }));

  paginatedResponse(res, formatted, { page: p, limit: l, total });
}

// ── GET /search/stores ────────────────────

export async function searchStores(req: Request, res: Response): Promise<void> {
  const keyword = typeof req.query.keyword === 'string' ? req.query.keyword.trim() : '';
  const { page: p, limit: l, skip } = parsePagination(req.query, 20, 100);

  const where: any = { role: 'RestaurantAccount', accountStatus: 'Active' };
  if (keyword) {
    where.OR = [
      { fullName: { contains: keyword, mode: 'insensitive' } },
      { bio: { contains: keyword, mode: 'insensitive' } },
      { storeProfile: { address: { contains: keyword, mode: 'insensitive' } } },
      { dishes: { some: { name: { contains: keyword, mode: 'insensitive' } } } },
    ];
  }

  const [stores, total] = await Promise.all([
    prisma.user.findMany({
      where,
      include: {
        storeProfile: true,
        dishes: { take: 3, orderBy: { rating: 'desc' } },
        _count: { select: { followers: true } },
      },
      skip,
      take: l,
    }),
    prisma.user.count({ where }),
  ]);

  const formatted = stores.map((s) => ({
    id: s.id,
    name: s.fullName,
    username: s.username,
    avatarUrl: s.avatarUrl,
    bio: s.bio || '',
    address: s.storeProfile?.address || '',
    openHours: s.storeProfile?.openHours || '',
    isOpen: s.storeProfile?.isOpen ?? true,
    rating: s.storeProfile?.rating ?? 0,
    highlightDish: s.storeProfile?.highlightDish || s.dishes[0]?.name || '',
    dishPrice: s.storeProfile?.dishPrice || s.dishes[0]?.formattedPrice || '',
    followersCount: s._count.followers,
  }));

  paginatedResponse(res, formatted, { page: p, limit: l, total });
}

// ── GET /search/trending ──────────────────

export async function getTrending(_req: Request, res: Response): Promise<void> {
  const cacheKey = 'search:trending';
  const cached = await getCache(cacheKey);
  if (cached) { successResponse(res, cached); return; }

  const [posts, topDishes] = await Promise.all([
    prisma.post.findMany({
      where: { status: 'Active', createdAt: { gte: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) } },
      select: { hashtags: true },
    }),
    prisma.dish.findMany({
      orderBy: [{ reviewsCount: 'desc' }, { rating: 'desc' }],
      take: 8,
      select: { id: true, name: true, category: true, reviewsCount: true },
    }),
  ]);

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

  const trendingDishes = topDishes.map((d) => d.name);

  const result = { trendingHashtags, trendingDishes };
  await setCache(cacheKey, result, CACHE_TTL.TRENDING);
  successResponse(res, result);
}
