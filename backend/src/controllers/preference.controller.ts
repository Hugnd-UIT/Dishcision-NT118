import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { getCache, setCache, CACHE_TTL } from '../config/redis';
import { successResponse, errorResponse } from '../utils/response';

// ── GET /preferences ──────────────────────

export async function listPreferences(_req: Request, res: Response): Promise<void> {
  const cacheKey = 'preferences:all';
  const cached = await getCache(cacheKey);
  if (cached) { successResponse(res, cached); return; }

  const preferences = await prisma.foodPreference.findMany({ orderBy: { id: 'asc' } });
  const formatted = preferences.map((p) => ({
    preferenceId: p.id,
    name: p.name,
    description: p.description,
    icon: p.icon,
  }));

  await setCache(cacheKey, formatted, CACHE_TTL.PREFERENCES);
  successResponse(res, formatted);
}

import { AuthRequest } from '../middlewares/auth.middleware';

// ── GET /users/me/preferences ─────────────

export async function getUserPreferences(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;

  const userPrefs = await prisma.userPreference.findMany({
    where: { userId },
    include: { preference: true },
  });

  const formatted = userPrefs.map((up) => ({
    preferenceId: up.preference.id,
    name: up.preference.name,
    description: up.preference.description,
    icon: up.preference.icon,
  }));

  successResponse(res, formatted);
}

// ── PUT /users/me/preferences ─────────────

export async function updateUserPreferences(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { preferenceIds } = req.body;

  // Verify all preferences exist
  const count = await prisma.foodPreference.count({ where: { id: { in: preferenceIds } } });
  if (count !== preferenceIds.length) {
    errorResponse(res, 'One or more preference IDs are invalid', 400); return;
  }

  // Replace all preferences
  await prisma.userPreference.deleteMany({ where: { userId } });
  await prisma.userPreference.createMany({
    data: preferenceIds.map((preferenceId: number) => ({ userId, preferenceId })),
  });

  successResponse(res, null, 'Preferences updated');
}
