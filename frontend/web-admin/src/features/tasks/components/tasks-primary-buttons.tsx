import { Download, Plus, RotateCw } from 'lucide-react'
import { useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { useTasks } from './tasks-provider'

export function TasksPrimaryButtons() {
  const { setOpen } = useTasks()
  const queryClient = useQueryClient()

  const handleRefresh = async () => {
    await queryClient.invalidateQueries({ queryKey: ['admin-posts'] })
    toast.success('Đã làm mới danh sách bài viết')
  }

  return (
    <div className='flex items-center gap-2'>
      <Button
        variant='outline'
        size='sm'
        className='space-x-1.5'
        onClick={handleRefresh}
      >
        <RotateCw size={16} />
        <span>Làm mới</span>
      </Button>
      <Button
        variant='outline'
        size='sm'
        className='space-x-1.5'
        onClick={() => setOpen('import')}
      >
        <Download size={16} />
        <span>Nhập / Xuất</span>
      </Button>
      <Button
        size='sm'
        className='space-x-1.5 bg-primary text-primary-foreground hover:bg-primary/90'
        onClick={() => setOpen('create')}
      >
        <Plus size={16} />
        <span>Thêm bài viết</span>
      </Button>
    </div>
  )
}
