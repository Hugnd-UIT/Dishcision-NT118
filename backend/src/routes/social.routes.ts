import { Router } from 'express';
import {
  followUser,
  unfollowUser,
  savePost,
  unsavePost,
  getSavedPosts,
  getChats,
  getMessages,
  sendMessage,
} from '../controllers/social.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { validate, sendMessageSchema } from '../middlewares/validate.middleware';

const router = Router();

// Follow / Unfollow
router.post('/users/:userId/follow', authenticate, followUser);
router.delete('/users/:userId/follow', authenticate, unfollowUser);

// Bookmarks
router.get('/users/me/saved-posts', authenticate, getSavedPosts);
router.post('/posts/:postId/save', authenticate, savePost);
router.delete('/posts/:postId/save', authenticate, unsavePost);

// Direct Messages / Chats
router.get('/chats', authenticate, getChats);
router.get('/chats/:recipientId/messages', authenticate, getMessages);
router.post('/chats/:recipientId/messages', authenticate, validate(sendMessageSchema), sendMessage);

export default router;
