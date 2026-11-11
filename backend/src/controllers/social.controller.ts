import { Response } from 'express';
import { prisma } from '../config/prisma';
import { delCache } from '../config/redis';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { parsePagination, parsePositiveInt } from '../utils/http';
import { AuthRequest } from '../middlewares/auth.middleware';
import { formatUser, attachPostFlags, resolveAvatarUrl } from './user.controller';

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
  const followingId = parsePositiveInt(req.params.userId);
  if (!followingId) {
    errorResponse(res, 'Invalid user ID', 400);
    return;
  }

  if (followerId === followingId) {
    errorResponse(res, 'Cannot follow yourself', 400);
    return;
  }

  const targetUser = await prisma.user.findUnique({
    where: { id: followingId, accountStatus: 'Active' },
    select: { id: true },
  });
  if (!targetUser) {
    errorResponse(res, 'User not found', 404);
    return;
  }

  try {
    await prisma.follow.create({ data: { followerId, followingId } });

    const follower = await prisma.user.findUnique({ where: { id: followerId }, select: { username: true } });
    await prisma.notification.create({
      data: { userId: followingId, type: 'Follow', message: `${follower?.username} started following you` },
    });
    await Promise.all([delCache(`user:${followingId}`), delCache(`user:${followerId}`)]);

    successResponse(res, null, 'Follow successful');
  } catch {
    errorResponse(res, 'Already following', 409);
  }
}

// ── DELETE /users/:userId/follow ──────────

export async function unfollowUser(req: AuthRequest, res: Response): Promise<void> {
  const followerId = req.user!.userId;
  const followingId = parsePositiveInt(req.params.userId);
  if (!followingId) {
    errorResponse(res, 'Invalid user ID', 400);
    return;
  }

  await prisma.follow.deleteMany({ where: { followerId, followingId } });
  await Promise.all([delCache(`user:${followingId}`), delCache(`user:${followerId}`)]);
  successResponse(res, null, 'Unfollow successful');
}

// ── GET /users/:userId/followers ──────────

export async function getFollowers(req: AuthRequest, res: Response): Promise<void> {
  const targetId = req.params.userId === 'me' ? req.user!.userId : parsePositiveInt(req.params.userId);
  if (!targetId) {
    errorResponse(res, 'Invalid user ID', 400);
    return;
  }
  const currentUserId = req.user!.userId;

  const follows = await prisma.follow.findMany({
    where: { followingId: targetId },
    include: {
      follower: {
        select: {
          id: true,
          username: true,
          fullName: true,
          avatarUrl: true,
          bio: true,
          role: true,
        },
      },
    },
    orderBy: { createdAt: 'desc' },
  });

  const myFollows = await prisma.follow.findMany({
    where: { followerId: currentUserId },
    select: { followingId: true },
  });
  const myFollowingSet = new Set(myFollows.map((f) => f.followingId));

  const data = follows.map((f) => ({
    userId: f.follower.id,
    username: f.follower.username,
    fullName: f.follower.fullName,
    avatarUrl: resolveAvatarUrl(f.follower.avatarUrl),
    bio: f.follower.bio,
    isFollowing: myFollowingSet.has(f.follower.id),
  }));

  successResponse(res, data, 'Followers retrieved successfully');
}

// ── GET /users/:userId/following ──────────

export async function getFollowing(req: AuthRequest, res: Response): Promise<void> {
  const targetId = req.params.userId === 'me' ? req.user!.userId : parsePositiveInt(req.params.userId);
  if (!targetId) {
    errorResponse(res, 'Invalid user ID', 400);
    return;
  }
  const currentUserId = req.user!.userId;

  const follows = await prisma.follow.findMany({
    where: { followerId: targetId },
    include: {
      following: {
        select: {
          id: true,
          username: true,
          fullName: true,
          avatarUrl: true,
          bio: true,
          role: true,
        },
      },
    },
    orderBy: { createdAt: 'desc' },
  });

  const myFollows = await prisma.follow.findMany({
    where: { followerId: currentUserId },
    select: { followingId: true },
  });
  const myFollowingSet = new Set(myFollows.map((f) => f.followingId));

  const data = follows.map((f) => ({
    userId: f.following.id,
    username: f.following.username,
    fullName: f.following.fullName,
    avatarUrl: resolveAvatarUrl(f.following.avatarUrl),
    bio: f.following.bio,
    isFollowing: myFollowingSet.has(f.following.id),
  }));

  successResponse(res, data, 'Following list retrieved successfully');
}

