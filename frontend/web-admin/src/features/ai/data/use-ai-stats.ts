import { useQuery } from '@tanstack/react-query'

export interface AILogItem {
  id: number
  user?: {
    id: number
    username: string
    fullName?: string
    avatarUrl?: string | null
  } | null
  craving: string
  suggestions: {
    rank?: number
    dishName: string
    reason: string
    estimatedPrice?: string
  }[]
  modelUsed: string
  promptTokens: number
  completionTokens: number
  totalTokens: number
  latencyMs: number
  isFormatValid: boolean
  feedbackRating?: number | null
  createdAt: string
}

export interface RatingDistributionItem {
  star: number
  label: string
  count: number
  percentage: number
}

export interface MonthlyAIChartItem {
  name: string
  requests: number
  promptTokens: number
  completionTokens: number
  totalTokens: number
  latency: number
  avgRating?: number
  ratingCount?: number
}

export interface AIStatsData {
  totalRequests: number
  geminiRequests: number
  fallbackRequests: number
  tokens: {
    totalPromptTokens: number
    totalCompletionTokens: number
    totalTokens: number
    avgPromptTokens: number
    avgCompletionTokens: number
  }
  performance: {
    avgLatency: number
    formatAccuracy: number
    successRate: number
    errorRate: number
  }
  rating: {
    avgRating: number
    feedbackCount: number
    distribution?: RatingDistributionItem[]
  }
  monthlyChart: MonthlyAIChartItem[]
  logs: AILogItem[]
}

const emptyAIStats: AIStatsData = {
  totalRequests: 0,
  geminiRequests: 0,
  fallbackRequests: 0,
  tokens: {
    totalPromptTokens: 0,
    totalCompletionTokens: 0,
    totalTokens: 0,
    avgPromptTokens: 0,
    avgCompletionTokens: 0,
  },
  performance: {
    avgLatency: 0,
    formatAccuracy: 0,
    successRate: 0,
    errorRate: 0,
  },
  rating: {
    avgRating: 0,
    feedbackCount: 0,
    distribution: [],
  },
  monthlyChart: [],
  logs: [],
}
  tokens: {
    totalPromptTokens: 0,
    totalCompletionTokens: 0,
    totalTokens: 0,
    avgPromptTokens: 0,
    avgCompletionTokens: 0,
  },
  performance: {
    avgLatency: 0,
    formatAccuracy: 0,
    successRate: 0,
    errorRate: 0,
  },
  rating: {
    avgRating: 0,
    feedbackCount: 0,
  },
  monthlyChart: [],
  logs: [],
}

export function useAIStats() {
  return useQuery<AIStatsData>({
    queryKey: ['admin-ai-stats'],
    queryFn: async () => {
      const res = await fetch('/api/v1/admin/ai-stats')
      if (!res.ok) {
        throw new Error('Failed to fetch AI stats from backend')
      }
      const json = await res.json()
      if (json.success && json.data) {
        return json.data as AIStatsData
      }
      return emptyAIStats
    },
    refetchInterval: 15000,
    staleTime: 5000,
  })
}
