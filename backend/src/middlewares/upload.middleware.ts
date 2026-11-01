import multer from 'multer';
import path from 'path';
import fs from 'fs';

// Khởi tạo các thư mục lưu trữ ảnh và video
const UPLOADS_ROOT = path.resolve(process.cwd(), 'uploads');
const IMAGES_DIR = path.join(UPLOADS_ROOT, 'images');
const VIDEOS_DIR = path.join(UPLOADS_ROOT, 'videos');

[UPLOADS_ROOT, IMAGES_DIR, VIDEOS_DIR].forEach((dir) => {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
});

const storage = multer.diskStorage({
  destination: (_req, file, cb) => {
    // Tự động phân loại: Video vào thư mục /videos, Ảnh vào /images
    if (file.mimetype.startsWith('video/')) {
      cb(null, VIDEOS_DIR);
    } else {
      cb(null, IMAGES_DIR);
    }
  },
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    const isVideo = file.mimetype.startsWith('video/');
    const cleanExt = ext || (isVideo ? '.mp4' : '.jpg');
    const prefix = isVideo ? 'vid' : 'img';
    const uniqueSuffix = `${Date.now()}-${Math.round(Math.random() * 1e9)}`;
    cb(null, `${prefix}-${uniqueSuffix}${cleanExt}`);
  },
});

const fileFilter: multer.Options['fileFilter'] = (_req, file, cb) => {
  const allowedMimes = [
    // Định dạng ảnh
    'image/jpeg',
    'image/png',
    'image/webp',
    'image/gif',
    'image/svg+xml',
    // Định dạng video
    'video/mp4',
    'video/webm',
    'video/quicktime',
    'video/x-matroska',
  ];

  if (
    allowedMimes.includes(file.mimetype) ||
    file.mimetype.startsWith('image/') ||
    file.mimetype.startsWith('video/')
  ) {
    cb(null, true);
  } else {
    cb(new Error(`Định dạng tệp không được hỗ trợ (${file.mimetype}). Chỉ chấp nhận ảnh và video.`));
  }
};

export const mediaUpload = multer({
  storage,
  fileFilter,
  limits: {
    fileSize: 100 * 1024 * 1024, // 100MB (hỗ trợ video chất lượng cao)
  },
});

