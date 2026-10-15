import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { redis } from '../config/redis';
import { hashPassword, comparePassword, generateAccessToken, generateRefreshToken, getExpiresIn } from '../utils/auth';
import { successResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';
import crypto from 'crypto';

// ── Helpers ───────────────────────────────

const USER_PUBLIC_SELECT = {
  id: true, username: true, fullName: true, avatarUrl: true, bio: true, role: true,
  _count: { select: { followers: true, following: true } },
};

function formatUser(user: any) {
  const { _count, ...rest } = user;
  return {
    userId: rest.id,
    username: rest.username,
    fullName: rest.fullName,
    avatarUrl: rest.avatarUrl,
    bio: rest.bio,
    role: rest.role,
    followersCount: _count?.followers ?? 0,
    followingCount: _count?.following ?? 0,
  };
}

function formatUserPrivate(user: any) {
  return {
    ...formatUser(user),
    email: user.email,
    gender: user.gender,
    dateOfBirth: user.dateOfBirth,
    accountStatus: user.accountStatus,
    createdAt: user.createdAt,
  };
}

// ── Register ──────────────────────────────

export async function register(req: Request, res: Response): Promise<void> {
  const { username, email, password, fullName } = req.body;

  let finalUsername = username && typeof username === 'string' && username.trim().length >= 3 ? username.trim() : '';

  if (!finalUsername) {
    let base = email.split('@')[0].toLowerCase().replace(/[^a-z0-9_]/g, '');
    if (base.length < 3) {
      base = `user_${base}`;
    }
    base = base.slice(0, 30);
    finalUsername = base;
    let candidate = finalUsername;
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
    finalUsername = candidate;
  }

  const existing = await prisma.user.findFirst({
    where: { OR: [{ email }, { username: finalUsername }] },
  });
  if (existing) {
    if (existing.email.toLowerCase() === email.toLowerCase()) {
      errorResponse(res, 'Email already exists', 409);
    } else {
      errorResponse(res, 'Username already exists', 409);
    }
    return;
  }

  const passwordHash = await hashPassword(password);
  const user = await prisma.user.create({
    data: { username: finalUsername, email, passwordHash, fullName },
    select: { ...USER_PUBLIC_SELECT, _count: { select: { followers: true, following: true } } },
  });

  const accessToken = generateAccessToken({ userId: user.id, role: user.role });
  const refreshToken = generateRefreshToken({ userId: user.id });
  await prisma.user.update({ where: { id: user.id }, data: { refreshToken } });

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
  const loginIdentifier = credential || username || email;

  const user = await prisma.user.findFirst({
    where: { OR: [{ email: loginIdentifier }, { username: loginIdentifier }] },
    include: { _count: { select: { followers: true, following: true } } },
  });

  if (!user || !(await comparePassword(password, user.passwordHash))) {
    errorResponse(res, 'Invalid credentials', 401);
    return;
  }

  if (user.accountStatus !== 'Active') {
    errorResponse(res, `Account is ${user.accountStatus.toLowerCase()}`, 403);
    return;
  }

  const accessToken = generateAccessToken({ userId: user.id, role: user.role });
  const refreshToken = generateRefreshToken({ userId: user.id });
  await prisma.user.update({ where: { id: user.id }, data: { refreshToken } });

  res.status(200).json({
    success: true,
    message: 'Login successful',
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

// ── Logout ────────────────────────────────

export async function logout(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  await prisma.user.update({ where: { id: userId }, data: { refreshToken: null } });
  // Blacklist token in Redis (simple approach: store until expiry)
  const token = req.headers.authorization?.split(' ')[1];
  if (token) {
    await redis.set(`blacklist:${token}`, '1', 'EX', getExpiresIn());
  }
  successResponse(res, null, 'Logout successful');
}

// ── Forgot Password ───────────────────────

export async function forgotPassword(req: Request, res: Response): Promise<void> {
  const { email } = req.body;
  const user = await prisma.user.findUnique({ where: { email } });

  // Always respond 200 to prevent email enumeration
  successResponse(res, null, 'If that email exists, a reset link has been sent');

  if (!user) return;

  const tokenHash = crypto.randomBytes(32).toString('hex');
  const expiresAt = new Date(Date.now() + 60 * 60 * 1000); // 1 hour

  await prisma.passwordResetToken.create({
    data: { userId: user.id, tokenHash, expiresAt },
  });

  // TODO: Send email with tokenHash (integrate with email service)
  console.log(`[DEV] Password reset token for ${email}: ${tokenHash}`);
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
  await prisma.user.update({ where: { id: token.userId }, data: { passwordHash } });
  await prisma.passwordResetToken.update({ where: { id: token.id }, data: { used: true } });

  successResponse(res, null, 'Password reset successful');
}

// ── Change Password ───────────────────────

export async function changePassword(req: AuthRequest, res: Response): Promise<void> {
  const { currentPassword, newPassword } = req.body;
  const userId = req.user!.userId;

  const user = await prisma.user.findUnique({ where: { id: userId } });
  if (!user || !(await comparePassword(currentPassword, user.passwordHash))) {
    errorResponse(res, 'Current password is incorrect', 401);
    return;
  }

  const passwordHash = await hashPassword(newPassword);
  await prisma.user.update({ where: { id: userId }, data: { passwordHash } });

  successResponse(res, null, 'Password changed successfully');
}
