import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Headphones,
  Search as SearchIcon,
  Clock,
  CheckCircle2,
  AlertCircle,
  Send,
  MessageSquare,
  Mail,
  Loader2,
  RefreshCw,
  FileQuestion,
} from 'lucide-react'
import { toast } from 'sonner'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { ThemeSwitch } from '@/components/theme-switch'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useAuthStore } from '@/stores/auth-store'

interface SupportTicketUser {
  id: number
  username: string
  fullName: string
  email: string
  avatarUrl: string | null
  role: string
}

interface TicketTimelineStep {
  step: string
  date: string
  desc: string
  isDone: boolean
}

interface SupportTicket {
  id: number
  userId: number
  user: SupportTicketUser
  type: string
  subject: string
  title: string
  description: string
  contactEmail: string | null
  status: string
  attachments: string[]
  timeline: TicketTimelineStep[]
  createdAt: string
  updatedAt: string
}

export function HelpCenterFeature() {
  const [statusFilter, setStatusFilter] = useState<'all' | 'pending' | 'resolved'>('all')
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedTicket, setSelectedTicket] = useState<SupportTicket | null>(null)
  const [replyMessage, setReplyMessage] = useState('')
  const [isSubmittingReply, setIsSubmittingReply] = useState(false)

  const queryClient = useQueryClient()

  const { data, isLoading, isFetching, refetch } = useQuery<{
    items: SupportTicket[]
    total: number
    stats?: { total: number; pending: number; resolved: number }
  }>({
    queryKey: ['admin-support-tickets', statusFilter, searchTerm],
    queryFn: async () => {
      const token = useAuthStore.getState().auth.accessToken
      const headers: Record<string, string> = { 'Content-Type': 'application/json' }
      if (token) headers['Authorization'] = `Bearer ${token}`

      const params = new URLSearchParams()
      if (statusFilter !== 'all') params.set('status', statusFilter)
      if (searchTerm.trim()) params.set('search', searchTerm.trim())

      const res = await fetch(`/api/v1/admin/support-tickets?${params.toString()}`, { headers })
      if (!res.ok) throw new Error('Không thể tải danh sách yêu cầu hỗ trợ')
      const json = await res.json()
      return {
        items: json.data || [],
        total: json.pagination?.total || (json.data || []).length,
        stats: json.stats,
      }
    },
    refetchInterval: 30000,
  })

  const tickets = data?.items || []
  const totalTickets = data?.stats?.total ?? data?.total ?? tickets.length

  const pendingCount =
    data?.stats?.pending ??
    tickets.filter((t) => ['Đã tiếp nhận', 'Chờ xử lý', 'Pending'].includes(t.status)).length
  const resolvedCount =
    data?.stats?.resolved ??
    tickets.filter((t) => ['Đã giải quyết', 'Đã xử lý', 'Resolved'].includes(t.status)).length
  const resolvedPercentage =
    totalTickets > 0 ? Math.round((resolvedCount / totalTickets) * 100) : 100

  const handleOpenReplyDialog = (ticket: SupportTicket) => {
    setSelectedTicket(ticket)
    setReplyMessage('')
  }

  const handleSubmitReply = async () => {
    if (!selectedTicket) return
    const cleanReply = replyMessage.trim()
    if (!cleanReply) {
      toast.error('Vui lòng nhập nội dung phản hồi trước khi gửi')
      return
    }

    try {
      setIsSubmittingReply(true)
      const token = useAuthStore.getState().auth.accessToken
      const headers: Record<string, string> = { 'Content-Type': 'application/json' }
      if (token) headers['Authorization'] = `Bearer ${token}`

      const res = await fetch(`/api/v1/admin/support-tickets/${selectedTicket.id}/reply`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          replyMessage: cleanReply,
          status: 'Đã giải quyết',
        }),
      })

      if (!res.ok) {
        throw new Error('Gửi phản hồi thất bại')
      }

      toast.success(`Đã phản hồi thành công và gửi thông báo đến tài khoản @${selectedTicket.user.username}`)
      setSelectedTicket(null)
      setReplyMessage('')
      queryClient.invalidateQueries({ queryKey: ['admin-support-tickets'] })
    } catch {
      toast.error('Có lỗi xảy ra khi gửi phản hồi, vui lòng thử lại sau')
    } finally {
      setIsSubmittingReply(false)
    }
  }

  return (
    <>
      <Header fixed>
        <Search className='me-auto' />
        <ThemeSwitch />
        <ProfileDropdown />
      </Header>

      <Main className='flex flex-1 flex-col gap-4 sm:gap-6'>
        {/* Header Title */}
        <div className='flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between'>
          <div>
            <div className='flex items-center gap-2'>
              <h1 className='flex items-center gap-2 text-2xl font-bold tracking-tight'>
                <Headphones className='size-6 text-primary' />
                Trung Tâm Hỗ Trợ Người Dùng
              </h1>
              <Badge
                variant='outline'
                className='border-emerald-500/30 bg-emerald-500/10 text-emerald-500 font-medium text-xs'
              >
                Hỗ trợ trực tuyến
              </Badge>
            </div>
            <p className='text-sm text-muted-foreground mt-1'>
              Tiếp nhận, xử lý các yêu cầu trợ giúp và gửi thông báo phản hồi trực tiếp đến người dùng ứng dụng.
            </p>
          </div>
          <div className='flex items-center gap-2'>
            <Button
              variant='outline'
              size='sm'
              onClick={() => refetch()}
              disabled={isFetching}
              className='gap-1.5 text-xs'
            >
              <RefreshCw className={`size-3.5 ${isFetching ? 'animate-spin' : ''}`} />
              Làm mới dữ liệu
            </Button>
          </div>
        </div>

        {/* 4 Summary Stat Cards */}
        <div className='grid gap-4 sm:grid-cols-2 lg:grid-cols-4'>
          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Tổng số yêu cầu
              </CardTitle>
              <FileQuestion className='size-4 text-primary' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>{totalTickets}</div>
              <p className='text-xs text-muted-foreground mt-1'>
                Toàn bộ phiếu hỗ trợ được tạo từ ứng dụng
              </p>
            </CardContent>
          </Card>

          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Chờ xử lý
              </CardTitle>
              <Clock className='size-4 text-amber-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-amber-600'>{pendingCount}</div>
              <p className='text-xs text-muted-foreground mt-1'>
                Yêu cầu đang chờ phản hồi từ quản trị viên
              </p>
            </CardContent>
          </Card>

          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Đã giải quyết
              </CardTitle>
              <CheckCircle2 className='size-4 text-emerald-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-emerald-600'>{resolvedCount}</div>
              <p className='text-xs text-muted-foreground mt-1'>
                Đã gửi thông báo giải đáp cho người dùng
              </p>
            </CardContent>
          </Card>

          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Tỷ lệ xử lý
              </CardTitle>
              <MessageSquare className='size-4 text-sky-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-sky-600'>{resolvedPercentage}%</div>
              <p className='text-xs text-muted-foreground mt-1'>
                Tiến độ xử lý phản hồi trên toàn hệ thống
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Filter and Search Bar */}
        <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <Tabs
            value={statusFilter}
            onValueChange={(val) => setStatusFilter(val as any)}
            className='w-full sm:w-auto'
          >
            <TabsList className='grid grid-cols-3 w-full sm:w-80'>
              <TabsTrigger value='all' className='text-xs'>
                Tất cả
              </TabsTrigger>
              <TabsTrigger value='pending' className='text-xs'>
                Chờ xử lý
              </TabsTrigger>
              <TabsTrigger value='resolved' className='text-xs'>
                Đã giải quyết
              </TabsTrigger>
            </TabsList>
          </Tabs>

          <div className='relative w-full sm:w-72'>
            <SearchIcon className='absolute left-2.5 top-2.5 size-4 text-muted-foreground' />
            <Input
              type='search'
              placeholder='Tìm kiếm người gửi, chủ đề, mã phiếu...'
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className='pl-8 text-xs'
            />
          </div>
        </div>

        {/* Tickets Table */}
        <Card className='border-border/60 shadow-xs overflow-hidden'>
          <div className='overflow-x-auto'>
            <Table>
              <TableHeader>
                <TableRow className='bg-muted/40'>
                  <TableHead className='w-20 text-xs font-semibold'>Mã phiếu</TableHead>
                  <TableHead className='text-xs font-semibold'>Người gửi</TableHead>
                  <TableHead className='text-xs font-semibold'>Chủ đề & Tiêu đề</TableHead>
                  <TableHead className='w-32 text-xs font-semibold'>Trạng thái</TableHead>
                  <TableHead className='w-36 text-xs font-semibold'>Thời gian gửi</TableHead>
                  <TableHead className='w-36 text-right text-xs font-semibold'>Thao tác</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={6} className='h-40 text-center'>
                      <div className='flex flex-col items-center justify-center gap-2 text-muted-foreground'>
                        <Loader2 className='size-6 animate-spin text-primary' />
                        <span className='text-xs'>Đang tải danh sách yêu cầu hỗ trợ...</span>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : tickets.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className='h-40 text-center'>
                      <div className='flex flex-col items-center justify-center gap-2 text-muted-foreground'>
                        <Headphones className='size-8 opacity-30' />
                        <span className='text-xs font-medium'>Không tìm thấy yêu cầu hỗ trợ nào</span>
                        <span className='text-[11px]'>Các yêu cầu từ ứng dụng sẽ hiển thị tại đây</span>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  tickets.map((ticket) => {
                    const isPending =
                      ['Đã tiếp nhận', 'Chờ xử lý', 'Pending'].includes(ticket.status)
                    const dateFormatted = new Date(ticket.createdAt).toLocaleString('vi-VN', {
                      hour: '2-digit',
                      minute: '2-digit',
                      day: '2-digit',
                      month: '2-digit',
                      year: 'numeric',
                    })

                    return (
                      <TableRow key={ticket.id} className='hover:bg-muted/30 transition-colors'>
                        <TableCell className='font-mono text-xs font-semibold text-primary'>
                          #{ticket.id}
                        </TableCell>
                        <TableCell>
                          <div className='flex items-center gap-2.5'>
                            <Avatar className='size-8 rounded-full border border-border'>
                              {ticket.user.avatarUrl && (
                                <AvatarImage src={ticket.user.avatarUrl} alt={ticket.user.username} />
                              )}
                              <AvatarFallback className='text-xs font-semibold bg-muted'>
                                {(ticket.user.fullName?.[0] || ticket.user.username?.[0] || 'U').toUpperCase()}
                              </AvatarFallback>
                            </Avatar>
                            <div className='flex flex-col text-left'>
                              <span className='text-xs font-medium text-foreground'>
                                {ticket.user.fullName || ticket.user.username}
                              </span>
                              <span className='font-mono text-[10px] text-muted-foreground'>
                                @{ticket.user.username}
                              </span>
                            </div>
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className='flex flex-col gap-0.5 max-w-md'>
                            <div className='flex items-center gap-1.5'>
                              <Badge
                                variant='outline'
                                className='bg-primary/5 text-primary border-primary/20 text-[10px] px-1.5 py-0 font-normal'
                              >
                                {ticket.subject}
                              </Badge>
                              <span className='text-xs font-semibold text-foreground line-clamp-1'>
                                {ticket.title}
                              </span>
                            </div>
                            <p className='text-[11px] text-muted-foreground line-clamp-1'>
                              {ticket.description}
                            </p>
                          </div>
                        </TableCell>
                        <TableCell>
                          {isPending ? (
                            <Badge
                              variant='outline'
                              className='border-amber-500/30 bg-amber-500/10 text-amber-600 font-medium text-xs gap-1 py-0.5'
                            >
                              <Clock className='size-3 animate-pulse' />
                              Chờ xử lý
                            </Badge>
                          ) : (
                            <Badge className='bg-emerald-500/15 text-emerald-600 border-emerald-500/20 font-medium text-xs gap-1 py-0.5'>
                              <CheckCircle2 className='size-3' />
                              Đã giải quyết
                            </Badge>
                          )}
                        </TableCell>
                        <TableCell className='text-xs text-muted-foreground font-mono'>
                          {dateFormatted}
                        </TableCell>
                        <TableCell className='text-right'>
                          <Button
                            size='sm'
                            variant={isPending ? 'default' : 'outline'}
                            onClick={() => handleOpenReplyDialog(ticket)}
                            className='h-7 px-2.5 text-xs gap-1 font-medium'
                          >
                            <MessageSquare className='size-3' />
                            {isPending ? 'Trả lời' : 'Xem chi tiết'}
                          </Button>
                        </TableCell>
                      </TableRow>
                    )
                  })
                )}
              </TableBody>
            </Table>
          </div>
        </Card>

        {/* Dialog Chi tiết & Trả lời yêu cầu hỗ trợ */}
        {selectedTicket && (
          <Dialog open={Boolean(selectedTicket)} onOpenChange={(open) => !open && setSelectedTicket(null)}>
            <DialogContent className='sm:max-w-[620px] max-h-[90vh] overflow-y-auto'>
              <DialogHeader>
                <div className='flex items-center gap-2'>
                  <Badge variant='outline' className='bg-primary/10 text-primary border-primary/20 text-xs'>
                    #{selectedTicket.id}
                  </Badge>
                  <DialogTitle className='text-lg font-bold'>
                    {selectedTicket.title}
                  </DialogTitle>
                </div>
                <DialogDescription className='text-xs text-muted-foreground'>
                  Xem chi tiết yêu cầu hỗ trợ và gửi tin nhắn phản hồi trực tiếp đến người dùng qua ứng dụng di động.
                </DialogDescription>
              </DialogHeader>

              <div className='flex flex-col gap-4 py-2 text-xs'>
                {/* User card info */}
                <div className='flex items-center justify-between p-3 rounded-lg border border-border/70 bg-muted/30'>
                  <div className='flex items-center gap-3'>
                    <Avatar className='size-9 rounded-full border border-border'>
                      {selectedTicket.user.avatarUrl && (
                        <AvatarImage src={selectedTicket.user.avatarUrl} alt={selectedTicket.user.username} />
                      )}
                      <AvatarFallback className='text-xs font-semibold bg-muted'>
                        {(selectedTicket.user.fullName?.[0] || selectedTicket.user.username?.[0] || 'U').toUpperCase()}
                      </AvatarFallback>
                    </Avatar>
                    <div className='flex flex-col'>
                      <span className='font-semibold text-foreground text-sm'>
                        {selectedTicket.user.fullName || selectedTicket.user.username}
                      </span>
                      <div className='flex items-center gap-2 text-muted-foreground text-[11px]'>
                        <span className='font-mono'>@{selectedTicket.user.username}</span>
                        <span>•</span>
                        <span className='flex items-center gap-1'>
                          <Mail className='size-3' />
                          {selectedTicket.contactEmail || selectedTicket.user.email}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className='text-right'>
                    <Badge variant='outline' className='text-[10px]'>
                      Chủ đề: {selectedTicket.subject}
                    </Badge>
                  </div>
                </div>

                {/* Submitted content */}
                <div className='flex flex-col gap-1.5'>
                  <Label className='text-xs font-semibold text-foreground flex items-center gap-1.5'>
                    <AlertCircle className='size-3.5 text-primary' />
                    Nội dung yêu cầu từ người dùng
                  </Label>
                  <div className='p-3.5 rounded-lg border border-border/80 bg-background text-foreground text-xs leading-relaxed whitespace-pre-wrap font-sans'>
                    {selectedTicket.description}
                  </div>
                </div>

                {/* Timeline history */}
                {selectedTicket.timeline && selectedTicket.timeline.length > 0 && (
                  <div className='flex flex-col gap-1.5'>
                    <Label className='text-xs font-semibold text-muted-foreground'>
                      Lịch sử tiến trình xử lý
                    </Label>
                    <div className='flex flex-col gap-2 p-3 rounded-lg border border-border/50 bg-muted/20'>
                      {selectedTicket.timeline.map((item, idx) => (
                        <div key={idx} className='flex items-start gap-2 text-xs'>
                          <div className='mt-0.5 size-2 rounded-full bg-primary shrink-0' />
                          <div className='flex flex-col'>
                            <span className='font-medium text-foreground'>{item.step} - {item.date}</span>
                            <span className='text-[11px] text-muted-foreground'>{item.desc}</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Admin Reply Input */}
                <div className='flex flex-col gap-2 pt-2 border-t border-border/60'>
                  <Label htmlFor='reply-message' className='text-xs font-semibold text-foreground flex items-center gap-1.5'>
                    <Send className='size-3.5 text-primary' />
                    Nội dung trả lời gửi về ứng dụng người dùng
                  </Label>
                  <Textarea
                    id='reply-message'
                    rows={4}
                    value={replyMessage}
                    onChange={(e) => setReplyMessage(e.target.value)}
                    placeholder='Nhập nội dung giải đáp, hướng dẫn hoặc giải pháp gửi đến người dùng...'
                    className='text-xs leading-relaxed resize-none'
                  />
                  <p className='text-[11px] text-muted-foreground'>
                    Hệ thống sẽ cập nhật trạng thái phiếu thành Đã giải quyết và gửi thông báo trực tiếp đến hộp thư của người dùng trong ứng dụng.
                  </p>
                </div>
              </div>

              <DialogFooter className='gap-2 sm:gap-0 pt-2'>
                <Button
                  type='button'
                  variant='outline'
                  size='sm'
                  onClick={() => setSelectedTicket(null)}
                  disabled={isSubmittingReply}
                >
                  Đóng
                </Button>
                <Button
                  type='button'
                  size='sm'
                  onClick={handleSubmitReply}
                  disabled={isSubmittingReply || !replyMessage.trim()}
                  className='gap-1.5'
                >
                  {isSubmittingReply ? (
                    <>
                      <Loader2 className='size-3.5 animate-spin' />
                      Đang gửi phản hồi...
                    </>
                  ) : (
                    <>
                      <Send className='size-3.5' />
                      Gửi phản hồi và hoàn tất
                    </>
                  )}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        )}
      </Main>
    </>
  )
}
