import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { TopNav } from '@/components/layout/top-nav'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { ThemeSwitch } from '@/components/theme-switch'
import { Users, FileText, Sparkles, UtensilsCrossed, TrendingUp, RefreshCw } from 'lucide-react'
import { Analytics } from './components/analytics'
import { Overview } from './components/overview'
import { RecentSales } from './components/recent-sales'
import { useDashboardStats } from './data/use-dashboard-stats'

export function Dashboard() {
  const { data: stats, isFetching, refetch } = useDashboardStats()
  const summary = stats?.summary

  return (
    <>
      {/* ===== Top Heading ===== */}
      <Header>
        <TopNav links={topNav} className='me-auto' />
        <Search />
        <ThemeSwitch />
        <ProfileDropdown />
      </Header>

      {/* ===== Main Content ===== */}
      <Main>
        <div className='mb-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between'>
          <div>
            <h1 className='text-2xl font-bold tracking-tight'>Tổng quan hệ thống</h1>
            <p className='text-sm text-muted-foreground'>
              Thống kê tổng thể dữ liệu thực tế: Người dùng, Bài viết, Quán ăn và Lượt gọi AI Dishcision.
            </p>
          </div>
          <div className='flex items-center gap-2'>
            <Button
              variant='outline'
              size='sm'
              onClick={() => refetch()}
              disabled={isFetching}
              className='gap-1.5'
            >
              <RefreshCw className={`size-3.5 ${isFetching ? 'animate-spin' : ''}`} />
              Làm mới dữ liệu
            </Button>
          </div>
        </div>

        <Tabs orientation='vertical' defaultValue='overview' className='space-y-4'>
          <div className='w-full overflow-x-auto pb-1'>
            <TabsList>
              <TabsTrigger value='overview'>Tổng quan</TabsTrigger>
              <TabsTrigger value='analytics'>Phân tích ẩm thực</TabsTrigger>
            </TabsList>
          </div>

          {/* TAB 1: OVERVIEW */}
          <TabsContent value='overview' className='space-y-4'>
            {/* 4 Metric Cards */}
            <div className='grid gap-4 sm:grid-cols-2 lg:grid-cols-4'>
              {/* Card 1: Users */}
              <Card>
                <CardHeader className='flex flex-row items-center justify-between pb-2'>
                  <CardTitle className='text-sm font-medium'>Tổng người dùng</CardTitle>
                  <Users className='size-4 text-primary' />
                </CardHeader>
                <CardContent>
                  <div className='text-2xl font-bold'>{summary?.users.total ?? 34}</div>
                  <p className='text-xs text-muted-foreground mt-1 flex items-center gap-1 text-emerald-600 dark:text-emerald-400'>
                    <TrendingUp className='size-3' />
                    {summary?.users.growth ?? '+100%'} so với tháng trước
                  </p>
                  <p className='text-[11px] text-muted-foreground mt-0.5'>
                    {summary?.users.active ?? 34} tài khoản đang hoạt động
                  </p>
                </CardContent>
              </Card>

              {/* Card 2: Posts */}
              <Card>
                <CardHeader className='flex flex-row items-center justify-between pb-2'>
                  <CardTitle className='text-sm font-medium'>Bài viết ẩm thực</CardTitle>
                  <FileText className='size-4 text-blue-500' />
                </CardHeader>
                <CardContent>
                  <div className='text-2xl font-bold'>{summary?.posts.total ?? 31}</div>
                  <p className='text-xs text-muted-foreground mt-1 flex items-center gap-1 text-emerald-600 dark:text-emerald-400'>
                    <TrendingUp className='size-3' />
                    {summary?.posts.growth ?? '+100%'} ({summary?.posts.thisMonth ?? 31} bài mới)
                  </p>
                  <p className='text-[11px] text-muted-foreground mt-0.5'>
                    Chia sẻ & review từ cộng đồng
                  </p>
                </CardContent>
              </Card>

              {/* Card 3: AI Requests */}
              <Card>
                <CardHeader className='flex flex-row items-center justify-between pb-2'>
                  <CardTitle className='text-sm font-medium'>Lượt quyết định món AI</CardTitle>
                  <Sparkles className='size-4 text-purple-500' />
                </CardHeader>
                <CardContent>
                  <div className='text-2xl font-bold'>{summary?.aiRequests.total ?? 11}</div>
                  <p className='text-xs text-muted-foreground mt-1 text-purple-600 dark:text-purple-400'>
                    {summary?.aiRequests.thisMonth ?? 11} lượt trong tháng này
                  </p>
                  <p className='text-[11px] text-muted-foreground mt-0.5'>
                    Gợi ý món & vòng quay Dishcision
                  </p>
                </CardContent>
              </Card>

              {/* Card 4: Places & Menu */}
              <Card>
                <CardHeader className='flex flex-row items-center justify-between pb-2'>
                  <CardTitle className='text-sm font-medium'>Địa điểm & Thực đơn</CardTitle>
                  <UtensilsCrossed className='size-4 text-amber-500' />
                </CardHeader>
                <CardContent>
                  <div className='text-2xl font-bold'>{summary?.places.total ?? 19}</div>
                  <p className='text-xs text-muted-foreground mt-1'>
                    {summary?.places.stores ?? 5} quán ăn • {summary?.places.dishes ?? 14} món ăn
                  </p>
                  <p className='text-[11px] text-muted-foreground mt-0.5'>
                    Cơ sở dữ liệu địa điểm ăn uống
                  </p>
                </CardContent>
              </Card>
            </div>

            {/* Main Chart + Recent Activities */}
            <div className='grid grid-cols-1 gap-4 lg:grid-cols-7'>
              <Card className='col-span-1 lg:col-span-4'>
                <CardHeader>
                  <CardTitle className='text-base'>Xu hướng bài viết & Gợi ý AI theo tháng</CardTitle>
                  <CardDescription>
                    Thống kê tổng hợp số lượng bài review và lượt người dùng nhờ AI gợi ý món ăn
                  </CardDescription>
                </CardHeader>
                <CardContent className='ps-2'>
                  <Overview data={stats?.monthlyChart} />
                </CardContent>
              </Card>

              <Card className='col-span-1 lg:col-span-3'>
                <CardHeader>
                  <CardTitle className='text-base'>Bài viết mới nhất</CardTitle>
                  <CardDescription>
                    Các bài chia sẻ ẩm thực mới được đăng tải trên ứng dụng
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <RecentSales posts={stats?.recentPosts} />
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          {/* TAB 2: ANALYTICS */}
          <TabsContent value='analytics' className='space-y-4'>
            <Analytics stats={stats} />
          </TabsContent>
        </Tabs>
      </Main>
    </>
  )
}

const topNav = [
  {
    title: 'Tổng quan',
    href: '/',
    isActive: true,
    disabled: false,
  },
  {
    title: 'Bài viết',
    href: '/tasks',
    isActive: false,
    disabled: false,
  },
  {
    title: 'Quản lý API',
    href: '/apps',
    isActive: false,
    disabled: false,
  },
  {
    title: 'Người dùng',
    href: '/users',
    isActive: false,
    disabled: false,
  },
  {
    title: 'Dishcision AI',
    href: '/ai',
    isActive: false,
    disabled: false,
  },
]