// ── POST /posts/:postId/save ──────────────

export async function savePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parsePositiveInt(req.params.postId);
  if (!postId) {
    errorResponse(res, 'Invalid post ID', 400);
    return;
  }

  const post = await prisma.post.findFirst({ where: { id: postId, status: 'Active' }, select: { id: true } });
  if (!post) {
    errorResponse(res, 'Post not found', 404);
    return;
  }
  const existing = await prisma.savedPost.findFirst({ where: { userId, postId } });
  if (!existing) await prisma.savedPost.create({ data: { userId, postId } });
  successResponse(res, { postId, isSaved: true }, 'Saved successfully');
}

// ── DELETE /posts/:postId/save ────────────

export async function unsavePost(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const postId = parsePositiveInt(req.params.postId);
  if (!postId) {
    errorResponse(res, 'Invalid post ID', 400);
    return;
  }

  await prisma.savedPost.deleteMany({ where: { userId, postId } });
  successResponse(res, null, 'Removed from bookmarks');
}

// ── GET /users/me/saved-posts ─────────────

export async function getSavedPosts(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { page, limit, skip } = parsePagination(req.query, 10, 100);

  const [saved, total] = await Promise.all([
    prisma.savedPost.findMany({
      where: { userId, post: { status: 'Active' } },
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
      skip,
      take: limit,
    }),
    prisma.savedPost.count({ where: { userId, post: { status: 'Active' } } }),
  ]);

  const formatted = await attachPostFlags(
    saved.map((s) => formatPost(s.post)),
    userId
  );
  paginatedResponse(res, formatted, { page, limit, total });
}

// ── GET /chats ────────────────────────────

export async function getChats(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;

  const messages = await prisma.message.findMany({
    where: { OR: [{ senderId: userId }, { recipientId: userId }] },
    orderBy: { createdAt: 'desc' },
    include: {
      sender: { select: AUTHOR_SELECT },
      recipient: { select: AUTHOR_SELECT },
    },
  });

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
  const recipientId = parsePositiveInt(req.params.recipientId);
  if (!recipientId) {
    errorResponse(res, 'Invalid recipient ID', 400);
    return;
  }
  const { page, limit, skip } = parsePagination(req.query, 20, 100);

  const [messages, total] = await Promise.all([
    prisma.message.findMany({
      where: { OR: [{ senderId: userId, recipientId }, { senderId: recipientId, recipientId: userId }] },
      include: { sender: { select: AUTHOR_SELECT } },
      orderBy: { createdAt: 'desc' },
      skip,
      take: limit,
    }),
    prisma.message.count({
      where: { OR: [{ senderId: userId, recipientId }, { senderId: recipientId, recipientId: userId }] },
    }),
  ]);

  await prisma.message.updateMany({
    where: { senderId: recipientId, recipientId: userId, isRead: false },
    data: { isRead: true },
  });

  paginatedResponse(res, messages.map(formatMessage), { page, limit, total });
}

// ── POST /chats/:recipientId/messages ─────

export async function sendMessage(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const recipientId = parsePositiveInt(req.params.recipientId);
  if (!recipientId) {
    errorResponse(res, 'Invalid recipient ID', 400);
    return;
  }
  const { content, mediaUrl } = req.body;

  if (!content && !mediaUrl) {
    errorResponse(res, 'Message content or media is required', 400);
    return;
  }

  const recipient = await prisma.user.findUnique({ where: { id: recipientId, accountStatus: 'Active' }, select: { id: true } });
  if (!recipient) {
    errorResponse(res, 'Recipient not found', 404);
    return;
  }

  const message = await prisma.message.create({
    data: { senderId: userId, recipientId, content, mediaUrl },
    include: { sender: { select: AUTHOR_SELECT } },
  });

  successResponse(res, formatMessage(message), 'Message sent', 201);
}
