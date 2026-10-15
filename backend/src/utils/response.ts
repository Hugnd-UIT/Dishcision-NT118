import { Response } from 'express';

export function successResponse(res: Response, data: unknown, message = 'Successful', statusCode = 200) {
  return res.status(statusCode).json({
    success: true,
    message,
    data,
  });
}

export function paginatedResponse(
  res: Response,
  data: unknown,
  pagination: { page: number; limit: number; total: number },
  message = 'Successful'
) {
  return res.status(200).json({
    success: true,
    message,
    data,
    pagination: {
      page: pagination.page,
      limit: pagination.limit,
      total: pagination.total,
      totalPages: Math.ceil(pagination.total / pagination.limit),
    },
  });
}

export function errorResponse(res: Response, message: string, statusCode = 500, error?: string) {
  return res.status(statusCode).json({
    success: false,
    message,
    ...(error && { error }),
  });
}
