import { Router } from 'express';
import { foodDecide, submitFeedback } from '../controllers/ai.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { asyncHandler } from '../utils/http';

const router = Router();

router.post('/food-decide', authenticate, asyncHandler(foodDecide));
router.post('/requests/:requestId/feedback', authenticate, asyncHandler(submitFeedback));

export default router;
