import { Router } from 'express';
import {
  getMe,
  updateMe,
  getUserById,
  getUserPosts,
  getPrivacySettings,
  updatePrivacySettings,
  getBlockedAccounts,
  unblockAccount,
  getRestrictedAccounts,
  unrestrictAccount,
} from '../controllers/user.controller';
import {
  getUserPreferences,
  updateUserPreferences,
} from '../controllers/preference.controller';
import { authenticate, optionalAuth } from '../middlewares/auth.middleware';
import { validate, updateProfileSchema } from '../middlewares/validate.middleware';

const router = Router();

router.get('/me', authenticate, getMe);
router.put('/me', authenticate, validate(updateProfileSchema), updateMe);
router.get('/me/preferences', authenticate, getUserPreferences);
router.put('/me/preferences', authenticate, updateUserPreferences);

// Privacy & Blocked / Restricted
router.get('/me/privacy', optionalAuth, getPrivacySettings);
router.put('/me/privacy', optionalAuth, updatePrivacySettings);
router.get('/me/blocked', optionalAuth, getBlockedAccounts);
router.post('/me/unblock', optionalAuth, unblockAccount);
router.get('/me/restricted', optionalAuth, getRestrictedAccounts);
router.post('/me/unrestrict', optionalAuth, unrestrictAccount);

router.get('/:userId', optionalAuth, getUserById);
router.get('/:userId/posts', optionalAuth, getUserPosts);

export default router;

