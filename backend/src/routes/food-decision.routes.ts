import { Router } from 'express';
import { getCategories, spinRandomWheel } from '../controllers/food-decision.controller';

const router = Router();

router.get('/categories', getCategories);
router.post('/random-wheel', spinRandomWheel);

export default router;
