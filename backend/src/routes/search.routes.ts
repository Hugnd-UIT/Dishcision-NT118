import { Router } from 'express';
import { searchFood, searchAccounts, getTrending } from '../controllers/search.controller';

const router = Router();

router.get('/food', searchFood);
router.get('/accounts', searchAccounts);
router.get('/trending', getTrending);

export default router;
