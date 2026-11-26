import { useState, useMemo } from 'react'
import {
  Search as SearchIcon,
  Copy,
  Check,
  Lock,
  Globe,
  Terminal,
  Layers,
  Send,
  Code2,
  Clock,
  Sparkles,
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
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
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

  // Interactive Test Dialog state
  const [selectedApi, setSelectedApi] = useState<ApiEndpoint | null>(null)
  const [requestBody, setRequestBody] = useState<string>('')
  const [authToken, setAuthToken] = useState<string>('')
  const [isLoading, setIsLoading] = useState<boolean>(false)
  const [apiResponse, setApiResponse] = useState<{
    status: number
    statusText: string
    time: number
    data: any
  } | null>(null)

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

  const getFullUrl = (endpoint: ApiEndpoint) => {
    const fullPath = endpoint.path.startsWith('/')
      ? endpoint.path
      : `/${endpoint.path}`
    return fullPath.startsWith('/health')
      ? 'https://dishcision.duckdns.org/health'
      : `${BASE_URL}${fullPath}`
  }

  const generateCurl = (endpoint: ApiEndpoint, customBody?: string) => {
    const url = getFullUrl(endpoint)
    const bodyContent = customBody ?? endpoint.body

    let curl = `curl -X ${endpoint.method} "${url}"`
    if (endpoint.auth === 'Bearer Token') {
      curl += ` \\\n  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>"`
    }
    if (bodyContent && ['POST', 'PUT', 'PATCH'].includes(endpoint.method)) {
      curl += ` \\\n  -H "Content-Type: application/json"`
      curl += ` \\\n  -d '${bodyContent.replace(/'/g, "\\'")}'`
    }
    return curl
  }

  const handleOpenDetail = (api: ApiEndpoint) => {
    setSelectedApi(api)
    setRequestBody(api.body ? api.body.trim() : '')
    setApiResponse(null)
  }

  const handleSendRequest = async () => {
    if (!selectedApi) return

    setIsLoading(true)
    setApiResponse(null)
    const startTime = performance.now()

    const url = getFullUrl(selectedApi)
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    }

    if (authToken.trim()) {
      headers['Authorization'] = `Bearer ${authToken.trim()}`
    }

    try {
      const options: RequestInit = {
        method: selectedApi.method,
        headers,
      }

      if (
        requestBody &&
        ['POST', 'PUT', 'PATCH'].includes(selectedApi.method)
      ) {
        options.body = requestBody
      }

      const res = await fetch(url, options)
      const endTime = performance.now()
      const time = Math.round(endTime - startTime)

      let data: any
      const text = await res.text()
      try {
        data = JSON.parse(text)
      } catch {
        data = text
      }

      setApiResponse({
        status: res.status,
        statusText: res.statusText || (res.ok ? 'OK' : 'Error'),
        time,
        data,
      })

      if (res.ok) {
        toast.success(`Request thành công! (${res.status} OK - ${time}ms)`)
      } else {
        toast.error(`Request trả về lỗi ${res.status}: ${res.statusText}`)
      }
    } catch (err: any) {
      const endTime = performance.now()
      setApiResponse({
        status: 0,
        statusText: 'Network / CORS Error',
        time: Math.round(endTime - startTime),
        data: {
          error: err.message || 'Không thể kết nối tới máy chủ.',
          hint: 'Vui lòng kiểm tra kết nối mạng hoặc thử lại.',
        },
      })
      toast.error(err.message || 'Lỗi gửi request')
    } finally {
      setIsLoading(false)
    }
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
              Quản lý API
            </h1>
            <p className='text-sm text-muted-foreground mt-0.5'>
              Tập hợp đầy đủ {apiEndpoints.length} endpoint API Backend của hệ thống Dishcision v1. Bấm vào bất kỳ endpoint nào để xem chi tiết hoặc chạy thử trực tiếp.
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
                placeholder='Tìm kiếm API...'
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

          {/* Category Filter Pills */}
          <div className='flex flex-wrap gap-1.5'>
            {apiCategories.map((cat) => {
              const isSelected = selectedCategory === cat
              const count =
                cat === 'Tất cả'
                  ? apiEndpoints.length
                  : apiEndpoints.filter((e) => e.category === cat).length

              return (
                <button
                  key={cat}
                  onClick={() => setSelectedCategory(cat)}
                  className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-medium transition-all ${
                    isSelected
                      ? 'bg-primary text-primary-foreground shadow-sm'
                      : 'bg-muted/70 text-muted-foreground hover:bg-muted hover:text-foreground'
                  }`}
                >
                  {cat}
                  <span
                    className={`rounded-full px-1.5 py-0.2 text-[10px] ${
                      isSelected
                        ? 'bg-primary-foreground/20 text-primary-foreground'
                        : 'bg-background/80 text-muted-foreground'
                    }`}
                  >
                    {count}
                  </span>
                </button>
              )
            })}
          </div>
        </div>

        {/* Results Stats */}
        <div className='mb-3 flex items-center justify-between text-xs text-muted-foreground'>
          <span>
            Hiển thị <strong>{filteredEndpoints.length}</strong> / {apiEndpoints.length} API
          </span>
          <span className='italic'>💡 Mẹo: Bấm vào thẻ API để xem request, headers và test trực tiếp</span>
        </div>

        {/* API Cards Grid */}
        <div className='grid grid-cols-1 gap-3.5'>
          {filteredEndpoints.map((item) => {
            const curlCmd = generateCurl(item)

            return (
              <Card
                key={item.id}
                onClick={() => handleOpenDetail(item)}
                className='cursor-pointer border-border/60 hover:border-primary/60 hover:shadow-md transition-all duration-150 active:scale-[0.995] group'
              >
                <CardHeader className='p-4 pb-2'>
                  <div className='flex flex-wrap items-center justify-between gap-2'>
                    <div className='flex flex-wrap items-center gap-2'>
                      {/* Method Badge */}
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded text-xs font-bold border font-mono tracking-wider ${
                          methodStyles[item.method] || 'bg-muted text-muted-foreground'
                        }`}
                      >
                        {item.method}
                      </span>

                      {/* Path */}
                      <code className='font-mono text-sm font-semibold text-foreground group-hover:text-primary transition-colors'>
                        {item.path}
                      </code>

                      {/* Auth requirement badge */}
                      {item.auth === 'Public' ? (
                        <Badge
                          variant='secondary'
                          className='bg-emerald-500/10 text-emerald-500 border-emerald-500/20 text-[11px] gap-1 py-0'
                        >
                          <Globe className='size-2.5' /> Public
                        </Badge>
                      ) : (
                        <Badge
                          variant='secondary'
                          className='bg-amber-500/10 text-amber-500 border-amber-500/20 text-[11px] gap-1 py-0'
                        >
                          <Lock className='size-2.5' /> Bearer Token
                        </Badge>
                      )}
                    </div>

                    {/* Actions */}
                    <div className='flex items-center gap-1.5' onClick={(e) => e.stopPropagation()}>
                      <Button
                        variant='ghost'
                        size='sm'
                        className='h-7 px-2 text-xs text-muted-foreground hover:text-foreground'
                        onClick={() => copyToClipboard(curlCmd, item.id + '-curl', 'lệnh cURL')}
                      >
                        {copiedId === item.id + '-curl' ? (
                          <>
                            <Check className='size-3 mr-1 text-emerald-500' />
                            Đã chép
                          </>
                        ) : (
                          <>
                            <Copy className='size-3 mr-1' />
                            cURL
                          </>
                        )}
                      </Button>
                      <Button
                        variant='outline'
                        size='sm'
                        className='h-7 px-2.5 text-xs text-primary group-hover:bg-primary group-hover:text-primary-foreground transition-colors'
                        onClick={() => handleOpenDetail(item)}
                      >
                        Chi tiết & Test
                      </Button>
                    </div>
                  </div>

                  <CardTitle className='text-base font-semibold mt-1 group-hover:text-primary transition-colors'>
                    {item.name}
                  </CardTitle>
                </CardHeader>

                <CardContent className='p-4 pt-1'>
                  {item.description && (
                    <CardDescription className='text-xs text-muted-foreground line-clamp-2 leading-relaxed'>
                      {item.description}
                    </CardDescription>
                  )}

                  <div className='mt-2.5 flex flex-wrap items-center justify-between border-t border-border/40 pt-2 text-[11px] text-muted-foreground'>
                    <span className='flex items-center gap-1'>
                      <Layers className='size-3' />
                      Folder: <strong className='text-foreground'>{item.folder}</strong>
                    </span>
                    <span className='font-mono text-[10px] text-muted-foreground/80'>
                      {item.rawUrl}
                    </span>
                  </div>
                </CardContent>
              </Card>
            )
          })}
        </div>

        {/* Empty State */}
        {filteredEndpoints.length === 0 && (
          <div className='rounded-lg border border-dashed border-border p-12 text-center'>
            <Terminal className='mx-auto size-10 text-muted-foreground' />
            <h3 className='mt-3 text-lg font-semibold'>Không tìm thấy API nào</h3>
            <p className='mt-1 text-sm text-muted-foreground'>
              Thử tìm kiếm với từ khóa khác hoặc đổi danh mục / HTTP method.
            </p>
          </div>
        )}
      </Main>

      {/* ===== Interactive API Detail & Test Dialog ===== */}
      {selectedApi && (
        <Dialog open={!!selectedApi} onOpenChange={(open) => !open && setSelectedApi(null)}>
          <DialogContent className='max-w-2xl max-h-[85vh] overflow-y-auto'>
            <DialogHeader>
              <div className='flex items-center gap-2 pr-6'>
                <span
                  className={`inline-flex items-center px-2.5 py-0.5 rounded text-xs font-bold border font-mono tracking-wider ${
                    methodStyles[selectedApi.method] || 'bg-muted text-muted-foreground'
                  }`}
                >
                  {selectedApi.method}
                </span>
                <code className='font-mono text-sm font-semibold text-primary'>
                  {selectedApi.path}
                </code>
                {selectedApi.auth === 'Public' ? (
                  <Badge variant='secondary' className='text-[10px] bg-emerald-500/10 text-emerald-500 border-emerald-500/20'>
                    Public
                  </Badge>
                ) : (
                  <Badge variant='secondary' className='text-[10px] bg-amber-500/10 text-amber-500 border-amber-500/20'>
                    Bearer Token
                  </Badge>
                )}
              </div>
              <DialogTitle className='text-lg font-bold text-foreground mt-2'>
                {selectedApi.name}
              </DialogTitle>
              <DialogDescription>
                {selectedApi.description || 'Endpoint API của hệ thống Dishcision.'}
              </DialogDescription>
            </DialogHeader>

            {/* URL Display */}
            <div className='flex items-center gap-2 rounded-md bg-muted/50 p-2.5 border border-border/60 text-xs font-mono'>
              <span className='truncate flex-1 text-foreground'>{getFullUrl(selectedApi)}</span>
              <Button
                variant='ghost'
                size='sm'
                className='h-6 px-2'
                onClick={() => copyToClipboard(getFullUrl(selectedApi), 'modal-url', 'URL')}
              >
                <Copy className='size-3 mr-1' /> Copy URL
              </Button>
            </div>

            {/* Tabs */}
            <Tabs defaultValue='request' className='mt-2'>
              <TabsList className='grid grid-cols-3 w-full'>
                <TabsTrigger value='request'>Request & Body</TabsTrigger>
                <TabsTrigger value='response'>
                  Response {apiResponse ? `(${apiResponse.status})` : ''}
                </TabsTrigger>
                <TabsTrigger value='curl'>cURL Preview</TabsTrigger>
              </TabsList>

              {/* Request Tab */}
              <TabsContent value='request' className='space-y-3 pt-2'>
                {selectedApi.auth === 'Bearer Token' && (
                  <div>
                    <label className='text-xs font-medium text-muted-foreground block mb-1'>
                      Mã xác thực Bearer Token:
                    </label>
                    <Input
                      placeholder='Dán accessToken vào đây để test API có xác thực...'
                      value={authToken}
                      onChange={(e) => setAuthToken(e.target.value)}
                      className='text-xs font-mono'
                    />
                  </div>
                )}

                {['POST', 'PUT', 'PATCH'].includes(selectedApi.method) && (
                  <div>
                    <div className='flex items-center justify-between mb-1'>
                      <label className='text-xs font-medium text-muted-foreground'>
                        Nội dung yêu cầu:
                      </label>
                      <Button
                        variant='ghost'
                        size='sm'
                        className='h-5 px-2 text-[10px]'
                        onClick={() => {
                          try {
                            const parsed = JSON.parse(requestBody)
                            setRequestBody(JSON.stringify(parsed, null, 2))
                            toast.success('Đã định dạng JSON')
                          } catch {
                            toast.error('JSON không hợp lệ')
                          }
                        }}
                      >
                        <Sparkles className='size-2.5 mr-1' /> Format JSON
                      </Button>
                    </div>
                    <textarea
                      rows={6}
                      value={requestBody}
                      onChange={(e) => setRequestBody(e.target.value)}
                      placeholder='{\n  "key": "value"\n}'
                      className='w-full rounded-md border border-border bg-background p-3 font-mono text-xs text-foreground focus:outline-none focus:ring-1 focus:ring-primary leading-relaxed'
                    />
                  </div>
                )}

                <div className='flex items-center justify-between pt-2'>
                  <span className='text-xs text-muted-foreground'>
                    Folder: <strong>{selectedApi.folder}</strong>
                  </span>
                  <Button
                    onClick={handleSendRequest}
                    disabled={isLoading}
                    className='bg-primary text-primary-foreground hover:bg-primary/90 gap-1.5'
                  >
                    <Send className='size-3.5' />
                    {isLoading ? 'Đang gửi...' : 'Gửi yêu cầu'}
                  </Button>
                </div>
              </TabsContent>

              {/* Response Tab */}
              <TabsContent value='response' className='space-y-3 pt-2'>
                {apiResponse ? (
                  <div>
                    <div className='flex items-center justify-between mb-2 p-2 rounded bg-muted/40 border border-border/50 text-xs'>
                      <div className='flex items-center gap-2'>
                        <span
                          className={`font-bold px-2 py-0.5 rounded text-[11px] ${
                            apiResponse.status >= 200 && apiResponse.status < 300
                              ? 'bg-emerald-500/20 text-emerald-500'
                              : 'bg-rose-500/20 text-rose-500'
                          }`}
                        >
                          {apiResponse.status} {apiResponse.statusText}
                        </span>
                        <span className='flex items-center gap-1 text-muted-foreground'>
                          <Clock className='size-3' /> {apiResponse.time} ms
                        </span>
                      </div>
                      <Button
                        variant='ghost'
                        size='sm'
                        className='h-6 text-xs'
                        onClick={() =>
                          copyToClipboard(
                            JSON.stringify(apiResponse.data, null, 2),
                            'res-data',
                            'kết quả'
                          )
                        }
                      >
                        <Copy className='size-3 mr-1' /> Copy
                      </Button>
                    </div>

                    <pre className='max-h-64 overflow-y-auto rounded-md bg-zinc-950 p-3 font-mono text-xs text-zinc-200 border border-border leading-relaxed'>
                      {typeof apiResponse.data === 'string'
                        ? apiResponse.data
                        : JSON.stringify(apiResponse.data, null, 2)}
                    </pre>
                  </div>
                ) : (
                  <div className='p-8 text-center text-muted-foreground border border-dashed rounded-md my-2'>
                    <Code2 className='mx-auto size-8 mb-2 opacity-50' />
                    <p className='text-xs'>
                      Chưa có kết quả phản hồi. Nhấn <strong>"Gửi Request"</strong> ở tab Request để chạy thử nghiệm API.
                    </p>
                  </div>
                )}
              </TabsContent>

              {/* cURL Tab */}
              <TabsContent value='curl' className='space-y-2 pt-2'>
                <div className='relative'>
                  <pre className='max-h-64 overflow-y-auto rounded-md bg-zinc-950 p-3 font-mono text-xs text-zinc-200 border border-border leading-relaxed whitespace-pre-wrap'>
                    {generateCurl(selectedApi, requestBody)}
                  </pre>
                  <Button
                    variant='outline'
                    size='sm'
                    className='absolute top-2 right-2 h-7 text-xs bg-zinc-900 border-zinc-700 hover:bg-zinc-800'
                    onClick={() =>
                      copyToClipboard(
                        generateCurl(selectedApi, requestBody),
                        'modal-curl',
                        'lệnh cURL'
                      )
                    }
                  >
                    <Copy className='size-3 mr-1' /> Copy cURL
                  </Button>
                </div>
              </TabsContent>
            </Tabs>
          </DialogContent>
        </Dialog>
      )}
    </>
  )
}
