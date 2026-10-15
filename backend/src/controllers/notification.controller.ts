import { Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, paginatedResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';

// ── GET /notifications ────────────────────

export async function getNotifications(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 20;
  const isRead = req.query.isRead !== undefined ? req.query.isRead === 'true' : undefined;
  const skip = (page - 1) * limit;

  const where: any = { userId };
  if (isRead !== undefined) where.isRead = isRead;

  const [notifications, total] = await Promise.all([
    prisma.notification.findMany({
      where, orderBy: { createdAt: 'desc' }, skip, take: limit,
    }),
    prisma.notification.count({ where }),
  ]);

  const formatted = notifications.map(({ id, ...n }) => ({ notificationId: id, ...n }));
  paginatedResponse(res, formatted, { page, limit, total });
}

// ── PATCH /notifications/:notificationId/read

export async function markAsRead(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const notificationId = parseInt(req.params.notificationId);

  await prisma.notification.updateMany({
    where: { id: notificationId, userId },
    data: { isRead: true },
  });

  successResponse(res, null, 'Notification marked as read');
}
