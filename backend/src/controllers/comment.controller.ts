import { Response } from 'express';
import { prisma } from '../config/prisma';
import { delCache, delCacheByPattern } from '../config/redis';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { parsePagination, parsePositiveInt } from '../utils/http';
import { AuthRequest } from '../middlewares/auth.middleware';
import { formatUser } from './user.controller';
import {
  areUsersBlocked,
  canAccessPrivacy,
  getBlockedUserIds,
  getUserPrivacy,
  isUserRestricted,
} from '../utils/privacy';

const AUTHOR_SELECT = {
  id: true, username: true, fullName: true, avatarUrl: true,
  bio: true, city: true, role: true, _count: { select: { followers: true, following: true } },
};

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

export async function getComments(req: AuthRequest, res: Response): Promise<void> {
  const postId = parsePositiveInt(req.params.postId);
  if (!postId) {
    errorResponse(res, 'Invalid post ID', 400);
    return;
  }
  const { page, limit, skip } = parsePagination(req.query, 10, 100);
  const viewerId = req.user?.userId;

  const post = await prisma.post.findFirst({
    where: { id: postId, status: 'Active', author: { accountStatus: 'Active' } },
    select: {
      id: true,
      authorId: true,
      author: { select: { privacySetting: { select: { newPosts: true } } } },
    },
  });
  if (!post) {
    errorResponse(res, 'Post not found', 404);
    return;
  }

  if (viewerId && viewerId !== post.authorId && (await areUsersBlocked(viewerId, post.authorId))) {
    errorResponse(res, 'Post not found', 404);
    return;
  }
  if (!(await canAccessPrivacy(post.author.privacySetting?.newPosts, post.authorId, viewerId))) {
    errorResponse(res, 'Bạn không có quyền xem bình luận của bài viết này', 403);
    return;
  }

  const blockedIds = await getBlockedUserIds(viewerId);
  const commentAuthorWhere = {
    accountStatus: 'Active' as const,
    ...(blockedIds.length > 0 ? { id: { notIn: blockedIds } } : {}),
  };

  const [comments, total] = await Promise.all([
    prisma.comment.findMany({
      where: { postId, parentCommentId: null, author: commentAuthorWhere },
      include: {
        author: { select: AUTHOR_SELECT },
        _count: { select: { likes: true } },
        replies: {
          where: { author: commentAuthorWhere },
          include: {
            author: { select: AUTHOR_SELECT },
            _count: { select: { likes: true } },
          },
          orderBy: { createdAt: 'asc' },
        },
      },
      orderBy: { createdAt: 'asc' },
      skip,
      take: limit,
    }),
    prisma.comment.count({ where: { postId, parentCommentId: null, author: commentAuthorWhere } }),
  ]);

  paginatedResponse(res, comments.map(formatComment), { page, limit, total });
}

// ── POST /posts/:postId/comments ──────────

export async function createComment(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parsePositiveInt(req.params.postId);
  if (!postId) {
    errorResponse(res, 'Invalid post ID', 400);
    return;
  }
  const { content, parentCommentId } = req.body;

  const post = await prisma.post.findFirst({
    where: { id: postId, status: 'Active', author: { accountStatus: 'Active' } },
  });
  if (!post) {
    errorResponse(res, 'Post not found', 404);
    return;
  }

  if (userId !== post.authorId) {
    if (await areUsersBlocked(userId, post.authorId)) {
      errorResponse(res, 'Bạn không thể bình luận trên bài viết này', 403);
      return;
    }
    const privacy = await getUserPrivacy(post.authorId);
    if (!(await canAccessPrivacy(privacy.newPosts, post.authorId, userId))) {
      errorResponse(res, 'Bạn không có quyền xem hoặc bình luận trên bài viết này', 403);
      return;
    }
    if (await isUserRestricted(post.authorId, userId)) {
      errorResponse(res, 'Bạn đã bị hạn chế bình luận trên bài viết của người dùng này', 403);
      return;
    }
    if (privacy.comments === 'no_one') {
      errorResponse(res, 'Tác giả đã tắt bình luận trên bài viết này', 403);
      return;
    }
    if (!(await canAccessPrivacy(privacy.comments, post.authorId, userId))) {
      errorResponse(res, 'Chỉ bạn bè của tác giả mới có thể bình luận', 403);
      return;
    }
  }

  if (parentCommentId) {
    const parent = await prisma.comment.findFirst({
      where: { id: parentCommentId, postId, author: { accountStatus: 'Active' } },
      select: { id: true },
    });
    if (!parent) {
      errorResponse(res, 'Parent comment not found on this post', 400);
      return;
    }
  }

  const comment = await prisma.comment.create({
    data: { postId, authorId: userId, content, parentCommentId: parentCommentId ?? null },
    include: {
      author: { select: AUTHOR_SELECT },
      _count: { select: { likes: true } },
    },
  });

  if (post.authorId !== userId) {
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

  await delCache(`post:${postId}`);
  successResponse(res, formatComment(comment), 'Comment added', 201);
}

// ── DELETE /comments/:commentId ───────────

export async function deleteComment(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const commentId = parsePositiveInt(req.params.commentId);
  if (!commentId) {
    errorResponse(res, 'Invalid comment ID', 400);
    return;
  }

  const comment = await prisma.comment.findUnique({ where: { id: commentId } });
  if (!comment) { errorResponse(res, 'Comment not found', 404); return; }
  if (comment.authorId !== userId && req.user!.role !== 'Admin') {
    errorResponse(res, 'Not authorized', 403); return;
  }

  const postComments = await prisma.comment.findMany({
    where: { postId: comment.postId },
    select: { id: true, parentCommentId: true },
  });
  const childrenMap = new Map<number, number[]>();
  for (const item of postComments) {
    if (item.parentCommentId !== null) {
      const children = childrenMap.get(item.parentCommentId) ?? [];
      children.push(item.id);
      childrenMap.set(item.parentCommentId, children);
    }
  }

  const idsToDelete: number[] = [];
  const queue: number[] = [commentId];
  const visited = new Set<number>();
  while (queue.length > 0) {
    const currentId = queue.shift()!;
    if (visited.has(currentId)) continue;
    visited.add(currentId);
    idsToDelete.push(currentId);
    const children = childrenMap.get(currentId);
    if (children) queue.push(...children);
  }

  await prisma.comment.deleteMany({
    where: { id: { in: idsToDelete } },
  });
  await Promise.all([
    delCache(`post:${comment.postId}`),
    delCacheByPattern('newsfeed:*'),
  ]);
  successResponse(res, { deletedIds: idsToDelete, deletedCount: idsToDelete.length }, 'Comment deleted');
}
