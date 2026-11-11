import { Router } from 'express';
import { searchFood, searchAccounts, searchDishes, searchStores, getTrending } from '../controllers/search.controller';
import { asyncHandler } from '../utils/http';

const router = Router();

router.get('/food', asyncHandler(searchFood));
router.get('/accounts', asyncHandler(searchAccounts));
router.get('/dishes', asyncHandler(searchDishes));
router.get('/stores', asyncHandler(searchStores));
router.get('/trending', asyncHandler(getTrending));

export default router;
