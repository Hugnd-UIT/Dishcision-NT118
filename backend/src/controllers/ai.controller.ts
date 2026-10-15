import { Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';

// Default suggestions based on Vietnamese food culture
const DEFAULT_SUGGESTIONS = [
  { dishName: 'Bún bò Huế', reason: 'Vị cay nồng ấm bụng, đậm đà kích thích vị giác giải tỏa mệt mỏi', estimatedPrice: '55,000 - 80,000 VND' },
  { dishName: 'Phở bò tái lăn', reason: 'Nước dùng ninh xương béo ngậy, thịt bò xào lăn thơm phức tỏi hành', estimatedPrice: '50,000 - 75,000 VND' },
  { dishName: 'Bánh mì pate giòn rụm', reason: 'Nhanh gọn, tiện lợi và tiết kiệm ngân sách', estimatedPrice: '25,000 - 45,000 VND' },
  { dishName: 'Cơm tấm sườn bì chả', reason: 'Chắc bụng, thơm ngậy vị sườn nướng mật ong và mỡ hành', estimatedPrice: '45,000 - 65,000 VND' },
  { dishName: 'Lẩu nấm thanh đạm', reason: 'Thanh mát, giàu dinh dưỡng, rất thích hợp tụ họp bạn bè', estimatedPrice: '120,000 - 180,000 VND' },
];

// ── POST /ai/food-decide ──────────────────

export async function foodDecide(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { mood, budget, companions, craving, preferenceIds } = req.body;

  // Retrieve active AI configuration from database
  const activeConfig = await prisma.aIConfig.findFirst({ where: { isActive: true } });
  const modelUsed = activeConfig?.modelName || 'gemini-2.0-flash';

  const shuffled = [...DEFAULT_SUGGESTIONS].sort(() => Math.random() - 0.5);
  const suggestions = shuffled.slice(0, 3).map((s, i) => ({ rank: i + 1, ...s }));

  const aiRequest = await prisma.aIRequest.create({
    data: {
      userId,
      mood,
      budget,
      companions,
      craving,
      resultJson: suggestions,
      modelUsed,
    },
  });

  successResponse(res, {
    requestId: aiRequest.id,
    suggestions,
    modelUsed,
  });
}

// ── POST /ai/requests/:requestId/feedback ─

export async function submitFeedback(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const requestId = parseInt(req.params.requestId);
  const { rating, comment } = req.body;

  const aiRequest = await prisma.aIRequest.findFirst({ where: { id: requestId, userId } });
  if (!aiRequest) {
    errorResponse(res, 'AI request not found', 404);
    return;
  }

  await prisma.aIRequest.update({
    where: { id: requestId },
    data: { feedbackRating: rating, feedbackComment: comment },
  });

  successResponse(res, null, 'Feedback recorded', 201);
}
