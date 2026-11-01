import { Request, Response } from 'express';
import { successResponse, errorResponse } from '../utils/response';

export function handleUploadedFile(req: Request, file: Express.Multer.File) {
  const isVideo = file.mimetype.startsWith('video/');
  const folder = isVideo ? 'videos' : 'images';
  const relativeUrl = `/uploads/${folder}/${file.filename}`;
  const host = req.get('host') || 'localhost:3000';
  const protocol = req.protocol || 'http';
  const fullUrl = `${protocol}://${host}${relativeUrl}`;

  return {
    filename: file.filename,
    originalName: file.originalname,
    mediaType: isVideo ? 'video' : 'image',
    mimetype: file.mimetype,
    size: file.size,
    url: relativeUrl,
    fullUrl,
  };
}

export async function uploadSingleMedia(req: Request, res: Response): Promise<void> {
  try {
    if (!req.file) {
      errorResponse(res, 'Vui lòng chọn tệp ảnh hoặc video để tải lên (key field: "file")', 400);
      return;
    }
    const data = handleUploadedFile(req, req.file);
    successResponse(res, data, 'Tải lên thành công');
  } catch (error: any) {
    errorResponse(res, error.message || 'Lỗi khi tải tệp lên', 500);
  }
}

export async function uploadMultipleMedia(req: Request, res: Response): Promise<void> {
  try {
    const files = req.files as Express.Multer.File[];
    if (!files || files.length === 0) {
      errorResponse(res, 'Vui lòng chọn ít nhất một tệp ảnh hoặc video (key field: "files")', 400);
      return;
    }
    const results = files.map((f) => handleUploadedFile(req, f));
    successResponse(res, { count: results.length, files: results }, 'Tải lên danh sách tệp thành công');
  } catch (error: any) {
    errorResponse(res, error.message || 'Lỗi khi tải tệp lên', 500);
  }
}

