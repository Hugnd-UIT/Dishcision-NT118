import { Response } from 'express';
import { prisma } from '../config/prisma';
import { redis } from '../config/redis';
import { successResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';

// ─────────────────────────────────────────
// 1. STORIES (StoryActivity, CreateStoryActivity)
// ─────────────────────────────────────────

const DEFAULT_STORIES = [
  {
    id: 1,
    userId: 2,
    username: 'phothin',
    userFullName: 'Phở Thìn Lò Đúc',
    userAvatar: 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=400',
    mediaUrl: 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800',
    caption: 'Bát phở tái lăn buổi sáng nóng hổi thơm nức lòng thực khách!',
    createdAt: new Date().toISOString(),
  },
  {
    id: 2,
    userId: 3,
    username: 'banhmi25',
    userFullName: 'Bánh Mì 25 Hàng Cá',
    userAvatar: 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=400',
    mediaUrl: 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800',
    caption: 'Bánh mì pate giòn rụm vừa ra lò mẻ mới! 🥖',
    createdAt: new Date().toISOString(),
  },
  {
    id: 3,
    userId: 5,
    username: 'tranthib',
    userFullName: 'Trần Thị B',
    userAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400',
    mediaUrl: 'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800',
    caption: 'Chill cuối tuần cùng bạn bè tại quán cafe quen ☕',
    createdAt: new Date().toISOString(),
  },
];

export async function getStories(_req: AuthRequest, res: Response): Promise<void> {
  const raw = await redis.get('app:stories');
  let stories = DEFAULT_STORIES;
  if (raw) {
    try {
      stories = JSON.parse(raw);
    } catch {
      stories = DEFAULT_STORIES;
    }
  } else {
    await redis.set('app:stories', JSON.stringify(DEFAULT_STORIES), 'EX', 86400);
  }
  successResponse(res, stories, 'Lấy danh sách stories thành công');
}

export async function createStory(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId || 1;
  const user = await prisma.user.findUnique({ where: { id: userId } });
  const { mediaUrl, caption } = req.body;

  if (!mediaUrl) {
    errorResponse(res, 'Media URL là bắt buộc', 400);
    return;
  }

  const raw = await redis.get('app:stories');
  let stories = DEFAULT_STORIES;
  if (raw) {
    try { stories = JSON.parse(raw); } catch { stories = DEFAULT_STORIES; }
  }

  const newStory = {
    id: Date.now(),
    userId,
    username: user?.username || 'user',
    userFullName: user?.fullName || 'Người dùng Dishcision',
    userAvatar: user?.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400',
    mediaUrl,
    caption: caption || '',
    createdAt: new Date().toISOString(),
  };

  stories.unshift(newStory);
  await redis.set('app:stories', JSON.stringify(stories), 'EX', 86400);

  successResponse(res, newStory, 'Đăng story thành công', 201);
}

// ─────────────────────────────────────────
// 2. DISHES & STORES (DishDetailActivity, StoreDetailActivity)
// ─────────────────────────────────────────

const SAMPLE_DISHES = [
  {
    id: 1,
    name: 'Phở bò tái lăn gia truyền',
    price: 65000,
    formattedPrice: '65.000 VND',
    description: 'Thịt bò tươi ngon xào lăn xém cạnh trên lửa lớn với tỏi hành thơm phức, nước dùng ninh xương ống đậm đà béo ngậy.',
    imageUrl: 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800',
    category: 'Phở & Bún',
    rating: 4.8,
    reviewsCount: 128,
    storeId: 2,
    storeName: 'Phở Thìn Lò Đúc',
    storeAddress: '13 Lò Đúc, Hai Bà Trưng, Hà Nội',
  },
  {
    id: 2,
    name: 'Bánh mì pate xá xíu đặc biệt',
    price: 35000,
    formattedPrice: '35.000 VND',
    description: 'Vỏ bánh mì nướng giòn rụm, pate gan béo ngậy tự làm, thịt xá xíu đậm vị cùng sốt bơ trứng đặc trưng phố cổ.',
    imageUrl: 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800',
    category: 'Bánh mì & Xôi',
    rating: 4.9,
    reviewsCount: 310,
    storeId: 3,
    storeName: 'Bánh Mì 25',
    storeAddress: '25 Hàng Cá, Hoàn Kiếm, Hà Nội',
  },
  {
    id: 3,
    name: 'Bún chả đặc biệt Obama',
    price: 60000,
    formattedPrice: '60.000 VND',
    description: 'Chả viên và chả miếng nướng than hoa thơm lừng vị mỡ hành, nem cua bể giòn tan ăn kèm nước chấm chua ngọt đu đủ xanh.',
    imageUrl: 'https://images.unsplash.com/photo-1559847844-5315695dadae?w=800',
    category: 'Phở & Bún',
    rating: 4.7,
    reviewsCount: 245,
    storeId: 4,
    storeName: 'Bún Chả Hương Liên',
    storeAddress: '24 Lê Văn Hưu, Hai Bà Trưng, Hà Nội',
  },
  {
    id: 4,
    name: 'Bún ốc sườn chua cay',
    price: 55000,
    formattedPrice: '55.000 VND',
    description: 'Ốc nhồi giòn sần sật, sườn sụn ninh mềm, nước dùng vị giấm bỗng chua thanh cay nồng kích thích vị giác.',
    imageUrl: 'https://images.unsplash.com/photo-1541832676-9b763b0239ab?w=800',
    category: 'Phở & Bún',
    rating: 4.6,
    reviewsCount: 89,
    storeId: 2,
    storeName: 'Phở Thìn Lò Đúc',
    storeAddress: '13 Lò Đúc, Hai Bà Trưng, Hà Nội',
  },
];

export async function getDishDetail(req: AuthRequest, res: Response): Promise<void> {
  const dishId = parseInt(req.params.id) || 1;
  const dish = SAMPLE_DISHES.find(d => d.id === dishId) || SAMPLE_DISHES[0];
  successResponse(res, dish, 'Lấy chi tiết món ăn thành công');
}

export async function getDishes(req: AuthRequest, res: Response): Promise<void> {
  const storeId = req.query.storeId ? parseInt(req.query.storeId as string) : null;
  const dishes = storeId ? SAMPLE_DISHES.filter(d => d.storeId === storeId) : SAMPLE_DISHES;
  successResponse(res, dishes, 'Lấy danh sách món ăn thành công');
}

export async function getStoreDetail(req: AuthRequest, res: Response): Promise<void> {
  const storeId = parseInt(req.params.id) || 2;
  const restaurantUser = await prisma.user.findFirst({
    where: { id: storeId },
    include: {
      posts: { include: { images: true } },
      _count: { select: { followers: true, reviews: true } },
    },
  });

  const dishes = SAMPLE_DISHES.filter(d => d.storeId === storeId);
  const storeInfo = {
    id: storeId,
    name: restaurantUser?.fullName || 'Nhà hàng ẩm thực Dishcision',
    username: restaurantUser?.username || 'nhahang',
    bio: restaurantUser?.bio || 'Chuyên phục vụ các món ăn truyền thống đậm đà bản sắc Việt 🍜',
    avatarUrl: restaurantUser?.avatarUrl || 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=400',
    address: 'Hà Nội, Việt Nam',
    rating: 4.8,
    followersCount: restaurantUser?._count?.followers || 150,
    reviewsCount: restaurantUser?._count?.reviews || 45,
    menu: dishes.length > 0 ? dishes : SAMPLE_DISHES,
    posts: restaurantUser?.posts || [],
  };

  successResponse(res, storeInfo, 'Lấy thông tin nhà hàng thành công');
}

// ─────────────────────────────────────────
// 3. DELIVERY ADDRESSES (DeliveryAddressesActivity, AddAddressActivity)
// ─────────────────────────────────────────

const DEFAULT_ADDRESSES = [
  {
    id: 1,
    recipientName: 'Nguyễn Văn A',
    phoneNumber: '0987654321',
    detailAddress: 'Số 12 ngõ 45 phố Chùa Bộc, Đống Đa, Hà Nội',
    isDefault: true,
  },
  {
    id: 2,
    recipientName: 'Nguyễn Văn A (Cơ quan)',
    phoneNumber: '0987654321',
    detailAddress: 'Tầng 5 Tòa nhà Keangnam Landmark 72, Nam Từ Liêm, Hà Nội',
    isDefault: false,
  },
];

export async function getAddresses(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId || 1;
  const raw = await redis.get(`user:${userId}:addresses`);
  let addresses = DEFAULT_ADDRESSES;
  if (raw) {
    try { addresses = JSON.parse(raw); } catch { addresses = DEFAULT_ADDRESSES; }
  } else {
    await redis.set(`user:${userId}:addresses`, JSON.stringify(DEFAULT_ADDRESSES), 'EX', 86400 * 7);
  }
  successResponse(res, addresses, 'Lấy danh sách địa chỉ thành công');
}

export async function addAddress(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId || 1;
  const { recipientName, phoneNumber, detailAddress, isDefault } = req.body;

  if (!recipientName || !phoneNumber || !detailAddress) {
    errorResponse(res, 'Vui lòng nhập đầy đủ họ tên, số điện thoại và địa chỉ chi tiết', 400);
    return;
  }

  const raw = await redis.get(`user:${userId}:addresses`);
  let addresses = DEFAULT_ADDRESSES;
  if (raw) {
    try { addresses = JSON.parse(raw); } catch { addresses = DEFAULT_ADDRESSES; }
  }

  if (isDefault) {
    addresses.forEach(a => { a.isDefault = false; });
  }

  const newAddress = {
    id: Date.now(),
    recipientName,
    phoneNumber,
    detailAddress,
    isDefault: isDefault ?? (addresses.length === 0),
  };

  addresses.push(newAddress);
  await redis.set(`user:${userId}:addresses`, JSON.stringify(addresses), 'EX', 86400 * 7);

  successResponse(res, newAddress, 'Thêm địa chỉ giao hàng thành công', 201);
}

export async function deleteAddress(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user?.userId || 1;
  const addressId = parseInt(req.params.id);

  const raw = await redis.get(`user:${userId}:addresses`);
  let addresses = DEFAULT_ADDRESSES;
  if (raw) {
    try { addresses = JSON.parse(raw); } catch { addresses = DEFAULT_ADDRESSES; }
  }

  addresses = addresses.filter(a => a.id !== addressId);
  await redis.set(`user:${userId}:addresses`, JSON.stringify(addresses), 'EX', 86400 * 7);

  successResponse(res, null, 'Xóa địa chỉ thành công');
}

