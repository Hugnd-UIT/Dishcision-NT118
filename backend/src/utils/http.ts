import { Request, Response, NextFunction, RequestHandler } from 'express';

export function asyncHandler(
  fn: (req: Request, res: Response, next: NextFunction) => Promise<void> | void
): RequestHandler {
  return (req, res, next) => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };
}

export function parsePositiveInt(raw: unknown): number | null {
  if (typeof raw === 'number') {
    return Number.isSafeInteger(raw) && raw > 0 ? raw : null;
  }
  if (typeof raw !== 'string') return null;
  const trimmed = raw.trim();
  if (!trimmed || !/^\d+$/.test(trimmed)) return null;
  const num = Number(trimmed);
  return Number.isSafeInteger(num) && num > 0 ? num : null;
}

export function parsePagination(query: Request['query'], defaultLimit = 10, maxLimit = 100) {
  const rawPage = Number(query.page);
  const rawLimit = Number(query.limit);
  const page = Number.isInteger(rawPage) && rawPage >= 1 ? rawPage : 1;
  const limit = Number.isInteger(rawLimit) && rawLimit >= 1 ? Math.min(rawLimit, maxLimit) : defaultLimit;
  return { page, limit, skip: (page - 1) * limit };
}

