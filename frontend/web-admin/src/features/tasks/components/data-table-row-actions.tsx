import { DotsHorizontalIcon } from '@radix-ui/react-icons'
import { type Row } from '@tanstack/react-table'
import { Eye, Pin, EyeOff, Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { taskSchema } from '../data/schema'
import { usePostMutations } from '../data/use-posts'
import { useTasks } from './tasks-provider'

type DataTableRowActionsProps<TData> = {
  row: Row<TData>
}

export function DataTableRowActions<TData>({
  row,
}: DataTableRowActionsProps<TData>) {
  const post = taskSchema.parse(row.original)
  const { setOpen, setCurrentRow } = useTasks()
  const { pinMutation, hideMutation } = usePostMutations()

  const postId = post.postId || parseInt(post.id.replace('POST-', '')) || 0
  const isPinned = post.isPinned || post.status === 'Pinned'
  const isHidden = post.status === 'Hidden'

  return (
    <DropdownMenu modal={false}>
      <DropdownMenuTrigger asChild>
        <Button
          variant='ghost'
          className='flex h-8 w-8 p-0 data-[state=open]:bg-muted'
        >
          <DotsHorizontalIcon className='h-4 w-4' />
          <span className='sr-only'>Mở menu</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end' className='w-48'>
        <DropdownMenuItem
          onClick={() => {
            setCurrentRow(post)
            setOpen('view')
          }}
        >
          <Eye className='mr-2 size-4 text-muted-foreground' />
          Xem chi tiết
        </DropdownMenuItem>

        <DropdownMenuItem
          disabled={pinMutation.isPending}
          onClick={() => {
            if (postId) {
              pinMutation.mutate({ postId, isPinned: !isPinned })
            }
          }}
        >
          <Pin className='mr-2 size-4 text-amber-500' />
          {isPinned ? 'Bỏ ghim bài' : 'Ghim bài lên đầu'}
        </DropdownMenuItem>

        <DropdownMenuItem
          disabled={hideMutation.isPending}
          onClick={() => {
            if (postId) {
              hideMutation.mutate({ postId, isHidden: !isHidden })
            }
          }}
        >
          {isHidden ? (
            <>
              <Eye className='mr-2 size-4 text-emerald-500' />
              Bỏ ẩn bài viết
            </>
          ) : (
            <>
              <EyeOff className='mr-2 size-4 text-rose-500' />
              Ẩn bài viết
            </>
          )}
        </DropdownMenuItem>

        <DropdownMenuSeparator />

        <DropdownMenuItem
          className='text-destructive focus:text-destructive'
          onClick={() => {
            setCurrentRow(post)
            setOpen('delete')
          }}
        >
          <Trash2 className='mr-2 size-4 text-destructive' />
          Xóa bài viết
          <DropdownMenuShortcut>⌫</DropdownMenuShortcut>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
