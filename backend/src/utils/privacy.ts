import { Prisma } from '@prisma/client';
import { prisma } from '../config/prisma';

export const DEFAULT_PRIVACY = {
  newPosts: 'public',
  profileInfo: 'public',
  followingList: 'public',
  findEmail: 'no_one',
  friendRequests: 'friends_of_friends',
  messages: 'public',
  comments: 'public',
} as const;

export async function getUserPrivacy(userId: number) {
  const row = await prisma.userPrivacy.findUnique({ where: { userId } });
  return { ...DEFAULT_PRIVACY, ...(row || {}) };
}

export async function areUsersBlocked(userA?: number, userB?: number): Promise<boolean> {
  if (!userA || !userB || userA === userB) return false;
  const row = await prisma.blockedAccount.findFirst({
    where: {
      OR: [
        { userId: userA, blockedUserId: userB },
        { userId: userB, blockedUserId: userA },
      ],
    },
    select: { id: true },
  });
  return Boolean(row);
}

export async function isUserRestricted(ownerId: number, viewerId?: number): Promise<boolean> {
  if (!ownerId || !viewerId || ownerId === viewerId) return false;
  const row = await prisma.restrictedAccount.findUnique({
    where: { userId_restrictedUserId: { userId: ownerId, restrictedUserId: viewerId } },
    select: { id: true },
  });
  return Boolean(row);
}

export async function getBlockedUserIds(viewerId?: number): Promise<number[]> {
  if (!viewerId) return [];
  const rows = await prisma.blockedAccount.findMany({
    where: {
      OR: [{ userId: viewerId }, { blockedUserId: viewerId }],
    },
    select: { userId: true, blockedUserId: true },
  });
  const ids = new Set<number>();
  for (const r of rows) {
    ids.add(r.userId === viewerId ? r.blockedUserId : r.userId);
  }
  return Array.from(ids);
}

/**
 * Checks whether `ownerId` considers `viewerId` a friend (owner follows viewer).
 * One-way follows initiated by the viewer do NOT grant friend access to the owner's private content.
 */
export async function isFriend(ownerId: number, viewerId?: number): Promise<boolean> {
  if (!viewerId) return false;
  if (ownerId === viewerId) return true;
  const follow = await prisma.follow.findUnique({
    where: {
      followerId_followingId: {
        followerId: ownerId,
        followingId: viewerId,
      },
    },
    select: { followerId: true },
  });
  return Boolean(follow);
}

export async function canAccessPrivacy(
  setting: string | null | undefined,
  ownerId: number,
  viewerId?: number
): Promise<boolean> {
  if (viewerId && viewerId === ownerId) return true;
  const effective = setting || 'public';
  if (effective === 'public') return true;
  if (effective === 'only_me' || effective === 'no_one') return false;
  if (effective === 'friends') return isFriend(ownerId, viewerId);
  return false;
}

export function buildPostAuthorWhere(
  viewerId?: number,
  blockedIds: number[] = []
): Prisma.UserWhereInput {
  return {
    accountStatus: 'Active',
    ...(blockedIds.length > 0 ? { id: { notIn: blockedIds } } : {}),
    OR: [
      ...(viewerId ? [{ id: viewerId }] : []),
      { privacySetting: { is: null } },
      { privacySetting: { is: { newPosts: 'public' } } },
      ...(viewerId
        ? [
            {
              privacySetting: { is: { newPosts: 'friends' } },
              following: { some: { followingId: viewerId } },
            },
          ]
        : []),
    ],
  };
}

