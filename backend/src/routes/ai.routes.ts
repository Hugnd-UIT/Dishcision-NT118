import { Router } from 'express';
import { foodDecide, submitFeedback } from '../controllers/ai.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { validate, aiFoodDecideSchema } from '../middlewares/validate.middleware';

const router = Router();

router.post('/food-decide', authenticate, validate(aiFoodDecideSchema), foodDecide);
router.post('/requests/:requestId/feedback', authenticate, submitFeedback);

export default router;
