import { useQuery } from '@tanstack/react-query'

export interface AuditLogItem {
  id: number
  adminId?: number | null
  actorUsername?: string | null
  actorRole?: string | null
  actorAvatar?: string | null
  admin?: {
    id: number
    username: string
    fullName?: string
    avatarUrl?: string | null
    role?: string
  } | null
  action: string
  targetType: string
  targetId: number
  details?: string | null
  ipAddress?: string | null
  createdAt: string
}

export interface ActiveAccountItem {
  id: number
  username: string
  fullName?: string | null
  avatarUrl?: string | null
  role: string
  lastActive: string
  deviceName?: string | null
  location?: string | null
}

export interface AuditLogsResponse {
  logs: AuditLogItem[]
  total: number
  page: number
  limit: number
  stats?: {
    totalLogs: number
    todayActionCount: number
    activeAccountsCount: number
    activeAccounts: ActiveAccountItem[]
  }
}

export function useAuditLogs(page = 1, limit = 20) {
  return useQuery<AuditLogsResponse>({
    queryKey: ['admin-audit-logs', page, limit],
    queryFn: async () => {
      try {
        const res = await fetch(`/api/v1/admin/audit-logs?page=${page}&limit=${limit}`)
        if (!res.ok) {
          return { logs: [], total: 0, page: 1, limit }
        }
        const json = await res.json()
        if (json.success && Array.isArray(json.data)) {
          return {
            logs: json.data as AuditLogItem[],
            total: json.pagination?.total ?? json.meta?.total ?? json.data.length,
            page: json.pagination?.page ?? json.meta?.page ?? page,
            limit: json.pagination?.limit ?? json.meta?.limit ?? limit,
            stats: json.stats ?? undefined,
          }
        }
        return { logs: [], total: 0, page: 1, limit }
      } catch {
        return { logs: [], total: 0, page: 1, limit }
      }
    },
    refetchInterval: 3000,
    staleTime: 1000,
  })
}
