import { Router } from 'express';
import {
  followUser,
  unfollowUser,
  getFollowers,
  getFollowing,
  savePost,
  unsavePost,
  getSavedPosts,
  getLikedPosts,
  getChats,
  getMessages,
  sendMessage,
} from '../controllers/social.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { asyncHandler } from '../utils/http';
import { validate, sendMessageSchema } from '../middlewares/validate.middleware';

const router = Router();

// Follow / Unfollow
router.get('/users/:userId/followers', authenticate, asyncHandler(getFollowers));
router.get('/users/:userId/following', authenticate, asyncHandler(getFollowing));
router.post('/users/:userId/follow', authenticate, asyncHandler(followUser));
router.delete('/users/:userId/follow', authenticate, asyncHandler(unfollowUser));

// Bookmarks & Liked Posts
router.get('/users/me/saved-posts', authenticate, asyncHandler(getSavedPosts));
router.get('/users/me/liked-posts', authenticate, asyncHandler(getLikedPosts));
router.post('/posts/:postId/save', authenticate, asyncHandler(savePost));
router.delete('/posts/:postId/save', authenticate, asyncHandler(unsavePost));

// Direct Messages / Chats
router.get('/chats', authenticate, asyncHandler(getChats));
router.get('/chats/:recipientId/messages', authenticate, asyncHandler(getMessages));
router.post('/chats/:recipientId/messages', authenticate, validate(sendMessageSchema), asyncHandler(sendMessage));

export default router;
