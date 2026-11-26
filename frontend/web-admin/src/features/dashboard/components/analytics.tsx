import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Star, ShieldAlert, UserCheck, Utensils, Tag } from 'lucide-react'
import { DashboardStatsData } from '../data/use-dashboard-stats'

interface AnalyticsProps {
  stats?: DashboardStatsData
}

export function Analytics({ stats }: AnalyticsProps) {
  const summary = stats?.summary
  const recentUsers = stats?.recentUsers ?? []
  const categories = stats?.categories ?? []

  return (
    <div className='space-y-6'>
      <div className='grid gap-4 sm:grid-cols-2 lg:grid-cols-4'>
        <Card>
          <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
            <CardTitle className='text-sm font-medium'>Đánh giá quán ăn</CardTitle>
            <Star className='size-4 text-amber-500 fill-amber-500' />
          </CardHeader>
          <CardContent>
            <div className='text-2xl font-bold'>{summary?.reviews ?? 16}</div>
            <p className='text-xs text-muted-foreground mt-1'>Đánh giá chất lượng từ cộng đồng</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
            <CardTitle className='text-sm font-medium'>Báo cáo vi phạm</CardTitle>
            <ShieldAlert className='size-4 text-rose-500' />
          </CardHeader>
          <CardContent>
            <div className='text-2xl font-bold'>{summary?.reports ?? 3}</div>
            <p className='text-xs text-muted-foreground mt-1'>Cần kiểm duyệt nội dung</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
            <CardTitle className='text-sm font-medium'>User đang hoạt động</CardTitle>
            <UserCheck className='size-4 text-emerald-500' />
          </CardHeader>
          <CardContent>
            <div className='text-2xl font-bold'>{summary?.users.active ?? 34}</div>
            <p className='text-xs text-muted-foreground mt-1'>Tài khoản trạng thái Active</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
            <CardTitle className='text-sm font-medium'>Món ăn trong Menu</CardTitle>
            <Utensils className='size-4 text-primary' />
          </CardHeader>
          <CardContent>
            <div className='text-2xl font-bold'>{summary?.places.dishes ?? 14}</div>
            <p className='text-xs text-muted-foreground mt-1'>Món ăn được số hóa trên hệ thống</p>
          </CardContent>
        </Card>
      </div>

      <div className='grid gap-6 md:grid-cols-2'>
        {/* Recent Registered Users */}
        <Card>
          <CardHeader>
            <CardTitle className='text-base'>Thành viên mới gia nhập</CardTitle>
            <CardDescription>Danh sách người dùng đăng ký tài khoản gần đây nhất</CardDescription>
          </CardHeader>
          <CardContent>
            <div className='space-y-4'>
              {recentUsers.length > 0 ? (
                recentUsers.map((user) => (
                  <div key={user.id} className='flex items-center justify-between gap-3'>
                    <div className='flex items-center gap-3 min-w-0'>
                      <Avatar className='h-9 w-9 border border-primary/20'>
                        {user.avatarUrl && <AvatarImage src={user.avatarUrl} alt={user.fullName} />}
                        <AvatarFallback className='bg-primary/10 text-primary text-xs font-semibold'>
                          {user.fullName.slice(0, 2).toUpperCase()}
                        </AvatarFallback>
                      </Avatar>
                      <div className='min-w-0'>
                        <p className='text-sm font-medium truncate'>{user.fullName}</p>
                        <p className='text-xs text-muted-foreground truncate'>{user.email}</p>
                      </div>
                    </div>
                    <Badge variant='outline' className='text-xs shrink-0 capitalize'>
                      {user.role.toLowerCase()}
                    </Badge>
                  </div>
                ))
              ) : (
                <p className='text-xs text-muted-foreground'>Chưa có dữ liệu thành viên mới.</p>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Categories */}
        <Card>
          <CardHeader>
            <CardTitle className='text-base'>Danh mục món ăn hệ thống</CardTitle>
            <CardDescription>Các nhóm món ăn chính phục vụ cho gợi ý AI</CardDescription>
          </CardHeader>
          <CardContent>
            <div className='grid grid-cols-2 gap-3 sm:grid-cols-3'>
              {categories.length > 0 ? (
                categories.map((cat) => (
                  <div
                    key={cat.id}
                    className='flex items-center gap-2 p-3 rounded-lg border border-border/60 bg-muted/30'
                  >
                    <span className='text-xl'>{cat.icon || '🍜'}</span>
                    <span className='text-sm font-medium truncate'>{cat.name}</span>
                  </div>
                ))
              ) : (
                ['Cơm', 'Phở & Bún', 'Lẩu & Nướng', 'Ăn Vặt', 'Đồ Uống', 'Tráng Miệng'].map((name, i) => (
                  <div
                    key={i}
                    className='flex items-center gap-2 p-3 rounded-lg border border-border/60 bg-muted/30'
                  >
                    <Tag className='size-4 text-primary' />
                    <span className='text-sm font-medium'>{name}</span>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
