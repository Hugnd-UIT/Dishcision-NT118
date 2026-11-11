import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { redis } from '../config/redis';
import { hashPassword, comparePassword, generateAccessToken, generateRefreshToken, getExpiresIn } from '../utils/auth';
import { successResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';
import { DEFAULT_AVATAR_URL, resolveAvatarUrl } from './user.controller';
import crypto from 'crypto';
import nodemailer from 'nodemailer';

// ── Helpers ───────────────────────────────

const USER_PUBLIC_SELECT = {
  id: true, username: true, fullName: true, avatarUrl: true, bio: true, role: true,
  _count: { select: { followers: true, following: true } },
};

function formatUser(user: any) {
  const { _count } = user;
  return {
    userId: user.id,
    username: user.username,
    fullName: user.fullName,
    avatarUrl: resolveAvatarUrl(user.avatarUrl),
    bio: user.bio,
    role: user.role,
    isGoogleAccount: typeof user.passwordHash === 'string' ? user.passwordHash === '' : undefined,
    followersCount: _count?.followers ?? 0,
    followingCount: _count?.following ?? 0,
  };
}

function hashToken(token: string): string {
  return crypto.createHash('sha256').update(token).digest('hex');
}

function extractDeviceName(req: Request): string {
  const explicit = (req.body as any)?.deviceName;
  if (explicit && typeof explicit === 'string' && explicit.trim()) {
    return explicit.trim().slice(0, 150);
  }
  const ua = (req.get('user-agent') || '').toLowerCase();
  if (ua.includes('okhttp') || ua.includes('android')) {
    return 'Điện thoại Android · Dishcision';
  }
  if (ua.includes('iphone') || ua.includes('ipad') || ua.includes('ios')) {
    return 'iOS · Dishcision';
  }
  if (ua.includes('mozilla') || ua.includes('chrome') || ua.includes('safari') || ua.includes('edge')) {
    return 'Trình duyệt Web · Dishcision';
  }
  return 'Thiết bị Dishcision';
}

function extractLocation(req: Request): string | null {
  const explicit = (req.body as any)?.location;
  if (explicit && typeof explicit === 'string' && explicit.trim()) {
    return explicit.trim().slice(0, 150);
  }
  const rawIp = (req.ip || '').replace(/^::ffff:/, '').trim();
  return rawIp ? `IP ${rawIp}` : null;
}

async function recordSession(userId: number, req: Request, token: string): Promise<void> {
  await prisma.userSession.create({
    data: {
      userId,
      deviceName: extractDeviceName(req),
      location: extractLocation(req),
      tokenHash: hashToken(token),
    },
  });
}

async function generateUniqueUsername(seedText: string): Promise<string> {
  let base = seedText.toLowerCase().replace(/[^a-z0-9_]/g, '');
  if (base.length < 3) {
    base = `user_${base}`;
  }
  base = base.slice(0, 30);
  let candidate = base;
  let count = 0;
  while (await prisma.user.findUnique({ where: { username: candidate } })) {
    count++;
    const suffix = Math.floor(1000 + Math.random() * 9000);
    candidate = `${base.slice(0, 40)}_${suffix}`;
    if (count > 20) {
      candidate = `user_${crypto.randomBytes(4).toString('hex')}`;
      break;
    }
  }
  return candidate;
}

type OtpEmailPurpose = 'reset_password' | 'enable_2fa' | 'login_2fa';

function maskEmail(email: string): string {
  const parts = String(email || '').split('@');
  if (parts.length !== 2) return email;
  const local = parts[0];
  const domain = parts[1];
  const prefix = local.slice(0, Math.min(2, local.length));
  return `${prefix}***@${domain}`;
}

async function sendOtpEmail(email: string, otp: string, purpose: OtpEmailPurpose = 'reset_password'): Promise<boolean> {
  const mailUser = process.env.MAIL_USER || 'hungnd.attt2024@gmail.com';
  const mailKey = process.env.MAIL_KEY?.replace(/\s+/g, '');
  if (!mailKey) {
    console.log(`[MAIL] No MAIL_KEY provided. Skip sending email. OTP (${purpose}) is: ${otp}`);
    return false;
  }

  let subject = 'Mã xác thực OTP Dishcision';
  let title = 'Dishcision - Mã xác nhận OTP';
  let description = 'Bạn vừa yêu cầu mã xác nhận để khôi phục mật khẩu tài khoản Dishcision.';

  if (purpose === 'enable_2fa') {
    subject = 'Mã xác thực bật bảo mật 2 lớp Dishcision';
    title = 'Dishcision - Bật xác thực 2 yếu tố';
    description = 'Nhập mã gồm 6 chữ số dưới đây để hoàn tất bật xác thực hai yếu tố qua email cho tài khoản Dishcision của bạn.';
  } else if (purpose === 'login_2fa') {
    subject = 'Mã xác thực đăng nhập Dishcision';
    title = 'Dishcision - Xác thực đăng nhập 2 yếu tố';
    description = 'Có yêu cầu đăng nhập vào tài khoản Dishcision của bạn. Vui lòng nhập mã gồm 6 chữ số dưới đây để hoàn tất đăng nhập:';
  }

  try {
    const transporter = nodemailer.createTransport({
      service: 'gmail',
      auth: {
        user: mailUser,
        pass: mailKey,
      },
    });

    await transporter.sendMail({
      from: `"Dishcision App" <${mailUser}>`,
      to: email,
      subject,
      html: `
        <div style="font-family: Arial, sans-serif; padding: 20px; line-height: 1.6; max-width: 500px; margin: 0 auto; border: 1px solid #eee; border-radius: 8px;">
          <h2 style="color: #ff6b35;">${title}</h2>
          <p>Xin chào,</p>
          <p>${description}</p>
          <div style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #ff6b35; margin: 24px 0; text-align: center;">
            ${otp}
          </div>
          <p>Mã này có hiệu lực trong 10 phút. Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email và kiểm tra bảo mật tài khoản.</p>
          <p style="color: #888; font-size: 12px; margin-top: 30px;">Trân trọng,<br/>Đội ngũ phát triển Dishcision</p>
        </div>
      `,
    });
    console.log(`[MAIL] Successfully sent ${purpose} email to ${email}`);
    return true;
  } catch (error) {
    console.error(`[MAIL] Failed to send ${purpose} email to ${email}:`, error);
    return false;
  }
}

async function issueAuthSession(user: any, req: Request, res: Response, message: string): Promise<void> {
  const accessToken = generateAccessToken({ userId: user.id, role: user.role });
  const refreshToken = generateRefreshToken({ userId: user.id });
  await Promise.all([
    prisma.user.update({ where: { id: user.id }, data: { refreshToken } }),
    recordSession(user.id, req, accessToken),
  ]);

  res.status(200).json({
    success: true,
    message,
    token: accessToken,
    accessToken,
    data: {
      accessToken,
      refreshToken,
      expiresIn: getExpiresIn(),
      user: formatUser(user),
    },
  });
}

async function issueLoginOr2FAChallenge(
  user: any,
  req: Request,
  res: Response,
  successMessage: string
): Promise<void> {
  const isTwoFactorEnabled = (await redis.get(`user:${user.id}:2fa_enabled`)) === 'true';
  if (!isTwoFactorEnabled) {
    await issueAuthSession(user, req, res, successMessage);
    return;
  }

  const otp = crypto.randomInt(100000, 1000000).toString();
  const tempToken = crypto.randomBytes(32).toString('hex');
  const payload = {
    userId: user.id,
    email: user.email,
    otp,
    attempts: 0,
    lastSentAt: Date.now(),
  };

  await redis.set(`2fa_login:${tempToken}`, JSON.stringify(payload), 'EX', 600);
  const sent = await sendOtpEmail(user.email, otp, 'login_2fa');
  if (!sent) {
    await redis.del(`2fa_login:${tempToken}`);
    errorResponse(res, 'Không thể gửi email mã xác thực lúc này. Vui lòng thử lại sau.', 503);
    return;
  }

  const emailMasked = maskEmail(user.email);
  res.status(200).json({
    success: true,
    message: 'Mã xác thực đã được gửi tới email của bạn',
    data: {
      requiresTwoFactor: true,
      tempToken,
      email: emailMasked,
      emailMasked,
    },
  });
}

// ── Register ──────────────────────────────

export async function register(req: Request, res: Response): Promise<void> {
  const { username, email, password, fullName } = req.body;
  const normalizedEmail = String(email).trim().toLowerCase();

  let finalUsername = username && typeof username === 'string' && username.trim().length >= 3 ? username.trim() : '';

  if (!finalUsername) {
    finalUsername = await generateUniqueUsername(normalizedEmail.split('@')[0] || 'user');
  }

  const existing = await prisma.user.findFirst({
    where: {
      OR: [
        { email: { equals: normalizedEmail, mode: 'insensitive' } },
        { username: finalUsername },
      ],
    },
  });
  if (existing) {
    if (existing.email.toLowerCase() === normalizedEmail) {
      errorResponse(res, 'Email already exists', 409);
    } else {
      errorResponse(res, 'Username already exists', 409);
    }
    return;
  }

  const passwordHash = await hashPassword(password);
  const user = await prisma.user.create({
    data: { username: finalUsername, email: normalizedEmail, passwordHash, fullName: fullName.trim(), avatarUrl: DEFAULT_AVATAR_URL },
    select: { ...USER_PUBLIC_SELECT, email: true, passwordHash: true },
  });

  const accessToken = generateAccessToken({ userId: user.id, role: user.role });
  const refreshToken = generateRefreshToken({ userId: user.id });
  await Promise.all([
    prisma.user.update({ where: { id: user.id }, data: { refreshToken } }),
    recordSession(user.id, req, accessToken),
  ]);

  res.status(201).json({
    success: true,
    message: 'Registration successful',
    token: accessToken,
    accessToken,
    data: {
      accessToken,
      refreshToken,
      expiresIn: getExpiresIn(),
      user: formatUser(user),
    },
  });
}

// ── Login ─────────────────────────────────

export async function login(req: Request, res: Response): Promise<void> {
  const { credential, username, email, password } = req.body;
  const loginIdentifier = String(credential || username || email || '').trim();

  const user = await prisma.user.findFirst({
    where: {
      OR: [
        { email: { equals: loginIdentifier, mode: 'insensitive' } },
        { username: loginIdentifier },
      ],
    },
    include: { _count: { select: { followers: true, following: true } } },
  });

  if (!user || !user.passwordHash || !(await comparePassword(password, user.passwordHash))) {
    errorResponse(res, 'Invalid credentials', 401);
    return;
  }

  if (user.accountStatus !== 'Active') {
    errorResponse(res, `Account is ${user.accountStatus.toLowerCase()}`, 403);
    return;
  }

  await issueLoginOr2FAChallenge(user, req, res, 'Login successful');
}

export async function verifyLogin2FA(req: Request, res: Response): Promise<void> {
  const { tempToken, otp } = req.body;
  const redisKey = `2fa_login:${String(tempToken).trim()}`;
  const raw = await redis.get(redisKey);

  if (!raw) {
    errorResponse(res, 'Phiên xác thực đã hết hạn hoặc không hợp lệ. Vui lòng đăng nhập lại.', 400);
    return;
  }

  let stored: {
    userId: number;
    email: string;
    otp: string;
    attempts: number;
    lastSentAt: number;
  };
  try {
    stored = JSON.parse(raw);
  } catch {
    await redis.del(redisKey);
    errorResponse(res, 'Phiên xác thực không hợp lệ. Vui lòng đăng nhập lại.', 400);
    return;
  }

  const cleanOtp = String(otp || '').trim();
  if (!stored.otp || stored.otp !== cleanOtp) {
    const nextAttempts = (stored.attempts || 0) + 1;
    if (nextAttempts >= 5) {
      await redis.del(redisKey);
      errorResponse(res, 'Bạn đã nhập sai mã quá nhiều lần. Vui lòng đăng nhập lại.', 429);
      return;
    }
    const ttl = await redis.ttl(redisKey);
    await redis.set(
      redisKey,
      JSON.stringify({ ...stored, attempts: nextAttempts }),
      'EX',
      ttl > 0 ? ttl : 300
    );
    errorResponse(res, 'Mã xác thực không chính xác', 400);
    return;
  }

  await redis.del(redisKey);

  const user = await prisma.user.findUnique({
    where: { id: stored.userId },
    include: { _count: { select: { followers: true, following: true } } },
  });

  if (!user) {
    errorResponse(res, 'Tài khoản không tồn tại', 404);
    return;
  }
  if (user.accountStatus !== 'Active') {
    errorResponse(res, `Account is ${user.accountStatus.toLowerCase()}`, 403);
    return;
  }

  await issueAuthSession(user, req, res, 'Login successful');
}

export async function resendLogin2FA(req: Request, res: Response): Promise<void> {
  const { tempToken } = req.body;
  const redisKey = `2fa_login:${String(tempToken).trim()}`;
  const raw = await redis.get(redisKey);

  if (!raw) {
    errorResponse(res, 'Phiên xác thực đã hết hạn. Vui lòng đăng nhập lại.', 400);
    return;
  }

  let stored: {
    userId: number;
    email: string;
    otp: string;
    attempts: number;
    lastSentAt: number;
  };
  try {
    stored = JSON.parse(raw);
  } catch {
    await redis.del(redisKey);
    errorResponse(res, 'Phiên xác thực không hợp lệ. Vui lòng đăng nhập lại.', 400);
    return;
  }

  if (stored.lastSentAt && Date.now() - stored.lastSentAt < 20000) {
    errorResponse(res, 'Vui lòng đợi vài giây trước khi gửi lại mã', 429);
    return;
  }

  const newOtp = crypto.randomInt(100000, 1000000).toString();
  const updated = {
    ...stored,
    otp: newOtp,
    attempts: 0,
    lastSentAt: Date.now(),
  };

  await redis.set(redisKey, JSON.stringify(updated), 'EX', 600);
  const sent = await sendOtpEmail(stored.email, newOtp, 'login_2fa');
  if (!sent) {
    errorResponse(res, 'Không thể gửi email mã xác thực lúc này. Vui lòng thử lại sau.', 503);
    return;
  }

  successResponse(res, { email: maskEmail(stored.email) }, 'Đã gửi lại mã xác thực tới email của bạn');
}

// ── Google Auth (Sign in / Auto Sign up) ───

export async function googleAuth(req: Request, res: Response): Promise<void> {
  const { email, fullName, avatarUrl } = req.body;
  const cleanGoogleAvatar = typeof avatarUrl === 'string' ? avatarUrl.trim() : '';
  const normalizedEmail = String(email || '').trim().toLowerCase();
  if (!normalizedEmail) {
    errorResponse(res, 'Email is required', 400);
    return;
  }

  let user = await prisma.user.findFirst({
    where: { email: { equals: normalizedEmail, mode: 'insensitive' } },
    include: { _count: { select: { followers: true, following: true } } },
  });

  if (!user) {
    const emailLocal = normalizedEmail.split('@')[0] || 'user';
    const username = await generateUniqueUsername(emailLocal);

    user = await prisma.user.create({
      data: {
        email: normalizedEmail,
        username,
        fullName: (fullName && String(fullName).trim()) || emailLocal,
        avatarUrl: cleanGoogleAvatar || DEFAULT_AVATAR_URL,
        passwordHash: '',
        role: 'User',
        accountStatus: 'Active',
      },
      include: { _count: { select: { followers: true, following: true } } },
    });
  } else if ((!user.avatarUrl || user.avatarUrl === DEFAULT_AVATAR_URL) && cleanGoogleAvatar) {
    user = await prisma.user.update({
      where: { id: user.id },
      data: { avatarUrl: cleanGoogleAvatar },
      include: { _count: { select: { followers: true, following: true } } },
    });
  }

  if (user.accountStatus !== 'Active') {
    errorResponse(res, `Account is ${user.accountStatus.toLowerCase()}`, 403);
    return;
  }

  await issueAuthSession(user, req, res, 'Google authentication successful');
}

// ── Logout ────────────────────────────────

export async function logout(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  await prisma.user.update({ where: { id: userId }, data: { refreshToken: null } });
  const token = req.headers.authorization?.slice(7).trim();
  if (token) {
    await Promise.all([
      redis.set(`blacklist:${token}`, '1', 'EX', getExpiresIn()),
      prisma.userSession.deleteMany({ where: { userId, tokenHash: hashToken(token) } }),
    ]);
  }
  successResponse(res, null, 'Logout successful');
}

// ── Forgot Password ───────────────────────

export async function forgotPassword(req: Request, res: Response): Promise<void> {
  const normalizedEmail = String(req.body.email).trim().toLowerCase();
  const user = await prisma.user.findFirst({
    where: { email: { equals: normalizedEmail, mode: 'insensitive' } },
  });

  if (user) {
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    const tokenHash = crypto.randomBytes(32).toString('hex');
    const expiresAt = new Date(Date.now() + 60 * 60 * 1000);

    await prisma.passwordResetToken.create({
      data: { userId: user.id, tokenHash, expiresAt },
    });
    await redis.set(`otp:${normalizedEmail}`, otp, 'EX', 600);
    await redis.set(`otp_token:${normalizedEmail}`, tokenHash, 'EX', 600);

    if (process.env.NODE_ENV !== 'production') {
      console.log(`[DEV/OTP] Password reset OTP for ${normalizedEmail}: ${otp}`);
    }
    sendOtpEmail(normalizedEmail, otp).catch(console.error);
  }

  successResponse(res, { email: normalizedEmail, otpSent: true }, 'If that email exists, an OTP code has been sent');
}

// ── Verify OTP ────────────────────────────

export async function verifyOtp(req: Request, res: Response): Promise<void> {
  const { email, otp } = req.body;
  const normalizedEmail = String(email || '').trim().toLowerCase();
  const cleanOtp = String(otp || '').trim();
  if (!normalizedEmail || !cleanOtp) {
    errorResponse(res, 'Email và mã OTP là bắt buộc', 400);
    return;
  }

  const storedOtp = await redis.get(`otp:${normalizedEmail}`);
  if (!storedOtp || storedOtp !== cleanOtp) {
    errorResponse(res, 'Mã OTP không chính xác hoặc đã hết hạn', 400);
    return;
  }

  const user = await prisma.user.findFirst({
    where: { email: { equals: normalizedEmail, mode: 'insensitive' } },
  });
  if (!user) {
    errorResponse(res, 'Mã OTP không chính xác hoặc đã hết hạn', 400);
    return;
  }

  let tokenHash = await redis.get(`otp_token:${normalizedEmail}`);
  if (!tokenHash) {
    tokenHash = crypto.randomBytes(32).toString('hex');
    await prisma.passwordResetToken.create({
      data: {
        userId: user.id,
        tokenHash,
        expiresAt: new Date(Date.now() + 60 * 60 * 1000),
      },
    });
  }

  await redis.del(`otp:${normalizedEmail}`);

  successResponse(res, {
    verified: true,
    email: normalizedEmail,
    tokenHash,
  }, 'Xác thực mã OTP thành công');
}

// ── Reset Password ────────────────────────

export async function resetPassword(req: Request, res: Response): Promise<void> {
  const { tokenHash, newPassword } = req.body;

  const token = await prisma.passwordResetToken.findFirst({
    where: { tokenHash, used: false, expiresAt: { gt: new Date() } },
  });

  if (!token) {
    errorResponse(res, 'Invalid or expired token', 400);
    return;
  }

  const passwordHash = await hashPassword(newPassword);
  await prisma.$transaction([
    prisma.user.update({ where: { id: token.userId }, data: { passwordHash, refreshToken: null } }),
    prisma.passwordResetToken.updateMany({ where: { userId: token.userId, used: false }, data: { used: true } }),
  ]);

  successResponse(res, null, 'Password reset successful');
}

// ── Change Password ───────────────────────

export async function changePassword(req: AuthRequest, res: Response): Promise<void> {
  const { currentPassword, newPassword } = req.body;
  const userId = req.user!.userId;

  const user = await prisma.user.findUnique({ where: { id: userId } });
  if (!user) {
    errorResponse(res, 'User not found', 404);
    return;
  }
  if (!user.passwordHash || user.passwordHash === '') {
    errorResponse(res, 'Tài khoản đăng nhập bằng Google không sử dụng mật khẩu', 400);
    return;
  }
  if (!(await comparePassword(currentPassword, user.passwordHash))) {
    errorResponse(res, 'Current password is incorrect', 401);
    return;
  }

  const passwordHash = await hashPassword(newPassword);
  await prisma.user.update({ where: { id: userId }, data: { passwordHash } });

  successResponse(res, null, 'Password changed successfully');
}

// ── 2FA & Security Preferences ───────────

export async function get2FAStatus(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const statusStr = await redis.get(`user:${userId}:2fa_enabled`);
  const isTwoFactorEnabled = statusStr === 'true';

  res.status(200).json({
    success: true,
    isTwoFactorEnabled,
    data: {
      isTwoFactorEnabled,
    },
    message: isTwoFactorEnabled ? 'Xác thực 2 yếu tố đang bật' : 'Xác thực 2 yếu tố chưa bật',
  });
}

export async function get2FASetup(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const user = await prisma.user.findUnique({ where: { id: userId }, select: { email: true, passwordHash: true } });
  if (!user || !user.email) {
    errorResponse(res, 'Không tìm thấy email tài khoản', 404);
    return;
  }
  if (!user.passwordHash || user.passwordHash === '') {
    errorResponse(res, 'Tài khoản đăng nhập bằng Google được bảo vệ trực tiếp qua Google', 400);
    return;
  }

  const setupKey = `user:${userId}:2fa_setup_otp`;
  const existingRaw = await redis.get(setupKey);
  if (existingRaw) {
    try {
      const existing = JSON.parse(existingRaw);
      if (existing.lastSentAt && Date.now() - existing.lastSentAt < 15000) {
        res.status(200).json({
          success: true,
          data: {
            email: maskEmail(user.email),
            emailMasked: maskEmail(user.email),
            otpSent: true,
          },
          message: 'Mã xác thực đã được gửi tới email của bạn',
        });
        return;
      }
    } catch {
      // Proceed to regenerate
    }
  }

  const otp = crypto.randomInt(100000, 1000000).toString();
  await redis.set(
    setupKey,
    JSON.stringify({ otp, attempts: 0, lastSentAt: Date.now() }),
    'EX',
    600
  );

  const sent = await sendOtpEmail(user.email, otp, 'enable_2fa');
  if (!sent) {
    await redis.del(setupKey);
    errorResponse(res, 'Không thể gửi email xác thực lúc này. Vui lòng thử lại.', 503);
    return;
  }

  const emailMasked = maskEmail(user.email);
  res.status(200).json({
    success: true,
    data: {
      email: emailMasked,
      emailMasked,
      otpSent: true,
    },
    message: 'Mã xác thực 6 chữ số đã được gửi tới email của bạn',
  });
}

export async function verify2FA(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const code = String(req.body.code || '').trim();
  const setupKey = `user:${userId}:2fa_setup_otp`;
  const raw = await redis.get(setupKey);

  if (!raw) {
    errorResponse(res, 'Mã xác thực đã hết hạn. Vui lòng bấm gửi lại mã.', 400);
    return;
  }

  let stored: { otp: string; attempts: number; lastSentAt: number };
  try {
    stored = JSON.parse(raw);
  } catch {
    await redis.del(setupKey);
    errorResponse(res, 'Mã xác thực không hợp lệ. Vui lòng bấm gửi lại mã.', 400);
    return;
  }

  if (!stored.otp || stored.otp !== code) {
    const nextAttempts = (stored.attempts || 0) + 1;
    if (nextAttempts >= 5) {
      await redis.del(setupKey);
      errorResponse(res, 'Bạn đã nhập sai mã quá nhiều lần. Vui lòng gửi lại mã mới.', 429);
      return;
    }
    const ttl = await redis.ttl(setupKey);
    await redis.set(
      setupKey,
      JSON.stringify({ ...stored, attempts: nextAttempts }),
      'EX',
      ttl > 0 ? ttl : 300
    );
    errorResponse(res, 'Mã xác thực không chính xác.', 400);
    return;
  }

  await Promise.all([
    redis.set(`user:${userId}:2fa_enabled`, 'true'),
    redis.del(setupKey),
    redis.del(`user:${userId}:2fa_secret`),
  ]);

  res.status(200).json({
    success: true,
    isTwoFactorEnabled: true,
    data: {
      isTwoFactorEnabled: true,
    },
    message: 'Đã bật xác thực 2 yếu tố qua email thành công!',
  });
}

export async function disable2FA(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  await Promise.all([
    redis.set(`user:${userId}:2fa_enabled`, 'false'),
    redis.del(`user:${userId}:2fa_setup_otp`),
    redis.del(`user:${userId}:2fa_secret`),
  ]);

  res.status(200).json({
    success: true,
    isTwoFactorEnabled: false,
    data: {
      isTwoFactorEnabled: false,
    },
    message: 'Đã tắt xác thực 2 yếu tố',
  });
}

// ── Active Sessions ───────────────────────

function formatSessions(rows: any[], currentHash: string | null) {
  return rows.map((s) => {
    const isCurrent = Boolean(currentHash && s.tokenHash === currentHash);
    const formattedDate = s.lastActive.toLocaleDateString('vi-VN');
    return {
      id: s.id,
      deviceName: s.deviceName,
      subtitle: isCurrent
        ? 'Phiên đang hoạt động'
        : `${s.location ? `${s.location} · ` : ''}${formattedDate}`,
      isCurrent,
      statusText: isCurrent ? 'Thiết bị này' : '',
      location: s.location || '',
      lastActive: s.lastActive,
    };
  });
}

export async function getSessions(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const currentToken = req.headers.authorization?.slice(7).trim() || '';
  const currentHash = currentToken ? hashToken(currentToken) : null;

  let rows = await prisma.userSession.findMany({
    where: { userId },
    orderBy: { lastActive: 'desc' },
  });

  if (currentHash && !rows.some((s) => s.tokenHash === currentHash)) {
    await recordSession(userId, req, currentToken);
    rows = await prisma.userSession.findMany({
      where: { userId },
      orderBy: { lastActive: 'desc' },
    });
  } else if (currentHash) {
    await prisma.userSession.updateMany({
      where: { userId, tokenHash: currentHash },
      data: { lastActive: new Date() },
    });
  }

  res.status(200).json({
    success: true,
    data: formatSessions(rows, currentHash),
    message: 'Lấy danh sách phiên thành công',
  });
}

export async function revokeSession(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { sessionId, sessionIds } = req.body;
  const currentToken = req.headers.authorization?.slice(7).trim() || '';
  const currentHash = currentToken ? hashToken(currentToken) : null;

  const toRevoke = new Set<string>();
  if (sessionId) toRevoke.add(String(sessionId));
  if (Array.isArray(sessionIds)) {
    sessionIds.forEach((id: string) => toRevoke.add(String(id)));
  }

  const rows = await prisma.userSession.findMany({ where: { userId } });
  const idsToDelete = rows
    .filter((s) => !(currentHash && s.tokenHash === currentHash))
    .filter((s) => toRevoke.size === 0 || toRevoke.has(s.id))
    .map((s) => s.id);

  if (idsToDelete.length > 0) {
    await prisma.userSession.deleteMany({
      where: { userId, id: { in: idsToDelete } },
    });
  }

  const remaining = await prisma.userSession.findMany({
    where: { userId },
    orderBy: { lastActive: 'desc' },
  });

  res.status(200).json({
    success: true,
    data: formatSessions(remaining, currentHash),
    message: 'Đã đăng xuất phiên đăng nhập thành công',
  });
}
