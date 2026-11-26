import { z } from 'zod'

export const taskSchema = z.object({
  id: z.string(),
  title: z.string(),
  status: z.string(),
  label: z.string().optional(),
  priority: z.string().optional(),
  content: z.string().optional(),
  author: z
    .object({
      id: z.number().optional(),
      fullName: z.string().optional(),
      username: z.string().optional(),
      avatarUrl: z.string().nullable().optional(),
    })
    .optional(),
  images: z.array(z.string()).optional(),
  likesCount: z.number().optional(),
  commentsCount: z.number().optional(),
  sharesCount: z.number().optional(),
  isPinned: z.boolean().optional(),
  createdAt: z.string().or(z.date()).optional(),
  updatedAt: z.string().or(z.date()).optional(),
  postId: z.number().optional(),
})

export type Task = {
  id: string
  title: string
  status: string
  label?: string
  priority?: string
  content?: string
  author?: {
    id?: number
    fullName?: string
    username?: string
    avatarUrl?: string | null
  }
  images?: string[]
  likesCount?: number
  commentsCount?: number
  sharesCount?: number
  isPinned?: boolean
  createdAt?: string | Date
  updatedAt?: string | Date
  postId?: number
}

export type Post = Task
export const postSchema = taskSchema
