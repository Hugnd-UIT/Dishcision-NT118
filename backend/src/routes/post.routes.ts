import { Router } from 'express';
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
import { savePost, unsavePost, getSavedPosts, getLikedPosts } from '../controllers/social.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { mediaUpload } from '../middlewares/upload.middleware';
import { asyncHandler } from '../utils/http';
import {
  validate,
  createPostSchema,
  updatePostSchema,
  createCommentSchema,
} from '../middlewares/validate.middleware';

const router = Router();

router.get('/', authenticate, asyncHandler(getNewsfeed));
router.get('/saved', authenticate, asyncHandler(getSavedPosts));
router.get('/liked', authenticate, asyncHandler(getLikedPosts));
router.post('/', authenticate, validate(createPostSchema), asyncHandler(createPost));
router.post('/upload-media', authenticate, mediaUpload.single('file'), asyncHandler(uploadMedia));

router.get('/:postId', authenticate, asyncHandler(getPostById));
router.put('/:postId', authenticate, validate(updatePostSchema), asyncHandler(updatePost));
router.delete('/:postId', authenticate, asyncHandler(deletePost));

router.get('/:postId/likes', authenticate, asyncHandler(getPostLikes));
router.post('/:postId/likes', authenticate, asyncHandler(likePost));
router.delete('/:postId/likes', authenticate, asyncHandler(unlikePost));

router.post('/:postId/shares', authenticate, asyncHandler(sharePost));

router.get('/:postId/comments', authenticate, asyncHandler(getComments));
router.post('/:postId/comments', authenticate, validate(createCommentSchema), asyncHandler(createComment));

router.post('/:postId/save', authenticate, asyncHandler(savePost));
router.delete('/:postId/save', authenticate, asyncHandler(unsavePost));

export default router;
