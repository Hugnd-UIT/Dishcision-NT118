import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';
import { formatUser } from './user.controller';

function formatComment(comment: any): any {
  const { id, author, _count, ...rest } = comment;
  return {
    commentId: id,
    author: author ? formatUser(author) : undefined,
    ...rest,
    likesCount: _count?.likes ?? 0,
    replies: comment.replies ? comment.replies.map(formatComment) : [],
  };
}

// ── GET /posts/:postId/comments ───────────

export async function getComments(req: Request, res: Response): Promise<void> {
  const postId = parseInt(req.params.postId);
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 10;
  const skip = (page - 1) * limit;

  const AUTHOR_SELECT = {
    id: true, username: true, fullName: true, avatarUrl: true,
    bio: true, role: true, _count: { select: { followers: true, following: true } },
  };

  const [comments, total] = await Promise.all([
    prisma.comment.findMany({
      where: { postId, parentCommentId: null },
      include: {
        author: { select: AUTHOR_SELECT },
        _count: { select: { likes: true } },
        replies: {
          include: {
            author: { select: AUTHOR_SELECT },
            _count: { select: { likes: true } },
          },
        },
      },
      orderBy: { createdAt: 'asc' },
      skip,
      take: limit,
    }),
    prisma.comment.count({ where: { postId, parentCommentId: null } }),
  ]);

  paginatedResponse(res, comments.map(formatComment), { page, limit, total });
}

// ── POST /posts/:postId/comments ──────────

export async function createComment(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parseInt(req.params.postId);
  const { content, parentCommentId } = req.body;

  const AUTHOR_SELECT = {
    id: true, username: true, fullName: true, avatarUrl: true,
    bio: true, role: true, _count: { select: { followers: true, following: true } },
  };

  const comment = await prisma.comment.create({
    data: { postId, authorId: userId, content, parentCommentId: parentCommentId ?? null },
    include: {
      author: { select: AUTHOR_SELECT },
      _count: { select: { likes: true } },
    },
  });

  // Notify post author
  const post = await prisma.post.findUnique({ where: { id: postId } });
  if (post && post.authorId !== userId) {
    const commenter = await prisma.user.findUnique({ where: { id: userId }, select: { username: true } });
    await prisma.notification.create({
      data: {
        userId: post.authorId,
        type: 'Comment',
        message: `${commenter?.username} commented on your post`,
        relatedPostId: postId,
      },
    });
  }

  successResponse(res, formatComment(comment), 'Comment added', 201);
}

// ── DELETE /comments/:commentId ───────────

export async function deleteComment(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const commentId = parseInt(req.params.commentId);

  const comment = await prisma.comment.findUnique({ where: { id: commentId } });
  if (!comment) { errorResponse(res, 'Comment not found', 404); return; }
  if (comment.authorId !== userId && req.user!.role !== 'Admin') {
    errorResponse(res, 'Not authorized', 403); return;
  }

  await prisma.comment.delete({ where: { id: commentId } });
  successResponse(res, null, 'Comment deleted');
}
