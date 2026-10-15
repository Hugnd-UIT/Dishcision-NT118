import { Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';

// ── POST /reports ─────────────────────────

export async function createReport(req: AuthRequest, res: Response): Promise<void> {
  const reporterId = req.user!.userId;
  const { postId, reportedUserId, reason, description } = req.body;

  if (!postId && !reportedUserId) {
    errorResponse(res, 'Either postId or reportedUserId must be provided', 400);
    return;
  }

  // Validate existence if provided
  if (postId) {
    const post = await prisma.post.findUnique({ where: { id: postId } });
    if (!post) {
      errorResponse(res, 'Post not found', 404);
      return;
    }
  }

  if (reportedUserId) {
    const user = await prisma.user.findUnique({ where: { id: reportedUserId } });
    if (!user) {
      errorResponse(res, 'Reported user not found', 404);
      return;
    }
  }

  const report = await prisma.report.create({
    data: {
      reporterId,
      postId: postId ?? undefined,
      reportedUserId: reportedUserId ?? undefined,
      reason,
      description,
    },
  });

  successResponse(res, { reportId: report.id }, 'Report submitted successfully', 201);
}
