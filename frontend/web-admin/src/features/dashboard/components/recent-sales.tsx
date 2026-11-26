import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Heart, MessageSquare } from 'lucide-react'
import { RecentPostItem } from '../data/use-dashboard-stats'

interface RecentActivitiesProps {
  posts?: RecentPostItem[]
}

const defaultPosts: RecentPostItem[] = [
  {
    id: 1,
    content: 'Quán bún chả gia truyền ở Hàng Than ngon đỉnh chóp, thịt nướng thơm lừng!',
    author: {
      id: 1,
      fullName: 'Nguyễn Hoàng Nam',
      username: 'nam_foodie',
      avatarUrl: null,
    },
    likesCount: 24,
    commentsCount: 6,
    createdAt: new Date(Date.now() - 1000 * 60 * 15).toISOString(),
  },
  {
    id: 2,
    content: 'Review quán lẩu nướng buffet 199k cực hot cho sinh viên cuối tuần nè.',
    author: {
      id: 2,
      fullName: 'Trần Mai Linh',
      username: 'linhtran',
      avatarUrl: null,
    },
    likesCount: 42,
    commentsCount: 11,
    createdAt: new Date(Date.now() - 1000 * 60 * 45).toISOString(),
  },
  {
    id: 3,
    content: 'Phở cuốn Ngũ Xã ăn giải ngấy ngày hè cực kỳ tươi mát, nước chấm vừa miệng.',
    author: {
      id: 3,
      fullName: 'Lê Quốc Bảo',
      username: 'baole',
      avatarUrl: null,
    },
    likesCount: 18,
    commentsCount: 3,
    createdAt: new Date(Date.now() - 1000 * 60 * 120).toISOString(),
  },
  {
    id: 4,
    content: 'Cơm tấm sườn bì chả đêm khuya, sườn ướp đậm đà mềm mọng chuẩn vị Sài Gòn.',
    author: {
      id: 4,
      fullName: 'Đặng Thảo Vy',
      username: 'thaovy_eats',
      avatarUrl: null,
    },
    likesCount: 35,
    commentsCount: 8,
    createdAt: new Date(Date.now() - 1000 * 60 * 240).toISOString(),
  },
  {
    id: 5,
    content: 'Bánh mì chảo sốt bơ tỏi xúc xích pate trứng lòng đào siêu cuốn!',
    author: {
      id: 5,
      fullName: 'Vũ Đức Huy',
      username: 'huyvu_travel',
      avatarUrl: null,
    },
    likesCount: 29,
    commentsCount: 5,
    createdAt: new Date(Date.now() - 1000 * 60 * 360).toISOString(),
  },
]

function formatTimeAgo(dateStr: string): string {
  try {
    const diffMs = Date.now() - new Date(dateStr).getTime()
    const diffMins = Math.floor(diffMs / (1000 * 60))
    if (diffMins < 1) return 'Vừa xong'
    if (diffMins < 60) return `${diffMins} phút trước`
    const diffHours = Math.floor(diffMins / 60)
    if (diffHours < 24) return `${diffHours} giờ trước`
    const diffDays = Math.floor(diffHours / 24)
    return `${diffDays} ngày trước`
  } catch {
    return 'Gần đây'
  }
}

function getInitials(name: string): string {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(-2)
    .map((w) => w[0]?.toUpperCase())
    .join('') || 'DS'
}

export function RecentSales({ posts }: RecentActivitiesProps) {
  const displayPosts = posts && posts.length > 0 ? posts : defaultPosts

  return (
    <div className='space-y-6'>
      {displayPosts.map((post) => (
        <div key={post.id} className='flex items-start gap-3.5'>
          <Avatar className='h-9 w-9 border border-primary/20 shrink-0 mt-0.5'>
            {post.author.avatarUrl && (
              <AvatarImage src={post.author.avatarUrl} alt={post.author.fullName} />
            )}
            <AvatarFallback className='bg-primary/10 text-primary font-semibold text-xs'>
              {getInitials(post.author.fullName)}
            </AvatarFallback>
          </Avatar>
          <div className='flex flex-1 flex-col gap-1 min-w-0'>
            <div className='flex items-center justify-between gap-2'>
              <p className='text-sm font-semibold truncate'>
                {post.author.fullName}
                <span className='ml-1.5 text-xs font-normal text-muted-foreground'>
                  @{post.author.username}
                </span>
              </p>
              <span className='text-[11px] text-muted-foreground shrink-0'>
                {formatTimeAgo(post.createdAt)}
              </span>
            </div>
            <p className='text-xs text-muted-foreground line-clamp-2 leading-relaxed'>
              {post.content}
            </p>
            <div className='flex items-center gap-3 mt-1 text-[11px] text-muted-foreground'>
              <span className='flex items-center gap-1 text-rose-500'>
                <Heart className='size-3 fill-rose-500' />
                {post.likesCount}
              </span>
              <span className='flex items-center gap-1 text-blue-500'>
                <MessageSquare className='size-3' />
                {post.commentsCount}
              </span>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}
