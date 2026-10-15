import { Router } from 'express';
import { deleteComment } from '../controllers/comment.controller';
import { authenticate } from '../middlewares/auth.middleware';

const router = Router();

router.delete('/:commentId', authenticate, deleteComment);

export default router;
