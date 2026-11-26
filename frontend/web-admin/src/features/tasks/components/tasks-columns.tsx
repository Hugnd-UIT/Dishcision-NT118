import { type ColumnDef } from '@tanstack/react-table'
import { Heart, MessageCircle, Pin, CheckCircle2, EyeOff } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Checkbox } from '@/components/ui/checkbox'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { DataTableColumnHeader } from '@/components/data-table'
import { labels, priorities, statuses } from '../data/data'
import { type Post } from '../data/schema'
import { DataTableRowActions } from './data-table-row-actions'

function formatDate(dateStr: string | Date | undefined) {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return d.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return String(dateStr)
  }
}

export const tasksColumns: ColumnDef<Post>[] = [
  {
    id: 'select',
    header: ({ table }) => (
      <Checkbox
        checked={
          table.getIsAllPageRowsSelected() ||
          (table.getIsSomePageRowsSelected() && 'indeterminate')
        }
        onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
        aria-label='Chọn tất cả'
        className='translate-y-0.5'
      />
    ),
    cell: ({ row }) => (
      <Checkbox
        checked={row.getIsSelected()}
        onCheckedChange={(value) => row.toggleSelected(!!value)}
        aria-label='Chọn hàng'
        className='translate-y-0.5'
      />
    ),
    enableSorting: false,
    enableHiding: false,
  },
  {
    accessorKey: 'id',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Mã bài viết' />
    ),
    cell: ({ row }) => (
      <div className='font-mono text-xs font-semibold text-primary'>
        {row.getValue('id')}
      </div>
    ),
    enableSorting: false,
    enableHiding: false,
  },
  {
    accessorKey: 'author',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Tác giả' />
    ),
    cell: ({ row }) => {
      const author = row.original.author
      const initials = author?.fullName
        ? author.fullName
            .split(' ')
            .map((n) => n[0])
            .slice(0, 2)
            .join('')
            .toUpperCase()
        : 'US'

      return (
        <div className='flex items-center gap-2.5'>
          <Avatar className='h-8 w-8 rounded-full border border-border'>
            {author?.avatarUrl && (
              <AvatarImage src={author.avatarUrl} alt={author.fullName} />
            )}
            <AvatarFallback className='text-xs font-semibold bg-muted'>
              {initials}
            </AvatarFallback>
          </Avatar>
          <div className='flex flex-col text-left'>
            <span className='text-sm font-medium leading-none text-foreground'>
              {author?.fullName || 'Người dùng'}
            </span>
            <span className='text-xs text-muted-foreground mt-0.5'>
              @{author?.username || 'user'}
            </span>
          </div>
        </div>
      )
    },
  },
  {
    accessorKey: 'title',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Nội dung bài viết' />
    ),
    meta: {
      className: 'ps-1 max-w-md w-1/3',
      tdClassName: 'ps-3',
    },
    cell: ({ row }) => {
      const label = labels.find((l) => l.value === row.original.label)
      const post = row.original
      const firstImage = post.images && post.images.length > 0 ? post.images[0] : null

      return (
        <div className='flex items-center gap-3'>
          {firstImage ? (
            <img
              src={firstImage}
              alt='Ảnh bài viết'
              className='h-10 w-10 shrink-0 rounded-md object-cover border border-border'
              onError={(e) => {
                e.currentTarget.style.display = 'none'
              }}
            />
          ) : (
            <div className='h-10 w-10 shrink-0 rounded-md bg-muted/60 flex items-center justify-center text-muted-foreground text-xs'>
              🍲
            </div>
          )}
          <div className='flex flex-col gap-1 min-w-0'>
            <div className='flex items-center gap-1.5'>
              {post.isPinned && (
                <Badge
                  variant='secondary'
                  className='bg-amber-500/15 text-amber-500 border-amber-500/20 text-[10px] px-1.5 py-0'
                >
                  <Pin className='size-2.5 mr-0.5' /> Ghim
                </Badge>
              )}
              {label && (
                <Badge
                  variant='outline'
                  className='text-[10px] px-1.5 py-0 text-muted-foreground'
                >
                  {label.label}
                </Badge>
              )}
            </div>
            <p className='truncate text-sm font-medium text-foreground' title={post.content}>
              {post.content || post.title}
            </p>
          </div>
        </div>
      )
    },
  },
  {
    id: 'engagement',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Tương tác' />
    ),
    cell: ({ row }) => {
      const post = row.original
      return (
        <div className='flex items-center gap-3 text-xs text-muted-foreground'>
          <span className='flex items-center gap-1 text-rose-500'>
            <Heart className='size-3.5 fill-rose-500/20' />
            {post.likesCount}
          </span>
          <span className='flex items-center gap-1 text-sky-500'>
            <MessageCircle className='size-3.5' />
            {post.commentsCount}
          </span>
        </div>
      )
    },
  },
  {
    accessorKey: 'status',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Trạng thái' />
    ),
    meta: { className: 'ps-1', tdClassName: 'ps-2' },
    cell: ({ row }) => {
      const post = row.original

      if (post.isPinned || post.status === 'Pinned') {
        return (
          <Badge className='bg-amber-500/15 text-amber-500 hover:bg-amber-500/20 border-amber-500/30 gap-1 font-normal'>
            <Pin className='size-3' /> Đã ghim
          </Badge>
        )
      }

      if (post.status === 'Hidden') {
        return (
          <Badge
            variant='secondary'
            className='bg-destructive/10 text-destructive border-destructive/20 gap-1 font-normal'
          >
            <EyeOff className='size-3' /> Đã ẩn
          </Badge>
        )
      }

      return (
        <Badge
          variant='secondary'
          className='bg-emerald-500/15 text-emerald-500 border-emerald-500/20 gap-1 font-normal'
        >
          <CheckCircle2 className='size-3' /> Đang hiển thị
        </Badge>
      )
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id))
    },
  },
  {
    accessorKey: 'createdAt',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Ngày đăng' />
    ),
    cell: ({ row }) => (
      <div className='text-xs text-muted-foreground'>
        {formatDate(row.getValue('createdAt'))}
      </div>
    ),
  },
  {
    id: 'actions',
    cell: ({ row }) => <DataTableRowActions row={row} />,
  },
]
