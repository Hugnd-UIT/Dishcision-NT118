import { Router } from 'express';
import {
  getStories,
  createStory,
  getDishDetail,
  getDishes,
  getStoreDetail,
  getStores,
  getFollowingRestaurants,
  getAddresses,
  addAddress,
  updateAddress,
  setDefaultAddress,
  deleteAddress,
} from '../controllers/extra.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { asyncHandler } from '../utils/http';
import { uploadSingleMedia, uploadMultipleMedia } from '../controllers/upload.controller';
import { mediaUpload } from '../middlewares/upload.middleware';

const router = Router();

// General Upload Media (Images & Videos)
router.post('/upload', authenticate, mediaUpload.single('file'), asyncHandler(uploadSingleMedia));
router.post('/upload/multiple', authenticate, mediaUpload.array('files', 10), asyncHandler(uploadMultipleMedia));

// Stories (StoryActivity, CreateStoryActivity)
router.get('/stories', authenticate, asyncHandler(getStories));
router.post('/stories', authenticate, asyncHandler(createStory));

// Dishes & Stores (DishDetailActivity, StoreDetailActivity, FavoriteRestaurantsActivity)
router.get('/dishes', authenticate, asyncHandler(getDishes));
router.get('/dishes/:id', authenticate, asyncHandler(getDishDetail));
router.get('/stores', authenticate, asyncHandler(getStores));
router.get('/stores/:id', authenticate, asyncHandler(getStoreDetail));
router.get('/restaurants/following', authenticate, asyncHandler(getFollowingRestaurants));
router.get('/restaurants', authenticate, asyncHandler(getStores));

// Delivery Addresses (DeliveryAddressesActivity, AddAddressActivity)
router.get('/users/me/addresses', authenticate, asyncHandler(getAddresses));
router.post('/users/me/addresses', authenticate, asyncHandler(addAddress));
router.put('/users/me/addresses/:id', authenticate, asyncHandler(updateAddress));
router.patch('/users/me/addresses/:id/default', authenticate, asyncHandler(setDefaultAddress));
router.delete('/users/me/addresses/:id', authenticate, asyncHandler(deleteAddress));

export default router;

