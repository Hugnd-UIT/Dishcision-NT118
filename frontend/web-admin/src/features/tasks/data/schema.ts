import { z } from 'zod'

export const postSchema = z.object({
  id: z.string(),
  postId: z.number().optional(),
  title: z.string(),
  content: z.string(),
  author: z
    .object({
      id: z.number().optional(),
      fullName: z.string(),
      username: z.string(),
      avatarUrl: z.string().nullable().optional(),
    })
    .optional(),
  images: z.array(z.string()).default([]),
  status: z.string(),
  label: z.string().default('review'),
  priority: z.string().default('latest'),
  likesCount: z.number().default(0),
  commentsCount: z.number().default(0),
  sharesCount: z.number().default(0),
  isPinned: z.boolean().default(false),
  createdAt: z.string().or(z.date()),
  updatedAt: z.string().or(z.date()).optional(),
})

export type Post = z.infer<typeof postSchema>
export type Task = Post
export const taskSchema = postSchema
