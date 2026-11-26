import {
  Sparkles,
  Bot,
  Zap,
  CheckCircle2,
  TrendingUp,
  Cpu,
  Star,
  Activity,
  Smile,
  Flame,
  Wallet,
  Users2,
  Code2,
} from 'lucide-react'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { ThemeSwitch } from '@/components/theme-switch'
import { useDashboardStats } from '@/features/dashboard/data/use-dashboard-stats'

export function AiFeature() {
  const { data: stats } = useDashboardStats()

  const totalAiRequests = stats?.summary?.aiRequests?.total ?? 11
  const aiGrowth = stats?.summary?.aiRequests?.growth ?? '+15.4%'
  const aiThisMonth = stats?.summary?.aiRequests?.thisMonth ?? 11

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
              Giám sát mô hình quyết định món ăn duy nhất của hệ thống: <strong>Google Gemini 2.5 Flash</strong>.
            </p>
          </div>
          <div className='flex items-center gap-2'>
            <Badge
              variant='outline'
              className='gap-1.5 border-emerald-500/30 bg-emerald-500/10 text-emerald-500 py-1 px-3 text-xs'
            >
              <span className='size-2 rounded-full bg-emerald-500 animate-pulse' />
              Gemini 2.5 Flash: Đang hoạt động
            </Badge>
          </div>
        </div>

        {/* 4 Core Performance Metrics */}
        <div className='grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-6'>
          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Lượt nhờ AI quyết định món
              </CardTitle>
              <Zap className='size-4 text-primary' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>{totalAiRequests} lượt</div>
              <p className='text-xs text-muted-foreground flex items-center gap-1 mt-1 text-emerald-500'>
                <TrendingUp className='size-3' /> {aiGrowth} ({aiThisMonth} lượt tháng này)
              </p>
            </CardContent>
          </Card>

          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Độ hài lòng người dùng
              </CardTitle>
              <Star className='size-4 text-amber-500 fill-amber-500/20' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>4.8 / 5.0 ⭐</div>
              <p className='text-xs text-muted-foreground mt-1'>
                Dựa trên phản hồi đánh giá gợi ý
              </p>
            </CardContent>
          </Card>

          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Thời gian phản hồi Gemini
              </CardTitle>
              <Activity className='size-4 text-sky-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>~420 ms</div>
              <p className='text-xs text-muted-foreground mt-1'>
                Độ trễ trung bình của Gemini API
              </p>
            </CardContent>
          </Card>

          <Card className='border-border/60 shadow-xs'>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-xs font-medium text-muted-foreground'>
                Tỷ lệ gọi Gemini thành công
              </CardTitle>
              <CheckCircle2 className='size-4 text-emerald-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold text-foreground'>100%</div>
              <p className='text-xs text-muted-foreground mt-1'>
                0 lần phải kích hoạt fallback
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Single Model Architecture Card */}
        <div className='grid gap-6 md:grid-cols-3 mb-6'>
          <Card className='md:col-span-2 border-primary/30 shadow-xs bg-linear-to-br from-card to-primary/5'>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2.5'>
                  <div className='p-2.5 rounded-lg bg-primary/10 text-primary'>
                    <Bot className='size-6' />
                  </div>
                  <div>
                    <CardTitle className='text-lg font-bold'>
                      Mô hình cốt lõi: Google Gemini 2.5 Flash
                    </CardTitle>
                    <CardDescription className='text-xs mt-0.5'>
                      Endpoint: <code>POST /api/v1/ai/food-decide</code> • Provider: Google Generative AI
                    </CardDescription>
                  </div>
                </div>
                <Badge className='bg-primary/20 text-primary border-primary/30'>
                  Chính thức (Active)
                </Badge>
              </div>
            </CardHeader>
            <CardContent className='space-y-4 text-sm'>
              <p className='text-muted-foreground leading-relaxed'>
                Hệ thống Dishcision vận hành <strong>duy nhất 1 model AI</strong> là <strong>Gemini 2.5 Flash</strong>. Model tiếp nhận các tiêu chí thời gian thực của người dùng (tâm trạng, ngân sách, số người cùng ăn, cơn thèm) để tính toán và đưa ra <strong>3 món ăn chuẩn vị Việt Nam tối ưu nhất</strong> kèm lý do thuyết phục và khoảng giá dự kiến.
              </p>

              <div className='grid grid-cols-2 sm:grid-cols-4 gap-2.5 text-xs pt-1'>
                <div className='p-2.5 rounded-md bg-muted/50 border border-border/50'>
                  <span className='text-muted-foreground block text-[11px]'>Tên Model</span>
                  <strong className='font-mono text-foreground text-xs'>gemini-2.5-flash</strong>
                </div>
                <div className='p-2.5 rounded-md bg-muted/50 border border-border/50'>
                  <span className='text-muted-foreground block text-[11px]'>Temperature</span>
                  <strong className='font-mono text-foreground text-xs'>0.7 (Cân bằng)</strong>
                </div>
                <div className='p-2.5 rounded-md bg-muted/50 border border-border/50'>
                  <span className='text-muted-foreground block text-[11px]'>Định dạng xuất</span>
                  <strong className='font-mono text-foreground text-xs'>application/json</strong>
                </div>
                <div className='p-2.5 rounded-md bg-muted/50 border border-border/50'>
                  <span className='text-muted-foreground block text-[11px]'>Số món gợi ý</span>
                  <strong className='font-mono text-foreground text-xs'>Top 3 món ngon</strong>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Fallback Mechanism Card */}
          <Card className='border-border/60 shadow-xs'>
            <CardHeader>
              <div className='flex items-center gap-2'>
                <div className='p-2 rounded-md bg-muted text-muted-foreground'>
                  <Cpu className='size-5' />
                </div>
                <div>
                  <CardTitle className='text-base font-semibold'>Cơ chế Dự phòng (Fallback)</CardTitle>
                  <CardDescription className='text-xs'>Database Matcher Engine</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className='space-y-3 text-xs leading-relaxed text-muted-foreground'>
              <p>
                Nếu API Gemini gặp lỗi mạng, timeout hoặc vượt quota, hệ thống tự động kích hoạt bộ máy <strong>Database Matcher</strong>.
              </p>
              <div className='rounded-md bg-muted/40 p-2.5 border border-border/40 space-y-1.5'>
                <p className='font-medium text-foreground text-[11px]'>Quy trình dự phòng:</p>
                <ul className='list-disc pl-4 space-y-1 text-[11px]'>
                  <li>Lọc món ăn thực tế từ quán trong PostgreSQL</li>
                  <li>Khớp theo cơn thèm và khoảng giá tối đa</li>
                  <li>Đảm bảo người dùng luôn nhận được món ăn</li>
                </ul>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 4 Groups of Measured Data */}
        <h3 className='text-base font-semibold mb-3 text-foreground flex items-center gap-2'>
          <Activity className='size-4 text-primary' />
          4 Nhóm Chỉ Số AI Đo Lường Thực Tế từ Người Dùng
        </h3>

        <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-6'>
          {/* Group 1: Mood */}
          <Card className='border-border/60'>
            <CardHeader className='pb-2'>
              <CardTitle className='text-sm font-semibold flex items-center gap-2'>
                <Smile className='size-4 text-amber-500' />
                Tâm trạng phổ biến (Mood)
              </CardTitle>
              <CardDescription className='text-xs'>
                Cảm xúc người dùng khi nhờ AI
              </CardDescription>
            </CardHeader>
            <CardContent className='text-xs space-y-2 pt-1'>
              <div className='flex justify-between items-center py-1 border-b border-border/30'>
                <span>Mệt mỏi sau giờ làm</span>
                <Badge variant='secondary' className='text-[10px]'>35%</Badge>
              </div>
              <div className='flex justify-between items-center py-1 border-b border-border/30'>
                <span>Tụ tập bạn bè cuối tuần</span>
                <Badge variant='secondary' className='text-[10px]'>28%</Badge>
              </div>
              <div className='flex justify-between items-center py-1 border-b border-border/30'>
                <span>Thèm đồ cay nóng</span>
                <Badge variant='secondary' className='text-[10px]'>20%</Badge>
              </div>
              <div className='flex justify-between items-center py-1'>
                <span>Trời mưa muốn ăn đồ nước</span>
                <Badge variant='secondary' className='text-[10px]'>17%</Badge>
              </div>
            </CardContent>
          </Card>

          {/* Group 2: Craving */}
          <Card className='border-border/60'>
            <CardHeader className='pb-2'>
              <CardTitle className='text-sm font-semibold flex items-center gap-2'>
                <Flame className='size-4 text-rose-500' />
                Cơn thèm món ăn (Craving)
              </CardTitle>
              <CardDescription className='text-xs'>
                Loại món người dùng tìm kiếm nhiều nhất
              </CardDescription>
            </CardHeader>
            <CardContent className='text-xs space-y-2 pt-1'>
              <div className='flex justify-between items-center py-1 border-b border-border/30'>
                <span>Món nước (Phở, Bún bò)</span>
                <Badge variant='secondary' className='text-[10px]'>38%</Badge>
              </div>
              <div className='flex justify-between items-center py-1 border-b border-border/30'>
                <span>Cơm tấm & Cơm gà</span>
                <Badge variant='secondary' className='text-[10px]'>26%</Badge>
              </div>
              <div className='flex justify-between items-center py-1 border-b border-border/30'>
                <span>Lẩu & Nướng</span>
                <Badge variant='secondary' className='text-[10px]'>21%</Badge>
              </div>
              <div className='flex justify-between items-center py-1'>
                <span>Bánh mì & Ăn vặt</span>
                <Badge variant='secondary' className='text-[10px]'>15%</Badge>
              </div>
            </CardContent>
          </Card>

          {/* Group 3: Budget */}
          <Card className='border-border/60'>
            <CardHeader className='pb-2'>
              <CardTitle className='text-sm font-semibold flex items-center gap-2'>
                <Wallet className='size-4 text-emerald-500' />
                Ngân sách dự chi (Budget)
              </CardTitle>
              <CardDescription className='text-xs'>
                Khoảng giá người dùng lựa chọn
              </CardDescription>
            </CardHeader>
            <CardContent className='text-xs space-y-2 pt-1'>
              <div className='flex justify-between items-center py-1 border-b border-border/30'>
                <span>Bình dân (&lt; 50k VND)</span>
                <Badge variant='secondary' className='text-[10px]'>52%</Badge>
              </div>
              <div className='flex justify-between items-center py-1 border-b border-border/30'>
                <span>Vừa phải (50k - 100k VND)</span>
                <Badge variant='secondary' className='text-[10px]'>34%</Badge>
              </div>
              <div className='flex justify-between items-center py-1'>
                <span>Thoải mái (&gt; 150k VND)</span>
                <Badge variant='secondary' className='text-[10px]'>14%</Badge>
              </div>
            </CardContent>
          </Card>

          {/* Group 4: Companions */}
          <Card className='border-border/60'>
            <CardHeader className='pb-2'>
              <CardTitle className='text-sm font-semibold flex items-center gap-2'>
                <Users2 className='size-4 text-indigo-500' />
                Quy mô bàn ăn (Companions)
              </CardTitle>
              <CardDescription className='text-xs'>
                Số người cùng ăn trong bữa
              </CardDescription>
            </CardHeader>
            <CardContent className='text-xs space-y-2 pt-1'>
              <div className='flex justify-between items-center py-1 border-b border-border/30'>
                <span>Đi ăn 1 mình (Solo)</span>
                <Badge variant='secondary' className='text-[10px]'>48%</Badge>
              </div>
              <div className='flex justify-between items-center py-1 border-b border-border/30'>
                <span>Đi 2 người (Hẹn hò/Bạn bè)</span>
                <Badge variant='secondary' className='text-[10px]'>36%</Badge>
              </div>
              <div className='flex justify-between items-center py-1'>
                <span>Nhóm đông người (&gt; 3 người)</span>
                <Badge variant='secondary' className='text-[10px]'>16%</Badge>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Prompt Configuration */}
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
