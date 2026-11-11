import { Router } from 'express';
import { searchFood, searchAccounts, searchDishes, searchStores, getTrending } from '../controllers/search.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { asyncHandler } from '../utils/http';

const router = Router();

router.get('/food', authenticate, asyncHandler(searchFood));
router.get('/accounts', authenticate, asyncHandler(searchAccounts));
router.get('/dishes', authenticate, asyncHandler(searchDishes));
router.get('/stores', authenticate, asyncHandler(searchStores));
router.get('/trending', authenticate, asyncHandler(getTrending));

export default router;
