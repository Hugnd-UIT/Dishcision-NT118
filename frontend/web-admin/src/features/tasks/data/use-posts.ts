import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Post } from './schema'
import { fallbackPosts } from './tasks'

export function usePosts() {
  return useQuery<Post[]>({
    queryKey: ['admin-posts'],
    queryFn: async () => {
      try {
        const res = await fetch('/api/v1/admin/posts?limit=100')
        if (!res.ok) return fallbackPosts
        const json = await res.json()
        if (json.success && Array.isArray(json.data) && json.data.length > 0) {
          return json.data.map((item: any) => {
            const rawImages = Array.isArray(item.images)
              ? item.images.map((img: any) =>
                  typeof img === 'string' ? img : img?.imageUrl || ''
                ).filter(Boolean)
              : []

            const isPinned = Boolean(item.isPinned)
            let status = 'Active'
            if (isPinned) {
              status = 'Pinned'
            } else if (item.status === 'Hidden') {
              status = 'Hidden'
            }

            return {
              id: `POST-${item.postId || item.id}`,
              postId: item.postId || item.id,
              title: item.content
                ? item.content.length > 60
                  ? item.content.slice(0, 60) + '...'
                  : item.content
                : 'Bài viết ẩm thực',
              content: item.content || '',
              author: {
                id: item.author?.id || 1,
                fullName:
                  item.author?.fullName || item.author?.username || 'Thành viên Dishcision',
                username: item.author?.username || 'user',
                avatarUrl: item.author?.avatarUrl || null,
              },
              images: rawImages,
              status,
              label: item.category || 'review',
              priority: isPinned ? 'featured' : (item.likesCount > 10 ? 'trending' : 'latest'),
              likesCount: item.likesCount ?? 0,
              commentsCount: item.commentsCount ?? 0,
              sharesCount: item.sharesCount ?? 0,
              isPinned,
              createdAt: item.createdAt || new Date().toISOString(),
              updatedAt: item.updatedAt || item.createdAt,
            } as Post
          })
        }
        return fallbackPosts
      } catch {
        return fallbackPosts
      }
    },
    refetchInterval: 20000,
    staleTime: 5000,
  })
}

export function usePostMutations() {
  const queryClient = useQueryClient()

  const pinMutation = useMutation({
    mutationFn: async ({ postId, isPinned }: { postId: number; isPinned: boolean }) => {
      const res = await fetch(`/api/v1/admin/posts/${postId}/pin`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ isPinned }),
      })
      if (!res.ok) throw new Error('Không thể cập nhật ghim bài viết')
      return res.json()
    },
    onSuccess: (_, variables) => {
      toast.success(
        variables.isPinned ? 'Đã ghim bài viết lên đầu trang' : 'Đã bỏ ghim bài viết'
      )
      queryClient.invalidateQueries({ queryKey: ['admin-posts'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard-stats'] })
    },
    onError: (err: any) => {
      toast.error(err.message || 'Lỗi khi ghim bài viết')
    },
  })

  const hideMutation = useMutation({
    mutationFn: async ({ postId, isHidden }: { postId: number; isHidden: boolean }) => {
      const res = await fetch(`/api/v1/admin/posts/${postId}/hide`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ isHidden }),
      })
      if (!res.ok) throw new Error('Không thể thay đổi trạng thái bài viết')
      return res.json()
    },
    onSuccess: (_, variables) => {
      toast.success(
        variables.isHidden ? 'Đã ẩn bài viết khỏi cộng đồng' : 'Đã bỏ ẩn bài viết'
      )
      queryClient.invalidateQueries({ queryKey: ['admin-posts'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard-stats'] })
    },
    onError: (err: any) => {
      toast.error(err.message || 'Lỗi khi thay đổi trạng thái bài viết')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: async (postId: number) => {
      const res = await fetch(`/api/v1/admin/posts/${postId}`, {
        method: 'DELETE',
      })
      if (!res.ok) throw new Error('Không thể xóa bài viết')
      return res.json()
    },
    onSuccess: () => {
      toast.success('Đã xóa bài viết thành công')
      queryClient.invalidateQueries({ queryKey: ['admin-posts'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard-stats'] })
    },
    onError: (err: any) => {
      toast.error(err.message || 'Lỗi khi xóa bài viết')
    },
  })

  return {
    pinMutation,
    hideMutation,
    deleteMutation,
  }
}
