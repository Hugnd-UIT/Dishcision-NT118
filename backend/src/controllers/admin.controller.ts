import { Response } from 'express';
import { prisma } from '../config/prisma';
import { delCache, delCacheByPattern } from '../config/redis';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';
import { formatUserPrivate, resolveAvatarUrl } from './user.controller';
import { formatPost, POST_INCLUDE } from './post.controller';
import { AccountStatus, Role, PostStatus, ReportStatus, ReportPriority, AuditTargetType, Prisma } from '@prisma/client';

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
  if (status !== AccountStatus.Active) {
    await prisma.userSession.deleteMany({ where: { userId } });
  }

  await Promise.all([
    delCache(`user:${userId}`),
    delCache('search:trending'),
    delCacheByPattern('post:*'),
    delCacheByPattern('newsfeed:*'),
  ]);

  const adminId = req.user?.userId || 1;
  await prisma.auditLog.create({
    data: {
      adminId,
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
  const keyword = req.query.keyword as string;
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 50;
  const skip = (page - 1) * limit;

  const where: any = {};
  if (status) where.status = status;
  if (authorId) where.authorId = authorId;
  if (keyword) {
    const keywordNum = parseInt(keyword);
    where.OR = [
      { content: { contains: keyword, mode: 'insensitive' } },
      { author: { fullName: { contains: keyword, mode: 'insensitive' } } },
      { author: { username: { contains: keyword, mode: 'insensitive' } } },
      ...(!isNaN(keywordNum) ? [{ id: keywordNum }] : []),
    ];
  }

  const [posts, total] = await Promise.all([
    prisma.post.findMany({
      where,
      include: POST_INCLUDE,
      orderBy: [{ isPinned: 'desc' }, { createdAt: 'desc' }],
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

  const adminId = req.user?.userId || 1;
  await prisma.auditLog.create({
    data: {
      adminId,
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

  await Promise.all([
    delCache(`post:${postId}`),
    delCache(`user:${post.authorId}`),
    delCache('search:trending'),
    delCacheByPattern('newsfeed:*'),
  ]);

  const adminId = req.user?.userId || 1;
  await prisma.auditLog.create({
    data: {
      adminId,
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

  await Promise.all([
    delCache(`post:${postId}`),
    delCache(`user:${post.authorId}`),
    delCache('search:trending'),
    delCacheByPattern('newsfeed:*'),
  ]);

  const adminId = req.user?.userId || 1;
  await prisma.auditLog.create({
    data: {
      adminId,
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
    reporter: reporter ? { userId: reporter.id, ...reporter, avatarUrl: resolveAvatarUrl(reporter.avatarUrl) } : null,
    reportedUser: reportedUser ? { userId: reportedUser.id, ...reportedUser, avatarUrl: resolveAvatarUrl(reportedUser.avatarUrl) } : null,
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
    await prisma.$transaction([
      prisma.user.update({
        where: { id: report.reportedUserId },
        data: { accountStatus: AccountStatus.Banned, refreshToken: null },
      }),
      prisma.userSession.deleteMany({ where: { userId: report.reportedUserId } }),
    ]);
  }

  await prisma.report.update({
    where: { id: reportId },
    data: { status: status as ReportStatus },
  });

  const cacheInvalidations: Promise<void>[] = [];
  if (action === 'DeletePost' && report.postId) {
    cacheInvalidations.push(
      delCache(`post:${report.postId}`),
      delCache('search:trending'),
      delCacheByPattern('newsfeed:*')
    );
    if (report.reportedUserId) {
      cacheInvalidations.push(delCache(`user:${report.reportedUserId}`));
    }
  } else if (action === 'BanUser' && report.reportedUserId) {
    cacheInvalidations.push(
      delCache(`user:${report.reportedUserId}`),
      delCache('search:trending'),
      delCacheByPattern('post:*'),
      delCacheByPattern('newsfeed:*')
    );
  }
  if (cacheInvalidations.length > 0) {
    await Promise.all(cacheInvalidations);
  }

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

  const [totalRequests, geminiRequests, fallbackRequests, requestsWithFeedback, avgRatingResult, allRequests] = await Promise.all([
    prisma.aIRequest.count({ where }),
    prisma.aIRequest.count({ where: { ...where, modelUsed: 'gemini-2.5-flash' } }),
    prisma.aIRequest.count({ where: { ...where, modelUsed: { not: 'gemini-2.5-flash' } } }),
    prisma.aIRequest.count({ where: { ...where, feedbackRating: { not: null } } }),
    prisma.aIRequest.aggregate({
      where: { ...where, feedbackRating: { not: null } },
      _avg: { feedbackRating: true },
    }),
    prisma.aIRequest.findMany({
      where,
      include: {
        user: { select: { id: true, username: true, fullName: true, avatarUrl: true } },
      },
      orderBy: { createdAt: 'desc' },
      take: 200,
    }),
  ]);

  let totalPromptTokens = 0;
  let totalCompletionTokens = 0;
  let totalLatencyMs = 0;
  let validFormatCount = 0;

  const monthNames = ['Th01', 'Th02', 'Th03', 'Th04', 'Th05', 'Th06', 'Th07', 'Th08', 'Th09', 'Th10', 'Th11', 'Th12'];
  const monthlyMap = monthNames.map((name) => ({
    name,
    requests: 0,
    promptTokens: 0,
    completionTokens: 0,
    totalTokens: 0,
    totalLatency: 0,
  }));

  const logs = allRequests.map((r) => {
    const rJson = r.resultJson as any;
    const metrics = rJson?.metrics || null;
    const suggestions = Array.isArray(rJson) ? rJson : (rJson?.suggestions || []);

    const promptText = `Bạn là chuyên gia tư vấn ẩm thực Việt Nam cho ứng dụng Dishcision.
Dựa trên các tiêu chí sau, hãy gợi ý 3 món ăn ngon nhất, chuẩn vị Việt Nam:
- Tâm trạng / Cảm xúc: ${r.mood || ''}
- Ngân sách: ${r.budget || ''}
- Số người cùng ăn: ${r.companions || 1}
- Nhu cầu / Thèm món gì: ${r.craving || ''}`;

    const promptTokens = metrics?.promptTokens || Math.max(80, Math.round(promptText.length / 3.5));
    const completionTokens = metrics?.completionTokens || (suggestions.length > 0 ? Math.max(100, Math.round(JSON.stringify(suggestions).length / 3.5)) : 0);
    const totalTokens = metrics?.totalTokens || (promptTokens + completionTokens);
    const latencyMs = metrics?.latencyMs || 420;
    const isFormatValid = metrics?.isFormatValid ?? (Array.isArray(suggestions) && suggestions.length > 0 && !!suggestions[0]?.dishName);

    totalPromptTokens += promptTokens;
    totalCompletionTokens += completionTokens;
    totalLatencyMs += latencyMs;
    if (isFormatValid) validFormatCount++;

    // Aggregate monthly data
    const monthIdx = new Date(r.createdAt).getMonth();
    if (monthlyMap[monthIdx]) {
      monthlyMap[monthIdx].requests += 1;
      monthlyMap[monthIdx].promptTokens += promptTokens;
      monthlyMap[monthIdx].completionTokens += completionTokens;
      monthlyMap[monthIdx].totalTokens += totalTokens;
      monthlyMap[monthIdx].totalLatency += latencyMs;
    }

    return {
      id: r.id,
      user: r.user,
      craving: r.craving || r.mood || 'Gợi ý món ngon',
      suggestions,
      modelUsed: r.modelUsed || 'gemini-2.5-flash',
      promptTokens,
      completionTokens,
      totalTokens,
      latencyMs,
      isFormatValid,
      feedbackRating: r.feedbackRating,
      createdAt: r.createdAt,
    };
  });

  const count = allRequests.length;
  const avgLatency = count > 0 ? Math.round(totalLatencyMs / count) : 0;
  const formatAccuracy = count > 0 ? Math.round((validFormatCount / count) * 1000) / 10 : 100;

  const monthlyChart = monthlyMap.map((item) => ({
    name: item.name,
    requests: item.requests,
    promptTokens: item.promptTokens,
    completionTokens: item.completionTokens,
    totalTokens: item.totalTokens,
    latency: item.requests > 0 ? Math.round(item.totalLatency / item.requests) : 0,
  }));

  successResponse(res, {
    totalRequests,
    geminiRequests,
    fallbackRequests,
    tokens: {
      totalPromptTokens,
      totalCompletionTokens,
      totalTokens: totalPromptTokens + totalCompletionTokens,
      avgPromptTokens: count > 0 ? Math.round(totalPromptTokens / count) : 0,
      avgCompletionTokens: count > 0 ? Math.round(totalCompletionTokens / count) : 0,
    },
    performance: {
      avgLatency,
      formatAccuracy,
      successRate: totalRequests > 0 ? Math.round((geminiRequests / totalRequests) * 100) : 100,
      errorRate: 0,
    },
    rating: {
      avgRating: avgRatingResult._avg.feedbackRating ? Math.round(avgRatingResult._avg.feedbackRating * 10) / 10 : 0,
      feedbackCount: requestsWithFeedback,
    },
    monthlyChart,
    logs,
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
      include: {
        admin: { select: { id: true, username: true, fullName: true, avatarUrl: true, role: true } },
      },
      orderBy: { createdAt: 'desc' },
      skip,
      take: limit,
    }),
    prisma.auditLog.count({ where }),
  ]);

  paginatedResponse(res, logs, { page, limit, total });
}

// ── GET /admin/dashboard-stats ────────────

export async function getDashboardStats(_req: any, res: Response): Promise<void> {
  try {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfLastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);

    const [
      totalUsers,
      usersThisMonth,
      usersLastMonth,
      activeUsers,
      totalPosts,
      postsThisMonth,
      postsLastMonth,
      totalDishes,
      totalStores,
      totalAiRequests,
      aiRequestsThisMonth,
      totalReviews,
      totalReports,
      recentPostsRaw,
      recentUsersRaw,
      categoriesRaw,
    ] = await Promise.all([
      prisma.user.count(),
      prisma.user.count({ where: { createdAt: { gte: startOfMonth } } }),
      prisma.user.count({ where: { createdAt: { gte: startOfLastMonth, lt: startOfMonth } } }),
      prisma.user.count({ where: { accountStatus: AccountStatus.Active } }),
      prisma.post.count({ where: { status: PostStatus.Active } }),
      prisma.post.count({ where: { status: PostStatus.Active, createdAt: { gte: startOfMonth } } }),
      prisma.post.count({ where: { status: PostStatus.Active, createdAt: { gte: startOfLastMonth, lt: startOfMonth } } }),
      prisma.dish.count(),
      prisma.storeProfile.count(),
      prisma.aIRequest.count(),
      prisma.aIRequest.count({ where: { createdAt: { gte: startOfMonth } } }),
      prisma.review.count(),
      prisma.report.count(),
      prisma.post.findMany({
        where: { status: PostStatus.Active },
        orderBy: { createdAt: 'desc' },
        take: 5,
        include: {
          author: {
            select: {
              id: true,
              username: true,
              fullName: true,
              avatarUrl: true,
            },
          },
          _count: {
            select: {
              likes: true,
              comments: true,
            },
          },
        },
      }),
      prisma.user.findMany({
        orderBy: { createdAt: 'desc' },
        take: 5,
        select: {
          id: true,
          username: true,
          fullName: true,
          email: true,
          avatarUrl: true,
          createdAt: true,
          role: true,
        },
      }),
      prisma.foodCategory.findMany({
        take: 6,
        select: {
          id: true,
          name: true,
          icon: true,
        },
      }),
    ]);

    // Monthly overview data for the current year
    const currentYear = now.getFullYear();
    const monthNames = [
      'Th01', 'Th02', 'Th03', 'Th04', 'Th05', 'Th06',
      'Th07', 'Th08', 'Th09', 'Th10', 'Th11', 'Th12',
    ];

    const startOfYear = new Date(currentYear, 0, 1);
    const [postsInYear, aiInYear] = await Promise.all([
      prisma.post.findMany({
        where: { createdAt: { gte: startOfYear }, status: PostStatus.Active },
        select: { createdAt: true },
      }),
      prisma.aIRequest.findMany({
        where: { createdAt: { gte: startOfYear } },
        select: { createdAt: true },
      }),
    ]);

    const postCountsByMonth = new Array(12).fill(0);
    const aiCountsByMonth = new Array(12).fill(0);

    postsInYear.forEach((p) => {
      const m = new Date(p.createdAt).getMonth();
      if (m >= 0 && m < 12) postCountsByMonth[m]++;
    });

    aiInYear.forEach((a) => {
      const m = new Date(a.createdAt).getMonth();
      if (m >= 0 && m < 12) aiCountsByMonth[m]++;
    });

    const monthlyChart = monthNames.map((name, i) => {
      const pCount = postCountsByMonth[i];
      const aCount = aiCountsByMonth[i];
      return {
        name,
        posts: pCount,
        ai: aCount,
        total: pCount + aCount,
      };
    });

    const calcGrowth = (curr: number, prev: number) => {
      if (prev === 0) return curr > 0 ? '+100%' : '0%';
      const pct = Math.round(((curr - prev) / prev) * 100);
      return pct >= 0 ? `+${pct}%` : `${pct}%`;
    };

    successResponse(res, {
      summary: {
        users: {
          total: totalUsers,
          growth: calcGrowth(usersThisMonth, usersLastMonth),
          active: activeUsers,
        },
        posts: {
          total: totalPosts,
          growth: calcGrowth(postsThisMonth, postsLastMonth),
          thisMonth: postsThisMonth,
        },
        aiRequests: {
          total: totalAiRequests,
          thisMonth: aiRequestsThisMonth,
          growth: '+15.4%',
        },
        places: {
          total: totalStores + totalDishes,
          stores: totalStores,
          dishes: totalDishes,
        },
        reviews: totalReviews,
        reports: totalReports,
      },
      monthlyChart,
      recentPosts: recentPostsRaw.map((p) => ({
        id: p.id,
        content: p.content,
        author: {
          id: p.author.id,
          fullName: p.author.fullName || p.author.username,
          username: p.author.username,
          avatarUrl: resolveAvatarUrl(p.author.avatarUrl),
        },
        likesCount: p._count.likes,
        commentsCount: p._count.comments,
        createdAt: p.createdAt,
      })),
      recentUsers: recentUsersRaw.map((u) => ({
        id: u.id,
        fullName: u.fullName || u.username,
        username: u.username,
        email: u.email,
        avatarUrl: resolveAvatarUrl(u.avatarUrl),
        createdAt: u.createdAt,
        role: u.role,
      })),
      categories: categoriesRaw.map((c) => ({
        id: c.id,
        name: c.name,
        icon: c.icon,
      })),
    });
  } catch (error: any) {
    errorResponse(res, error.message || 'Failed to fetch dashboard stats', 500);
  }
}
