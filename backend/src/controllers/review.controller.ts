import { Request, Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, paginatedResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';
import { formatUser } from './user.controller';

function formatReview(review: any) {
  const { id, author, ...rest } = review;
  return {
    reviewId: id,
    author: author ? formatUser(author) : undefined,
    ...rest,
  };
}

const AUTHOR_SELECT = {
  id: true, username: true, fullName: true, avatarUrl: true,
  bio: true, role: true, _count: { select: { followers: true, following: true } },
};

// ── GET /reviews ──────────────────────────

export async function getReviews(req: Request, res: Response): Promise<void> {
  const { restaurantName, rating, page = '1', limit = '10' } = req.query;
  const p = parseInt(page as string);
  const l = parseInt(limit as string);
  const skip = (p - 1) * l;

  const where: any = {};
  if (restaurantName) where.restaurantName = { contains: restaurantName as string, mode: 'insensitive' };
  if (rating) where.rating = parseInt(rating as string);

  const [reviews, total] = await Promise.all([
    prisma.review.findMany({
      where,
      include: { author: { select: AUTHOR_SELECT }, images: true },
      orderBy: { createdAt: 'desc' },
      skip, take: l,
    }),
    prisma.review.count({ where }),
  ]);

  paginatedResponse(res, reviews.map(formatReview), { page: p, limit: l, total });
}

// ── POST /reviews ─────────────────────────

export async function createReview(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { restaurantName, locationUrl, rating, content, images, tags } = req.body;

  const review = await prisma.review.create({
    data: {
      authorId: userId,
      restaurantName,
      locationUrl,
      rating,
      content,
      tags: tags || [],
      images: images
        ? { create: images.map((img: any) => ({ imageUrl: img.imageUrl, displayOrder: img.displayOrder ?? 0 })) }
        : undefined,
    },
    include: { author: { select: AUTHOR_SELECT }, images: true },
  });

  successResponse(res, formatReview(review), 'Review posted', 201);
}

// ── DELETE /reviews/:reviewId ─────────────

export async function deleteReview(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const reviewId = parseInt(req.params.reviewId);

  const review = await prisma.review.findUnique({ where: { id: reviewId } });
  if (!review) { errorResponse(res, 'Review not found', 404); return; }
  if (review.authorId !== userId && req.user!.role !== 'Admin') {
    errorResponse(res, 'Not authorized', 403); return;
  }

  await prisma.review.delete({ where: { id: reviewId } });
  successResponse(res, null, 'Review deleted');
}
