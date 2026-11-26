import {
  Sparkles,
  Bot,
  CheckCircle2,
  TrendingUp,
  Cpu,
  Star,
  Activity,
  Code2,
  Coins,
  Clock,
  ArrowDownLeft,
  ArrowUpRight,
  BarChart3,
  Layers,
  UtensilsCrossed,
  Wallet,
  Users2,
  Loader2,
} from 'lucide-react'
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
  const cravingsChart = aiData?.cravingsChart ?? []
  const budgetChart = aiData?.budgetChart ?? []
  const companionChart = aiData?.companionChart ?? []
  const logs = aiData?.logs ?? []
  const totalRequests = aiData?.totalRequests ?? 0

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
              Hệ Thống Trí Tuệ Nhân Tạo (Dishcision AI)
            </h1>
            <p className='text-sm text-muted-foreground mt-0.5'>
              Giám sát mô hình quyết định món ăn duy nhất: <strong>Google Gemini 2.5 Flash</strong> • Dữ liệu đo lường thời gian thực từ PostgreSQL.
            </p>
          </div>
          <div className='flex items-center gap-2'>
            {isLoading ? (
              <Badge variant='outline' className='gap-1.5 py-1 px-3 text-xs'>
                <Loader2 className='size-3 animate-spin text-primary' />
                Đang tải dữ liệu thực tế...
              </Badge>
            ) : (
              <Badge
                variant='outline'
                className='gap-1.5 border-emerald-500/30 bg-emerald-500/10 text-emerald-500 py-1 px-3 text-xs'
              >
                <span className='size-2 rounded-full bg-emerald-500 animate-pulse' />
                Gemini 2.5 Flash: Đang hoạt động ({totalRequests} lượt gọi thực tế)
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
                Tổng Token Tiêu Thụ Thực Tế
              </CardTitle>
              <Coins className='size-4 text-amber-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                {tokens.totalTokens.toLocaleString()}{' '}
                <span className='text-xs font-normal text-muted-foreground'>tokens</span>
              </div>
              <div className='flex items-center gap-3 text-xs text-muted-foreground mt-1.5'>
                <span className='inline-flex items-center gap-1 text-sky-400'>
                  <ArrowDownLeft className='size-3' /> In: {tokens.totalPromptTokens.toLocaleString()}
                </span>
                <span className='inline-flex items-center gap-1 text-primary'>
                  <ArrowUpRight className='size-3' /> Out: {tokens.totalCompletionTokens.toLocaleString()}
                </span>
              </div>
            </CardContent>
          </Card>

          {/* Card 2: Average Latency */}
          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Thời Gian Phản Hồi TB (Latency)
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
                Độ Chuẩn Xác Định Dạng JSON
              </CardTitle>
              <CheckCircle2 className='size-4 text-emerald-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>
                {performance.formatAccuracy}%
              </div>
              <p className='text-xs text-muted-foreground mt-1'>
                Tỷ lệ trả về đúng cấu trúc Top 3 món ăn
              </p>
            </CardContent>
          </Card>

          {/* Card 4: User Rating & Success Rate */}
          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Độ Hài Lòng & Tỷ Lệ Thành Công
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

        {/* RECHARTS SECTION 1: Dual Chart (Token In/Out & Latency Trend) */}
        <div className='grid gap-6 lg:grid-cols-12 mb-6'>
          {/* Chart 1: Token Usage Chart (In vs Out) */}
          <Card className='lg:col-span-8 border-border/60 shadow-xs'>
            <CardHeader className='pb-3'>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <BarChart3 className='size-5 text-primary' />
                  <div>
                    <CardTitle className='text-base font-semibold'>
                      Biểu Đồ Tiêu Thụ Token Hàng Tháng (Token In vs Token Out)
                    </CardTitle>
                    <CardDescription className='text-xs'>
                      Số lượng Prompt Tokens (Đầu vào) và Completion Tokens (Đầu ra) thực tế theo thời gian
                    </CardDescription>
                  </div>
                </div>
                <div className='flex items-center gap-3 text-xs'>
                  <span className='inline-flex items-center gap-1.5'>
                    <span className='size-2.5 rounded-xs bg-[#38bdf8]' /> Token In (Prompt)
                  </span>
                  <span className='inline-flex items-center gap-1.5'>
                    <span className='size-2.5 rounded-xs bg-[#f97316]' /> Token Out (Completion)
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
                        const label = name === 'promptTokens' ? 'Token In (Prompt)' : 'Token Out (Completion)'
                        return [`${Number(val).toLocaleString()} tokens`, label]
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
                    Xu Hướng Độ Trễ (Latency)
                  </CardTitle>
                  <CardDescription className='text-xs'>
                    Thời gian phản hồi Gemini API thực tế (ms)
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
                      formatter={(val: any) => [`${val} ms`, 'Độ trễ trung bình']}
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

        {/* RECHARTS SECTION 2: Cravings, Budget, Companions (All Real from DB) */}
        <div className='grid gap-6 md:grid-cols-3 mb-6'>
          {/* Cravings Distribution Chart */}
          <Card className='border-border/60 shadow-xs md:col-span-1'>
            <CardHeader className='pb-2'>
              <div className='flex items-center gap-2'>
                <UtensilsCrossed className='size-5 text-rose-500' />
                <div>
                  <CardTitle className='text-base font-semibold'>
                    Phân Bổ Cơn Thèm Món Ăn
                  </CardTitle>
                  <CardDescription className='text-xs'>
                    Tần suất từ khóa cơn thèm thực tế trong cơ sở dữ liệu
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className='h-[250px] w-full'>
                {cravingsChart.length === 0 ? (
                  <div className='h-full flex items-center justify-center text-xs text-muted-foreground'>
                    Chưa có dữ liệu cơn thèm
                  </div>
                ) : (
                  <ResponsiveContainer width='100%' height='100%'>
                    <BarChart
                      data={cravingsChart}
                      layout='vertical'
                      margin={{ top: 10, right: 20, left: 20, bottom: 0 }}
                    >
                      <CartesianGrid strokeDasharray='3 3' stroke='#33333333' horizontal={false} />
                      <XAxis type='number' stroke='#888888' fontSize={10} tickLine={false} axisLine={false} />
                      <YAxis
                        type='category'
                        dataKey='name'
                        stroke='#888888'
                        fontSize={10}
                        tickLine={false}
                        axisLine={false}
                        width={110}
                      />
                      <Tooltip
                        contentStyle={{
                          backgroundColor: '#18181b',
                          borderColor: '#27272a',
                          borderRadius: '8px',
                          color: '#f4f4f5',
                          fontSize: '12px',
                        }}
                        formatter={(val: any) => [`${val} lượt`, 'Số lượt yêu cầu']}
                      />
                      <Bar dataKey='count' fill='#ec4899' radius={[0, 4, 4, 0]} maxBarSize={16} />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Budget Distribution Chart */}
          <Card className='border-border/60 shadow-xs md:col-span-1'>
            <CardHeader className='pb-2'>
              <div className='flex items-center gap-2'>
                <Wallet className='size-5 text-emerald-500' />
                <div>
                  <CardTitle className='text-base font-semibold'>
                    Phân Bổ Ngân Sách Dự Chi
                  </CardTitle>
                  <CardDescription className='text-xs'>
                    Khoảng giá người dùng thực tế lựa chọn khi hỏi AI
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className='h-[250px] w-full'>
                {budgetChart.length === 0 ? (
                  <div className='h-full flex items-center justify-center text-xs text-muted-foreground'>
                    Chưa có dữ liệu ngân sách
                  </div>
                ) : (
                  <ResponsiveContainer width='100%' height='100%'>
                    <BarChart
                      data={budgetChart}
                      layout='vertical'
                      margin={{ top: 10, right: 20, left: 10, bottom: 0 }}
                    >
                      <CartesianGrid strokeDasharray='3 3' stroke='#33333333' horizontal={false} />
                      <XAxis type='number' stroke='#888888' fontSize={10} tickLine={false} axisLine={false} />
                      <YAxis
                        type='category'
                        dataKey='name'
                        stroke='#888888'
                        fontSize={10}
                        tickLine={false}
                        axisLine={false}
                        width={90}
                      />
                      <Tooltip
                        contentStyle={{
                          backgroundColor: '#18181b',
                          borderColor: '#27272a',
                          borderRadius: '8px',
                          color: '#f4f4f5',
                          fontSize: '12px',
                        }}
                        formatter={(val: any) => [`${val} lượt`, 'Số lượt']}
                      />
                      <Bar dataKey='count' fill='#10b981' radius={[0, 4, 4, 0]} maxBarSize={16} />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Companions Distribution Chart */}
          <Card className='border-border/60 shadow-xs md:col-span-1'>
            <CardHeader className='pb-2'>
              <div className='flex items-center gap-2'>
                <Users2 className='size-5 text-indigo-500' />
                <div>
                  <CardTitle className='text-base font-semibold'>
                    Quy Mô Bàn Ăn Thực Tế
                  </CardTitle>
                  <CardDescription className='text-xs'>
                    Số lượng người cùng ăn trong các phiên nhờ AI gợi ý
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className='h-[250px] w-full'>
                {companionChart.length === 0 ? (
                  <div className='h-full flex items-center justify-center text-xs text-muted-foreground'>
                    Chưa có dữ liệu người cùng ăn
                  </div>
                ) : (
                  <ResponsiveContainer width='100%' height='100%'>
                    <BarChart
                      data={companionChart}
                      layout='vertical'
                      margin={{ top: 10, right: 20, left: 10, bottom: 0 }}
                    >
                      <CartesianGrid strokeDasharray='3 3' stroke='#33333333' horizontal={false} />
                      <XAxis type='number' stroke='#888888' fontSize={10} tickLine={false} axisLine={false} />
                      <YAxis
                        type='category'
                        dataKey='name'
                        stroke='#888888'
                        fontSize={10}
                        tickLine={false}
                        axisLine={false}
                        width={90}
                      />
                      <Tooltip
                        contentStyle={{
                          backgroundColor: '#18181b',
                          borderColor: '#27272a',
                          borderRadius: '8px',
                          color: '#f4f4f5',
                          fontSize: '12px',
                        }}
                        formatter={(val: any) => [`${val} lượt`, 'Số lượt']}
                      />
                      <Bar dataKey='count' fill='#6366f1' radius={[0, 4, 4, 0]} maxBarSize={16} />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* SINGLE MODEL ARCHITECTURE & FALLBACK CARDS */}
        <div className='grid gap-6 md:grid-cols-2 mb-6'>
          {/* Core Model Card */}
          <Card className='border-primary/30 shadow-xs bg-linear-to-br from-card to-primary/5'>
            <CardHeader className='pb-2'>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2.5'>
                  <div className='p-2.5 rounded-lg bg-primary/10 text-primary'>
                    <Bot className='size-5' />
                  </div>
                  <div>
                    <CardTitle className='text-base font-bold'>
                      Mô hình cốt lõi: Google Gemini 2.5 Flash
                    </CardTitle>
                    <CardDescription className='text-xs mt-0.5'>
                      Endpoint: <code>POST /api/v1/ai/food-decide</code> • Provider: Google Generative AI
                    </CardDescription>
                  </div>
                </div>
                <Badge className='bg-primary/20 text-primary border-primary/30 text-xs'>
                  Chính thức (Active)
                </Badge>
              </div>
            </CardHeader>
            <CardContent className='space-y-3 text-xs leading-relaxed text-muted-foreground pt-1'>
              <p>
                Hệ thống Dishcision vận hành <strong>duy nhất 1 model AI</strong> là <strong>Gemini 2.5 Flash</strong>. Model tiếp nhận các tiêu chí thời gian thực của người dùng (tâm trạng, ngân sách, số người cùng ăn, cơn thèm) để đưa ra <strong>3 món ăn chuẩn vị Việt Nam tối ưu nhất</strong> kèm lý do thuyết phục và khoảng giá dự kiến.
              </p>
              <div className='grid grid-cols-2 sm:grid-cols-4 gap-2 pt-1'>
                <div className='p-2 rounded-md bg-muted/50 border border-border/50'>
                  <span className='text-muted-foreground block text-[10px]'>Tên Model</span>
                  <strong className='font-mono text-foreground text-xs'>gemini-2.5-flash</strong>
                </div>
                <div className='p-2 rounded-md bg-muted/50 border border-border/50'>
                  <span className='text-muted-foreground block text-[10px]'>Temperature</span>
                  <strong className='font-mono text-foreground text-xs'>0.7 (Cân bằng)</strong>
                </div>
                <div className='p-2 rounded-md bg-muted/50 border border-border/50'>
                  <span className='text-muted-foreground block text-[10px]'>Định dạng</span>
                  <strong className='font-mono text-foreground text-xs'>application/json</strong>
                </div>
                <div className='p-2 rounded-md bg-muted/50 border border-border/50'>
                  <span className='text-muted-foreground block text-[10px]'>Đầu ra</span>
                  <strong className='font-mono text-foreground text-xs'>Top 3 món ngon</strong>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Fallback Engine Card */}
          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='pb-2'>
              <div className='flex items-center gap-2.5'>
                <div className='p-2.5 rounded-lg bg-muted text-muted-foreground'>
                  <Cpu className='size-5' />
                </div>
                <div>
                  <CardTitle className='text-base font-semibold'>Cơ chế Dự phòng (Fallback)</CardTitle>
                  <CardDescription className='text-xs mt-0.5'>Database Matcher Engine</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className='space-y-3 text-xs leading-relaxed text-muted-foreground pt-1'>
              <p>
                Nếu API Gemini gặp sự cố mạng, timeout hoặc vượt hạn mức (quota), hệ thống sẽ chuyển đổi mượt mà sang bộ máy <strong>Database Matcher</strong>.
              </p>
              <div className='rounded-md bg-muted/40 p-2.5 border border-border/40 space-y-1 text-xs'>
                <p className='font-medium text-foreground'>Quy trình dự phòng tự động:</p>
                <ul className='list-disc pl-4 space-y-1 text-muted-foreground'>
                  <li>Lọc món ăn thực tế từ các quán ăn trong PostgreSQL</li>
                  <li>Khớp theo từ khóa cơn thèm và khoảng giá tối đa</li>
                  <li>Đảm bảo người dùng luôn nhận được món ăn gợi ý liên tục</li>
                </ul>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* REAL REQUEST INFERENCE LOGS TABLE */}
        <Card className='border-border/60 shadow-xs mb-6'>
          <CardHeader className='pb-3'>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-2'>
                <Layers className='size-5 text-primary' />
                <div>
                  <CardTitle className='text-base font-semibold'>
                    Nhật Ký Gọi AI Thực Tế Từ Cơ Sở Dữ Liệu (Inference Logs)
                  </CardTitle>
                  <CardDescription className='text-xs'>
                    Tất cả các bản ghi truy vấn Gemini được lưu trong PostgreSQL: Token In, Token Out, Độ trễ (ms) và 3 món được gợi ý
                  </CardDescription>
                </div>
              </div>
              <Badge variant='outline' className='text-xs font-mono'>
                {logs.length} bản ghi thực tế
              </Badge>
            </div>
          </CardHeader>
          <CardContent>
            <div className='overflow-x-auto'>
              <Table>
                <TableHeader>
                  <TableRow className='text-xs'>
                    <TableHead className='w-[140px]'>Thời gian</TableHead>
                    <TableHead className='w-[160px]'>Người dùng</TableHead>
                    <TableHead>Nhu cầu / Cơn thèm</TableHead>
                    <TableHead>Top 3 Món Gemini Gợi Ý</TableHead>
                    <TableHead className='text-right w-[120px]'>Token (In/Out)</TableHead>
                    <TableHead className='text-right w-[90px]'>Độ trễ</TableHead>
                    <TableHead className='text-center w-[100px]'>Chuẩn JSON</TableHead>
                    <TableHead className='text-center w-[80px]'>Đánh giá</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {logs.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={8} className='text-center py-8 text-muted-foreground text-xs'>
                        Chưa có bản ghi gọi AI nào trong cơ sở dữ liệu.
                      </TableCell>
                    </TableRow>
                  ) : (
                    logs.map((log) => (
                      <TableRow key={log.id} className='text-xs hover:bg-muted/30'>
                        <TableCell className='font-mono text-[11px] text-muted-foreground whitespace-nowrap'>
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
                                    <span className='text-[10px] text-muted-foreground'>({dish.estimatedPrice})</span>
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
                        <TableCell className='text-center font-mono text-[11px] text-amber-400'>
                          {log.feedbackRating ? `${log.feedbackRating} ⭐` : '—'}
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
          </CardContent>
        </Card>

        {/* SYSTEM PROMPT CONFIGURATION */}
        <Card className='border-border/60'>
          <CardHeader>
            <div className='flex items-center justify-between'>
              <div className='flex items-center gap-2'>
                <Code2 className='size-5 text-primary' />
                <div>
                  <CardTitle className='text-base font-semibold'>
                    System Prompt Điều Hướng Gemini
                  </CardTitle>
                  <CardDescription className='text-xs'>
                    Prompt chính thức được truyền vào Gemini 2.5 Flash tại backend Dishcision
                  </CardDescription>
                </div>
              </div>
              <Badge variant='outline' className='text-xs'>
                Cố định trong mã nguồn backend
              </Badge>
            </div>
          </CardHeader>
          <CardContent>
            <pre className='rounded-md bg-zinc-950 p-4 font-mono text-xs text-zinc-200 border border-border leading-relaxed whitespace-pre-wrap overflow-x-auto'>
{`Bạn là chuyên gia tư vấn ẩm thực Việt Nam cho ứng dụng Dishcision.
Dựa trên các tiêu chí sau, hãy gợi ý 3 món ăn ngon nhất, chuẩn vị Việt Nam:
- Tâm trạng / Cảm xúc: \${mood}
- Ngân sách: \${budget}
- Số người cùng ăn: \${companions}
- Nhu cầu / Thèm món gì: \${craving}

YÊU CẦU: Trả về DUY NHẤT một mảng JSON gồm 3 object (không thêm bất kỳ văn bản nào khác ngoài JSON):
[
  {
    "dishName": "Tên món ăn (tiếng Việt)",
    "reason": "Lý do gợi ý ngắn gọn, thu hút, đúng với tâm trạng",
    "estimatedPrice": "Khoảng giá dự kiến (ví dụ: 45.000 - 65.000 VND)"
  }
]`}
            </pre>
          </CardContent>
        </Card>
      </Main>
    </>
  )
}
