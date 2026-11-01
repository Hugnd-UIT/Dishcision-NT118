import { Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';

// Default suggestions based on Vietnamese food culture (fallback)
const DEFAULT_SUGGESTIONS = [
  { dishName: 'Bún bò Huế', reason: 'Vị cay nồng ấm bụng, đậm đà kích thích vị giác giải tỏa mệt mỏi', estimatedPrice: '55,000 - 80,000 VND' },
  { dishName: 'Phở bò tái lăn', reason: 'Nước dùng ninh xương béo ngậy, thịt bò xào lăn thơm phức tỏi hành', estimatedPrice: '50,000 - 75,000 VND' },
  { dishName: 'Bánh mì pate giòn rụm', reason: 'Nhanh gọn, tiện lợi và tiết kiệm ngân sách', estimatedPrice: '25,000 - 45,000 VND' },
  { dishName: 'Cơm tấm sườn bì chả', reason: 'Chắc bụng, thơm ngậy vị sườn nướng mật ong và mỡ hành', estimatedPrice: '45,000 - 65,000 VND' },
  { dishName: 'Lẩu nấm thanh đạm', reason: 'Thanh mát, giàu dinh dưỡng, rất thích hợp tụ họp bạn bè', estimatedPrice: '120,000 - 180,000 VND' },
];

async function callGeminiAI(prompt: string): Promise<any[] | null> {
  const apiKey = process.env.AI_KEY;
  if (!apiKey) return null;

  try {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`;
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt }] }],
        generationConfig: {
          temperature: 0.7,
          responseMimeType: 'application/json',
        },
      }),
    });

    if (!response.ok) {
      console.error(`[AI] Gemini API error: ${response.status} ${response.statusText}`);
      return null;
    }

    const data: any = await response.json();
    const rawText = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!rawText) return null;

    const cleaned = rawText.replace(/```json/g, '').replace(/```/g, '').trim();
    const parsed = JSON.parse(cleaned);
    if (Array.isArray(parsed) && parsed.length > 0) {
      return parsed;
    }
    return null;
  } catch (error) {
    console.error('[AI] Call to Gemini failed:', error);
    return null;
  }
}

// ── POST /ai/food-decide ──────────────────

export async function foodDecide(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId || 1;
  const { mood, budget, companions, craving, preferenceIds } = req.body;

  let modelUsed = 'gemini-2.5-flash';
  let suggestions: any[] | null = null;

  const prompt = `Bạn là chuyên gia tư vấn ẩm thực Việt Nam cho ứng dụng Dishcision.
Dựa trên các tiêu chí sau, hãy gợi ý 3 món ăn ngon nhất, chuẩn vị Việt Nam:
- Tâm trạng / Cảm xúc: ${mood || 'Muốn ăn gì đó ngon miệng'}
- Ngân sách: ${budget || 'Bình dân'}
- Số người cùng ăn: ${companions || 1}
- Nhu cầu / Thèm món gì: ${craving || 'Không có'}

YÊU CẦU: Trả về DUY NHẤT một mảng JSON gồm 3 object (không thêm bất kỳ văn bản nào khác ngoài JSON):
[
  {
    "dishName": "Tên món ăn (tiếng Việt)",
    "reason": "Lý do gợi ý ngắn gọn, thu hút, đúng với tâm trạng",
    "estimatedPrice": "Khoảng giá dự kiến (ví dụ: 45.000 - 65.000 VND)"
  }
]`;

  const aiResult = await callGeminiAI(prompt);
  if (aiResult && aiResult.length > 0) {
    suggestions = aiResult.slice(0, 3).map((item, idx) => ({
      rank: idx + 1,
      dishName: item.dishName || 'Món ăn gợi ý',
      reason: item.reason || 'Món ngon thích hợp cho hôm nay',
      estimatedPrice: item.estimatedPrice || '40,000 - 60,000 VND',
    }));
  } else {
    // Fallback to random default suggestions
    modelUsed = 'local-rule-based';
    const shuffled = [...DEFAULT_SUGGESTIONS].sort(() => Math.random() - 0.5);
    suggestions = shuffled.slice(0, 3).map((s, i) => ({ rank: i + 1, ...s }));
  }

  const aiRequest = await prisma.aIRequest.create({
    data: {
      userId,
      mood: mood || 'Tự động gợi ý',
      budget: String(budget || 'Bình dân'),
      companions: companions ? Number(companions) : 1,
      craving: craving || null,
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
  const userId = req.user?.userId || 1;
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
