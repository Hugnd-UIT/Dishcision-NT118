import { type ColumnDef } from '@tanstack/react-table'
import { Checkbox } from '@/components/ui/checkbox'
import { Badge } from '@/components/ui/badge'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { DataTableColumnHeader } from '@/components/data-table'
import { LongText } from '@/components/long-text'
import { type User } from '../data/schema'
import { DataTableRowActions } from './data-table-row-actions'

export const usersColumns: ColumnDef<User>[] = [
  {
    id: 'select',
    header: ({ table }) => (
      <Checkbox
        checked={
          table.getIsAllPageRowsSelected() ||
          (table.getIsSomePageRowsSelected() && 'indeterminate')
        }
        onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
        aria-label='Select all'
        className='translate-y-0.5'
      />
    ),
    cell: ({ row }) => (
      <Checkbox
        checked={row.getIsSelected()}
        onCheckedChange={(value) => row.toggleSelected(!!value)}
        aria-label='Select row'
        className='translate-y-0.5'
      />
    ),
    enableSorting: false,
    enableHiding: false,
  },
  {
    accessorKey: 'username',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Tài khoản' />
    ),
    cell: ({ row }) => {
      const user = row.original
      const initials = (user.firstName?.[0] || user.username?.[0] || 'U').toUpperCase()

      return (
        <div className='flex items-center gap-2.5'>
          <Avatar className='h-8 w-8 rounded-full border border-border'>
            {user.avatarUrl && (
              <AvatarImage src={user.avatarUrl} alt={user.username} />
            )}
            <AvatarFallback className='text-xs font-semibold bg-muted'>
              {initials}
            </AvatarFallback>
          </Avatar>
          <div className='flex flex-col text-left'>
            <span className='font-mono text-xs font-semibold text-primary'>
              @{user.username}
            </span>
            <span className='text-[10px] text-muted-foreground'>
              ID: #{user.id}
            </span>
          </div>
        </div>
      )
    },
    enableHiding: false,
  },
  {
    id: 'fullName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Họ và tên' />
    ),
    cell: ({ row }) => {
      const { firstName, lastName } = row.original
      const fullName = `${firstName} ${lastName}`.trim() || 'Thành viên'
      return <LongText className='max-w-36 font-medium text-sm'>{fullName}</LongText>
    },
    meta: { className: 'w-36' },
  },
  {
    accessorKey: 'email',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Email' />
    ),
    cell: ({ row }) => (
      <div className='w-fit text-xs text-muted-foreground'>{row.getValue('email')}</div>
    ),
  },
  {
    accessorKey: 'phoneNumber',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Khu vực / Tỉnh thành' />
    ),
    cell: ({ row }) => (
      <div className='text-xs text-muted-foreground'>
        {row.getValue('phoneNumber') || 'Việt Nam'}
      </div>
    ),
    enableSorting: false,
  },
  {
    accessorKey: 'role',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Vai trò' />
    ),
    cell: ({ row }) => {
      const role = String(row.getValue('role') || '').toLowerCase()

      if (role === 'admin' || role === 'superadmin') {
        return (
          <Badge className='bg-amber-500/15 text-amber-500 border-amber-500/20 font-normal text-xs'>
            Quản trị viên
          </Badge>
        )
      }

      if (role === 'manager') {
        return (
          <Badge className='bg-sky-500/15 text-sky-500 border-sky-500/20 font-normal text-xs'>
            Chủ quán ăn
          </Badge>
        )
      }

      return (
        <Badge
          variant='outline'
          className='text-muted-foreground border-border font-normal text-xs'
        >
          Thành viên
        </Badge>
      )
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id))
    },
    enableHiding: false,
    enableSorting: false,
  },
  {
    accessorKey: 'status',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Trạng thái' />
    ),
    cell: ({ row }) => {
      const status = String(row.getValue('status') || '').toLowerCase()

      if (status === 'active') {
        return (
          <Badge className='bg-emerald-500/15 text-emerald-500 border-emerald-500/20 font-normal text-xs'>
            Hoạt động
          </Badge>
        )
      }

      return (
        <Badge
          variant='secondary'
          className='bg-destructive/15 text-destructive border-destructive/20 font-normal text-xs'
        >
          Bị khóa
        </Badge>
      )
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id))
    },
    enableHiding: false,
    enableSorting: false,
  },
  {
    id: 'actions',
    cell: ({ row }) => <DataTableRowActions row={row} />,
  },
]
