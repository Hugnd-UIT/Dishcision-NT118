import { Request, Response } from 'express';
import { errorResponse } from '../utils/response';

export function notFound(_req: Request, res: Response): void {
  errorResponse(res, 'Route not found', 404);
}
