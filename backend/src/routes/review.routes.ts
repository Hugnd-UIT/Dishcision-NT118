import { Router } from 'express';
import { getReviews, createReview, deleteReview } from '../controllers/review.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { validate, createReviewSchema } from '../middlewares/validate.middleware';

const router = Router();

router.get('/', getReviews);
router.post('/', authenticate, validate(createReviewSchema), createReview);
router.delete('/:reviewId', authenticate, deleteReview);

export default router;
