import { z } from 'zod';
import { Request, Response, NextFunction } from 'express';
import { errorResponse } from '../utils/response';

export function validate(schema: z.ZodTypeAny) {
  return (req: Request, res: Response, next: NextFunction): void => {
    const result = schema.safeParse(req.body);
    if (!result.success) {
      const message = result.error.errors.map((e) => e.message).join(', ');
      errorResponse(res, message, 400);
      return;
    }
    req.body = result.data;
    next();
  };
}

// ── Shared schemas ─────────────────────────

export const paginationSchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  limit: z.coerce.number().int().min(1).max(100).default(10),
});

export const registerSchema = z.object({
  username: z.string().min(3).max(50),
  email: z.string().email(),
  password: z.string().min(8),
  fullName: z.string().min(1).max(100),
});

export const loginSchema = z.object({
  credential: z.string().min(1),
  password: z.string().min(1),
});

export const forgotPasswordSchema = z.object({
  email: z.string().email(),
});

export const resetPasswordSchema = z.object({
  tokenHash: z.string().min(1),
  newPassword: z.string().min(8),
});

export const changePasswordSchema = z.object({
  currentPassword: z.string().min(1),
  newPassword: z.string().min(8),
});

export const updateProfileSchema = z.object({
  fullName: z.string().min(1).max(100).optional(),
  bio: z.string().max(255).optional(),
  gender: z.enum(['Male', 'Female', 'Other']).optional(),
  dateOfBirth: z.string().optional(),
  avatarUrl: z.string().url().optional(),
});

export const createPostSchema = z.object({
  content: z.string().min(1).max(2000),
  images: z
    .array(
      z.object({
        imageUrl: z.string().url(),
        displayOrder: z.number().int().optional(),
      })
    )
    .optional(),
  locationUrl: z.string().url().optional(),
  voucherCode: z.string().max(50).optional(),
  hashtags: z.array(z.string()).optional(),
});

export const createCommentSchema = z.object({
  content: z.string().min(1).max(1000),
  parentCommentId: z.number().int().nullable().optional(),
});

export const createReviewSchema = z.object({
  restaurantName: z.string().min(1).max(255),
  locationUrl: z.string().url().optional(),
  rating: z.number().int().min(1).max(5),
  content: z.string().min(1),
  images: z
    .array(
      z.object({
        imageUrl: z.string().url(),
        displayOrder: z.number().int().optional(),
      })
    )
    .optional(),
  tags: z.array(z.string()).optional(),
});

export const aiFoodDecideSchema = z.object({
  mood: z.string().max(255).optional(),
  budget: z.string().max(50).optional(),
  companions: z.number().int().min(0).optional(),
  craving: z.string().max(255).optional(),
  preferenceIds: z.array(z.number().int()).optional(),
});

export const sendMessageSchema = z.object({
  content: z.string().max(5000).optional(),
  mediaUrl: z.string().url().nullable().optional(),
});

export const reportSchema = z.object({
  postId: z.number().int().nullable().optional(),
  reportedUserId: z.number().int().nullable().optional(),
  reason: z.enum(['Spam', 'Nudity', 'Violence', 'FakeNews', 'Harassment', 'Other']),
  description: z.string().max(500).optional(),
});

export const updateUserStatusSchema = z.object({
  status: z.enum(['Active', 'Banned', 'Suspended']),
  reason: z.string().min(1),
});

export const warnUserSchema = z.object({
  message: z.string().min(1),
});

export const processReportSchema = z.object({
  status: z.enum(['Resolved', 'Dismissed']),
  action: z.enum(['DeletePost', 'WarnUser', 'BanUser', 'NoAction']),
  note: z.string().optional(),
});

export const systemNotificationSchema = z.object({
  title: z.string().min(1),
  message: z.string().min(1),
  targetGroup: z.enum(['All', 'User', 'RestaurantAccount']).default('All'),
});
