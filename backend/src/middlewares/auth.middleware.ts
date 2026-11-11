import { Request, Response, NextFunction } from 'express';
import { prisma } from '../config/prisma';
import { redis } from '../config/redis';
import { verifyAccessToken } from '../utils/auth';
import { errorResponse } from '../utils/response';

export interface AuthRequest extends Request {
  user?: { userId: number; role: string };
}

function extractBearerToken(req: Request): string | null {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) return null;
  const token = authHeader.slice(7).trim();
  if (!token || token === 'demo_token' || token.startsWith('google_token_')) return null;
  return token;
}

export async function authenticate(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
  const token = extractBearerToken(req);
  if (!token) {
    errorResponse(res, 'Authentication required', 401);
    return;
  }

  let payload: { userId: number; role: string };
  try {
    payload = verifyAccessToken(token);
  } catch {
    errorResponse(res, 'Invalid or expired token', 401);
    return;
  }

  try {
    const blacklisted = await redis.get(`blacklist:${token}`);
    if (blacklisted) {
      errorResponse(res, 'Token has been revoked', 401);
      return;
    }

    const user = await prisma.user.findUnique({
      where: { id: payload.userId },
      select: { id: true, role: true, accountStatus: true },
    });
    if (!user || user.accountStatus !== 'Active') {
      errorResponse(res, 'Account is inactive or not found', 401);
      return;
    }

    req.user = { userId: user.id, role: user.role };
    next();
  } catch (err) {
    next(err);
  }
}

export function authorizeAdmin(req: AuthRequest, res: Response, next: NextFunction): void {
  if (req.user?.role !== 'Admin') {
    errorResponse(res, 'Admin access required', 403);
    return;
  }
  next();
}

export async function optionalAuth(req: AuthRequest, _res: Response, next: NextFunction): Promise<void> {
  const token = extractBearerToken(req);
  if (!token) {
    req.user = undefined;
    next();
    return;
  }

  try {
    const payload = verifyAccessToken(token);
    const blacklisted = await redis.get(`blacklist:${token}`);
    if (!blacklisted) {
      const user = await prisma.user.findUnique({
        where: { id: payload.userId },
        select: { id: true, role: true, accountStatus: true },
      });
      if (user && user.accountStatus === 'Active') {
        req.user = { userId: user.id, role: user.role };
      }
    }
  } catch {
    req.user = undefined;
  }
  next();
}
