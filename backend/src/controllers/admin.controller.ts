import { Response } from 'express';
import { prisma } from '../config/prisma';
import { delCache, delCacheByPattern } from '../config/redis';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';
import { formatUserPrivate, resolveAvatarUrl } from './user.controller';
import { formatPost, POST_INCLUDE } from './post.controller';
import { AccountStatus, Role, PostStatus, ReportStatus, ReportPriority, AuditTargetType, Prisma } from '@prisma/client';

// ── Helper to resolve admin actor for AuditLog ────
async function getAdminActorInfo(req: AuthRequest) {
  let adminUser = null;
  if (req.user?.userId) {
    adminUser = await prisma.user.findUnique({ where: { id: req.user.userId } });
  }
  if (!adminUser) {
    adminUser = await prisma.user.findFirst({
      where: { role: 'Admin' },
      orderBy: { id: 'asc' },
    });
  }
  return {
    adminId: adminUser ? adminUser.id : undefined,
    actorUsername: adminUser?.username || 'Admin',
    actorRole: 'Admin',
  };
}

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

  try {
    const actor = await getAdminActorInfo(req);
    await prisma.auditLog.create({
      data: {
        adminId: actor.adminId,
        actorUsername: actor.actorUsername,
        actorRole: actor.actorRole,
        action: `UPDATE_USER_STATUS_${status}`,
        targetType: AuditTargetType.User,
        targetId: userId,
        ipAddress: req.ip,
        details: reason,
      },
    });
  } catch (logErr) {
    console.error('AuditLog creation warning in updateUserStatus:', logErr);
  }

  successResponse(res, null, 'Status updated successfully');
}

// ── POST /admin/users/:userId/verify ───────

