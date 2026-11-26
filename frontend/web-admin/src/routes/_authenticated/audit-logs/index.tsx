import { createFileRoute } from '@tanstack/react-router'
import { AuditLogsFeature } from '@/features/audit-logs'

export const Route = createFileRoute('/_authenticated/audit-logs/')({
  component: AuditLogsFeature,
})
