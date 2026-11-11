import { Router } from 'express';
import {
  register,
  login,
  verifyLogin2FA,
  resendLogin2FA,
  googleAuth,
  logout,
  forgotPassword,
  verifyOtp,
  resetPassword,
  changePassword,
  get2FAStatus,
  get2FASetup,
  verify2FA,
  disable2FA,
  getSessions,
  revokeSession,
  getSessionStatus,
} from '../controllers/auth.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { asyncHandler } from '../utils/http';
import {
  validate,
  registerSchema,
  loginSchema,
  verifyLogin2FASchema,
  resendLogin2FASchema,
  googleAuthSchema,
  forgotPasswordSchema,
  verifyOtpSchema,
  resetPasswordSchema,
  changePasswordSchema,
  verify2FASchema,
} from '../middlewares/validate.middleware';

const router = Router();

router.post('/register', validate(registerSchema), asyncHandler(register));
router.post('/login', validate(loginSchema), asyncHandler(login));
router.post('/login/verify-2fa', validate(verifyLogin2FASchema), asyncHandler(verifyLogin2FA));
router.post('/login/resend-2fa', validate(resendLogin2FASchema), asyncHandler(resendLogin2FA));
router.post('/google', validate(googleAuthSchema), asyncHandler(googleAuth));
router.post('/logout', authenticate, asyncHandler(logout));
router.post('/forgot-password', validate(forgotPasswordSchema), asyncHandler(forgotPassword));
router.post('/verify-otp', validate(verifyOtpSchema), asyncHandler(verifyOtp));
router.post('/reset-password', validate(resetPasswordSchema), asyncHandler(resetPassword));
router.post('/change-password', authenticate, validate(changePasswordSchema), asyncHandler(changePassword));

// 2FA Security
router.get('/2fa/status', authenticate, asyncHandler(get2FAStatus));
router.get('/2fa/setup', authenticate, asyncHandler(get2FASetup));
router.post('/2fa/verify', authenticate, validate(verify2FASchema), asyncHandler(verify2FA));
router.post('/2fa/disable', authenticate, asyncHandler(disable2FA));

// Active Sessions
router.get('/sessions', authenticate, asyncHandler(getSessions));
router.post('/sessions/revoke', authenticate, asyncHandler(revokeSession));
router.get('/session-status', authenticate, asyncHandler(getSessionStatus));

export default router;
