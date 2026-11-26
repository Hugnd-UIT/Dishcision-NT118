import { Heart, MessageCircle, Share2, Pin, CheckCircle2, EyeOff } from 'lucide-react'
import { ConfirmDialog } from '@/components/confirm-dialog'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { TasksImportDialog } from './tasks-import-dialog'
import { TasksMutateDrawer } from './tasks-mutate-drawer'
import { usePostMutations } from '../data/use-posts'
import { useTasks } from './tasks-provider'

export function TasksDialogs() {
  const { open, setOpen, currentRow, setCurrentRow } = useTasks()
  const { pinMutation, hideMutation, deleteMutation } = usePostMutations()

  const postId = currentRow?.postId || (currentRow?.id ? parseInt(currentRow.id.replace('POST-', '')) : 0)
  const isPinned = currentRow?.isPinned || currentRow?.status === 'Pinned'
  const isHidden = currentRow?.status === 'Hidden'

  return (
    <>
      <TasksMutateDrawer
        key='task-create'
        open={open === 'create'}
        onOpenChange={() => setOpen('create')}
      />

      <TasksImportDialog
        key='tasks-import'
        open={open === 'import'}
        onOpenChange={() => setOpen('import')}
      />

      {currentRow && (
        <>
          {/* View Post Detail Modal */}
          <Dialog
            open={open === 'view'}
            onOpenChange={(isOpen) => {
              if (!isOpen) {
                setOpen(null)
                setTimeout(() => setCurrentRow(null), 300)
              }
            }}
          >
            <DialogContent className='sm:max-w-lg'>
              <DialogHeader>
                <div className='flex items-center justify-between gap-2 pr-6'>
                  <span className='font-mono text-sm font-semibold text-primary'>
                    {currentRow.id}
                  </span>
                  <div className='flex items-center gap-1.5'>
                    {isPinned && (
                      <Badge className='bg-amber-500/15 text-amber-500 border-amber-500/20 text-xs font-normal'>
                        <Pin className='size-3 mr-1' /> Đã ghim
                      </Badge>
                    )}
                    {isHidden ? (
                      <Badge
                        variant='secondary'
                        className='bg-destructive/10 text-destructive border-destructive/20 text-xs font-normal'
                      >
                        <EyeOff className='size-3 mr-1' /> Đã ẩn
                      </Badge>
                    ) : (
                      <Badge
                        variant='secondary'
                        className='bg-emerald-500/15 text-emerald-500 border-emerald-500/20 text-xs font-normal'
                      >
                        <CheckCircle2 className='size-3 mr-1' /> Đang hiển thị
                      </Badge>
                    )}
                  </div>
                </div>
                <DialogTitle className='text-lg font-bold text-foreground mt-2'>
                  Chi tiết bài viết ẩm thực
                </DialogTitle>
                <DialogDescription>
                  Xem nội dung, hình ảnh và phản hồi từ cộng đồng Dishcision
                </DialogDescription>
              </DialogHeader>

              {/* Author info */}
              <div className='flex items-center gap-3 p-3 rounded-lg bg-muted/40 border border-border/50 my-2'>
                <Avatar className='h-10 w-10 border border-border'>
                  {currentRow.author?.avatarUrl && (
                    <AvatarImage
                      src={currentRow.author.avatarUrl}
                      alt={currentRow.author.fullName}
                    />
                  )}
                  <AvatarFallback className='text-xs font-semibold bg-primary/10 text-primary'>
                    {currentRow.author?.fullName?.[0] || 'U'}
                  </AvatarFallback>
                </Avatar>
                <div className='flex flex-col text-left'>
                  <span className='text-sm font-semibold text-foreground'>
                    {currentRow.author?.fullName || 'Người dùng'}
                  </span>
                  <span className='text-xs text-muted-foreground'>
                    @{currentRow.author?.username || 'user'} •{' '}
                    {new Date(currentRow.createdAt).toLocaleDateString('vi-VN')}
                  </span>
                </div>
              </div>

              {/* Post content */}
              <div className='space-y-3 py-2'>
                <p className='text-sm text-foreground leading-relaxed whitespace-pre-wrap bg-background p-3 rounded-md border border-border/40'>
                  {currentRow.content || currentRow.title}
                </p>

                {/* Attached images */}
                {currentRow.images && currentRow.images.length > 0 && (
                  <div className='grid grid-cols-2 gap-2 mt-2'>
                    {currentRow.images.map((img, idx) => (
                      <img
                        key={idx}
                        src={img}
                        alt={`Ảnh đính kèm ${idx + 1}`}
                        className='w-full h-36 object-cover rounded-md border border-border'
                      />
                    ))}
                  </div>
                )}

                {/* Engagement stats */}
                <div className='flex items-center justify-around p-2.5 rounded-md bg-muted/30 border border-border/40 text-xs font-medium text-muted-foreground'>
                  <span className='flex items-center gap-1.5 text-rose-500'>
                    <Heart className='size-4 fill-rose-500/20' /> {currentRow.likesCount} Yêu thích
                  </span>
                  <span className='flex items-center gap-1.5 text-sky-500'>
                    <MessageCircle className='size-4' /> {currentRow.commentsCount} Bình luận
                  </span>
                  <span className='flex items-center gap-1.5 text-indigo-500'>
                    <Share2 className='size-4' /> {currentRow.sharesCount} Chia sẻ
                  </span>
                </div>
              </div>

              {/* Quick actions */}
              <div className='flex items-center justify-end gap-2 pt-2 border-t border-border'>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => {
                    if (postId) {
                      pinMutation.mutate({ postId, isPinned: !isPinned })
                    }
                  }}
                >
                  <Pin className='size-3.5 mr-1 text-amber-500' />
                  {isPinned ? 'Bỏ ghim' : 'Ghim bài'}
                </Button>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => {
                    if (postId) {
                      hideMutation.mutate({ postId, isHidden: !isHidden })
                    }
                  }}
                >
                  {isHidden ? (
                    <>
                      <CheckCircle2 className='size-3.5 mr-1 text-emerald-500' /> Bỏ ẩn
                    </>
                  ) : (
                    <>
                      <EyeOff className='size-3.5 mr-1 text-rose-500' /> Ẩn bài
                    </>
                  )}
                </Button>
                <Button
                  size='sm'
                  onClick={() => {
                    setOpen(null)
                    setTimeout(() => setCurrentRow(null), 300)
                  }}
                >
                  Đóng
                </Button>
              </div>
            </DialogContent>
          </Dialog>

          {/* Edit Drawer */}
          <TasksMutateDrawer
            key={`task-update-${currentRow.id}`}
            open={open === 'update'}
            onOpenChange={() => {
              setOpen('update')
              setTimeout(() => {
                setCurrentRow(null)
              }, 500)
            }}
            currentRow={currentRow}
          />

          {/* Delete Confirmation */}
          <ConfirmDialog
            key='task-delete'
            destructive
            open={open === 'delete'}
            onOpenChange={() => {
              setOpen('delete')
              setTimeout(() => {
                setCurrentRow(null)
              }, 500)
            }}
            handleConfirm={() => {
              if (postId) {
                deleteMutation.mutate(postId)
              }
              setOpen(null)
              setTimeout(() => {
                setCurrentRow(null)
              }, 500)
            }}
            className='max-w-md'
            title={`Xác nhận xóa bài viết ${currentRow.id}?`}
            desc={
              <>
                Bạn sắp xóa vĩnh viễn bài viết của{' '}
                <strong>{currentRow.author?.fullName || 'Người dùng'}</strong>. <br />
                Hành động này sẽ xóa dữ liệu trên hệ thống và không thể hoàn tác.
              </>
            }
            confirmText='Xác nhận xóa'
          />
        </>
      )}
    </>
  )
}
