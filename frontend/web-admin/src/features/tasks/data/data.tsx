import {
  CheckCircle2,
  Pin,
  EyeOff,
  Flame,
  Sparkles,
  Clock,
  Utensils,
  ChefHat,
  Camera,
  MessageSquare,
} from 'lucide-react'

export const labels = [
  {
    value: 'review',
    label: 'Review quán',
    icon: Utensils,
  },
  {
    value: 'recipe',
    label: 'Công thức nấu',
    icon: ChefHat,
  },
  {
    value: 'checkin',
    label: 'Check-in món',
    icon: Camera,
  },
  {
    value: 'qa',
    label: 'Hỏi đáp ẩm thực',
    icon: MessageSquare,
  },
]

export const statuses = [
  {
    label: 'Đang hiển thị',
    value: 'Active' as const,
    icon: CheckCircle2,
  },
  {
    label: 'Đã ghim',
    value: 'Pinned' as const,
    icon: Pin,
  },
  {
    label: 'Đã ẩn',
    value: 'Hidden' as const,
    icon: EyeOff,
  },
]

export const priorities = [
  {
    label: 'Nổi bật',
    value: 'featured' as const,
    icon: Flame,
  },
  {
    label: 'Phổ biến',
    value: 'trending' as const,
    icon: Sparkles,
  },
  {
    label: 'Mới nhất',
    value: 'latest' as const,
    icon: Clock,
  },
]
