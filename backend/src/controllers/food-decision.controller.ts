import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { getCache, setCache, CACHE_TTL } from '../config/redis';
import { successResponse, errorResponse } from '../utils/response';

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

export async function spinRandomWheel(req: Request, res: Response): Promise<void> {
  const { budget, category } = req.body || {};
  const maxBudget = budget ? parseInt(String(budget).replace(/[^\d]/g, ''), 10) : 0;

  let dishes = await prisma.dish.findMany({
    where: category ? { category: { contains: String(category), mode: 'insensitive' } } : undefined,
    include: {
      storeUser: {
        select: { id: true, fullName: true, storeProfile: true },
      },
    },
  });

  if (maxBudget > 0) {
    const affordable = dishes.filter((d) => d.price <= maxBudget);
    if (affordable.length > 0) dishes = affordable;
  }

  if (dishes.length > 0) {
    const selected = dishes[Math.floor(Math.random() * dishes.length)];
    const storeName = selected.storeUser.fullName;
    const address = selected.storeUser.storeProfile?.address || storeName;
    const rating = selected.storeUser.storeProfile?.rating ?? selected.rating;

    successResponse(res, {
      dishId: selected.id,
      dishName: selected.name,
      imageUrl: selected.imageUrl,
      category: selected.category,
      storeId: selected.storeUserId,
      restaurant: {
        id: selected.storeUserId,
        name: storeName,
        address,
        mapsUrl: `https://maps.google.com/?q=${encodeURIComponent(address)}`,
        estimatedPrice: selected.price,
        formattedPrice: selected.formattedPrice,
        rating,
      },
    });
    return;
  }

  const reviews = await prisma.review.findMany({
    select: { restaurantName: true, locationUrl: true, rating: true },
    take: 20,
  });

  if (reviews.length === 0) {
    errorResponse(res, 'Chưa có dữ liệu món ăn trên hệ thống', 404);
    return;
  }

  const selected = reviews[Math.floor(Math.random() * reviews.length)];
  successResponse(res, {
    dishName: selected.restaurantName,
    restaurant: {
      name: selected.restaurantName,
      address: selected.restaurantName,
      mapsUrl: selected.locationUrl || `https://maps.google.com/?q=${encodeURIComponent(selected.restaurantName)}`,
      estimatedPrice: maxBudget > 0 ? maxBudget : 0,
      rating: selected.rating,
    },
  });
}
