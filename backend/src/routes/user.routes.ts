import { Router } from 'express';
import {
  getMe,
  updateMe,
  deleteMe,
  getUserById,
  getUserPosts,
  getMyActivity,
  getPrivacySettings,
  updatePrivacySettings,
  getBlockedAccounts,
  blockAccount,
  unblockAccount,
  getRestrictedAccounts,
  restrictAccount,
  unrestrictAccount,
} from '../controllers/user.controller';
import {
  getUserPreferences,
  updateUserPreferences,
} from '../controllers/preference.controller';
import { authenticate, optionalAuth } from '../middlewares/auth.middleware';
import { asyncHandler } from '../utils/http';
import { validate, updateProfileSchema } from '../middlewares/validate.middleware';

const router = Router();

router.get('/me', authenticate, asyncHandler(getMe));
router.get('/me/posts', authenticate, asyncHandler(getUserPosts));
router.get('/me/activity', authenticate, asyncHandler(getMyActivity));
router.put('/me', authenticate, validate(updateProfileSchema), asyncHandler(updateMe));
router.delete('/me', authenticate, asyncHandler(deleteMe));
router.get('/me/preferences', authenticate, asyncHandler(getUserPreferences));
router.put('/me/preferences', authenticate, asyncHandler(updateUserPreferences));

// Privacy & Blocked / Restricted
router.get('/me/privacy', authenticate, asyncHandler(getPrivacySettings));
router.put('/me/privacy', authenticate, asyncHandler(updatePrivacySettings));
router.get('/me/blocked', authenticate, asyncHandler(getBlockedAccounts));
router.post('/me/block', authenticate, asyncHandler(blockAccount));
router.post('/me/unblock', authenticate, asyncHandler(unblockAccount));
router.get('/me/restricted', authenticate, asyncHandler(getRestrictedAccounts));
router.post('/me/restrict', authenticate, asyncHandler(restrictAccount));
router.post('/me/unrestrict', authenticate, asyncHandler(unrestrictAccount));

router.get('/:userId', optionalAuth, asyncHandler(getUserById));
router.get('/:userId/posts', optionalAuth, asyncHandler(getUserPosts));

export default router;

