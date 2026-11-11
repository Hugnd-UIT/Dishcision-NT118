import { Request, Response, NextFunction, RequestHandler } from 'express';

export function asyncHandler(
  fn: (req: Request, res: Response, next: NextFunction) => Promise<void> | void
): RequestHandler {
  return (req, res, next) => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };
}

export function parsePositiveInt(raw: string | undefined): number | null {
  if (!raw || !/^\d+$/.test(raw.trim())) return null;
  const num = Number(raw.trim());
  return Number.isSafeInteger(num) && num > 0 ? num : null;
}

export function parsePagination(query: Request['query'], defaultLimit = 10, maxLimit = 100) {
  const rawPage = Number(query.page);
  const rawLimit = Number(query.limit);
  const page = Number.isInteger(rawPage) && rawPage >= 1 ? rawPage : 1;
  const limit = Number.isInteger(rawLimit) && rawLimit >= 1 ? Math.min(rawLimit, maxLimit) : defaultLimit;
  return { page, limit, skip: (page - 1) * limit };
}

