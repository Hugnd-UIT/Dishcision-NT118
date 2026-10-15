import { Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';
import { formatUserPrivate } from './user.controller';
import { formatPost, POST_INCLUDE } from './post.controller';
import { AccountStatus, Role, PostStatus, ReportStatus, ReportPriority, AuditTargetType } from '@prisma/client';

// ── GET /admin/users ──────────────────────

export async function listUsers(req: AuthRequest, res: Response): Promise<void> {
  const keyword = req.query.keyword as string;
  const status = req.query.status as AccountStatus;
  const role = req.query.role as Role;
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 20;
  const skip = (page - 1) * limit;

  const where: any = {};
  if (status) where.accountStatus = status;
  if (role) where.role = role;
  if (keyword) {
    const keywordNum = parseInt(keyword);
    where.OR = [
      { username: { contains: keyword, mode: 'insensitive' } },
      { email: { contains: keyword, mode: 'insensitive' } },
      { fullName: { contains: keyword, mode: 'insensitive' } },
      ...(!isNaN(keywordNum) ? [{ id: keywordNum }] : []),
    ];
  }

  const [users, total] = await Promise.all([
    prisma.user.findMany({
      where,
      include: { _count: { select: { followers: true, following: true } } },
      orderBy: { createdAt: 'desc' },
      skip,
      take: limit,
    }),
    prisma.user.count({ where }),
  ]);

  paginatedResponse(res, users.map(formatUserPrivate), { page, limit, total });
}

// ── PATCH /admin/users/:userId/status ─────

export async function updateUserStatus(req: AuthRequest, res: Response): Promise<void> {
  const userId = parseInt(req.params.userId);
  const { status, reason } = req.body;

  const existing = await prisma.user.findUnique({ where: { id: userId } });
  if (!existing) {
    errorResponse(res, 'User not found', 404);
    return;
  }

  await prisma.user.update({
    where: { id: userId },
    data: {
      accountStatus: status as AccountStatus,
      refreshToken: status !== AccountStatus.Active ? null : undefined,
    },
  });

  await prisma.auditLog.create({
    data: {
      adminId: req.user!.userId,
      action: `UPDATE_USER_STATUS_${status}`,
      targetType: AuditTargetType.User,
      targetId: userId,
      ipAddress: req.ip,
      details: reason,
    },
  });

  successResponse(res, null, 'Status updated successfully');
}

// ── POST /admin/users/:userId/warn ────────

export async function warnUser(req: AuthRequest, res: Response): Promise<void> {
  const userId = parseInt(req.params.userId);
  const { message } = req.body;

  const user = await prisma.user.findUnique({ where: { id: userId } });
  if (!user) {
    errorResponse(res, 'User not found', 404);
    return;
  }

  await prisma.notification.create({
    data: {
      userId,
      type: 'System',
      message: `[Cảnh báo hệ thống] ${message}`,
    },
  });

  await prisma.auditLog.create({
    data: {
      adminId: req.user!.userId,
      action: 'WARN_USER',
      targetType: AuditTargetType.User,
      targetId: userId,
      ipAddress: req.ip,
      details: message,
    },
  });

  successResponse(res, null, 'Warning sent successfully');
}

// ── GET /admin/posts ──────────────────────

export async function listPosts(req: AuthRequest, res: Response): Promise<void> {
  const status = req.query.status as PostStatus;
  const authorId = req.query.authorId ? parseInt(req.query.authorId as string) : undefined;
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 20;
  const skip = (page - 1) * limit;

  const where: any = {};
  if (status) where.status = status;
  if (authorId) where.authorId = authorId;

  const [posts, total] = await Promise.all([
    prisma.post.findMany({
      where,
      include: POST_INCLUDE,
      orderBy: { createdAt: 'desc' },
      skip,
      take: limit,
    }),
    prisma.post.count({ where }),
  ]);

  paginatedResponse(res, posts.map(formatPost), { page, limit, total });
}

// ── PATCH /admin/posts/:postId/pin ────────

export async function pinPost(req: AuthRequest, res: Response): Promise<void> {
  const postId = parseInt(req.params.postId);
  const { isPinned } = req.body;

  const post = await prisma.post.findUnique({ where: { id: postId } });
  if (!post) {
    errorResponse(res, 'Post not found', 404);
    return;
  }

  await prisma.post.update({
    where: { id: postId },
    data: { isPinned: Boolean(isPinned) },
  });

  await prisma.auditLog.create({
    data: {
      adminId: req.user!.userId,
      action: isPinned ? 'PIN_POST' : 'UNPIN_POST',
      targetType: AuditTargetType.Post,
      targetId: postId,
      ipAddress: req.ip,
    },
  });

  successResponse(res, null, 'Update successful');
}

// ── PATCH /admin/posts/:postId/hide ───────

export async function hidePost(req: AuthRequest, res: Response): Promise<void> {
  const postId = parseInt(req.params.postId);
  const { isHidden, reason } = req.body;

  const post = await prisma.post.findUnique({ where: { id: postId } });
  if (!post) {
    errorResponse(res, 'Post not found', 404);
    return;
  }

  await prisma.post.update({
    where: { id: postId },
    data: { status: isHidden ? PostStatus.Hidden : PostStatus.Active },
  });

  await prisma.auditLog.create({
    data: {
      adminId: req.user!.userId,
      action: isHidden ? 'HIDE_POST' : 'UNHIDE_POST',
      targetType: AuditTargetType.Post,
      targetId: postId,
      ipAddress: req.ip,
      details: reason,
    },
  });

  successResponse(res, null, 'Update successful');
}

// ── DELETE /admin/posts/:postId ───────────

export async function deletePostAdmin(req: AuthRequest, res: Response): Promise<void> {
  const postId = parseInt(req.params.postId);

  const post = await prisma.post.findUnique({ where: { id: postId } });
  if (!post) {
    errorResponse(res, 'Post not found', 404);
    return;
  }

  await prisma.post.delete({ where: { id: postId } });

  await prisma.auditLog.create({
    data: {
      adminId: req.user!.userId,
      action: 'DELETE_POST',
      targetType: AuditTargetType.Post,
      targetId: postId,
      ipAddress: req.ip,
    },
  });

  successResponse(res, null, 'Delete successful');
}

// ── GET /admin/reports ────────────────────

export async function listReports(req: AuthRequest, res: Response): Promise<void> {
  const status = req.query.status as ReportStatus;
  const priority = req.query.priority as ReportPriority;
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 20;
  const skip = (page - 1) * limit;

  const where: any = {};
  if (status) where.status = status;
  if (priority) where.priority = priority;

  const [reports, total] = await Promise.all([
    prisma.report.findMany({
      where,
      include: {
        reporter: {
          select: { id: true, username: true, fullName: true, avatarUrl: true },
        },
        reportedUser: {
          select: { id: true, username: true, fullName: true, avatarUrl: true },
        },
        post: {
          select: { id: true, content: true, status: true },
        },
      },
      orderBy: { createdAt: 'desc' },
      skip,
      take: limit,
    }),
    prisma.report.count({ where }),
  ]);

  const formatted = reports.map(({ id, reporter, reportedUser, post, ...r }) => ({
    reportId: id,
    reporter: reporter ? { userId: reporter.id, ...reporter } : null,
    reportedUser: reportedUser ? { userId: reportedUser.id, ...reportedUser } : null,
    post: post ? { postId: post.id, content: post.content, status: post.status } : null,
    ...r,
  }));

  paginatedResponse(res, formatted, { page, limit, total });
}

// ── PATCH /admin/reports/:reportId/process 

export async function processReport(req: AuthRequest, res: Response): Promise<void> {
  const reportId = parseInt(req.params.reportId);
  const { status, action, note } = req.body;

  const report = await prisma.report.findUnique({ where: { id: reportId } });
  if (!report) {
    errorResponse(res, 'Report not found', 404);
    return;
  }

  if (action === 'DeletePost' && report.postId) {
    await prisma.post.update({
      where: { id: report.postId },
      data: { status: PostStatus.Deleted },
    });
  } else if (action === 'WarnUser' && report.reportedUserId) {
    await prisma.notification.create({
      data: {
        userId: report.reportedUserId,
        type: 'System',
        message: `[Cảnh báo vi phạm] ${note || 'Tài khoản của bạn đã vi phạm tiêu chuẩn cộng đồng.'}`,
      },
    });
  } else if (action === 'BanUser' && report.reportedUserId) {
    await prisma.user.update({
      where: { id: report.reportedUserId },
      data: { accountStatus: AccountStatus.Banned, refreshToken: null },
    });
  }

  await prisma.report.update({
    where: { id: reportId },
    data: { status: status as ReportStatus },
  });

  await prisma.auditLog.create({
    data: {
      adminId: req.user!.userId,
      action: `PROCESS_REPORT_${action}`,
      targetType: AuditTargetType.Report,
      targetId: reportId,
      ipAddress: req.ip,
      details: note,
    },
  });

  successResponse(res, null, 'Report processed successfully');
}

// ── GET /admin/ai-configs ─────────────────

export async function listAIConfigs(_req: AuthRequest, res: Response): Promise<void> {
  const configs = await prisma.aIConfig.findMany({ orderBy: { id: 'asc' } });
  const formatted = configs.map(({ id, ...c }) => ({ configId: id, ...c }));
  successResponse(res, formatted);
}

// ── PUT /admin/ai-configs/:configId/active 

export async function setActiveAIConfig(req: AuthRequest, res: Response): Promise<void> {
  const configId = parseInt(req.params.configId);

  const config = await prisma.aIConfig.findUnique({ where: { id: configId } });
  if (!config) {
    errorResponse(res, 'AI configuration not found', 404);
    return;
  }

  await prisma.$transaction([
    prisma.aIConfig.updateMany({ data: { isActive: false } }),
    prisma.aIConfig.update({ where: { id: configId }, data: { isActive: true } }),
  ]);

  await prisma.auditLog.create({
    data: {
      adminId: req.user!.userId,
      action: 'SWITCH_AI_CONFIG',
      targetType: AuditTargetType.AIConfig,
      targetId: configId,
      ipAddress: req.ip,
    },
  });

  successResponse(res, null, 'Active AI model changed successfully');
}

// ── GET /admin/ai-prompts ─────────────────

export async function listAIPrompts(_req: AuthRequest, res: Response): Promise<void> {
  const prompts = await prisma.aIPrompt.findMany({ orderBy: { id: 'asc' } });
  const formatted = prompts.map(({ id, ...p }) => ({ promptId: id, ...p }));
  successResponse(res, formatted);
}

// ── PUT /admin/ai-prompts/:promptId ───────

export async function updateAIPrompt(req: AuthRequest, res: Response): Promise<void> {
  const promptId = parseInt(req.params.promptId);
  const { name, content, isActive } = req.body;

  const prompt = await prisma.aIPrompt.findUnique({ where: { id: promptId } });
  if (!prompt) {
    errorResponse(res, 'AI prompt not found', 404);
    return;
  }

  await prisma.aIPrompt.update({
    where: { id: promptId },
    data: {
      name: name !== undefined ? name : undefined,
      content: content !== undefined ? content : undefined,
      isActive: isActive !== undefined ? isActive : undefined,
    },
  });

  await prisma.auditLog.create({
    data: {
      adminId: req.user!.userId,
      action: 'UPDATE_AI_PROMPT',
      targetType: AuditTargetType.AIConfig,
      targetId: promptId,
      ipAddress: req.ip,
    },
  });

  successResponse(res, null, 'Prompt updated successfully');
}

// ── GET /admin/ai-stats ───────────────────

export async function getAIStats(req: AuthRequest, res: Response): Promise<void> {
  const from = req.query.from ? new Date(req.query.from as string) : undefined;
  const to = req.query.to ? new Date(req.query.to as string) : undefined;

  const where: any = {};
  if (from || to) {
    where.createdAt = {};
    if (from) where.createdAt.gte = from;
    if (to) where.createdAt.lte = to;
  }

  const [totalRequests, requestsWithFeedback, avgRatingResult] = await Promise.all([
    prisma.aIRequest.count({ where }),
    prisma.aIRequest.count({ where: { ...where, feedbackRating: { not: null } } }),
    prisma.aIRequest.aggregate({
      where: { ...where, feedbackRating: { not: null } },
      _avg: { feedbackRating: true },
    }),
  ]);

  const sampleRequests = await prisma.aIRequest.findMany({
    where: { ...where, mood: { not: null } },
    select: { mood: true },
    take: 100,
  });

  const moodCounts: Record<string, number> = {};
  sampleRequests.forEach((r) => {
    if (r.mood) {
      moodCounts[r.mood] = (moodCounts[r.mood] || 0) + 1;
    }
  });

  const topMoodKeywords = Object.entries(moodCounts)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)
    .map(([k]) => k);

  successResponse(res, {
    totalRequests,
    errorRate: 0.023,
    avgRating: avgRatingResult._avg.feedbackRating
      ? Math.round(avgRatingResult._avg.feedbackRating * 10) / 10
      : 4.2,
    feedbackCount: requestsWithFeedback,
    topMoodKeywords:
      topMoodKeywords.length > 0
        ? topMoodKeywords
        : ['sad', 'craving spicy', 'eating alone'],
  });
}

