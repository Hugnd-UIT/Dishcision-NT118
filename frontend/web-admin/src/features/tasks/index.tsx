import { FileText, CheckCircle2, Pin, EyeOff } from 'lucide-react'
import { ConfigDrawer } from '@/components/config-drawer'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { ThemeSwitch } from '@/components/theme-switch'
import { Badge } from '@/components/ui/badge'
import { TasksDialogs } from './components/tasks-dialogs'
import { TasksPrimaryButtons } from './components/tasks-primary-buttons'
import { TasksProvider } from './components/tasks-provider'
import { TasksTable } from './components/tasks-table'
import { usePosts } from './data/use-posts'
import { fallbackPosts } from './data/tasks'

export function Tasks() {
  const { data: posts = fallbackPosts, isLoading } = usePosts()

  const totalCount = posts.length
  const activeCount = posts.filter(
    (p) => p.status === 'Active' && !p.isPinned
  ).length
  const pinnedCount = posts.filter((p) => p.isPinned || p.status === 'Pinned').length
  const hiddenCount = posts.filter((p) => p.status === 'Hidden').length

  return (
    <TasksProvider>
      <Header fixed>
        <Search className='me-auto' />
        <ThemeSwitch />
        <ConfigDrawer />
        <ProfileDropdown />
      </Header>

      <Main className='flex flex-1 flex-col gap-4 sm:gap-6'>
        {/* Header section */}
        <div className='flex flex-wrap items-end justify-between gap-4'>
          <div>
            <div className='flex items-center gap-2'>
              <h2 className='text-2xl font-bold tracking-tight'>
                Quản lý Bài viết ẩm thực
              </h2>
              <Badge
                variant='outline'
                className='border-primary/30 text-primary bg-primary/10 font-medium'
              >
                {isLoading ? 'Đang tải...' : `${totalCount} bài viết`}
              </Badge>
            </div>
            <p className='text-muted-foreground mt-1 text-sm'>
              Danh sách bài review, chia sẻ quán ăn và công thức từ cộng đồng Dishcision.
            </p>
          </div>
          <TasksPrimaryButtons />
        </div>

        {/* Quick status summary cards */}
        <div className='grid grid-cols-2 sm:grid-cols-4 gap-3'>
          <div className='rounded-lg border border-border/60 bg-card p-3 shadow-sm flex items-center justify-between'>
            <div>
              <p className='text-xs text-muted-foreground font-medium'>Tổng bài viết</p>
              <p className='text-xl font-bold text-foreground mt-0.5'>{totalCount}</p>
            </div>
            <div className='h-9 w-9 rounded-md bg-primary/10 flex items-center justify-center text-primary'>
              <FileText className='size-5' />
            </div>
          </div>

          <div className='rounded-lg border border-border/60 bg-card p-3 shadow-sm flex items-center justify-between'>
            <div>
              <p className='text-xs text-muted-foreground font-medium'>Đang hiển thị</p>
              <p className='text-xl font-bold text-emerald-500 mt-0.5'>{activeCount}</p>
            </div>
            <div className='h-9 w-9 rounded-md bg-emerald-500/10 flex items-center justify-center text-emerald-500'>
              <CheckCircle2 className='size-5' />
            </div>
          </div>

          <div className='rounded-lg border border-border/60 bg-card p-3 shadow-sm flex items-center justify-between'>
            <div>
              <p className='text-xs text-muted-foreground font-medium'>Đã ghim nổi bật</p>
              <p className='text-xl font-bold text-amber-500 mt-0.5'>{pinnedCount}</p>
            </div>
            <div className='h-9 w-9 rounded-md bg-amber-500/10 flex items-center justify-center text-amber-500'>
              <Pin className='size-5' />
            </div>
          </div>

          <div className='rounded-lg border border-border/60 bg-card p-3 shadow-sm flex items-center justify-between'>
            <div>
              <p className='text-xs text-muted-foreground font-medium'>Đã ẩn / Vi phạm</p>
              <p className='text-xl font-bold text-destructive mt-0.5'>{hiddenCount}</p>
            </div>
            <div className='h-9 w-9 rounded-md bg-destructive/10 flex items-center justify-center text-destructive'>
              <EyeOff className='size-5' />
            </div>
          </div>
        </div>

        {/* Table */}
        <TasksTable data={posts} />
      </Main>

      <TasksDialogs />
    </TasksProvider>
  )
}
