import { useState } from 'react'
import {
  Sparkles,
  CheckCircle2,
  TrendingUp,
  Star,
  Activity,
  Coins,
  Clock,
  ArrowDownLeft,
  ArrowUpRight,
  BarChart3,
  Layers,
  Loader2,
} from 'lucide-react'
import {
  ChevronLeftIcon,
  ChevronRightIcon,
  DoubleArrowLeftIcon,
  DoubleArrowRightIcon,
} from '@radix-ui/react-icons'
import {
  Bar,
  BarChart,
  Area,
  AreaChart,
  ResponsiveContainer,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
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
import { useAIStats } from './data/use-ai-stats'

export function AiFeature() {
  const { data: aiData, isLoading } = useAIStats()
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)

  const tokens = aiData?.tokens ?? {
    totalPromptTokens: 0,
    totalCompletionTokens: 0,
    totalTokens: 0,
    avgPromptTokens: 0,
    avgCompletionTokens: 0,
  }

  const performance = aiData?.performance ?? {
    avgLatency: 0,
    formatAccuracy: 0,
    successRate: 0,
    errorRate: 0,
  }

  const rating = aiData?.rating ?? {
    avgRating: 0,
    feedbackCount: 0,
  }

  const monthlyChart = aiData?.monthlyChart ?? []
  const logs = aiData?.logs ?? []
  const totalRequests = aiData?.totalRequests ?? 0

  const totalLogs = logs.length
  const totalPages = Math.max(1, Math.ceil(totalLogs / pageSize))
  const paginatedLogs = logs.slice((currentPage - 1) * pageSize, currentPage * pageSize)

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
              <Sparkles className='size-6 text-primary' />
              Hệ Thống Trí Tuệ Nhân Tạo
            </h1>
            <p className='text-sm text-muted-foreground mt-0.5'>
              Giám sát mô hình quyết định món ăn: <strong>Google Gemini 2.5 Flash</strong> • Dữ liệu đo lường thời gian thực từ cơ sở dữ liệu.
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
                Gemini 2.5 Flash: Đang hoạt động • {totalRequests} lượt gọi thực tế
              </Badge>
            )}
          </div>
        </div>

        {/* 4 Core LLM Technical Metrics */}
        <div className='grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-6'>
          {/* Card 1: Token In / Token Out */}
          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Tổng token tiêu thụ
              </CardTitle>
              <Coins className='size-4 text-amber-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                {tokens.totalTokens.toLocaleString()}{' '}
                <span className='text-xs font-normal text-muted-foreground'>token</span>
              </div>
              <div className='flex items-center gap-3 text-xs text-muted-foreground mt-1.5'>
                <span className='inline-flex items-center gap-1 text-sky-400'>
                  <ArrowDownLeft className='size-3' /> Token vào: {tokens.totalPromptTokens.toLocaleString()}
                </span>
                <span className='inline-flex items-center gap-1 text-primary'>
                  <ArrowUpRight className='size-3' /> Token ra: {tokens.totalCompletionTokens.toLocaleString()}
                </span>
              </div>
            </CardContent>
          </Card>

          {/* Card 2: Average Latency */}
          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Thời gian phản hồi trung bình
              </CardTitle>
              <Activity className='size-4 text-sky-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                {performance.avgLatency > 0 ? `${performance.avgLatency} ms` : '—'}
              </div>
              <p className='text-xs text-muted-foreground flex items-center gap-1 mt-1 text-emerald-500'>
                <TrendingUp className='size-3' /> Đo lường trực tiếp từ Gemini API
              </p>
            </CardContent>
          </Card>

          {/* Card 3: JSON Schema Accuracy */}
          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Độ chuẩn xác định dạng JSON
              </CardTitle>
              <CheckCircle2 className='size-4 text-emerald-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                {performance.formatAccuracy}%
              </div>
              <p className='text-xs text-muted-foreground mt-1'>
                Tỷ lệ trả về đúng cấu trúc 3 món ăn
              </p>
            </CardContent>
          </Card>

          {/* Card 4: User Rating & Success Rate */}
          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Độ hài lòng và tỷ lệ thành công
              </CardTitle>
              <Star className='size-4 text-amber-500 fill-amber-500/20' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                {rating.avgRating > 0 ? `${rating.avgRating} / 5.0 ⭐` : 'Chưa có đánh giá'}
              </div>
              <p className='text-xs text-muted-foreground mt-1'>
                {rating.feedbackCount} phản hồi thực tế • Thành công: {performance.successRate}%
              </p>
            </CardContent>
          </Card>
        </div>

        {/* RECHARTS SECTION: Dual Chart (Token In/Out & Latency Trend) */}
        <div className='grid gap-6 lg:grid-cols-12 mb-6'>
          {/* Chart 1: Token Usage Chart (In vs Out) */}
          <Card className='lg:col-span-8 border-border/60 shadow-xs'>
            <CardHeader className='pb-3'>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <BarChart3 className='size-5 text-primary' />
                  <div>
                    <CardTitle className='text-base font-semibold'>
                      Biểu Đồ Tiêu Thụ Token Hàng Tháng
                    </CardTitle>
                    <CardDescription className='text-xs'>
                      Số lượng token vào và token ra thực tế theo thời gian
                    </CardDescription>
                  </div>
                </div>
                <div className='flex items-center gap-3 text-xs'>
                  <span className='inline-flex items-center gap-1.5'>
                    <span className='size-2.5 rounded-xs bg-[#38bdf8]' /> Token vào
                  </span>
                  <span className='inline-flex items-center gap-1.5'>
                    <span className='size-2.5 rounded-xs bg-[#f97316]' /> Token ra
                  </span>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className='h-[310px] w-full'>
                <ResponsiveContainer width='100%' height='100%'>
                  <BarChart
                    data={monthlyChart}
                    margin={{ top: 15, right: 15, left: -10, bottom: 0 }}
                  >
                    <CartesianGrid strokeDasharray='3 3' stroke='#33333333' vertical={false} />
                    <XAxis
                      dataKey='name'
                      stroke='#888888'
                      fontSize={11}
                      tickLine={false}
                      axisLine={false}
                    />
                    <YAxis
                      stroke='#888888'
                      fontSize={11}
                      tickLine={false}
                      axisLine={false}
                      allowDecimals={false}
                    />
                    <Tooltip
                      cursor={{ fill: 'rgba(255, 255, 255, 0.05)' }}
                      contentStyle={{
                        backgroundColor: '#18181b',
                        borderColor: '#27272a',
                        borderRadius: '8px',
                        color: '#f4f4f5',
                        fontSize: '12px',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
                      }}
                      formatter={(val: any, name: any) => {
                        const label = name === 'promptTokens' ? 'Token vào' : 'Token ra'
                        return [`${Number(val).toLocaleString()} token`, label]
                      }}
                    />
                    <Bar
                      dataKey='promptTokens'
                      name='promptTokens'
                      fill='#38bdf8'
                      radius={[4, 4, 0, 0]}
                      maxBarSize={22}
                    />
                    <Bar
                      dataKey='completionTokens'
                      name='completionTokens'
                      fill='#f97316'
                      radius={[4, 4, 0, 0]}
                      maxBarSize={22}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>

          {/* Chart 2: Latency Trend Chart */}
          <Card className='lg:col-span-4 border-border/60 shadow-xs'>
            <CardHeader className='pb-3'>
              <div className='flex items-center gap-2'>
                <Clock className='size-5 text-sky-400' />
                <div>
                  <CardTitle className='text-base font-semibold'>
                    Xu Hướng Thời Gian Phản Hồi
                  </CardTitle>
                  <CardDescription className='text-xs'>
                    Thời gian phản hồi Gemini API thực tế
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className='h-[310px] w-full'>
                <ResponsiveContainer width='100%' height='100%'>
                  <AreaChart
                    data={monthlyChart}
                    margin={{ top: 15, right: 10, left: -20, bottom: 0 }}
                  >
                    <defs>
                      <linearGradient id='latencyGradient' x1='0' y1='0' x2='0' y2='1'>
                        <stop offset='5%' stopColor='#a855f7' stopOpacity={0.4} />
                        <stop offset='95%' stopColor='#a855f7' stopOpacity={0.0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray='3 3' stroke='#33333333' vertical={false} />
                    <XAxis
                      dataKey='name'
                      stroke='#888888'
                      fontSize={11}
                      tickLine={false}
                      axisLine={false}
                    />
                    <YAxis
                      stroke='#888888'
                      fontSize={11}
                      tickLine={false}
                      axisLine={false}
                    />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: '#18181b',
                        borderColor: '#27272a',
                        borderRadius: '8px',
                        color: '#f4f4f5',
                        fontSize: '12px',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
                      }}
                      formatter={(val: any) => [`${val} ms`, 'Thời gian phản hồi trung bình']}
                    />
                    <Area
                      type='monotone'
                      dataKey='latency'
                      stroke='#a855f7'
                      strokeWidth={2}
                      fillOpacity={1}
                      fill='url(#latencyGradient)'
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* REAL REQUEST INFERENCE LOGS TABLE WITH FULL PAGINATION */}
        <Card className='border-border/60 shadow-xs mb-6'>
          <CardHeader className='pb-3'>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-2'>
                <Layers className='size-5 text-primary' />
                <div>
                  <CardTitle className='text-base font-semibold'>
                    Nhật Ký Truy Vấn AI Thực Tế
                  </CardTitle>
                  <CardDescription className='text-xs'>
                    Tất cả các bản ghi truy vấn Gemini được lưu trong cơ sở dữ liệu
                  </CardDescription>
                </div>
              </div>
              <Badge variant='outline' className='text-xs font-mono'>
                {totalLogs} bản ghi thực tế
              </Badge>
            </div>
          </CardHeader>
          <CardContent className='p-0'>
            <div className='overflow-x-auto'>
              <Table>
                <TableHeader>
                  <TableRow className='text-xs'>
                    <TableHead className='w-[140px] pl-6'>Thời gian</TableHead>
                    <TableHead className='w-[160px]'>Người dùng</TableHead>
                    <TableHead>Nhu cầu ẩm thực</TableHead>
                    <TableHead>Top 3 món gợi ý</TableHead>
                    <TableHead className='text-right w-[140px]'>Token vào / ra</TableHead>
                    <TableHead className='text-right w-[90px]'>Thời gian xử lý</TableHead>
                    <TableHead className='text-center w-[110px]'>Định dạng chuẩn</TableHead>
                    <TableHead className='text-center w-[90px] pr-6'>Đánh giá</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {paginatedLogs.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={8} className='text-center py-8 text-muted-foreground text-xs'>
                        Chưa có bản ghi gọi AI nào trong cơ sở dữ liệu.
                      </TableCell>
                    </TableRow>
                  ) : (
                    paginatedLogs.map((log) => (
                      <TableRow key={log.id} className='text-xs hover:bg-muted/30'>
                        <TableCell className='font-mono text-[11px] text-muted-foreground whitespace-nowrap pl-6'>
                          {new Date(log.createdAt).toLocaleString('vi-VN', {
                            hour: '2-digit',
                            minute: '2-digit',
                            second: '2-digit',
                            day: '2-digit',
                            month: '2-digit',
                          })}
                        </TableCell>
                        <TableCell>
                          <div className='flex items-center gap-2'>
                            <Avatar className='size-6'>
                              <AvatarImage src={log.user?.avatarUrl || ''} />
                              <AvatarFallback className='text-[10px] bg-primary/10 text-primary'>
                                {(log.user?.username || 'U')[0].toUpperCase()}
                              </AvatarFallback>
                            </Avatar>
                            <span className='font-medium text-foreground truncate max-w-[100px]'>
                              @{log.user?.username || 'khach'}
                            </span>
                          </div>
                        </TableCell>
                        <TableCell className='max-w-[200px] truncate text-foreground font-medium'>
                          {log.craving}
                        </TableCell>
                        <TableCell className='max-w-[320px]'>
                          <div className='space-y-1 py-1'>
                            {log.suggestions && log.suggestions.length > 0 ? (
                              log.suggestions.slice(0, 3).map((dish, i) => (
                                <div key={i} className='flex items-baseline gap-1.5 text-[11px]'>
                                  <span className='font-bold text-primary font-mono text-[10px]'>#{i + 1}</span>
                                  <span className='font-semibold text-foreground'>{dish.dishName}</span>
                                  {dish.estimatedPrice && (
                                    <span className='text-[10px] text-muted-foreground font-mono'>
                                      {dish.estimatedPrice}
                                    </span>
                                  )}
                                </div>
                              ))
                            ) : (
                              <span className='text-muted-foreground text-[11px] italic'>Chưa có gợi ý</span>
                            )}
                          </div>
                        </TableCell>
                        <TableCell className='text-right font-mono text-[11px] whitespace-nowrap'>
                          <span className='text-sky-400'>{log.promptTokens}</span>
                          <span className='text-muted-foreground'> / </span>
                          <span className='text-primary'>{log.completionTokens}</span>
                        </TableCell>
                        <TableCell className='text-right font-mono text-[11px] text-emerald-400 whitespace-nowrap'>
                          {log.latencyMs} ms
                        </TableCell>
                        <TableCell className='text-center'>
                          <Badge
                            variant='outline'
                            className={`text-[10px] py-0 px-2 ${
                              log.isFormatValid
                                ? 'border-emerald-500/30 bg-emerald-500/10 text-emerald-400'
                                : 'border-rose-500/30 bg-rose-500/10 text-rose-400'
                            }`}
                          >
                            {log.isFormatValid ? 'Hợp lệ 100%' : 'Sai format'}
                          </Badge>
                        </TableCell>
                        <TableCell className='text-center font-mono text-[11px] text-amber-400 pr-6'>
                          {log.feedbackRating ? `${log.feedbackRating} ⭐` : '—'}
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
                    setCurrentPage(1)
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
                  Trang {currentPage} trên {totalPages} • Tổng {totalLogs} bản ghi
                </span>
                <div className='flex items-center gap-1'>
                  <Button
                    variant='outline'
                    size='icon'
                    className='size-8'
                    onClick={() => setCurrentPage(1)}
                    disabled={currentPage === 1}
                  >
                    <span className='sr-only'>Trang đầu</span>
                    <DoubleArrowLeftIcon className='size-3.5' />
                  </Button>
                  <Button
                    variant='outline'
                    size='icon'
                    className='size-8'
                    onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                    disabled={currentPage === 1}
                  >
                    <span className='sr-only'>Trang trước</span>
                    <ChevronLeftIcon className='size-3.5' />
                  </Button>
                  <Button
                    variant='outline'
                    size='icon'
                    className='size-8'
                    onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                    disabled={currentPage === totalPages}
                  >
                    <span className='sr-only'>Trang sau</span>
                    <ChevronRightIcon className='size-3.5' />
                  </Button>
                  <Button
                    variant='outline'
                    size='icon'
                    className='size-8'
                    onClick={() => setCurrentPage(totalPages)}
                    disabled={currentPage === totalPages}
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
