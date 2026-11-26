import { prisma } from '../config/prisma';
import { AuditTargetType } from '@prisma/client';

export async function logActivity({
  userId,
  action,
  targetType,
  targetId,
  details,
  ipAddress,
}: {
  userId: number;
  action: string;
  targetType: AuditTargetType;
  targetId: number;
  details?: string;
  ipAddress?: string;
}): Promise<void> {
  if (!userId) return;
  try {
    await prisma.auditLog.create({
      data: {
        adminId: userId,
        action,
        targetType,
        targetId,
        details,
        ipAddress: ipAddress || '127.0.0.1',
      },
    });
  } catch (err) {
    console.error('[AuditLog] Failed to log activity:', err);
  }
}
