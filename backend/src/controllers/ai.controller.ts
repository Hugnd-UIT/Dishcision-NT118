import { Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, errorResponse } from '../utils/response';
import { parsePositiveInt } from '../utils/http';
import { AuthRequest } from '../middlewares/auth.middleware';

interface GeminiCallResult {
  suggestions: any[] | null;
  usageMetadata: {
    promptTokenCount: number;
    candidatesTokenCount: number;
    totalTokenCount: number;
  };
  latencyMs: number;
  isFormatValid: boolean;
}

async function callGeminiAI(prompt: string): Promise<GeminiCallResult> {
  const startTime = Date.now();
  const apiKey = process.env.AI_KEY;
  if (!apiKey) {
    return {
      suggestions: null,
      usageMetadata: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
      latencyMs: 0,
      isFormatValid: false,
    };
  }

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

    const latencyMs = Date.now() - startTime;

    if (!response.ok) {
      console.error(`[AI] Gemini API error: ${response.status} ${response.statusText}`);
      return {
        suggestions: null,
        usageMetadata: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
        latencyMs,
        isFormatValid: false,
      };
    }

    const data: any = await response.json();
    const rawText = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!rawText) {
      return {
        suggestions: null,
        usageMetadata: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
        latencyMs,
        isFormatValid: false,
      };
    }

    const promptTokens = data?.usageMetadata?.promptTokenCount ?? Math.round(prompt.length / 3.5);
    const completionTokens = data?.usageMetadata?.candidatesTokenCount ?? Math.round(rawText.length / 3.5);
    const totalTokens = data?.usageMetadata?.totalTokenCount ?? (promptTokens + completionTokens);

    const usageMetadata = {
      promptTokenCount: promptTokens,
      candidatesTokenCount: completionTokens,
      totalTokenCount: totalTokens,
    };

    const cleaned = rawText.replace(/```json/g, '').replace(/```/g, '').trim();
    let parsed: any = null;
    let isFormatValid = false;
    try {
      parsed = JSON.parse(cleaned);
      if (Array.isArray(parsed) && parsed.length > 0 && parsed[0]?.dishName) {
        isFormatValid = true;
      }
    } catch {
      isFormatValid = false;
    }

    return {
      suggestions: isFormatValid ? parsed : null,
      usageMetadata,
      latencyMs,
      isFormatValid,
    };
  } catch (error) {
    console.error('[AI] Call to Gemini failed:', error);
    return {
      suggestions: null,
      usageMetadata: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
      latencyMs: Date.now() - startTime,
      isFormatValid: false,
    };
  }
}

// ── POST /ai/food-decide ──────────────────

export async function foodDecide(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId;
  const { mood, budget, companions, craving } = req.body;

  let modelUsed = 'gemini-2.5-flash';
  let suggestions: any[] | null = null;
  let metrics = {
    promptTokens: 0,
    completionTokens: 0,
    totalTokens: 0,
    latencyMs: 0,
    isFormatValid: false,
  };

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
  metrics = {
    promptTokens: aiResult.usageMetadata.promptTokenCount,
    completionTokens: aiResult.usageMetadata.candidatesTokenCount,
    totalTokens: aiResult.usageMetadata.totalTokenCount,
    latencyMs: aiResult.latencyMs,
    isFormatValid: aiResult.isFormatValid,
  };

  if (aiResult.suggestions && aiResult.suggestions.length > 0) {
    suggestions = aiResult.suggestions.slice(0, 3).map((item, idx) => ({
      rank: idx + 1,
      dishName: item.dishName || '',
      reason: item.reason || '',
      estimatedPrice: item.estimatedPrice || '',
    }));
  } else {
    modelUsed = 'database-matcher';
    const maxBudget = budget ? parseInt(String(budget).replace(/[^\d]/g, ''), 10) : 0;
    let dbDishes = await prisma.dish.findMany({
      include: { storeUser: { select: { fullName: true } } },
    });
    if (dbDishes.length === 0) {
      errorResponse(res, 'Dịch vụ gợi ý món ăn tạm thời chưa có dữ liệu', 503);
      return;
    }
    if (craving) {
      const q = String(craving).toLowerCase();
      const matched = dbDishes.filter(
        (d) => d.name.toLowerCase().includes(q) || d.category.toLowerCase().includes(q) || d.description.toLowerCase().includes(q),
      );
      if (matched.length > 0) dbDishes = matched;
    }
    if (maxBudget > 0) {
      const affordable = dbDishes.filter((d) => d.price <= maxBudget);
      if (affordable.length > 0) dbDishes = affordable;
    }
    const shuffled = [...dbDishes].sort(() => Math.random() - 0.5).slice(0, 3);
    suggestions = shuffled.map((d, i) => ({
      rank: i + 1,
      dishName: d.name,
      reason: `${d.description} (${d.storeUser.fullName})`,
      estimatedPrice: d.formattedPrice,
    }));
  }

  let requestId: number | null = null;
  if (userId) {
    const aiRequest = await prisma.aIRequest.create({
      data: {
        userId,
        mood: mood || 'Tự động gợi ý',
        budget: String(budget || 'Bình dân'),
        companions: companions ? Number(companions) : 1,
        craving: craving || null,
        resultJson: {
          suggestions,
          metrics,
        },
        modelUsed,
      },
    });
    requestId = aiRequest.id;
  }

  successResponse(res, {
    requestId,
    suggestions,
    modelUsed,
  });
}

// ── POST /ai/requests/:requestId/feedback ─

export async function submitFeedback(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const requestId = parsePositiveInt(req.params.requestId);
  if (!requestId) {
    errorResponse(res, 'Invalid AI request ID', 400);
    return;
  }
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
