import { z } from 'zod'

export const postSchema = z.object({
  id: z.string(),
  postId: z.number().optional(),
  title: z.string(),
  content: z.string().optional().default(''),
  author: z
    .object({
      id: z.number().optional(),
      fullName: z.string(),
      username: z.string(),
      avatarUrl: z.string().nullable().optional(),
    })
    .optional(),
  images: z.array(z.string()).optional().default([]),
  status: z.string(),
  label: z.string().optional().default('review'),
  priority: z.string().optional().default('latest'),
  likesCount: z.number().optional().default(0),
  commentsCount: z.number().optional().default(0),
  sharesCount: z.number().optional().default(0),
  isPinned: z.boolean().optional().default(false),
  createdAt: z.string().or(z.date()).optional().default(() => new Date().toISOString()),
  updatedAt: z.string().or(z.date()).optional(),
})

export type Post = z.infer<typeof postSchema>
export type Task = Post
export const taskSchema = postSchema
