import { Router } from 'express';
import { getCategories, spinRandomWheel } from '../controllers/food-decision.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { asyncHandler } from '../utils/http';

const router = Router();

router.get('/categories', authenticate, asyncHandler(getCategories));
router.post('/random-wheel', authenticate, asyncHandler(spinRandomWheel));

export default router;
