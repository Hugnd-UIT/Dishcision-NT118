import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { getCache, setCache, CACHE_TTL } from '../config/redis';
import { successResponse } from '../utils/response';

// ── GET /food-decision/categories ─────────

export async function getCategories(_req: Request, res: Response): Promise<void> {
  const cacheKey = 'food:categories';
  const cached = await getCache(cacheKey);
  if (cached) {
    successResponse(res, cached);
    return;
  }

  const categories = await prisma.foodCategory.findMany({ orderBy: { id: 'asc' } });
  const formatted = categories.map((c) => ({
    categoryId: c.id,
    name: c.name,
    icon: c.icon,
  }));

  await setCache(cacheKey, formatted, CACHE_TTL.PREFERENCES);
  successResponse(res, formatted);
}

// ── POST /food-decision/random-wheel ──────

const FALLBACK_DISHES = [
  'Bún bò Huế', 'Phở bò tái lăn', 'Bánh mì pate', 'Bún chả',
  'Cơm tấm sườn bì chả', 'Bún đậu mắm tôm', 'Bánh xèo', 'Phở cuốn ngũ xã',
];

export async function spinRandomWheel(req: Request, res: Response): Promise<void> {
  const { budget, preferenceIds } = req.body || {};

  // Fetch reviews and categories from database to recommend real seeded spots
  const [reviews, categories] = await Promise.all([
    prisma.review.findMany({
      select: { restaurantName: true, locationUrl: true, rating: true },
      take: 20,
    }),
    prisma.foodCategory.findMany({ select: { name: true } }),
  ]);

  let dish = FALLBACK_DISHES[Math.floor(Math.random() * FALLBACK_DISHES.length)];
  if (categories.length > 0) {
    dish = categories[Math.floor(Math.random() * categories.length)].name;
  }

  let restaurantName = 'Phở Thìn Lò Đúc';
  let address = '13 Lò Đúc, Hai Bà Trưng, Hà Nội';
  let rating = 4.8;
  let mapsUrl = `https://maps.google.com/?q=${encodeURIComponent(restaurantName)}`;

  if (reviews.length > 0) {
    const selected = reviews[Math.floor(Math.random() * reviews.length)];
    restaurantName = selected.restaurantName;
    rating = selected.rating;
    mapsUrl = selected.locationUrl || `https://maps.google.com/?q=${encodeURIComponent(restaurantName)}`;
    address = restaurantName;
  }

  const estimatedPrice = budget
    ? Math.floor(Math.random() * (parseInt(budget) * 0.8)) + 30000
    : 55000;

  successResponse(res, {
    dishName: dish,
    restaurant: {
      name: restaurantName,
      address,
      mapsUrl,
      estimatedPrice,
      rating,
    },
  });
}
