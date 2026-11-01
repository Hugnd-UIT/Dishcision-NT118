import { Router } from 'express';
import { foodDecide, submitFeedback } from '../controllers/ai.controller';
import { optionalAuth } from '../middlewares/auth.middleware';

const router = Router();

router.post('/food-decide', optionalAuth, foodDecide);
router.post('/requests/:requestId/feedback', optionalAuth, submitFeedback);

export default router;
