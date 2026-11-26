import { Sparkles, Bot, Brain, Zap, CheckCircle2, TrendingUp, Sliders, ShieldCheck } from 'lucide-react'
import { Button } from '@/components/ui/button'
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

export function AiFeature() {
  return (
    <>
      <Header>
        <Search />
        <ThemeSwitch />
        <ProfileDropdown />
      </Header>

      <Main>
        <div className='mb-6 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between'>
          <div>
            <h1 className='flex items-center gap-2 text-2xl font-bold tracking-tight'>
              <Sparkles className='size-6 text-primary' />
              Hệ Thống Trí Tuệ Nhân Tạo (Dishcision AI)
            </h1>
            <p className='text-sm text-muted-foreground'>
              Quản lý mô hình gợi ý món ăn thông minh, phân tích khẩu vị và tự động hóa dịch vụ ẩm thực.
            </p>
          </div>
          <div className='flex items-center gap-2'>
            <Badge variant='outline' className='gap-1 border-green-500/30 bg-green-500/10 text-green-600 dark:text-green-400'>
              <span className='size-2 rounded-full bg-green-500 animate-pulse' />
              Hệ thống AI đang hoạt động
            </Badge>
          </div>
        </div>

        {/* Overview Stats */}
        <div className='grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-6'>
          <Card>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-sm font-medium'>Lượt gợi ý món ăn hôm nay</CardTitle>
              <Zap className='size-4 text-primary' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold'>12,840</div>
              <p className='text-xs text-muted-foreground flex items-center gap-1 mt-1 text-green-600'>
                <TrendingUp className='size-3' /> +18.2% so với hôm qua
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-sm font-medium'>Độ chính xác gợi ý</CardTitle>
              <CheckCircle2 className='size-4 text-green-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold'>98.6%</div>
              <p className='text-xs text-muted-foreground mt-1'>Dựa trên đánh giá người dùng</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-sm font-medium'>Thời gian phản hồi TB</CardTitle>
              <Brain className='size-4 text-blue-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold'>138 ms</div>
              <p className='text-xs text-muted-foreground mt-1'>Tối ưu hóa độ trễ thấp</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className='flex flex-row items-center justify-between pb-2'>
              <CardTitle className='text-sm font-medium'>Vi phạm đã chặn tự động</CardTitle>
              <ShieldCheck className='size-4 text-amber-500' />
            </CardHeader>
            <CardContent>
              <div className='text-2xl font-bold'>342</div>
              <p className='text-xs text-muted-foreground mt-1'>Bộ lọc an toàn nội dung</p>
            </CardContent>
          </Card>
        </div>

        {/* AI Services List */}
        <div className='grid gap-6 md:grid-cols-2 lg:grid-cols-3'>
          <Card className='border-primary/20 shadow-xs'>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <div className='p-2 rounded-lg bg-primary/10 text-primary'>
                    <Bot className='size-5' />
                  </div>
                  <CardTitle className='text-base'>Smart Decision Engine</CardTitle>
                </div>
                <Badge variant='secondary' className='text-xs'>Active</Badge>
              </div>
              <CardDescription className='mt-2'>
                Mô hình phân tích sở thích, khoảng cách vị trí và ngân sách để đưa ra quyết định món ăn trong 3 giây.
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-2 text-xs text-muted-foreground border-t pt-4'>
              <div className='flex justify-between'>
                <span>Mô hình:</span>
                <span className='font-medium text-foreground'>Gemini Flash 2.0 / DishDecision-v2</span>
              </div>
              <div className='flex justify-between'>
                <span>Tỷ lệ chấp thuận:</span>
                <span className='font-medium text-green-600'>94.2%</span>
              </div>
            </CardContent>
          </Card>

          <Card className='border-primary/20 shadow-xs'>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <div className='p-2 rounded-lg bg-blue-500/10 text-blue-500'>
                    <Brain className='size-5' />
                  </div>
                  <CardTitle className='text-base'>Khẩu Vị & Cá Nhân Hóa</CardTitle>
                </div>
                <Badge variant='secondary' className='text-xs'>Active</Badge>
              </div>
              <CardDescription className='mt-2'>
                Học thói quen ăn uống, dị ứng thực phẩm và xu hướng đánh giá để cá nhân hóa danh sách gợi ý quán ăn.
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-2 text-xs text-muted-foreground border-t pt-4'>
              <div className='flex justify-between'>
                <span>Vector Embedding:</span>
                <span className='font-medium text-foreground'>OpenAI text-embedding-3-small</span>
              </div>
              <div className='flex justify-between'>
                <span>Cập nhật hồ sơ:</span>
                <span className='font-medium text-foreground'>Thời gian thực (Real-time)</span>
              </div>
            </CardContent>
          </Card>

          <Card className='border-primary/20 shadow-xs'>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <div className='p-2 rounded-lg bg-emerald-500/10 text-emerald-500'>
                    <ShieldCheck className='size-5' />
                  </div>
                  <CardTitle className='text-base'>Kiểm Duyệt Nội Dung & Ảnh</CardTitle>
                </div>
                <Badge variant='secondary' className='text-xs'>Active</Badge>
              </div>
              <CardDescription className='mt-2'>
                Tự động quét và phát hiện hình ảnh không phù hợp, bình luận spam hoặc đánh giá giả mạo.
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-2 text-xs text-muted-foreground border-t pt-4'>
              <div className='flex justify-between'>
                <span>Bộ lọc an toàn:</span>
                <span className='font-medium text-foreground'>Google Vision & Text Safety</span>
              </div>
              <div className='flex justify-between'>
                <span>Tự động chặn:</span>
                <span className='font-medium text-green-600'>Bật (Enabled)</span>
              </div>
            </CardContent>
          </Card>
        </div>
      </Main>
    </>
  )
}