export async function verifyUser(req: AuthRequest, res: Response): Promise<void> {
  const userId = parseInt(req.params.userId);
  const { approved, reason } = req.body;

  const targetUser = await prisma.user.findUnique({ where: { id: userId } });
  if (!targetUser) {
    errorResponse(res, 'User not found', 404);
    return;
  }

  const isApproved = Boolean(approved);
  const rejectionReason = isApproved
    ? null
    : reason && String(reason).trim()
    ? String(reason).trim()
    : 'Không đáp ứng đủ điều kiện tiêu chuẩn quán ăn';

  const updatedUser = await prisma.user.update({
    where: { id: userId },
    data: {
      isVerified: isApproved,
      verificationStatus: isApproved ? 'Approved' : 'Rejected',
      verificationRejectionReason: rejectionReason,
    },
  });

  await Promise.all([
    delCache(`user:${userId}`),
    delCache('search:trending'),
    delCacheByPattern('post:*'),
    delCacheByPattern('newsfeed:*'),
  ]);

  const notificationMsg = isApproved
    ? 'Chúc mừng! Hồ sơ của bạn đã được quản trị viên phê duyệt cấp tích xanh xác thực thành công.'
    : `Yêu cầu cấp tích xanh xác thực của bạn không được phê duyệt. Lý do: ${rejectionReason}`;

  await prisma.notification.create({
    data: {
      userId,
      type: 'System',
      message: notificationMsg,
    },
  });

  try {
    const actor = await getAdminActorInfo(req);
    await prisma.auditLog.create({
      data: {
        adminId: actor.adminId,
        actorUsername: actor.actorUsername,
        actorRole: actor.actorRole,
        action: isApproved ? 'APPROVE_VERIFICATION' : 'REJECT_VERIFICATION',
        targetType: AuditTargetType.User,
        targetId: userId,
        ipAddress: req.ip,
        details: isApproved
          ? `Phê duyệt cấp tích xanh xác thực cho tài khoản @${targetUser.username}`
          : `Từ chối cấp tích xanh cho tài khoản @${targetUser.username}. Lý do: ${rejectionReason}`,
      },
    });
  } catch (logErr) {
    console.error('AuditLog creation warning in verifyUser:', logErr);
  }

  successResponse(
    res,
    {
      userId: updatedUser.id,
      isVerified: updatedUser.isVerified,
      verificationStatus: updatedUser.verificationStatus,
      verificationRejectionReason: updatedUser.verificationRejectionReason,
    },
    isApproved ? 'Phê duyệt tích xanh thành công' : 'Đã từ chối cấp tích xanh'
  );
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

  try {
    const actor = await getAdminActorInfo(req);
    await prisma.auditLog.create({
      data: {
        adminId: actor.adminId,
        actorUsername: actor.actorUsername,
        actorRole: actor.actorRole,
        action: 'WARN_USER',
        targetType: AuditTargetType.User,
        targetId: userId,
        ipAddress: req.ip,
        details: message,
      },
    });
  } catch (logErr) {
    console.error('AuditLog creation warning in warnUser:', logErr);
  }

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

  try {
    const actor = await getAdminActorInfo(req);
    await prisma.auditLog.create({
      data: {
        adminId: actor.adminId,
        actorUsername: actor.actorUsername,
        actorRole: actor.actorRole,
        action: isPinned ? 'PIN_POST' : 'UNPIN_POST',
        targetType: AuditTargetType.Post,
        targetId: postId,
        ipAddress: req.ip,
      },
    });
  } catch (logErr) {
    console.error('AuditLog creation warning in pinPost:', logErr);
  }

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

  try {
    const actor = await getAdminActorInfo(req);
    await prisma.auditLog.create({
      data: {
        adminId: actor.adminId,
        actorUsername: actor.actorUsername,
        actorRole: actor.actorRole,
        action: isHidden ? 'HIDE_POST' : 'UNHIDE_POST',
        targetType: AuditTargetType.Post,
        targetId: postId,
        ipAddress: req.ip,
        details: reason,
      },
    });
  } catch (logErr) {
    console.error('AuditLog creation warning in hidePost:', logErr);
  }

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

  try {
    const actor = await getAdminActorInfo(req);
    await prisma.auditLog.create({
      data: {
        adminId: actor.adminId,
        actorUsername: actor.actorUsername,
        actorRole: actor.actorRole,
        action: 'DELETE_POST',
        targetType: AuditTargetType.Post,
        targetId: postId,
        ipAddress: req.ip,
      },
    });
  } catch (logErr) {
    console.error('AuditLog creation warning in deletePostAdmin:', logErr);
  }

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

  try {
    const actor = await getAdminActorInfo(req);
    await prisma.auditLog.create({
      data: {
        adminId: actor.adminId,
        actorUsername: actor.actorUsername,
        actorRole: actor.actorRole,
        action: `PROCESS_REPORT_${action}`,
        targetType: AuditTargetType.Report,
        targetId: reportId,
        ipAddress: req.ip,
        details: note,
      },
    });
  } catch (logErr) {
    console.error('AuditLog creation warning in processReport:', logErr);
  }

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

  try {
    const actor = await getAdminActorInfo(req);
    await prisma.auditLog.create({
      data: {
        adminId: actor.adminId,
        actorUsername: actor.actorUsername,
        actorRole: actor.actorRole,
        action: 'SWITCH_AI_CONFIG',
        targetType: AuditTargetType.AIConfig,
        targetId: configId,
        ipAddress: req.ip,
      },
    });
  } catch (logErr) {
    console.error('AuditLog creation warning in setActiveAIConfig:', logErr);
  }

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

  try {
    const actor = await getAdminActorInfo(req);
    await prisma.auditLog.create({
      data: {
        adminId: actor.adminId,
        actorUsername: actor.actorUsername,
        actorRole: actor.actorRole,
        action: 'UPDATE_AI_PROMPT',
        targetType: AuditTargetType.AIConfig,
        targetId: promptId,
        ipAddress: req.ip,
      },
    });
  } catch (logErr) {
    console.error('AuditLog creation warning in updateAIPrompt:', logErr);
  }

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
    totalRating: 0,
    ratingCount: 0,
  }));

  const starCounts: Record<number, number> = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
  let feedbackCount = 0;
  let totalRatingSum = 0;

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

    const monthIdx = new Date(r.createdAt).getMonth();
    if (monthlyMap[monthIdx]) {
      monthlyMap[monthIdx].requests += 1;
      monthlyMap[monthIdx].promptTokens += promptTokens;
      monthlyMap[monthIdx].completionTokens += completionTokens;
      monthlyMap[monthIdx].totalTokens += totalTokens;
      monthlyMap[monthIdx].totalLatency += latencyMs;
    }

    if (typeof r.feedbackRating === 'number' && r.feedbackRating >= 1 && r.feedbackRating <= 5) {
      starCounts[r.feedbackRating] = (starCounts[r.feedbackRating] || 0) + 1;
      feedbackCount += 1;
      totalRatingSum += r.feedbackRating;
      if (monthlyMap[monthIdx]) {
        monthlyMap[monthIdx].totalRating += r.feedbackRating;
        monthlyMap[monthIdx].ratingCount += 1;
      }
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

  const totalFeedbacks = requestsWithFeedback || feedbackCount;
  const distribution = [5, 4, 3, 2, 1].map((star) => {
    const starCount = starCounts[star] || 0;
    const percentage = totalFeedbacks > 0 ? Math.round((starCount / totalFeedbacks) * 1000) / 10 : 0;
    return {
      star,
      label: `${star} sao`,
      count: starCount,
      percentage,
    };
  });

  const monthlyChart = monthlyMap.map((item) => ({
    name: item.name,
    requests: item.requests,
    promptTokens: item.promptTokens,
    completionTokens: item.completionTokens,
    totalTokens: item.totalTokens,
    latency: item.requests > 0 ? Math.round(item.totalLatency / item.requests) : 0,
    avgRating: item.ratingCount > 0 ? Math.round((item.totalRating / item.ratingCount) * 10) / 10 : 0,
    ratingCount: item.ratingCount,
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
      distribution,
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

  const oneDayAgo = new Date(Date.now() - 24 * 60 * 60 * 1000);

  const [logs, total, todayCount, recentSessions, recentLogsUsers] = await Promise.all([
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
    prisma.auditLog.count({ where: { createdAt: { gte: oneDayAgo } } }),
    prisma.userSession.findMany({
      orderBy: { lastActive: 'desc' },
      take: 40,
      include: {
        user: { select: { id: true, username: true, fullName: true, avatarUrl: true, role: true, accountStatus: true } },
      },
    }),
    prisma.auditLog.findMany({
      where: { createdAt: { gte: oneDayAgo } },
      take: 50,
      orderBy: { createdAt: 'desc' },
      include: {
        admin: { select: { id: true, username: true, fullName: true, avatarUrl: true, role: true, accountStatus: true } },
      },
    }),
  ]);

  const uniqueUserMap = new Map<number, any>();

  for (const s of recentSessions) {
    if (s.user && s.user.accountStatus === 'Active' && !uniqueUserMap.has(s.user.id)) {
      uniqueUserMap.set(s.user.id, {
        id: s.user.id,
        username: s.user.username,
        fullName: s.user.fullName || s.user.username,
        avatarUrl: s.user.avatarUrl || null,
        role: s.user.role,
        lastActive: s.lastActive,
        deviceName: s.deviceName || 'Thiết bị di động',
        location: s.location || 'Trực tuyến',
      });
    }
  }

  for (const l of recentLogsUsers) {
    if (l.admin && l.admin.accountStatus === 'Active' && !uniqueUserMap.has(l.admin.id)) {
      uniqueUserMap.set(l.admin.id, {
        id: l.admin.id,
        username: l.admin.username,
        fullName: l.admin.fullName || l.admin.username,
        avatarUrl: l.admin.avatarUrl || null,
        role: l.admin.role,
        lastActive: l.createdAt,
        deviceName: 'Trang quản trị web',
        location: l.ipAddress || 'Trực tuyến',
      });
    }
  }

  const activeAccounts = Array.from(uniqueUserMap.values());

  const formattedLogs = logs.map((log: any) => ({
    ...log,
    admin: log.admin
      ? log.admin
      : log.actorUsername
      ? {
          id: log.adminId || log.targetId || 0,
          username: log.actorUsername,
          role: log.actorRole || 'User',
          avatarUrl: log.actorAvatar || null,
        }
      : null,
  }));

  res.status(200).json({
    success: true,
    message: 'Successful',
    data: formattedLogs,
    pagination: {
      page,
      limit,
      total,
      totalPages: Math.ceil(total / limit),
    },
    stats: {
      totalLogs: total,
      todayActionCount: todayCount,
      activeAccountsCount: activeAccounts.length,
      activeAccounts,
    },
  });
}

// ── GET /admin/active-accounts ────────────

export async function getActiveAccounts(_req: AuthRequest, res: Response): Promise<void> {
  const oneDayAgo = new Date(Date.now() - 24 * 60 * 60 * 1000);

  const [recentSessions, recentLogsUsers] = await Promise.all([
    prisma.userSession.findMany({
      orderBy: { lastActive: 'desc' },
      take: 40,
      include: {
        user: { select: { id: true, username: true, fullName: true, avatarUrl: true, role: true, accountStatus: true } },
      },
    }),
    prisma.auditLog.findMany({
      where: { createdAt: { gte: oneDayAgo } },
      take: 50,
      orderBy: { createdAt: 'desc' },
      include: {
        admin: { select: { id: true, username: true, fullName: true, avatarUrl: true, role: true, accountStatus: true } },
      },
    }),
  ]);

  const uniqueUserMap = new Map<number, any>();

  for (const s of recentSessions) {
    if (s.user && s.user.accountStatus === 'Active' && !uniqueUserMap.has(s.user.id)) {
      uniqueUserMap.set(s.user.id, {
        id: s.user.id,
        username: s.user.username,
        fullName: s.user.fullName || s.user.username,
        avatarUrl: s.user.avatarUrl || null,
        role: s.user.role,
        lastActive: s.lastActive,
        deviceName: s.deviceName || 'Thiết bị di động',
        location: s.location || 'Trực tuyến',
      });
    }
  }

  for (const l of recentLogsUsers) {
    if (l.admin && l.admin.accountStatus === 'Active' && !uniqueUserMap.has(l.admin.id)) {
      uniqueUserMap.set(l.admin.id, {
        id: l.admin.id,
        username: l.admin.username,
        fullName: l.admin.fullName || l.admin.username,
        avatarUrl: l.admin.avatarUrl || null,
        role: l.admin.role,
        lastActive: l.createdAt,
        deviceName: 'Trang quản trị web',
        location: l.ipAddress || 'Trực tuyến',
      });
    }
  }

  const activeAccounts = Array.from(uniqueUserMap.values());
  res.status(200).json({
    success: true,
    data: activeAccounts,
    total: activeAccounts.length,
  });
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

// ── GET /admin/support-tickets ──────────────────────
export async function listSupportTickets(req: AuthRequest, res: Response): Promise<void> {
  const status = req.query.status as string;
  const search = ((req.query.search as string) || '').trim().toLowerCase();
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 50;
  const skip = (page - 1) * limit;

  const where: any = {};
  if (status && status !== 'all') {
    if (status === 'pending') {
      where.status = { in: ['Đã tiếp nhận', 'Chờ xử lý', 'Pending'] };
    } else if (status === 'resolved') {
      where.status = { in: ['Đã giải quyết', 'Đã xử lý', 'Resolved'] };
    } else {
      where.status = status;
    }
  }

  if (search) {
    const searchNum = parseInt(search);
    where.OR = [
      { title: { contains: search, mode: 'insensitive' } },
      { subject: { contains: search, mode: 'insensitive' } },
      { description: { contains: search, mode: 'insensitive' } },
      { user: { fullName: { contains: search, mode: 'insensitive' } } },
      { user: { username: { contains: search, mode: 'insensitive' } } },
      ...(!isNaN(searchNum) ? [{ id: searchNum }] : []),
    ];
  }

  const [tickets, total, totalAll, pendingCount, resolvedCount] = await Promise.all([
    prisma.supportTicket.findMany({
      where,
      include: {
        user: {
          select: {
            id: true,
            username: true,
            fullName: true,
            email: true,
            avatarUrl: true,
            role: true,
          },
        },
      },
      orderBy: { createdAt: 'desc' },
      skip,
      take: limit,
    }),
    prisma.supportTicket.count({ where }),
    prisma.supportTicket.count(),
    prisma.supportTicket.count({
      where: { status: { in: ['Đã tiếp nhận', 'Chờ xử lý', 'Pending'] } },
    }),
    prisma.supportTicket.count({
      where: { status: { in: ['Đã giải quyết', 'Đã xử lý', 'Resolved'] } },
    }),
  ]);

  const formattedTickets = tickets.map((t) => ({
    id: t.id,
    userId: t.userId,
    user: {
      id: t.user.id,
      username: t.user.username,
      fullName: t.user.fullName || t.user.username,
      email: t.user.email,
      avatarUrl: resolveAvatarUrl(t.user.avatarUrl),
      role: t.user.role,
    },
    type: t.type,
    subject: t.subject,
    title: t.title,
    description: t.description,
    contactEmail: t.contactEmail || t.user.email,
    status: t.status,
    attachments: Array.isArray(t.attachments) ? t.attachments : [],
    timeline: Array.isArray(t.timeline) ? t.timeline : [],
    createdAt: t.createdAt,
    updatedAt: t.updatedAt,
  }));

  res.status(200).json({
    success: true,
    message: 'Successful',
    data: formattedTickets,
    pagination: {
      page,
      limit,
      total,
      totalPages: Math.ceil(total / limit),
    },
    stats: {
      total: totalAll,
      pending: pendingCount,
      resolved: resolvedCount,
    },
  });
}

// ── POST /admin/support-tickets/:ticketId/reply ──────
export async function replySupportTicket(req: AuthRequest, res: Response): Promise<void> {
  const ticketId = parseInt(req.params.ticketId);
  const { replyMessage, status } = req.body;

  if (!replyMessage || !String(replyMessage).trim()) {
    errorResponse(res, 'Vui lòng nhập nội dung phản hồi', 400);
    return;
  }

  const ticket = await prisma.supportTicket.findUnique({
    where: { id: ticketId },
    include: { user: true },
  });

  if (!ticket) {
    errorResponse(res, 'Không tìm thấy yêu cầu hỗ trợ', 404);
    return;
  }

  const cleanReply = String(replyMessage).trim();
  const nextStatus = status && String(status).trim() ? String(status).trim() : 'Đã giải quyết';
  const dateStr = new Date().toLocaleDateString('vi-VN');

  const currentTimeline = Array.isArray(ticket.timeline) ? (ticket.timeline as any[]) : [];
  const updatedTimeline = [
    ...currentTimeline,
    {
      step: nextStatus,
      date: dateStr,
      desc: `Quản trị viên đã phản hồi: ${cleanReply}`,
      isDone: true,
    },
  ];

  const updatedTicket = await prisma.supportTicket.update({
    where: { id: ticketId },
    data: {
      status: nextStatus,
      timeline: updatedTimeline,
    },
    include: {
      user: {
        select: {
          id: true,
          username: true,
          fullName: true,
          email: true,
          avatarUrl: true,
          role: true,
        },
      },
    },
  });

  const notificationContent = `Ban quản trị đã phản hồi yêu cầu hỗ trợ "${ticket.title}": ${cleanReply}`;
  await prisma.notification.create({
    data: {
      userId: ticket.userId,
      type: 'System',
      message: notificationContent,
    },
  });

  try {
    const actor = await getAdminActorInfo(req);
    await prisma.auditLog.create({
      data: {
        adminId: actor.adminId,
        actorUsername: actor.actorUsername,
        actorRole: actor.actorRole,
        action: 'REPLY_SUPPORT_TICKET',
        targetType: AuditTargetType.User,
        targetId: ticket.userId,
        ipAddress: req.ip,
        details: `Phản hồi yêu cầu hỗ trợ #${ticket.id} cho tài khoản @${ticket.user.username}: ${cleanReply}`,
      },
    });
  } catch (logErr) {
    console.error('AuditLog creation warning in replySupportTicket:', logErr);
  }

  successResponse(
    res,
    {
      id: updatedTicket.id,
      status: updatedTicket.status,
      timeline: updatedTicket.timeline,
      replyMessage: cleanReply,
    },
    'Phản hồi yêu cầu hỗ trợ thành công và đã gửi thông báo đến người dùng',
  );
}
