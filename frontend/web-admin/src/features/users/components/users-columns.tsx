import { useState } from 'react'
import { type ColumnDef } from '@tanstack/react-table'
import { useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { CheckCircle2, Clock, Check, X, XCircle } from 'lucide-react'
import { Checkbox } from '@/components/ui/checkbox'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { DataTableColumnHeader } from '@/components/data-table'
import { LongText } from '@/components/long-text'
import { useAuthStore } from '@/stores/auth-store'
import { type User } from '../data/schema'
import { DataTableRowActions } from './data-table-row-actions'

function VerificationCell({ user }: { user: User }) {
  const [isRejectOpen, setIsRejectOpen] = useState(false)
  const [rejectionReason, setRejectionReason] = useState('Hồ sơ chưa đủ điều kiện tiêu chuẩn quán ăn')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const queryClient = useQueryClient()

  const handleVerify = async (approved: boolean, reason?: string) => {
    try {
      setIsSubmitting(true)
      const token = useAuthStore.getState().auth.accessToken
      const headers: Record<string, string> = { 'Content-Type': 'application/json' }
      if (token) headers['Authorization'] = `Bearer ${token}`

      const res = await fetch(`/api/v1/admin/users/${user.id}/verify`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          approved,
          reason: approved ? undefined : reason || 'Không đáp ứng đủ điều kiện tiêu chuẩn quán ăn',
        }),
      })

      if (!res.ok) {
        throw new Error('Lỗi cập nhật trạng thái tích xanh')
      }

      toast.success(
        approved
          ? `Đã phê duyệt tích xanh cho tài khoản @${user.username}`
          : `Đã gửi thông báo từ chối cấp tích xanh cho tài khoản @${user.username}`
      )
      setIsRejectOpen(false)
      queryClient.invalidateQueries({ queryKey: ['admin-users'] })
    } catch {
      toast.error('Thao tác thất bại, vui lòng thử lại sau')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (user.isVerified) {
    return (
      <Badge className='bg-sky-500/15 text-sky-500 border-sky-500/20 font-medium text-xs gap-1 py-0.5 px-2'>
        <CheckCircle2 className='size-3.5' />
        Đã có tích xanh
      </Badge>
    )
  }

  if (user.verificationStatus === 'Pending') {
    return (
      <div className='flex flex-col gap-1.5 items-start'>
        <Badge
          variant='outline'
          className='border-amber-500/30 bg-amber-500/10 text-amber-500 font-medium text-xs gap-1 py-0.5'
        >
          <Clock className='size-3 animate-pulse' />
          Chờ duyệt
        </Badge>
        <div className='flex items-center gap-1.5'>
          <Button
            size='sm'
            variant='outline'
            disabled={isSubmitting}
            onClick={() => handleVerify(true)}
            className='h-6 px-2 text-[11px] font-semibold border-emerald-500/40 text-emerald-600 hover:bg-emerald-500/10 hover:text-emerald-700'
          >
            <Check className='size-3 mr-1' />
            Duyệt
          </Button>
          <Button
            size='sm'
            variant='outline'
            disabled={isSubmitting}
            onClick={() => setIsRejectOpen(true)}
            className='h-6 px-2 text-[11px] font-semibold border-rose-500/40 text-rose-600 hover:bg-rose-500/10 hover:text-rose-700'
          >
            <X className='size-3 mr-1' />
            Từ chối
          </Button>
        </div>

        <Dialog open={isRejectOpen} onOpenChange={setIsRejectOpen}>
          <DialogContent className='sm:max-w-[425px]'>
            <DialogHeader>
              <DialogTitle>Từ chối cấp tích xanh</DialogTitle>
              <DialogDescription>
                Hệ thống sẽ gửi thông báo trực tiếp kèm lý do đến tài khoản @{user.username}.
              </DialogDescription>
            </DialogHeader>
            <div className='grid gap-3 py-2'>
              <Label htmlFor={`rejection-reason-${user.id}`}>Lý do từ chối</Label>
              <Textarea
                id={`rejection-reason-${user.id}`}
                value={rejectionReason}
                onChange={(e) => setRejectionReason(e.target.value)}
                placeholder='Nhập lý do không phê duyệt cấp tích xanh...'
                rows={3}
              />
            </div>
            <DialogFooter className='gap-2 sm:gap-0'>
              <Button
                type='button'
                variant='outline'
                onClick={() => setIsRejectOpen(false)}
              >
                Hủy
              </Button>
              <Button
                type='button'
                variant='destructive'
                disabled={isSubmitting}
                onClick={() => handleVerify(false, rejectionReason)}
              >
                {isSubmitting ? 'Đang gửi...' : 'Gửi thông báo từ chối'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    )
  }

  if (user.verificationStatus === 'Rejected') {
    return (
      <div className='flex flex-col gap-0.5 items-start max-w-[140px]'>
        <Badge
          variant='outline'
          className='border-rose-500/30 bg-rose-500/10 text-rose-500 font-normal text-xs gap-1'
        >
          <XCircle className='size-3' />
          Bị từ chối
        </Badge>
        {user.verificationRejectionReason && (
          <span
            className='text-[10px] text-muted-foreground line-clamp-1'
            title={user.verificationRejectionReason}
          >
            {user.verificationRejectionReason}
          </span>
        )}
      </div>
    )
  }

  return <span className='text-xs text-muted-foreground'>Chưa đăng ký</span>
}

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
    accessorKey: 'isVerified',
    id: 'verification',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='Tích xanh' />
    ),
    cell: ({ row }) => <VerificationCell user={row.original} />,
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
