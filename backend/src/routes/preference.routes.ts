import { Router } from 'express';
import {
  listPreferences,
  getUserPreferences,
  updateUserPreferences,
} from '../controllers/preference.controller';
import { authenticate } from '../middlewares/auth.middleware';

const router = Router();

router.get('/', listPreferences);
router.get('/me', authenticate, getUserPreferences);
router.put('/me', authenticate, updateUserPreferences);

export default router;
