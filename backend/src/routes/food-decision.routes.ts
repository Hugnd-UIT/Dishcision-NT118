import { Router } from 'express';
import { getCategories, spinRandomWheel } from '../controllers/food-decision.controller';
import { asyncHandler } from '../utils/http';

const router = Router();

router.get('/categories', asyncHandler(getCategories));
router.post('/random-wheel', asyncHandler(spinRandomWheel));

export default router;