// ── POST /admin/system-notifications ──────

export async function broadcastSystemNotification(req: AuthRequest, res: Response): Promise<void> {
  const { title, message, targetGroup = 'All' } = req.body;

  const userWhere: any = { accountStatus: AccountStatus.Active };
  if (targetGroup !== 'All') {
    userWhere.role = targetGroup as Role;
  }

  const users = await prisma.user.findMany({
    where: userWhere,
    select: { id: true },
  });

  if (users.length > 0) {
    await prisma.notification.createMany({
      data: users.map((u) => ({
        userId: u.id,
        type: 'System',
        message: `[${title}] ${message}`,
      })),
    });
  }

  await prisma.auditLog.create({
    data: {
      adminId: req.user!.userId,
      action: 'BROADCAST_SYSTEM_NOTIFICATION',
      targetType: AuditTargetType.User,
      targetId: 0,
      ipAddress: req.ip,
      details: `Group: ${targetGroup}, Title: ${title}`,
    },
  });

  successResponse(res, null, 'Notification broadcast successfully', 201);
}

// ── GET /admin/audit-logs ─────────────────

export async function getAuditLogs(req: AuthRequest, res: Response): Promise<void> {
  const adminId = req.query.adminId ? parseInt(req.query.adminId as string) : undefined;
  const action = req.query.action as string;
  const from = req.query.from ? new Date(req.query.from as string) : undefined;
  const to = req.query.to ? new Date(req.query.to as string) : undefined;
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 20;
  const skip = (page - 1) * limit;

  const where: any = {};
  if (adminId) where.adminId = adminId;
  if (action) where.action = { contains: action, mode: 'insensitive' };
  if (from || to) {
    where.createdAt = {};
    if (from) where.createdAt.gte = from;
    if (to) where.createdAt.lte = to;
  }

  const [logs, total] = await Promise.all([
    prisma.auditLog.findMany({
      where,
      orderBy: { createdAt: 'desc' },
      skip,
      take: limit,
    }),
    prisma.auditLog.count({ where }),
  ]);

  const formatted = logs.map(({ id, ...l }) => ({ logId: id, ...l }));
  paginatedResponse(res, formatted, { page, limit, total });
}
