import { Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';

export async function getInbox(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;

  const [realReports, tickets] = await Promise.all([
    prisma.report.findMany({
      where: { reporterId: userId },
      orderBy: { createdAt: 'desc' },
    }),
    prisma.supportTicket.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
    }),
  ]);

  const combined = [
    ...realReports.map((r) => ({
      sortTime: r.createdAt.getTime(),
      id: `real_${r.id}`,
      type: 'Report',
      title: r.postId ? 'Báo cáo bài viết' : 'Báo cáo người dùng',
      subtitle: r.reason,
      date: r.createdAt.toLocaleDateString('vi-VN'),
      status: r.status === 'Pending' ? 'Đã tiếp nhận' : r.status === 'Resolved' ? 'Đã xử lý' : 'Từ chối',
    })),
    ...tickets.map((t) => ({
      sortTime: t.createdAt.getTime(),
      id: String(t.id),
      type: t.type,
      title: t.title,
      subtitle: t.subject,
      date: t.createdAt.toLocaleDateString('vi-VN'),
      status: t.status,
    })),
  ]
    .sort((a, b) => b.sortTime - a.sortTime)
    .map(({ sortTime: _s, ...item }) => item);

  successResponse(res, { items: combined }, 'Lấy danh sách hộp thư thành công');
}

export async function getTicketDetail(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { ticketId } = req.params;

  if (!ticketId) {
    errorResponse(res, 'Thiếu mã yêu cầu', 400);
    return;
  }

  if (ticketId.startsWith('real_')) {
    const reportId = parseInt(ticketId.slice(5), 10);
    if (Number.isNaN(reportId)) {
      errorResponse(res, 'Mã báo cáo không hợp lệ', 400);
      return;
    }

    const report = await prisma.report.findFirst({
      where: { id: reportId, reporterId: userId },
      include: { reporter: { select: { email: true } } },
    });

    if (!report) {
      errorResponse(res, 'Không tìm thấy báo cáo', 404);
      return;
    }

    const dateStr = report.createdAt.toLocaleDateString('vi-VN');
    const statusLabel = report.status === 'Pending' ? 'Đã tiếp nhận' : report.status === 'Resolved' ? 'Đã xử lý' : 'Từ chối';

    successResponse(
      res,
      {
        id: ticketId,
        title: report.postId ? 'Báo cáo bài viết' : 'Báo cáo người dùng',
        date: dateStr,
        category: String(report.reason),
        status: statusLabel,
        submittedContent: report.description || `Lý do báo cáo: ${report.reason}`,
        submissionMethod: 'Gửi từ chức năng báo cáo vi phạm',
        contactEmail: report.reporter.email,
        attachments: [],
        timeline: [
          { step: 'Đã gửi báo cáo', date: dateStr, desc: 'Người dùng gửi báo cáo từ ứng dụng', isDone: true },
          {
            step: statusLabel,
            date: dateStr,
            desc: report.status === 'Pending' ? 'Hệ thống đã tiếp nhận và đang xem xét.' : 'Yêu cầu đã được kiểm duyệt.',
            isDone: report.status !== 'Pending',
          },
        ],
      },
      'Lấy chi tiết báo cáo thành công',
    );
    return;
  }

  const numId = parseInt(ticketId, 10);
  if (Number.isNaN(numId)) {
    errorResponse(res, 'Mã yêu cầu không hợp lệ', 400);
    return;
  }

  const ticket = await prisma.supportTicket.findFirst({
    where: { id: numId, userId },
    include: { user: { select: { email: true } } },
  });

  if (!ticket) {
    errorResponse(res, 'Không tìm thấy yêu cầu hỗ trợ', 404);
    return;
  }

  const dateStr = ticket.createdAt.toLocaleDateString('vi-VN');
  const attachments = Array.isArray(ticket.attachments) ? ticket.attachments : [];
  const timeline = Array.isArray(ticket.timeline)
    ? ticket.timeline
    : [
        { step: 'Đã gửi yêu cầu', date: dateStr, desc: 'Qua biểu mẫu hỗ trợ', isDone: true },
        { step: ticket.status, date: ticket.updatedAt.toLocaleDateString('vi-VN'), desc: 'Đội ngũ hỗ trợ đã tiếp nhận yêu cầu.', isDone: ticket.status !== 'Đã tiếp nhận' },
      ];

  successResponse(
    res,
    {
      id: String(ticket.id),
      title: ticket.title,
      date: dateStr,
      category: ticket.subject,
      status: ticket.status,
      submittedContent: ticket.description,
      submissionMethod: 'Gửi từ biểu mẫu yêu cầu hỗ trợ',
      contactEmail: ticket.contactEmail || ticket.user.email,
      attachments,
      timeline,
    },
    'Lấy chi tiết yêu cầu thành công',
  );
}

export async function createTicket(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { subject, title, description, email, attachments } = req.body;

  if (!subject || !title || !description) {
    errorResponse(res, 'Vui lòng điền đầy đủ thông tin bắt buộc', 400);
    return;
  }

  const user = await prisma.user.findUnique({ where: { id: userId }, select: { email: true } });
  const dateStr = new Date().toLocaleDateString('vi-VN');

  const ticket = await prisma.supportTicket.create({
    data: {
      userId,
      type: 'Support',
      subject: String(subject).trim(),
      title: String(title).trim(),
      description: String(description).trim(),
      contactEmail: (email && String(email).trim()) || user?.email || null,
      status: 'Đã tiếp nhận',
      attachments: Array.isArray(attachments) ? attachments : [],
      timeline: [
        { step: 'Đã gửi yêu cầu', date: dateStr, desc: 'Qua biểu mẫu hỗ trợ', isDone: true },
        { step: 'Đã tiếp nhận', date: dateStr, desc: 'Đội ngũ hỗ trợ đã tiếp nhận yêu cầu của bạn.', isDone: false },
      ],
    },
  });

  successResponse(res, { ticketId: String(ticket.id) }, 'Gửi yêu cầu thành công', 201);
}
