import { createFileRoute } from '@tanstack/react-router'
import { AiFeature } from '@/features/ai'

export const Route = createFileRoute('/_authenticated/ai/')({
  component: AiFeature,
})
