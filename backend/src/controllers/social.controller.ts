import { Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';
import { formatUser, formatUserPrivate } from './user.controller';

const AUTHOR_SELECT = {
  id: true, username: true, fullName: true, avatarUrl: true,
  bio: true, role: true, _count: { select: { followers: true, following: true } },
};

function formatPost(post: any) {
  const { id, _count, author, ...rest } = post;
  return {
    postId: id,
    author: author ? formatUser(author) : undefined,
    ...rest,
    likesCount: _count?.likes ?? 0,
    commentsCount: _count?.comments ?? 0,
    sharesCount: _count?.shares ?? 0,
  };
}

function formatMessage(msg: any) {
  const { id, sender, ...rest } = msg;
  return {
    messageId: id,
    sender: sender ? formatUser(sender) : undefined,
    ...rest,
  };
}

// ── POST /users/:userId/follow ────────────

export async function followUser(req: AuthRequest, res: Response): Promise<void> {
  const followerId = req.user!.userId;
  const followingId = parseInt(req.params.userId);

  if (followerId === followingId) { errorResponse(res, 'Cannot follow yourself', 400); return; }

  try {
    await prisma.follow.create({ data: { followerId, followingId } });

    const follower = await prisma.user.findUnique({ where: { id: followerId }, select: { username: true } });
    await prisma.notification.create({
      data: { userId: followingId, type: 'Follow', message: `${follower?.username} started following you` },
    });

    successResponse(res, null, 'Follow successful');
  } catch {
    errorResponse(res, 'Already following', 409);
  }
}

// ── DELETE /users/:userId/follow ──────────

export async function unfollowUser(req: AuthRequest, res: Response): Promise<void> {
  const followerId = req.user!.userId;
  const followingId = parseInt(req.params.userId);

  await prisma.follow.deleteMany({ where: { followerId, followingId } });
  successResponse(res, null, 'Unfollow successful');
}

// ── POST /posts/:postId/save ──────────────

export async function savePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parseInt(req.params.postId);

  try {
    await prisma.savedPost.create({ data: { userId, postId } });
    successResponse(res, null, 'Saved successfully');
  } catch {
    errorResponse(res, 'Already saved', 409);
  }
}

// ── DELETE /posts/:postId/save ────────────

export async function unsavePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parseInt(req.params.postId);

  await prisma.savedPost.deleteMany({ where: { userId, postId } });
  successResponse(res, null, 'Removed from bookmarks');
}

// ── GET /users/me/saved-posts ─────────────

export async function getSavedPosts(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 10;
  const skip = (page - 1) * limit;

  const [saved, total] = await Promise.all([
    prisma.savedPost.findMany({
      where: { userId },
      include: {
        post: {
          include: {
            author: { select: AUTHOR_SELECT },
            images: true,
            _count: { select: { likes: true, comments: true, shares: true } },
          },
        },
      },
      orderBy: { createdAt: 'desc' },
      skip, take: limit,
    }),
    prisma.savedPost.count({ where: { userId } }),
  ]);

  paginatedResponse(res, saved.map((s) => formatPost(s.post)), { page, limit, total });
}

// ── GET /chats ────────────────────────────

export async function getChats(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;

  // Get unique conversations
  const messages = await prisma.message.findMany({
    where: { OR: [{ senderId: userId }, { recipientId: userId }] },
    orderBy: { createdAt: 'desc' },
    include: {
      sender: { select: AUTHOR_SELECT },
      recipient: { select: AUTHOR_SELECT },
    },
  });

  // Deduplicate by conversation partner
  const seen = new Set<number>();
  const conversations: any[] = [];

  for (const msg of messages) {
    const partnerId = msg.senderId === userId ? msg.recipientId : msg.senderId;
    if (seen.has(partnerId)) continue;
    seen.add(partnerId);

    const partner = msg.senderId === userId ? msg.recipient : msg.sender;
    const unreadCount = await prisma.message.count({
      where: { senderId: partnerId, recipientId: userId, isRead: false },
    });

    conversations.push({
      recipient: formatUser(partner),
      lastMessage: formatMessage(msg),
      unreadCount,
    });
  }

  successResponse(res, conversations);
}

// ── GET /chats/:recipientId/messages ──────

export async function getMessages(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const recipientId = parseInt(req.params.recipientId);
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 20;
  const skip = (page - 1) * limit;

  const [messages, total] = await Promise.all([
    prisma.message.findMany({
      where: { OR: [{ senderId: userId, recipientId }, { senderId: recipientId, recipientId: userId }] },
      include: { sender: { select: AUTHOR_SELECT } },
      orderBy: { createdAt: 'desc' },
      skip, take: limit,
    }),
    prisma.message.count({
      where: { OR: [{ senderId: userId, recipientId }, { senderId: recipientId, recipientId: userId }] },
    }),
  ]);

  // Mark incoming as read
  await prisma.message.updateMany({
    where: { senderId: recipientId, recipientId: userId, isRead: false },
    data: { isRead: true },
  });

  paginatedResponse(res, messages.map(formatMessage), { page, limit, total });
}

// ── POST /chats/:recipientId/messages ─────

export async function sendMessage(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const recipientId = parseInt(req.params.recipientId);
  const { content, mediaUrl } = req.body;

  if (!content && !mediaUrl) { errorResponse(res, 'Message content or media is required', 400); return; }

  const message = await prisma.message.create({
    data: { senderId: userId, recipientId, content, mediaUrl },
    include: { sender: { select: AUTHOR_SELECT } },
  });

  successResponse(res, formatMessage(message), 'Message sent', 201);
}
