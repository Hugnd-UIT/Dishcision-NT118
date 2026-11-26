import { useState, useMemo } from 'react'
import {
  Search as SearchIcon,
  Copy,
  Check,
  Lock,
  Globe,
  Terminal,
  Layers,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { ThemeSwitch } from '@/components/theme-switch'
import { toast } from 'sonner'
import { apiEndpoints, apiCategories, ApiEndpoint } from './data/api-endpoints'

const methodStyles: Record<string, string> = {
  GET: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/30',
  POST: 'bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-500/30',
  PUT: 'bg-blue-500/15 text-blue-600 dark:text-blue-400 border-blue-500/30',
  PATCH: 'bg-purple-500/15 text-purple-600 dark:text-purple-400 border-purple-500/30',
  DELETE: 'bg-rose-500/15 text-rose-600 dark:text-rose-400 border-rose-500/30',
}

const BASE_URL = 'https://dishcision.duckdns.org/api/v1'

export function Apps() {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<string>('Tất cả')
  const [selectedMethod, setSelectedMethod] = useState<string>('ALL')
  const [copiedId, setCopiedId] = useState<string | null>(null)

  const filteredEndpoints = useMemo(() => {
    return apiEndpoints.filter((item) => {
      const matchCategory =
        selectedCategory === 'Tất cả' || item.category === selectedCategory
      const matchMethod =
        selectedMethod === 'ALL' || item.method === selectedMethod
      const term = searchTerm.toLowerCase().trim()
      const matchSearch =
        !term ||
        item.name.toLowerCase().includes(term) ||
        item.path.toLowerCase().includes(term) ||
        item.description.toLowerCase().includes(term) ||
        item.folder.toLowerCase().includes(term)

      return matchCategory && matchMethod && matchSearch
    })
  }, [searchTerm, selectedCategory, selectedMethod])

  const copyToClipboard = (text: string, id: string, label: string) => {
    navigator.clipboard.writeText(text)
    setCopiedId(id)
    toast.success(`Đã copy ${label}!`)
    setTimeout(() => setCopiedId(null), 2000)
  }

  const generateCurl = (endpoint: ApiEndpoint) => {
    const fullPath = endpoint.path.startsWith('/')
      ? endpoint.path
      : `/${endpoint.path}`
    const url = fullPath.startsWith('/health')
      ? 'https://dishcision.duckdns.org/health'
      : `${BASE_URL}${fullPath}`

    if (endpoint.auth === 'Public') {
      return `curl -X ${endpoint.method} "${url}"`
    }
    return `curl -X ${endpoint.method} "${url}" \\\n  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>" \\\n  -H "Content-Type: application/json"`
  }

  return (
    <>
      {/* ===== Top Heading ===== */}
      <Header>
        <Search className='me-auto' />
        <ThemeSwitch />
        <ProfileDropdown />
      </Header>

      {/* ===== Content ===== */}
      <Main>
        {/* Header Title */}
        <div className='mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <div>
            <h1 className='flex items-center gap-2 text-2xl font-bold tracking-tight'>
              <Terminal className='size-6 text-primary' />
              Quản lý API (Dishcision Postman Collection)
            </h1>
            <p className='text-sm text-muted-foreground mt-0.5'>
              Tập hợp đầy đủ {apiEndpoints.length} endpoint API Backend của hệ thống Dishcision v1.
            </p>
          </div>

          <div className='flex items-center gap-2'>
            <Badge variant='outline' className='gap-1.5 py-1 px-3 text-xs'>
              <span className='size-2 rounded-full bg-emerald-500 animate-pulse' />
              Live Server: <code className='font-mono text-primary'>{BASE_URL}</code>
            </Badge>
          </div>
        </div>

        {/* Filter & Search Bar */}
        <div className='mb-6 space-y-3'>
          <div className='flex flex-col gap-3 sm:flex-row sm:items-center'>
            <div className='relative flex-1'>
              <SearchIcon className='absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                placeholder='Tìm kiếm API (vd: /auth/login, post, AI decide, store...)'
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className='pl-9'
              />
            </div>

            <div className='flex items-center gap-2'>
              <Select value={selectedMethod} onValueChange={setSelectedMethod}>
                <SelectTrigger className='w-[130px]'>
                  <SelectValue placeholder='Method' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='ALL'>Tất cả Method</SelectItem>
                  <SelectItem value='GET'>GET</SelectItem>
                  <SelectItem value='POST'>POST</SelectItem>
                  <SelectItem value='PUT'>PUT</SelectItem>
                  <SelectItem value='PATCH'>PATCH</SelectItem>
                  <SelectItem value='DELETE'>DELETE</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Category Pills */}
          <div className='flex flex-wrap items-center gap-1.5 pt-1'>
            {apiCategories.map((cat) => {
              const count =
                cat === 'Tất cả'
                  ? apiEndpoints.length
                  : apiEndpoints.filter((i) => i.category === cat).length
              return (
                <Button
                  key={cat}
                  variant={selectedCategory === cat ? 'default' : 'outline'}
                  size='sm'
                  onClick={() => setSelectedCategory(cat)}
                  className='h-7 text-xs rounded-full px-3 gap-1.5'
                >
                  {cat}
                  <span
                    className={`text-[10px] px-1.5 py-0.2 rounded-full ${
                      selectedCategory === cat
                        ? 'bg-primary-foreground/20 text-primary-foreground'
                        : 'bg-muted text-muted-foreground'
                    }`}
                  >
                    {count}
                  </span>
                </Button>
              )
            })}
          </div>
        </div>

        {/* API Endpoint List */}
        <div className='space-y-3'>
          {filteredEndpoints.length > 0 ? (
            filteredEndpoints.map((ep) => {
              const fullPath = ep.path.startsWith('/') ? ep.path : `/${ep.path}`
              const isCopied = copiedId === ep.id

              return (
                <Card key={ep.id} className='transition-colors hover:border-primary/40 shadow-xs'>
                  <CardHeader className='p-4 pb-2'>
                    <div className='flex flex-wrap items-center justify-between gap-2'>
                      <div className='flex items-center gap-2.5 min-w-0'>
                        {/* Method Badge */}
                        <Badge
                          variant='outline'
                          className={`font-mono font-bold text-xs px-2 py-0.5 border ${
                            methodStyles[ep.method] || 'bg-muted'
                          }`}
                        >
                          {ep.method}
                        </Badge>

                        {/* Endpoint Path */}
                        <code className='font-mono font-semibold text-sm text-foreground break-all'>
                          {fullPath}
                        </code>

                        {/* Auth Badge */}
                        {ep.auth === 'Public' ? (
                          <Badge variant='secondary' className='gap-1 text-[11px] py-0 px-2'>
                            <Globe className='size-3 text-emerald-500' />
                            Public
                          </Badge>
                        ) : (
                          <Badge variant='outline' className='gap-1 text-[11px] py-0 px-2 text-muted-foreground'>
                            <Lock className='size-3 text-amber-500' />
                            Bearer Token
                          </Badge>
                        )}
                      </div>

                      {/* Quick Copy Buttons */}
                      <div className='flex items-center gap-1.5'>
                        <Button
                          variant='ghost'
                          size='sm'
                          onClick={() => copyToClipboard(generateCurl(ep), ep.id, 'cURL')}
                          className='h-7 px-2.5 text-xs text-muted-foreground hover:text-foreground gap-1'
                          title='Copy cURL command'
                        >
                          {isCopied ? (
                            <Check className='size-3.5 text-emerald-500' />
                          ) : (
                            <Copy className='size-3.5' />
                          )}
                          cURL
                        </Button>
                      </div>
                    </div>

                    <CardTitle className='text-sm font-medium mt-1 text-foreground/90'>
                      {ep.name}
                    </CardTitle>
                    {ep.description && (
                      <CardDescription className='text-xs line-clamp-2 mt-0.5'>
                        {ep.description}
                      </CardDescription>
                    )}
                  </CardHeader>
                  <CardContent className='px-4 pb-3 pt-0'>
                    <div className='flex items-center justify-between text-[11px] text-muted-foreground border-t pt-2 mt-2'>
                      <span className='flex items-center gap-1'>
                        <Layers className='size-3' />
                        Folder: {ep.folder}
                      </span>
                      <span className='font-mono text-[10px] text-muted-foreground/80'>
                        {ep.rawUrl}
                      </span>
                    </div>
                  </CardContent>
                </Card>
              )
            })
          ) : (
            <div className='p-12 text-center border rounded-lg border-dashed'>
              <Terminal className='size-10 mx-auto text-muted-foreground mb-3 opacity-40' />
              <p className='text-sm font-medium'>Không tìm thấy endpoint phù hợp</p>
              <p className='text-xs text-muted-foreground mt-1'>
                Thử thay đổi từ khóa tìm kiếm hoặc danh mục bộ lọc.
              </p>
            </div>
          )}
        </div>
      </Main>
    </>
  )
}
