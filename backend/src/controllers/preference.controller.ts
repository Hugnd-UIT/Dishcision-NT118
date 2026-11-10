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
  const { preferenceIds, preferences } = req.body;

  let finalIds: number[] = [];
  if (Array.isArray(preferenceIds)) {
    finalIds = preferenceIds;
  } else if (Array.isArray(preferences)) {
    for (const pName of preferences) {
      if (!pName || typeof pName !== 'string') continue;
      const cleanName = pName.trim();
      if (!cleanName) continue;
      let pref = await prisma.foodPreference.findFirst({ where: { name: cleanName } });
      if (!pref) {
        pref = await prisma.foodPreference.create({ data: { name: cleanName } });
      }
      finalIds.push(pref.id);
    }
  }

  // Replace all preferences
  await prisma.userPreference.deleteMany({ where: { userId } });
  if (finalIds.length > 0) {
    await prisma.userPreference.createMany({
      data: finalIds.map((preferenceId: number) => ({ userId, preferenceId })),
      skipDuplicates: true,
    });
  }

  successResponse(res, null, 'Preferences updated');
}
