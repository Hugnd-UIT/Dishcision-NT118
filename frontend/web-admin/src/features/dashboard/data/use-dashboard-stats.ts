import { useQuery } from '@tanstack/react-query'

export interface DashboardSummary {
  users: {
    total: number
    growth: string
    active: number
  }
  posts: {
    total: number
    growth: string
    thisMonth: number
  }
  aiRequests: {
    total: number
    thisMonth: number
    growth: string
  }
  places: {
    total: number
    stores: number
    dishes: number
  }
  reviews: number
  reports: number
}

export interface MonthlyChartItem {
  name: string
  posts: number
  ai: number
  total: number
}

export interface RecentPostItem {
  id: number
  content: string
  author: {
    id: number
    fullName: string
    username: string
    avatarUrl: string | null
  }
  likesCount: number
  commentsCount: number
  createdAt: string
}

export interface RecentUserItem {
  id: number
  fullName: string
  username: string
  email: string
  avatarUrl: string | null
  createdAt: string
  role: string
}

export interface CategoryItem {
  id: number
  name: string
  icon: string | null
}

export interface DashboardStatsData {
  summary: DashboardSummary
  monthlyChart: MonthlyChartItem[]
  recentPosts: RecentPostItem[]
  recentUsers: RecentUserItem[]
  categories: CategoryItem[]
}

const fallbackStats: DashboardStatsData = {
  summary: {
    users: { total: 34, growth: '+100%', active: 34 },
    posts: { total: 31, growth: '+100%', thisMonth: 31 },
    aiRequests: { total: 11, thisMonth: 11, growth: '+15.4%' },
    places: { total: 19, stores: 5, dishes: 14 },
    reviews: 16,
    reports: 3,
  },
  monthlyChart: [
    { name: 'Th01', posts: 0, ai: 0, total: 0 },
    { name: 'Th02', posts: 0, ai: 0, total: 0 },
    { name: 'Th03', posts: 0, ai: 0, total: 0 },
    { name: 'Th04', posts: 0, ai: 0, total: 0 },
    { name: 'Th05', posts: 0, ai: 0, total: 0 },
    { name: 'Th06', posts: 0, ai: 0, total: 0 },
    { name: 'Th07', posts: 0, ai: 0, total: 0 },
    { name: 'Th08', posts: 0, ai: 0, total: 0 },
    { name: 'Th09', posts: 0, ai: 0, total: 0 },
    { name: 'Th10', posts: 10, ai: 3, total: 13 },
    { name: 'Th11', posts: 21, ai: 8, total: 29 },
    { name: 'Th12', posts: 0, ai: 0, total: 0 },
  ],
  recentPosts: [],
  recentUsers: [],
  categories: [],
}

export function useDashboardStats() {
  return useQuery<DashboardStatsData>({
    queryKey: ['dashboard-stats'],
    queryFn: async () => {
      try {
        const res = await fetch('/api/v1/admin/dashboard-stats')
        if (!res.ok) {
          return fallbackStats
        }
        const json = await res.json()
        if (json.success && json.data) {
          return json.data as DashboardStatsData
        }
        return fallbackStats
      } catch {
        return fallbackStats
      }
    },
    refetchInterval: 30000,
    staleTime: 10000,
  })
}
