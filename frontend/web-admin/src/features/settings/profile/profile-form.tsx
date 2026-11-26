import { z } from 'zod'
import { useFieldArray, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { showSubmittedData } from '@/lib/show-submitted-data'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'

const profileFormSchema = z.object({
  username: z
    .string('Vui lòng nhập tên người dùng.')
    .min(2, 'Tên người dùng phải có ít nhất 2 ký tự.')
    .max(30, 'Tên người dùng không được vượt quá 30 ký tự.'),
  email: z.email({
    error: (iss) =>
      iss.input === undefined
        ? 'Vui lòng chọn email hợp lệ.'
        : undefined,
  }),
  bio: z.string().max(160).min(4),
  urls: z
    .array(
      z.object({
        value: z.url('Vui lòng nhập đường dẫn hợp lệ.'),
      })
    )
    .optional(),
})

type ProfileFormValues = z.infer<typeof profileFormSchema>

const defaultValues: Partial<ProfileFormValues> = {
  username: 'admin',
  email: 'admin@dishcision.com',
  bio: 'Quản trị viên hệ thống Dishcision.',
  urls: [
    { value: 'https://dishcision.duckdns.org' },
  ],
}

export function ProfileForm() {
  const form = useForm<ProfileFormValues>({
    resolver: zodResolver(profileFormSchema),
    defaultValues,
    mode: 'onChange',
  })

  const { fields, append } = useFieldArray({
    name: 'urls',
    control: form.control,
  })

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit((data) => showSubmittedData(data))}
        className='space-y-8'
      >
        <FormField
          control={form.control}
          name='username'
          render={({ field }) => (
            <FormItem>
              <FormLabel>Tên người dùng</FormLabel>
              <FormControl>
                <Input placeholder='admin' {...field} />
              </FormControl>
              <FormDescription>
                Đây là tên hiển thị công khai của bạn trên hệ thống.
              </FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name='email'
          render={({ field }) => (
            <FormItem>
              <FormLabel>Email</FormLabel>
              <Select onValueChange={field.onChange} defaultValue={field.value}>
                <FormControl>
                  <SelectTrigger>
                    <SelectValue placeholder='Chọn email xác thực để hiển thị' />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  <SelectItem value='admin@dishcision.com'>admin@dishcision.com</SelectItem>
                  <SelectItem value='support@dishcision.com'>support@dishcision.com</SelectItem>
                </SelectContent>
              </Select>
              <FormDescription>
                Địa chỉ email liên kết với tài khoản quản trị.
              </FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name='bio'
          render={({ field }) => (
            <FormItem>
              <FormLabel>Tiểu sử</FormLabel>
              <FormControl>
                <Textarea
                  placeholder='Giới thiệu đôi nét về bản thân'
                  className='resize-none'
                  {...field}
                />
              </FormControl>
              <FormDescription>
                Mô tả ngắn gọn về vai trò hoặc thông tin cá nhân của bạn.
              </FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />
        <div>
          {fields.map((field, index) => (
            <FormField
              control={form.control}
              key={field.id}
              name={`urls.${index}.value`}
              render={({ field }) => (
                <FormItem>
                  <FormLabel className={cn(index !== 0 && 'sr-only')}>
                    Liên kết
                  </FormLabel>
                  <FormDescription className={cn(index !== 0 && 'sr-only')}>
                    Thêm liên kết đến trang web hoặc hồ sơ mạng xã hội của bạn.
                  </FormDescription>
                  <FormControl className={cn(index !== 0 && 'mt-1.5')}>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          ))}
          <Button
            type='button'
            variant='outline'
            size='sm'
            className='mt-2'
            onClick={() => append({ value: '' })}
          >
            Thêm liên kết
          </Button>
        </div>
        <Button type='submit'>Cập nhật hồ sơ</Button>
      </form>
    </Form>
  )
}
