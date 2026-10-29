import { Router } from 'express';
import {
  getStories,
  createStory,
  getDishDetail,
  getDishes,
  getStoreDetail,
  getAddresses,
  addAddress,
  deleteAddress,
} from '../controllers/extra.controller';
import { optionalAuth } from '../middlewares/auth.middleware';

const router = Router();

// Stories (StoryActivity, CreateStoryActivity)
router.get('/stories', optionalAuth, getStories);
router.post('/stories', optionalAuth, createStory);

// Dishes & Stores (DishDetailActivity, StoreDetailActivity)
router.get('/dishes', optionalAuth, getDishes);
router.get('/dishes/:id', optionalAuth, getDishDetail);
router.get('/stores/:id', optionalAuth, getStoreDetail);

// Delivery Addresses (DeliveryAddressesActivity, AddAddressActivity)
router.get('/users/me/addresses', optionalAuth, getAddresses);
router.post('/users/me/addresses', optionalAuth, addAddress);
router.delete('/users/me/addresses/:id', optionalAuth, deleteAddress);

export default router;

