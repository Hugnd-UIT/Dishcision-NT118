import { Router } from 'express';
import { getNotifications, markAsRead } from '../controllers/notification.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { asyncHandler } from '../utils/http';

const router = Router();

router.get('/', authenticate, asyncHandler(getNotifications));
router.patch('/:notificationId/read', authenticate, asyncHandler(markAsRead));

export default router;
