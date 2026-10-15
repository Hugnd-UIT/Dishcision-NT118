import { Router } from 'express';
import {
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

// Protect all admin routes
router.use(authenticate, authorizeAdmin);

// User Management
router.get('/users', listUsers);
router.patch('/users/:userId/status', validate(updateUserStatusSchema), updateUserStatus);
router.post('/users/:userId/warn', validate(warnUserSchema), warnUser);

// Post Management
router.get('/posts', listPosts);
router.patch('/posts/:postId/pin', pinPost);
router.patch('/posts/:postId/hide', hidePost);
router.delete('/posts/:postId', deletePostAdmin);

// Reports Management
router.get('/reports', listReports);
router.patch('/reports/:reportId/process', validate(processReportSchema), processReport);

// AI Management
router.get('/ai-configs', listAIConfigs);
router.put('/ai-configs/:configId/active', setActiveAIConfig);
router.get('/ai-prompts', listAIPrompts);
router.put('/ai-prompts/:promptId', updateAIPrompt);
router.get('/ai-stats', getAIStats);

// System Notifications & Audit Logs
router.post('/system-notifications', validate(systemNotificationSchema), broadcastSystemNotification);
router.get('/audit-logs', getAuditLogs);

export default router;
