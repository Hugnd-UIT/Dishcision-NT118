import { prisma } from '../config/prisma';
import { AuditTargetType } from '@prisma/client';

export async function logActivity({
  userId,
  username,
  role,
  avatarUrl,
  action,
  targetType,
  targetId,
  details,
  ipAddress,
}: {
  userId?: number | null;
  username?: string;
  role?: string;
  avatarUrl?: string;
  action: string;
  targetType: AuditTargetType;
  targetId: number;
  details?: string;
  ipAddress?: string;
}): Promise<void> {
  try {
    let uName = username;
    let uRole = role;
    let uAvatar = avatarUrl;

    if (userId && (!uName || !uRole)) {
      try {
        const u = await prisma.user.findUnique({
          where: { id: userId },
          select: { username: true, role: true, avatarUrl: true },
        });
        if (u) {
          uName = u.username;
          uRole = u.role;
          uAvatar = u.avatarUrl || undefined;
        }
      } catch {
        // ignore lookup error
      }
    }

    await prisma.auditLog.create({
      data: {
        adminId: userId ? userId : undefined,
        actorUsername: uName,
        actorRole: uRole,
        actorAvatar: uAvatar,
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

