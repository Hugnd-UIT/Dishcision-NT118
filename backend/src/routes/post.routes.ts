import { Router } from 'express';
import multer from 'multer';
import {
  getNewsfeed,
  createPost,
  getPostById,
  updatePost,
  deletePost,
  likePost,
  unlikePost,
  getPostLikes,
  sharePost,
  uploadMedia,
} from '../controllers/post.controller';
import { getComments, createComment } from '../controllers/comment.controller';
import { savePost, unsavePost } from '../controllers/social.controller';
import { authenticate, optionalAuth } from '../middlewares/auth.middleware';
import {
  validate,
  createPostSchema,
  createCommentSchema,
} from '../middlewares/validate.middleware';

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 10 * 1024 * 1024 }, // 10MB
});

const router = Router();

router.get('/', optionalAuth, getNewsfeed);
router.post('/', authenticate, validate(createPostSchema), createPost);
router.post('/upload-media', authenticate, upload.single('file'), uploadMedia);

router.get('/:postId', optionalAuth, getPostById);
router.put('/:postId', authenticate, updatePost);
router.delete('/:postId', authenticate, deletePost);

router.get('/:postId/likes', getPostLikes);
router.post('/:postId/likes', authenticate, likePost);
router.delete('/:postId/likes', authenticate, unlikePost);

router.post('/:postId/shares', authenticate, sharePost);

router.get('/:postId/comments', getComments);
router.post('/:postId/comments', authenticate, validate(createCommentSchema), createComment);

router.post('/:postId/save', authenticate, savePost);
router.delete('/:postId/save', authenticate, unsavePost);

export default router;
