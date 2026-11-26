import { Users as UsersIcon, ShieldCheck, Store, UserCheck } from 'lucide-react'
import { getRouteApi } from '@tanstack/react-router'
import { ConfigDrawer } from '@/components/config-drawer'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { ThemeSwitch } from '@/components/theme-switch'
import { Badge } from '@/components/ui/badge'
import { UsersDialogs } from './components/users-dialogs'
import { UsersPrimaryButtons } from './components/users-primary-buttons'
import { UsersProvider } from './components/users-provider'
import { UsersTable } from './components/users-table'
import { useUsers } from './data/use-users'
import { users as fallbackUsers } from './data/users'

const route = getRouteApi('/_authenticated/users/')

export function Users() {
  const search = route.useSearch()
  const navigate = route.useNavigate()
  const { data: userList = fallbackUsers, isLoading } = useUsers()

  const totalUsers = userList.length
  const activeUsers = userList.filter((u) => u.status === 'active').length
  const adminUsers = userList.filter(
    (u) => u.role === 'admin' || u.role === 'superadmin'
  ).length
  const storeOwners = userList.filter((u) => u.role === 'manager').length

  return (
    <UsersProvider>
      <Header fixed>
        <Search className='me-auto' />
        <ThemeSwitch />
        <ConfigDrawer />
        <ProfileDropdown />
      </Header>

      <Main className='flex flex-1 flex-col gap-4 sm:gap-6'>
        {/* Header Title */}
        <div className='flex flex-wrap items-end justify-between gap-4'>
          <div>
            <div className='flex items-center gap-2'>
              <h2 className='text-2xl font-bold tracking-tight'>
                Quản lý Người dùng
              </h2>
              <Badge
                variant='outline'
                className='border-primary/30 text-primary bg-primary/10 font-medium'
              >
                {isLoading ? 'Đang tải...' : `${totalUsers} tài khoản`}
              </Badge>
            </div>
            <p className='text-muted-foreground mt-1 text-sm'>
              Danh sách tài khoản thành viên, chủ quán và quản trị viên hệ thống Dishcision.
            </p>
          </div>
          <UsersPrimaryButtons />
        </div>

        {/* Quick Stats Cards */}
        <div className='grid grid-cols-2 sm:grid-cols-4 gap-3'>
          <div className='rounded-lg border border-border/60 bg-card p-3 shadow-xs flex items-center justify-between'>
            <div>
              <p className='text-xs text-muted-foreground font-medium'>Tổng người dùng</p>
              <p className='text-xl font-bold text-foreground mt-0.5'>{totalUsers}</p>
            </div>
            <div className='h-9 w-9 rounded-md bg-primary/10 flex items-center justify-center text-primary'>
              <UsersIcon className='size-5' />
            </div>
          </div>

          <div className='rounded-lg border border-border/60 bg-card p-3 shadow-xs flex items-center justify-between'>
            <div>
              <p className='text-xs text-muted-foreground font-medium'>Đang hoạt động</p>
              <p className='text-xl font-bold text-emerald-500 mt-0.5'>{activeUsers}</p>
            </div>
            <div className='h-9 w-9 rounded-md bg-emerald-500/10 flex items-center justify-center text-emerald-500'>
              <UserCheck className='size-5' />
            </div>
          </div>

          <div className='rounded-lg border border-border/60 bg-card p-3 shadow-xs flex items-center justify-between'>
            <div>
              <p className='text-xs text-muted-foreground font-medium'>Quản trị viên</p>
              <p className='text-xl font-bold text-amber-500 mt-0.5'>{adminUsers}</p>
            </div>
            <div className='h-9 w-9 rounded-md bg-amber-500/10 flex items-center justify-center text-amber-500'>
              <ShieldCheck className='size-5' />
            </div>
          </div>

          <div className='rounded-lg border border-border/60 bg-card p-3 shadow-xs flex items-center justify-between'>
            <div>
              <p className='text-xs text-muted-foreground font-medium'>Chủ quán ăn</p>
              <p className='text-xl font-bold text-sky-500 mt-0.5'>{storeOwners}</p>
            </div>
            <div className='h-9 w-9 rounded-md bg-sky-500/10 flex items-center justify-center text-sky-500'>
              <Store className='size-5' />
            </div>
          </div>
        </div>

        {/* Users Table */}
        <UsersTable data={userList} search={search} navigate={navigate} />
      </Main>

      <UsersDialogs />
    </UsersProvider>
  )
}
