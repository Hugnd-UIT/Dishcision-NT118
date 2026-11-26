import { z } from 'zod'

const userStatusSchema = z.string()
export type UserStatus = string

const userRoleSchema = z.string()

export const userSchema = z.object({
  id: z.string(),
  firstName: z.string(),
  lastName: z.string(),
  username: z.string(),
  email: z.string(),
  phoneNumber: z.string().optional().default(''),
  status: userStatusSchema,
  role: userRoleSchema,
  createdAt: z.coerce.date().optional(),
  updatedAt: z.coerce.date().optional(),
  avatarUrl: z.string().nullable().optional(),
  postsCount: z.number().optional(),
  followersCount: z.number().optional(),
  city: z.string().nullable().optional(),
  isVerified: z.boolean().optional(),
  verificationStatus: z.string().optional(),
  verificationRejectionReason: z.string().nullable().optional(),
})

export type User = {
  id: string
  firstName: string
  lastName: string
  username: string
  email: string
  phoneNumber: string
  status: string
  role: string
  createdAt: Date | string
  updatedAt: Date | string
  avatarUrl?: string | null
  postsCount?: number
  followersCount?: number
  city?: string | null
  isVerified?: boolean
  verificationStatus?: string
  verificationRejectionReason?: string | null
}
