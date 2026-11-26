import { Router } from 'express';
import {
  getDashboardStats,
  listUsers,
  updateUserStatus,
  warnUser,
  listPosts,
  pinPost,
  hidePost,
  deletePostAdmin,
  listReports,
  processReport,
  listAIConfigs,
  setActiveAIConfig,
  listAIPrompts,
  updateAIPrompt,
  getAIStats,
  broadcastSystemNotification,
  getAuditLogs,
} from '../controllers/admin.controller';
import { authenticate, authorizeAdmin } from '../middlewares/auth.middleware';
import {
  validate,
  updateUserStatusSchema,
  warnUserSchema,
  processReportSchema,
  systemNotificationSchema,
} from '../middlewares/validate.middleware';

const router = Router();

// Dashboard Stats (overview)
router.get('/dashboard-stats', getDashboardStats);

// Post Management (accessible from web-admin)
router.get('/posts', listPosts);
router.patch('/posts/:postId/pin', pinPost);
router.patch('/posts/:postId/hide', hidePost);
router.delete('/posts/:postId', deletePostAdmin);

// User Management (accessible from web-admin)
router.get('/users', listUsers);
router.patch('/users/:userId/status', updateUserStatus);

// AI Management (accessible from web-admin)
router.get('/ai-stats', getAIStats);
router.get('/ai-configs', listAIConfigs);
router.get('/ai-prompts', listAIPrompts);

// Audit Logs (accessible from web-admin)
router.get('/audit-logs', getAuditLogs);

// Protect all other admin routes
router.use(authenticate, authorizeAdmin);

router.post('/users/:userId/warn', validate(warnUserSchema), warnUser);

// Reports Management
router.get('/reports', listReports);
router.patch('/reports/:reportId/process', validate(processReportSchema), processReport);

// AI Mutation Management
router.put('/ai-configs/:configId/active', setActiveAIConfig);
router.put('/ai-prompts/:promptId', updateAIPrompt);

// System Notifications & Audit Logs
router.post('/system-notifications', validate(systemNotificationSchema), broadcastSystemNotification);
router.get('/audit-logs', getAuditLogs);

export default router;
