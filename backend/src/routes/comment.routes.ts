import { Router } from 'express';
import { deleteComment } from '../controllers/comment.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { asyncHandler } from '../utils/http';

const router = Router();

router.delete('/:commentId', authenticate, asyncHandler(deleteComment));

export default router;
