import { useState } from 'react'
import {
  ScrollText,
  Clock,
  UserCheck,
  Search as SearchIcon,
  Loader2,
  RotateCw,
  Activity,
  Users,
} from 'lucide-react'
import {
  ChevronLeftIcon,
  ChevronRightIcon,
  DoubleArrowLeftIcon,
  DoubleArrowRightIcon,
} from '@radix-ui/react-icons'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { ScrollArea } from '@/components/ui/scroll-area'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { ThemeSwitch } from '@/components/theme-switch'
import { useAuditLogs } from './data/use-audit-logs'

export function AuditLogsFeature() {
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [searchTerm, setSearchTerm] = useState('')
  const [isAccountsDialogOpen, setIsAccountsDialogOpen] = useState(false)
  const [accountSearch, setAccountSearch] = useState('')

  const { data, isLoading, refetch, isFetching } = useAuditLogs(page, pageSize)

  const logs = data?.logs ?? []
  const totalLogs = data?.total ?? 0
  const totalPages = Math.max(1, Math.ceil(totalLogs / pageSize))

  const stats = data?.stats
  const activeAccounts = stats?.activeAccounts ?? []
  const activeCount = stats?.activeAccountsCount ?? activeAccounts.length
  const todayActionCount = stats?.todayActionCount ?? logs.length

  const filteredLogs = logs.filter((log) => {
    if (!searchTerm) return true
    const term = searchTerm.toLowerCase()
    const actorUser = (log.admin?.username || log.actorUsername || '').toLowerCase()
    return (
      log.action.toLowerCase().includes(term) ||
      (log.details || '').toLowerCase().includes(term) ||
      actorUser.includes(term) ||
      log.targetType.toLowerCase().includes(term)
    )
  })

  return (
    <>
      <Header>
        <Search />
        <ThemeSwitch />
        <ProfileDropdown />
      </Header>

      <Main>
        {/* Header Title */}
        <div className='mb-6 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between'>
          <div>
            <h1 className='flex items-center gap-2 text-2xl font-bold tracking-tight'>
              <ScrollText className='size-6 text-primary' />
              Nhật Ký Hoạt Động
            </h1>
            <p className='text-sm text-muted-foreground mt-0.5'>
              Giám sát toàn bộ thao tác quản trị và các sự kiện hệ thống được ghi nhận trong cơ sở dữ liệu.
            </p>
          </div>
          <div className='flex items-center gap-2'>
            {isLoading ? (
              <Badge variant='outline' className='gap-1.5 py-1 px-3 text-xs'>
                <Loader2 className='size-3 animate-spin text-primary' />
                Đang tải dữ liệu thực tế
              </Badge>
            ) : (
              <Badge
                variant='outline'
                className='gap-1.5 border-emerald-500/30 bg-emerald-500/10 text-emerald-500 py-1 px-3 text-xs'
              >
                <span className='size-2 rounded-full bg-emerald-500 animate-pulse' />
                Ghi nhật ký: Đang hoạt động • {totalLogs} sự kiện
              </Badge>
            )}
          </div>
        </div>

        {/* 4 Summary Stat Cards */}
        <div className='grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-6'>
          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Tổng số bản ghi hoạt động
              </CardTitle>
              <ScrollText className='size-4 text-primary' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                {totalLogs.toLocaleString()}
              </div>
              <p className='text-xs text-muted-foreground mt-1'>
                Lưu trữ vĩnh viễn trong PostgreSQL
              </p>
            </CardContent>
          </Card>

          {/* Card 2: Realtime Active Accounts */}
          <Card
            onClick={() => setIsAccountsDialogOpen(true)}
            className='border-border/60 shadow-xs cursor-pointer hover:border-emerald-500/50 hover:bg-emerald-500/5 transition-all'
          >
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Tài khoản đang hoạt động
              </CardTitle>
              <div className='flex items-center gap-1.5'>
                <span className='size-2 rounded-full bg-emerald-500 animate-pulse' />
                <Badge variant='outline' className='text-[10px] border-emerald-500/30 bg-emerald-500/10 text-emerald-500 py-0 px-1.5 font-normal'>
                  Trực tuyến
                </Badge>
              </div>
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                {activeCount > 0 ? `${activeCount} tài khoản` : 'Đang cập nhật'}
              </div>
              <div className='flex items-center justify-between mt-2'>
                <div className='flex -space-x-1.5 overflow-hidden'>
                  {activeAccounts.slice(0, 4).map((acc) => (
                    <Avatar key={acc.id} className='size-5 ring-1 ring-background'>
                      <AvatarImage src={acc.avatarUrl || ''} />
                      <AvatarFallback className='text-[9px] bg-primary/20 text-primary'>
                        {acc.username[0].toUpperCase()}
                      </AvatarFallback>
                    </Avatar>
                  ))}
                  {activeAccounts.length > 4 && (
                    <span className='flex size-5 items-center justify-center rounded-full bg-muted text-[9px] text-muted-foreground ring-1 ring-background'>
                      +{activeAccounts.length - 4}
                    </span>
                  )}
                </div>
                <span className='text-xs font-medium text-emerald-600 dark:text-emerald-400 hover:underline inline-flex items-center gap-1'>
                  Xem danh sách
                </span>
              </div>
            </CardContent>
          </Card>

          {/* Card 3: Today's Operations (replaced security card) */}
          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Hoạt động trong 24 giờ qua
              </CardTitle>
              <Activity className='size-4 text-sky-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                {todayActionCount.toLocaleString()} thao tác
              </div>
              <p className='text-xs text-muted-foreground mt-1'>
                Ghi nhận tự động từ tất cả tài khoản
              </p>
            </CardContent>
          </Card>

          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Thao tác gần nhất
              </CardTitle>
              <Clock className='size-4 text-amber-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                {logs.length > 0
                  ? new Date(logs[0].createdAt).toLocaleTimeString('vi-VN', {
                      hour: '2-digit',
                      minute: '2-digit',
                    })
                  : '—'}
              </div>
              <p className='text-xs text-muted-foreground mt-1 truncate'>
                {logs.length > 0 ? logs[0].action : 'Chưa có hoạt động mới'}
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Audit Log Table Card */}
        <Card className='border-border/60 shadow-xs mb-6'>
          <CardHeader className='pb-3'>
            <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
              <div>
                <CardTitle className='text-base font-semibold'>
                  Bảng Lịch Sử Thao Tác Hệ Thống
                </CardTitle>
                <CardDescription className='text-xs'>
                  Danh sách chi tiết các hành động và hoạt động của tất cả tài khoản trên hệ thống
                </CardDescription>
              </div>
              <div className='flex items-center gap-2 w-full sm:w-auto'>
                <div className='relative w-full sm:w-64'>
                  <SearchIcon className='absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground' />
                  <Input
                    placeholder='Tìm kiếm thao tác...'
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className='pl-9 h-8 text-xs'
                  />
                </div>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => refetch()}
                  disabled={isFetching}
                  className='h-8 text-xs gap-1.5 shrink-0'
                >
                  <RotateCw className={`size-3.5 ${isFetching ? 'animate-spin' : ''}`} />
                  Làm mới
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent className='p-0'>
            <div className='overflow-x-auto'>
              <Table>
                <TableHeader>
                  <TableRow className='text-xs'>
                    <TableHead className='w-[150px] pl-6'>Thời gian</TableHead>
                    <TableHead className='w-[180px]'>Tài khoản thực hiện</TableHead>
                    <TableHead className='w-[180px]'>Hành động</TableHead>
                    <TableHead className='w-[120px]'>Đối tượng</TableHead>
                    <TableHead>Chi tiết thao tác</TableHead>
                    <TableHead className='w-[130px] pr-6 text-right'>Địa chỉ IP</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredLogs.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} className='text-center py-8 text-muted-foreground text-xs'>
                        Chưa có bản ghi nhật ký hoạt động nào.
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredLogs.map((log) => (
                      <TableRow key={log.id} className='text-xs hover:bg-muted/30'>
                        <TableCell className='font-mono text-[11px] text-muted-foreground whitespace-nowrap pl-6'>
                          {new Date(log.createdAt).toLocaleString('vi-VN', {
                            hour: '2-digit',
                            minute: '2-digit',
                            second: '2-digit',
                            day: '2-digit',
                            month: '2-digit',
                            year: 'numeric',
                          })}
                        </TableCell>
                        <TableCell>
                          <div className='flex items-center gap-2'>
                            <Avatar className='size-6'>
                              <AvatarImage src={log.admin?.avatarUrl || log.actorAvatar || ''} />
                              <AvatarFallback className='text-[10px] bg-primary/10 text-primary'>
                                {(log.admin?.username || log.actorUsername || 'U')[0].toUpperCase()}
                              </AvatarFallback>
                            </Avatar>
                            <div className='flex flex-col'>
                              <span className='font-medium text-foreground truncate max-w-[120px]'>
                                @{log.admin?.username || log.actorUsername || 'nguoidung'}
                              </span>
                              {(log.admin?.role || log.actorRole) && (
                                <span className='text-[10px] text-muted-foreground'>
                                  {(log.admin?.role || log.actorRole) === 'Admin' ? 'Quản trị viên' : (log.admin?.role || log.actorRole) === 'RestaurantAccount' ? 'Chủ quán' : 'Người dùng'}
                                </span>
                              )}
                            </div>
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant='outline'
                            className='font-mono text-[10px] border-primary/30 bg-primary/10 text-primary'
                          >
                            {log.action}
                          </Badge>
                        </TableCell>
                        <TableCell className='font-mono text-[11px] text-muted-foreground'>
                          {log.targetType} #{log.targetId}
                        </TableCell>
                        <TableCell className='text-foreground text-xs max-w-[300px] truncate'>
                          {log.details || 'Không có chi tiết'}
                        </TableCell>
                        <TableCell className='font-mono text-[11px] text-muted-foreground pr-6 text-right whitespace-nowrap'>
                          {log.ipAddress || '127.0.0.1'}
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>

            {/* Pagination Controls */}
            <div className='flex items-center justify-between border-t border-border/50 px-6 py-3 text-xs'>
              <div className='flex items-center gap-2'>
                <span className='text-muted-foreground'>Số dòng mỗi trang:</span>
                <Select
                  value={String(pageSize)}
                  onValueChange={(val) => {
                    setPageSize(Number(val))
                    setPage(1)
                  }}
                >
                  <SelectTrigger className='h-8 w-16'>
                    <SelectValue placeholder={pageSize} />
                  </SelectTrigger>
                  <SelectContent side='top'>
                    {[5, 10, 20, 50].map((size) => (
                      <SelectItem key={size} value={String(size)}>
                        {size}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='flex items-center gap-4'>
                <span className='text-muted-foreground'>
                  Trang {page} trên {totalPages} • Tổng {totalLogs} bản ghi
                </span>
                <div className='flex items-center gap-1'>
                  <Button
                    variant='outline'
                    size='icon'
                    className='size-8'
                    onClick={() => setPage(1)}
                    disabled={page === 1}
                  >
                    <span className='sr-only'>Trang đầu</span>
                    <DoubleArrowLeftIcon className='size-3.5' />
                  </Button>
                  <Button
                    variant='outline'
                    size='icon'
                    className='size-8'
                    onClick={() => setPage((p) => Math.max(1, p - 1))}
                    disabled={page === 1}
                  >
                    <span className='sr-only'>Trang trước</span>
                    <ChevronLeftIcon className='size-3.5' />
                  </Button>
                  <Button
                    variant='outline'
                    size='icon'
                    className='size-8'
                    onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                    disabled={page === totalPages}
                  >
                    <span className='sr-only'>Trang sau</span>
                    <ChevronRightIcon className='size-3.5' />
                  </Button>
                  <Button
                    variant='outline'
                    size='icon'
                    className='size-8'
                    onClick={() => setPage(totalPages)}
                    disabled={page === totalPages}
                  >
                    <span className='sr-only'>Trang cuối</span>
                    <DoubleArrowRightIcon className='size-3.5' />
                  </Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Realtime Active Accounts Dialog */}
        <Dialog open={isAccountsDialogOpen} onOpenChange={setIsAccountsDialogOpen}>
          <DialogContent className='sm:max-w-[560px] max-h-[85vh] flex flex-col p-0 gap-0'>
            <DialogHeader className='p-6 pb-4 border-b border-border/50'>
              <div className='flex items-center gap-2'>
                <span className='size-2.5 rounded-full bg-emerald-500 animate-pulse' />
                <DialogTitle className='text-base font-semibold'>
                  Danh sách tài khoản đang hoạt động thời gian thực
                </DialogTitle>
              </div>
              <DialogDescription className='text-xs text-muted-foreground mt-1'>
                Tự động cập nhật mỗi 3 giây • Tổng cộng {activeAccounts.length} tài khoản đang có kết nối
              </DialogDescription>
              <div className='relative mt-3'>
                <SearchIcon className='absolute left-3 top-1/2 -translate-y-1/2 size-3.5 text-muted-foreground' />
                <Input
                  placeholder='Tìm kiếm theo tên tài khoản hoặc vai trò...'
                  value={accountSearch}
                  onChange={(e) => setAccountSearch(e.target.value)}
                  className='pl-8 h-9 text-xs'
                />
              </div>
            </DialogHeader>

            <ScrollArea className='p-6 overflow-y-auto max-h-[50vh]'>
              {activeAccounts
                .filter((acc) => {
                  if (!accountSearch) return true
                  const term = accountSearch.toLowerCase()
                  return (
                    acc.username.toLowerCase().includes(term) ||
                    (acc.fullName || '').toLowerCase().includes(term) ||
                    acc.role.toLowerCase().includes(term)
                  )
                })
                .length === 0 ? (
                <div className='text-center py-8 text-xs text-muted-foreground'>
                  Không tìm thấy tài khoản nào phù hợp
                </div>
              ) : (
                <div className='space-y-3'>
                  {activeAccounts
                    .filter((acc) => {
                      if (!accountSearch) return true
                      const term = accountSearch.toLowerCase()
                      return (
                        acc.username.toLowerCase().includes(term) ||
                        (acc.fullName || '').toLowerCase().includes(term) ||
                        acc.role.toLowerCase().includes(term)
                      )
                    })
                    .map((acc) => (
                      <div
                        key={acc.id}
                        className='flex items-center justify-between p-3 rounded-lg border border-border/50 hover:bg-muted/40 transition-colors'
                      >
                        <div className='flex items-center gap-3'>
                          <div className='relative'>
                            <Avatar className='size-10'>
                              <AvatarImage src={acc.avatarUrl || ''} />
                              <AvatarFallback className='text-xs font-semibold bg-primary/10 text-primary'>
                                {acc.username[0].toUpperCase()}
                              </AvatarFallback>
                            </Avatar>
                            <span className='absolute bottom-0 right-0 size-2.5 rounded-full bg-emerald-500 ring-2 ring-background' />
                          </div>
                          <div className='flex flex-col'>
                            <div className='flex items-center gap-2'>
                              <span className='font-semibold text-sm text-foreground'>
                                @{acc.username}
                              </span>
                              <Badge
                                variant='outline'
                                className='text-[10px] py-0 px-1.5 font-normal border-primary/30 bg-primary/10 text-primary'
                              >
                                {acc.role === 'Admin'
                                  ? 'Quản trị viên'
                                  : acc.role === 'RestaurantAccount'
                                  ? 'Chủ quán'
                                  : 'Người dùng'}
                              </Badge>
                            </div>
                            <span className='text-xs text-muted-foreground'>
                              {acc.fullName && acc.fullName !== acc.username ? acc.fullName : 'Tài khoản hoạt động'} • {acc.deviceName || 'Thiết bị di động'}
                            </span>
                          </div>
                        </div>

                        <div className='text-right text-xs'>
                          <span className='inline-flex items-center gap-1 font-mono text-[11px] text-muted-foreground'>
                            {acc.location || 'Trực tuyến'}
                          </span>
                          <div className='text-[10px] text-emerald-600 dark:text-emerald-400 font-medium'>
                            Vừa hoạt động lúc {new Date(acc.lastActive).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                          </div>
                        </div>
                      </div>
                    ))}
                </div>
              )}
            </ScrollArea>
          </DialogContent>
        </Dialog>
      </Main>
    </>
  )
}
