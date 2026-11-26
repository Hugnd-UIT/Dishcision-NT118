import { useQuery } from '@tanstack/react-query'
import { User } from './schema'
import { users as fallbackUsers } from './users'

export function useUsers() {
  return useQuery<User[]>({
    queryKey: ['admin-users'],
    queryFn: async () => {
      try {
        const res = await fetch('/api/v1/admin/users?limit=100')
        if (!res.ok) return fallbackUsers
        const json = await res.json()
        if (json.success && Array.isArray(json.data) && json.data.length > 0) {
          return json.data.map((u: any) => {
            const rawFullName = (u.fullName || '').trim()
            const parts = rawFullName.split(' ')
            const firstName = parts.length > 1 ? parts.slice(0, -1).join(' ') : rawFullName || 'User'
            const lastName = parts.length > 1 ? parts[parts.length - 1] : ''

            let roleStr = 'user'
            if (u.role === 'Admin') roleStr = 'admin'
            else if (u.role === 'StoreOwner') roleStr = 'manager'

            let statusStr = 'active'
            if (u.accountStatus === 'Suspended') statusStr = 'suspended'
            else if (u.accountStatus === 'Banned') statusStr = 'inactive'

            return {
              id: String(u.userId || u.id),
              firstName,
              lastName,
              username: u.username || 'user',
              email: u.email || 'user@dishcision.com',
              phoneNumber: u.city ? `TP. ${u.city}` : 'Việt Nam',
              status: statusStr,
              role: roleStr,
              createdAt: u.createdAt || new Date().toISOString(),
              updatedAt: u.updatedAt || u.createdAt || new Date().toISOString(),
              avatarUrl: u.avatarUrl || null,
              postsCount: u.postsCount || 0,
              followersCount: u.followersCount || 0,
              city: u.city || null,
            } as User
          })
        }
        return fallbackUsers
      } catch {
        return fallbackUsers
      }
    },
    refetchInterval: 30000,
    staleTime: 10000,
  })
}
