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

export const strongPasswordSchema = z
  .string()
  .min(8, 'Mật khẩu phải từ 8 ký tự trở lên')
  .max(100, 'Mật khẩu không được vượt quá 100 ký tự')
  .refine((val) => !/\s/.test(val), 'Mật khẩu không được chứa khoảng trắng')
  .refine((val) => /[a-z]/.test(val), 'Mật khẩu phải có ít nhất 1 chữ thường')
  .refine((val) => /[A-Z]/.test(val), 'Mật khẩu phải có ít nhất 1 chữ hoa')
  .refine((val) => /\d/.test(val), 'Mật khẩu phải có ít nhất 1 chữ số')
  .refine((val) => /[^A-Za-z0-9\s]/.test(val), 'Mật khẩu phải có ít nhất 1 ký tự đặc biệt');

export const registerSchema = z.object({
  username: z.string().min(3).max(50).optional().or(z.literal('')),
  email: z.string().email(),
  password: strongPasswordSchema,
  fullName: z.string().min(1).max(100),
});

export const loginSchema = z
  .object({
    credential: z.string().optional(),
    username: z.string().optional(),
    email: z.string().optional(),
    password: z.string().min(1),
  })
  .refine((data) => Boolean(data.credential || data.username || data.email), {
    message: 'credential, username, or email is required',
  });

const urlOrUploadPath = z.string().refine(
  (val) => val.startsWith('/uploads/') || /^https?:\/\/.+/.test(val),
  'Invalid URL'
);

export const googleAuthSchema = z.object({
  email: z.string().email(),
  fullName: z.string().max(100).optional(),
  avatarUrl: z.string().optional().nullable(),
});

export const forgotPasswordSchema = z.object({
  email: z.string().email(),
});

export const verifyOtpSchema = z.object({
  email: z.string().email(),
  otp: z.string().regex(/^\d{6}$/, 'Mã OTP phải gồm 6 chữ số'),
});

export const verify2FASchema = z.object({
  code: z.string().trim().regex(/^\d{6}$/, 'Mã xác thực phải gồm 6 chữ số'),
});

export const verifyLogin2FASchema = z.object({
  tempToken: z.string().min(10, 'Phiên xác thực không hợp lệ'),
  otp: z.string().trim().regex(/^\d{6}$/, 'Mã xác thực phải gồm 6 chữ số'),
});

export const resendLogin2FASchema = z.object({
  tempToken: z.string().min(10, 'Phiên xác thực không hợp lệ'),
});

export const resetPasswordSchema = z.object({
  tokenHash: z.string().min(1),
  newPassword: strongPasswordSchema,
});

export const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1),
    newPassword: strongPasswordSchema,
  })
  .refine((data) => data.newPassword !== data.currentPassword, {
    message: 'Mật khẩu mới không được trùng với mật khẩu hiện tại',
    path: ['newPassword'],
  });

export const updateProfileSchema = z.object({
  username: z.string().min(3).max(50).regex(/^[a-zA-Z0-9_.]+$/, 'Tên người dùng không hợp lệ').optional(),
  fullName: z.string().min(1).max(100).optional(),
  bio: z.string().max(255).optional(),
  city: z.string().max(100, 'Thành phố không được vượt quá 100 ký tự').nullable().optional(),
  gender: z.enum(['Male', 'Female', 'Other']).optional(),
  dateOfBirth: z.string().optional(),
  avatarUrl: urlOrUploadPath.nullable().optional().or(z.literal('')),
  preferences: z.array(z.string()).optional(),
});

export const updatePrivacySchema = z
  .object({
    newPosts: z.enum(['public', 'friends', 'only_me']).optional(),
    profileInfo: z.enum(['public', 'friends', 'only_me']).optional(),
    followingList: z.enum(['public', 'friends', 'only_me']).optional(),
    findEmail: z.enum(['public', 'friends_of_friends', 'friends', 'no_one']).optional(),
    friendRequests: z.enum(['public', 'friends_of_friends']).optional(),
    messages: z.enum(['public', 'friends', 'no_one']).optional(),
    comments: z.enum(['public', 'friends', 'no_one']).optional(),
  })
  .strict()
  .refine((data) => Object.values(data).some((v) => v !== undefined), {
    message: 'Phải cung cấp ít nhất một trường cài đặt quyền riêng tư',
  });

export const createPostSchema = z.object({
  content: z.string().min(1).max(2000),
  images: z
    .array(
      z.object({
        imageUrl: urlOrUploadPath,
        displayOrder: z.number().int().optional(),
      })
    )
    .optional(),
  locationUrl: z.string().url().nullable().optional().or(z.literal('')),
  voucherCode: z.string().max(50).nullable().optional(),
  hashtags: z.array(z.string()).optional(),
});

export const updatePostSchema = z.object({
  content: z.string().min(1).max(2000).optional(),
  images: z
    .array(
      z.object({
        imageUrl: urlOrUploadPath,
        displayOrder: z.number().int().optional(),
      })
    )
    .optional(),
  locationUrl: z.string().url().nullable().optional().or(z.literal('')),
  voucherCode: z.string().max(50).nullable().optional(),
  hashtags: z.array(z.string()).optional(),
  status: z.enum(['Active', 'Hidden']).optional(),
});

export const createCommentSchema = z.object({
  content: z.string().min(1).max(1000),
  parentCommentId: z.number().int().nullable().optional(),
});

export const createReviewSchema = z.object({
  restaurantName: z.string().min(1).max(255),
  locationUrl: z.string().url().nullable().optional().or(z.literal('')),
  rating: z.number().int().min(1).max(5),
  content: z.string().min(1),
  images: z
    .array(
      z.object({
        imageUrl: urlOrUploadPath,
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
  mediaUrl: urlOrUploadPath.nullable().optional().or(z.literal('')),
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
