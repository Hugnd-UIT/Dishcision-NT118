import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet'
import { SelectDropdown } from '@/components/select-dropdown'
import { type Post } from '../data/schema'

type TaskMutateDrawerProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  currentRow?: Post
}

const formSchema = z.object({
  title: z.string().min(1, 'Tiêu đề không được để trống.'),
  status: z.string().min(1, 'Vui lòng chọn trạng thái.'),
  label: z.string().min(1, 'Vui lòng chọn thể loại.'),
  priority: z.string().min(1, 'Vui lòng chọn mức độ ưu tiên.'),
})
type TaskForm = z.infer<typeof formSchema>

export function TasksMutateDrawer({
  open,
  onOpenChange,
  currentRow,
}: TaskMutateDrawerProps) {
  const isUpdate = !!currentRow

  const form = useForm<TaskForm>({
    resolver: zodResolver(formSchema),
    defaultValues: currentRow
      ? {
          title: currentRow.title || currentRow.content || '',
          status: currentRow.status || 'Active',
          label: currentRow.label || 'review',
          priority: currentRow.priority || 'latest',
        }
      : {
          title: '',
          status: 'Active',
          label: 'review',
          priority: 'latest',
        },
  })

  const onSubmit = (data: TaskForm) => {
    onOpenChange(false)
    form.reset()
    toast.success(
      isUpdate
        ? `Đã cập nhật bài viết: "${data.title}"`
        : `Đã tạo bài viết mới: "${data.title}"`
    )
  }

  return (
    <Sheet
      open={open}
      onOpenChange={(v) => {
        onOpenChange(v)
        form.reset()
      }}
    >
      <SheetContent className='flex flex-col'>
        <SheetHeader className='text-start'>
          <SheetTitle>
            {isUpdate ? 'Cập nhật bài viết' : 'Thêm bài viết mới'}
          </SheetTitle>
          <SheetDescription>
            {isUpdate
              ? 'Chỉnh sửa thông tin và trạng thái bài viết cộng đồng.'
              : 'Tạo bài viết chia sẻ ẩm thực mới trên Dishcision.'}
          </SheetDescription>
        </SheetHeader>
        <Form {...form}>
          <form
            id='tasks-form'
            onSubmit={form.handleSubmit(onSubmit)}
            className='flex-1 space-y-5 overflow-y-auto px-4 py-2'
          >
            <FormField
              control={form.control}
              name='title'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Tiêu đề / Nội dung tóm tắt</FormLabel>
                  <FormControl>
                    <Input
                      {...field}
                      placeholder='Nhập tiêu đề hoặc trích dẫn bài viết...'
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name='status'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Trạng thái hiển thị</FormLabel>
                  <SelectDropdown
                    defaultValue={field.value}
                    onValueChange={field.onChange}
                    placeholder='Chọn trạng thái'
                    items={[
                      { label: 'Đang hiển thị', value: 'Active' },
                      { label: 'Đã ghim nổi bật', value: 'Pinned' },
                      { label: 'Đã ẩn / Vi phạm', value: 'Hidden' },
                    ]}
                  />
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name='label'
              render={({ field }) => (
                <FormItem className='relative'>
                  <FormLabel>Chuyên mục ẩm thực</FormLabel>
                  <FormControl>
                    <RadioGroup
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                      className='flex flex-col space-y-1'
                    >
                      <FormItem className='flex items-center'>
                        <FormControl>
                          <RadioGroupItem value='review' />
                        </FormControl>
                        <FormLabel className='font-normal ml-2'>
                          Review quán ăn
                        </FormLabel>
                      </FormItem>
                      <FormItem className='flex items-center'>
                        <FormControl>
                          <RadioGroupItem value='recipe' />
                        </FormControl>
                        <FormLabel className='font-normal ml-2'>
                          Công thức nấu ăn
                        </FormLabel>
                      </FormItem>
                      <FormItem className='flex items-center'>
                        <FormControl>
                          <RadioGroupItem value='checkin' />
                        </FormControl>
                        <FormLabel className='font-normal ml-2'>
                          Check-in món ngon
                        </FormLabel>
                      </FormItem>
                      <FormItem className='flex items-center'>
                        <FormControl>
                          <RadioGroupItem value='qa' />
                        </FormControl>
                        <FormLabel className='font-normal ml-2'>
                          Hỏi đáp ẩm thực
                        </FormLabel>
                      </FormItem>
                    </RadioGroup>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name='priority'
              render={({ field }) => (
                <FormItem className='relative'>
                  <FormLabel>Mức độ ưu tiên</FormLabel>
                  <FormControl>
                    <RadioGroup
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                      className='flex flex-col space-y-1'
                    >
                      <FormItem className='flex items-center'>
                        <FormControl>
                          <RadioGroupItem value='featured' />
                        </FormControl>
                        <FormLabel className='font-normal ml-2'>
                          Nổi bật
                        </FormLabel>
                      </FormItem>
                      <FormItem className='flex items-center'>
                        <FormControl>
                          <RadioGroupItem value='trending' />
                        </FormControl>
                        <FormLabel className='font-normal ml-2'>
                          Phổ biến
                        </FormLabel>
                      </FormItem>
                      <FormItem className='flex items-center'>
                        <FormControl>
                          <RadioGroupItem value='latest' />
                        </FormControl>
                        <FormLabel className='font-normal ml-2'>
                          Mới nhất
                        </FormLabel>
                      </FormItem>
                    </RadioGroup>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          </form>
        </Form>
        <SheetFooter className='gap-2'>
          <SheetClose asChild>
            <Button variant='outline'>Hủy</Button>
          </SheetClose>
          <Button form='tasks-form' type='submit'>
            {isUpdate ? 'Lưu thay đổi' : 'Tạo bài viết'}
          </Button>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  )
}
