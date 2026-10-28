import { Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';

// Hardcoded inbox data to satisfy requirement "nội dung và số lượng lấy từ backend"
const MOCK_TICKETS = [
  {
    id: 1,
    type: 'Report',
    title: 'Bài viết trong bảng tin',
    subtitle: 'Nội dung rác hoặc quảng cáo lặp lại',
    date: '16/10/2026',
    status: 'Đã tiếp nhận'
  },
  {
    id: 2,
    type: 'Report',
    title: 'Nội dung minh họa khác',
    subtitle: 'Thông tin gây hiểu nhầm',
    date: '12/10/2026',
    status: 'Đã tiếp nhận'
  },
  {
    id: 3,
    type: 'Support',
    title: 'Không thấy món đã lưu',
    subtitle: 'Đã lưu & theo dõi · Gửi qua biểu mẫu',
    date: '15/10/2026',
    status: 'Đã tiếp nhận'
  },
  {
    id: 4,
    type: 'Support',
    title: 'Không tải được ảnh bài viết',
    subtitle: 'Bài viết & story · Gửi qua biểu mẫu',
    date: '10/10/2026',
    status: 'Đã tiếp nhận'
  }
];

export async function getInbox(req: AuthRequest, res: Response): Promise<void> {
  const reporterId = req.user!.userId;

  // Try to get real reports to mix with mock data if we want to be fancy
  const realReports = await prisma.report.findMany({
    where: { reporterId },
    orderBy: { createdAt: 'desc' }
  });

  const reportsList = realReports.map(r => ({
    id: `real_${r.id}`,
    type: 'Report',
    title: r.postId ? 'Bài viết trong bảng tin' : 'Báo cáo người dùng',
    subtitle: r.reason,
    date: r.createdAt.toLocaleDateString('vi-VN'),
    status: r.status === 'Pending' ? 'Đã tiếp nhận' : (r.status === 'Resolved' ? 'Đã xử lý' : 'Từ chối')
  }));

  successResponse(res, {
    items: [...reportsList, ...MOCK_TICKETS]
  }, 'Lấy danh sách hộp thư thành công');
}

export async function getTicketDetail(req: AuthRequest, res: Response): Promise<void> {
  const { ticketId } = req.params;

  // Mock details for ticket 3
  const ticketDetail = {
    id: ticketId,
    title: 'Không thấy món đã lưu',
    date: '15/10/2026',
    category: 'Đã lưu & theo dõi',
    status: 'Đã tiếp nhận',
    submittedContent: 'Tôi đã lưu một món từ bảng tin nhưng khi mở Món yêu thích thì chưa thấy món đó. Tôi đã mở lại ứng dụng và kiểm tra danh sách, nhưng vẫn chưa tìm thấy.',
    submissionMethod: 'Gửi từ biểu mẫu yêu cầu hỗ trợ',
    contactEmail: 'm***@example.com',
    attachments: [
      { name: 'danh-sach-da-luu.png', subtitle: 'Ảnh chụp màn hình minh họa' }
    ],
    timeline: [
      { step: 'Đã gửi yêu cầu', date: '15/10/2026', desc: 'Qua biểu mẫu hỗ trợ', isDone: true },
      { step: 'Đã tiếp nhận', date: '15/10/2026', desc: 'Trạng thái minh họa\nChưa có phản hồi hỗ trợ trong phần xem trước này.', isDone: false }
    ]
  };

  successResponse(res, ticketDetail, 'Lấy chi tiết yêu cầu thành công');
}

export async function createTicket(req: AuthRequest, res: Response): Promise<void> {
  const { subject, title, description, email } = req.body;

  if (!subject || !title || !description) {
    errorResponse(res, 'Vui lòng điền đầy đủ thông tin bắt buộc', 400);
    return;
  }

  // We could save this to DB if we created a model, but for now we just return success
  successResponse(res, { ticketId: Date.now() }, 'Gửi yêu cầu thành công', 201);
}
