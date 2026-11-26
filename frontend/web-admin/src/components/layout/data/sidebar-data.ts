import {
  LayoutDashboard,
  Monitor,
  ListTodo,
  Bell,
  Package,
  Palette,
  Settings,
  Wrench,
  UserCog,
  Users,
  Sparkles,
  ScrollText,
  Headphones,
} from 'lucide-react'
import { DishcisionLogo } from '@/assets/logo'
import { type SidebarData } from '../types'

export const sidebarData: SidebarData = {
  user: {
    name: 'Admin',
    email: 'admin@dishcision.com',
    avatar: '/images/dishcision-logo.png',
  },
  teams: [
    {
      name: 'Dishcision',
      logo: DishcisionLogo,
      plan: 'Bảng điều khiển quản trị',
    },
  ],
  navGroups: [
    {
      title: 'Chung',
      items: [
        {
          title: 'Tổng quan',
          url: '/',
          icon: LayoutDashboard,
        },
        {
          title: 'Bài viết',
          url: '/tasks',
          icon: ListTodo,
        },
        {
          title: 'Quản lý API',
          url: '/apps',
          icon: Package,
        },
        {
          title: 'Người dùng',
          url: '/users',
          icon: Users,
        },
        {
          title: 'Trí tuệ nhân tạo',
          url: '/ai',
          icon: Sparkles,
        },
        {
          title: 'Hỗ trợ',
          url: '/help-center',
          icon: Headphones,
        },
      ],
    },
    {
      title: 'Khác',
      items: [
        {
          title: 'Nhật ký hoạt động',
          url: '/audit-logs',
          icon: ScrollText,
        },
        {
          title: 'Cài đặt',
          icon: Settings,
          items: [
            {
              title: 'Hồ sơ',
              url: '/settings',
              icon: UserCog,
            },
            {
              title: 'Tài khoản',
              url: '/settings/account',
              icon: Wrench,
            },
            {
              title: 'Giao diện',
              url: '/settings/appearance',
              icon: Palette,
            },
            {
              title: 'Thông báo',
              url: '/settings/notifications',
              icon: Bell,
            },
            {
              title: 'Hiển thị',
              url: '/settings/display',
              icon: Monitor,
            },
          ],
        },
      ],
    },
  ],
}
