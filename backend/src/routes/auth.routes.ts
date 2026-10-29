import { Router } from 'express';
import {
  register,
  login,
  logout,
  forgotPassword,
  verifyOtp,
  resetPassword,
  changePassword,
  get2FAStatus,
  verify2FA,
  disable2FA,
  getSessions,
  revokeSession,
} from '../controllers/auth.controller';
import { authenticate, optionalAuth } from '../middlewares/auth.middleware';
import {
  validate,
  registerSchema,
  loginSchema,
  forgotPasswordSchema,
  resetPasswordSchema,
  changePasswordSchema,
} from '../middlewares/validate.middleware';

const router = Router();

router.post('/register', validate(registerSchema), register);
router.post('/login', validate(loginSchema), login);
router.post('/logout', optionalAuth, logout);
router.post('/forgot-password', validate(forgotPasswordSchema), forgotPassword);
router.post('/verify-otp', verifyOtp);
router.post('/reset-password', validate(resetPasswordSchema), resetPassword);
router.post('/change-password', optionalAuth, validate(changePasswordSchema), changePassword);

// 2FA Security
router.get('/2fa/status', optionalAuth, get2FAStatus);
router.post('/2fa/verify', optionalAuth, verify2FA);
router.post('/2fa/disable', optionalAuth, disable2FA);

// Active Sessions
router.get('/sessions', optionalAuth, getSessions);
router.post('/sessions/revoke', optionalAuth, revokeSession);

export default router;
