import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { getCache, setCache, delCache, CACHE_TTL } from '../config/redis';
import { successResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';

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

// ── GET /users/me/preferences ─────────────

export async function getUserPreferences(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;

  const [userPrefs, user] = await Promise.all([
    prisma.userPreference.findMany({
      where: { userId },
      include: { preference: true },
    }),
    prisma.user.findUnique({
      where: { id: userId },
      select: { allergies: true },
    }),
  ]);

  const formatted = userPrefs.map((up) => ({
    preferenceId: up.preference.id,
    name: up.preference.name,
    description: up.preference.description,
    icon: up.preference.icon,
  }));

  res.status(200).json({
    success: true,
    message: 'Success',
    data: formatted,
    allergies: Array.isArray(user?.allergies) ? user.allergies : [],
  });
}

// ── PUT /users/me/preferences ─────────────

export async function updateUserPreferences(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { preferenceIds, preferences, allergies } = req.body;

  if (allergies !== undefined && allergies !== null && !Array.isArray(allergies)) {
    errorResponse(res, 'Danh sách dị ứng không hợp lệ', 400);
    return;
  }

  let savedAllergies: string[] | undefined;
  if (allergies !== undefined) {
    const cleanSet = new Set<string>();
    if (Array.isArray(allergies)) {
      for (const item of allergies) {
        if (typeof item !== 'string') continue;
        const trimmed = item.trim().slice(0, 50);
        if (trimmed) cleanSet.add(trimmed);
        if (cleanSet.size >= 20) break;
      }
    }
    savedAllergies = Array.from(cleanSet);
    await prisma.user.update({
      where: { id: userId },
      data: { allergies: savedAllergies },
    });
  }

  if (Array.isArray(preferenceIds) || Array.isArray(preferences)) {
    let finalIds: number[] = [];
    if (Array.isArray(preferenceIds)) {
      finalIds = preferenceIds.filter((id) => Number.isInteger(id) && id > 0);
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

    await prisma.userPreference.deleteMany({ where: { userId } });
    if (finalIds.length > 0) {
      await prisma.userPreference.createMany({
        data: finalIds.map((preferenceId: number) => ({ userId, preferenceId })),
        skipDuplicates: true,
      });
    }
  }

  await delCache(`user:${userId}`);
  const currentUser = savedAllergies === undefined
    ? await prisma.user.findUnique({ where: { id: userId }, select: { allergies: true } })
    : null;

  successResponse(
    res,
    { allergies: savedAllergies ?? currentUser?.allergies ?? [] },
    'Preferences updated'
  );
}
