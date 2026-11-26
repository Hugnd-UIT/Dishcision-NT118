import { useState } from 'react'
import {
  ScrollText,
  ShieldAlert,
  Clock,
  UserCheck,
  Search as SearchIcon,
  Loader2,
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

  const { data, isLoading } = useAuditLogs(page, pageSize)

  const logs = data?.logs ?? []
  const totalLogs = data?.total ?? 0
  const totalPages = Math.max(1, Math.ceil(totalLogs / pageSize))

  const filteredLogs = logs.filter((log) => {
    if (!searchTerm) return true
    const term = searchTerm.toLowerCase()
    return (
      log.action.toLowerCase().includes(term) ||
      (log.details || '').toLowerCase().includes(term) ||
      (log.admin?.username || '').toLowerCase().includes(term) ||
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

          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Tài khoản hoạt động
              </CardTitle>
              <UserCheck className='size-4 text-emerald-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                Đa dạng tài khoản
              </div>
              <p className='text-xs text-muted-foreground mt-1'>
                Gồm quản trị viên, chủ quán và người dùng
              </p>
            </CardContent>
          </Card>

          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Mức độ an toàn hệ thống
              </CardTitle>
              <ShieldAlert className='size-4 text-sky-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                Bảo vệ 100%
              </div>
              <p className='text-xs text-muted-foreground mt-1'>
                Mọi hành động đều được kiểm duyệt
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
              <div className='relative w-full sm:w-64'>
                <SearchIcon className='absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  placeholder='Tìm kiếm thao tác...'
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className='pl-9 h-8 text-xs'
                />
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
                              <AvatarImage src={log.admin?.avatarUrl || ''} />
                              <AvatarFallback className='text-[10px] bg-primary/10 text-primary'>
                                {(log.admin?.username || 'U')[0].toUpperCase()}
                              </AvatarFallback>
                            </Avatar>
                            <div className='flex flex-col'>
                              <span className='font-medium text-foreground truncate max-w-[120px]'>
                                @{log.admin?.username || 'nguoidung'}
                              </span>
                              {log.admin?.role && (
                                <span className='text-[10px] text-muted-foreground'>
                                  {log.admin.role === 'Admin' ? 'Quản trị viên' : log.admin.role === 'RestaurantAccount' ? 'Chủ quán' : 'Người dùng'}
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
      </Main>
    </>
  )
}
