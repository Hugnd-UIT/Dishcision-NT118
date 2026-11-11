import { Response } from 'express';
import { prisma } from '../config/prisma';
import { successResponse, errorResponse } from '../utils/response';
import { AuthRequest } from '../middlewares/auth.middleware';
import { parsePositiveInt } from '../utils/http';
import { resolveAvatarUrl } from './user.controller';

// ─────────────────────────────────────────
// 1. STORIES
// ─────────────────────────────────────────

function formatStory(s: any) {
  return {
    id: s.id,
    userId: s.userId,
    username: s.user?.username || '',
    userFullName: s.user?.fullName || '',
    userAvatar: resolveAvatarUrl(s.user?.avatarUrl),
    mediaUrl: s.mediaUrl,
    caption: s.caption || '',
    createdAt: s.createdAt,
  };
}

export async function getStories(req: AuthRequest, res: Response): Promise<void> {
  const userIdFilter = req.query.userId ? parsePositiveInt(req.query.userId as string) : null;
  const storeName = typeof req.query.storeName === 'string' ? req.query.storeName.trim() : '';

  const where: any = {};
  if (userIdFilter) {
    where.userId = userIdFilter;
  } else if (storeName) {
    where.user = { fullName: { contains: storeName, mode: 'insensitive' } };
  }

  const stories = await prisma.story.findMany({
    where,
    include: {
      user: { select: { id: true, username: true, fullName: true, avatarUrl: true } },
    },
    orderBy: { createdAt: 'desc' },
    take: 50,
  });

  successResponse(res, stories.map(formatStory), 'Lấy danh sách stories thành công');
}

export async function createStory(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { mediaUrl, caption } = req.body;

  if (!mediaUrl || typeof mediaUrl !== 'string' || !mediaUrl.trim()) {
    errorResponse(res, 'Media URL là bắt buộc', 400);
    return;
  }

  const newStory = await prisma.story.create({
    data: {
      userId,
      mediaUrl: mediaUrl.trim(),
      caption: typeof caption === 'string' ? caption.trim() : '',
    },
    include: {
      user: { select: { id: true, username: true, fullName: true, avatarUrl: true } },
    },
  });

  successResponse(res, formatStory(newStory), 'Đăng story thành công', 201);
}

// ─────────────────────────────────────────
// 2. DISHES & STORES
// ─────────────────────────────────────────

function formatDish(d: any) {
  return {
    id: d.id,
    name: d.name,
    price: d.price,
    formattedPrice: d.formattedPrice,
    description: d.description,
    imageUrl: d.imageUrl,
    category: d.category,
    rating: d.rating,
    reviewsCount: d.reviewsCount,
    storeId: d.storeUserId,
    storeName: d.storeUser?.fullName || '',
    storeAddress: d.storeUser?.storeProfile?.address || '',
  };
}

export async function getDishDetail(req: AuthRequest, res: Response): Promise<void> {
  const dishId = parsePositiveInt(req.params.id);
  if (!dishId) {
    errorResponse(res, 'ID món ăn không hợp lệ', 400);
    return;
  }

  const dish = await prisma.dish.findUnique({
    where: { id: dishId },
    include: {
      storeUser: {
        select: {
          id: true,
          fullName: true,
          avatarUrl: true,
          storeProfile: true,
        },
      },
    },
  });

  if (!dish) {
    errorResponse(res, 'Không tìm thấy món ăn', 404);
    return;
  }

  const reviews = await prisma.review.findMany({
    where: { restaurantName: { contains: dish.storeUser.fullName, mode: 'insensitive' } },
    include: {
      author: { select: { id: true, username: true, fullName: true, avatarUrl: true } },
    },
    orderBy: { createdAt: 'desc' },
    take: 5,
  });

  successResponse(
    res,
    {
      ...formatDish(dish),
      storeRating: dish.storeUser.storeProfile?.rating ?? dish.rating,
      storeOpenHours: dish.storeUser.storeProfile?.openHours || '',
      storeReviewsCount: reviews.length,
      reviews: reviews.map((r) => ({
        reviewId: r.id,
        rating: r.rating,
        content: r.content,
        authorName: r.author.fullName,
        createdAt: r.createdAt,
      })),
    },
    'Lấy chi tiết món ăn thành công',
  );
}

export async function getDishes(req: AuthRequest, res: Response): Promise<void> {
  const storeId = req.query.storeId ? parsePositiveInt(req.query.storeId as string) : null;
  const category = typeof req.query.category === 'string' ? req.query.category.trim() : '';
  const keyword = typeof req.query.keyword === 'string' ? req.query.keyword.trim() : '';
  const limit = req.query.limit ? Math.min(Math.max(parseInt(req.query.limit as string, 10) || 50, 1), 100) : 50;

  const where: any = {};
  if (storeId) where.storeUserId = storeId;
  if (category) where.category = { contains: category, mode: 'insensitive' };
  if (keyword) {
    where.OR = [
      { name: { contains: keyword, mode: 'insensitive' } },
      { category: { contains: keyword, mode: 'insensitive' } },
      { description: { contains: keyword, mode: 'insensitive' } },
      { storeUser: { fullName: { contains: keyword, mode: 'insensitive' } } },
    ];
  }

  const dishes = await prisma.dish.findMany({
    where,
    include: {
      storeUser: {
        select: {
          id: true,
          fullName: true,
          storeProfile: { select: { address: true } },
        },
      },
    },
    orderBy: [{ reviewsCount: 'desc' }, { id: 'asc' }],
    take: limit,
  });

  successResponse(res, dishes.map(formatDish), 'Lấy danh sách món ăn thành công');
}

export async function getStoreDetail(req: AuthRequest, res: Response): Promise<void> {
  const storeId = parsePositiveInt(req.params.id);
  if (!storeId) {
    errorResponse(res, 'ID quán ăn không hợp lệ', 400);
    return;
  }

  const restaurantUser = await prisma.user.findFirst({
    where: { id: storeId, accountStatus: 'Active' },
    include: {
      storeProfile: true,
      dishes: { orderBy: { id: 'asc' } },
      posts: {
        where: { status: 'Active' },
        include: { images: true },
        orderBy: { createdAt: 'desc' },
      },
      _count: { select: { followers: true } },
    },
  });

  if (!restaurantUser) {
    errorResponse(res, 'Không tìm thấy quán ăn', 404);
    return;
  }

  let isFollowing = false;
  if (req.user?.userId && req.user.userId !== storeId) {
    const follow = await prisma.follow.findUnique({
      where: {
        followerId_followingId: {
          followerId: req.user.userId,
          followingId: storeId,
        },
      },
      select: { followerId: true },
    });
    isFollowing = Boolean(follow);
  }

  const reviews = await prisma.review.findMany({
    where: { restaurantName: { contains: restaurantUser.fullName, mode: 'insensitive' } },
    include: {
      author: { select: { id: true, username: true, fullName: true, avatarUrl: true } },
      images: true,
    },
    orderBy: { createdAt: 'desc' },
  });

  const avgReviewRating =
    reviews.length > 0
      ? Math.round((reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length) * 10) / 10
      : 0;

  const menu = restaurantUser.dishes.map((d) => ({
    id: d.id,
    name: d.name,
    price: d.formattedPrice,
    priceInt: d.price,
    desc: d.description,
    description: d.description,
    imageUrl: d.imageUrl,
    category: d.category,
    rating: d.rating,
  }));

  const storeInfo = {
    id: restaurantUser.id,
    name: restaurantUser.fullName,
    username: restaurantUser.username,
    bio: restaurantUser.bio || '',
    avatarUrl: resolveAvatarUrl(restaurantUser.avatarUrl),
    coverUrl: restaurantUser.storeProfile?.coverUrl || resolveAvatarUrl(restaurantUser.avatarUrl),
    address: restaurantUser.storeProfile?.address || '',
    openHours: restaurantUser.storeProfile?.openHours || '',
    isOpen: restaurantUser.storeProfile?.isOpen ?? true,
    rating: restaurantUser.storeProfile?.rating ?? avgReviewRating,
    highlightDish: restaurantUser.storeProfile?.highlightDish || restaurantUser.dishes[0]?.name || '',
    dishPrice: restaurantUser.storeProfile?.dishPrice || restaurantUser.dishes[0]?.formattedPrice || '',
    dishDescription: restaurantUser.storeProfile?.dishDescription || restaurantUser.dishes[0]?.description || '',
    isFollowing,
    followersCount: restaurantUser._count.followers,
    reviewsCount: reviews.length,
    reviews: reviews.map((r) => ({
      reviewId: r.id,
      rating: r.rating,
      content: r.content,
      tags: r.tags,
      createdAt: r.createdAt,
      author: r.author ? { ...r.author, avatarUrl: resolveAvatarUrl(r.author.avatarUrl) } : r.author,
      images: r.images,
    })),
    menu,
    posts: restaurantUser.posts,
  };

  successResponse(res, storeInfo, 'Lấy thông tin nhà hàng thành công');
}

async function buildRestaurantsPayload(userId: number | undefined, keyword: string, onlyFollowing: boolean) {
  const where: any = { role: 'RestaurantAccount', accountStatus: 'Active' };
  if (keyword) {
    where.OR = [
      { fullName: { contains: keyword, mode: 'insensitive' } },
      { bio: { contains: keyword, mode: 'insensitive' } },
      { storeProfile: { address: { contains: keyword, mode: 'insensitive' } } },
    ];
  }

  let followingIds = new Set<number>();
  if (userId) {
    const follows = await prisma.follow.findMany({
      where: { followerId: userId },
      select: { followingId: true },
    });
    followingIds = new Set(follows.map((f) => f.followingId));
  }

  if (onlyFollowing) {
    if (!userId || followingIds.size === 0) {
      return {
        newDishesSummary: '0 quán đang theo dõi',
        totalFollowing: 0,
        openStoresCount: 0,
        stories: [],
        restaurants: [],
      };
    }
    where.id = { in: Array.from(followingIds) };
  }

  const restaurantUsers = await prisma.user.findMany({
    where,
    include: {
      storeProfile: true,
      dishes: { orderBy: { id: 'asc' } },
      _count: { select: { followers: true } },
    },
    orderBy: { id: 'asc' },
  });

  const storeIds = restaurantUsers.map((r) => r.id);

  const [reviews, storeStories] = await Promise.all([
    prisma.review.findMany({
      select: { restaurantName: true },
    }),
    storeIds.length > 0
      ? prisma.story.findMany({
          where: { userId: { in: storeIds } },
          include: { user: { select: { id: true, fullName: true, avatarUrl: true } } },
          orderBy: { createdAt: 'desc' },
          take: 20,
        })
      : Promise.resolve([]),
  ]);

  const restaurants = restaurantUsers.map((ru) => {
    const nameLower = ru.fullName.toLowerCase();
    const matchedReviewsCount = reviews.filter((rev) =>
      rev.restaurantName.toLowerCase().includes(nameLower) ||
      nameLower.includes(rev.restaurantName.toLowerCase()),
    ).length;

    const firstDish = ru.dishes[0];
    return {
      id: ru.id,
      name: ru.fullName,
      username: ru.username,
      avatarUrl: resolveAvatarUrl(ru.avatarUrl),
      coverUrl: ru.storeProfile?.coverUrl || resolveAvatarUrl(ru.avatarUrl),
      address: ru.storeProfile?.address || '',
      openHours: ru.storeProfile?.openHours || '',
      isOpen: ru.storeProfile?.isOpen ?? true,
      rating: ru.storeProfile?.rating ?? 0,
      reviewsCount: matchedReviewsCount,
      followersCount: ru._count.followers,
      isFollowing: followingIds.has(ru.id),
      highlightDish: ru.storeProfile?.highlightDish || firstDish?.name || '',
      dishPrice: ru.storeProfile?.dishPrice || firstDish?.formattedPrice || '',
      dishDescription: ru.storeProfile?.dishDescription || firstDish?.description || '',
      menu: ru.dishes.map((d) => ({
        id: d.id,
        name: d.name,
        price: d.formattedPrice,
        desc: d.description,
      })),
    };
  });

  const stories = storeStories.map((st) => ({
    id: st.id,
    userId: st.userId,
    storeName: st.user.fullName,
    title: st.caption || st.user.fullName,
    avatarUrl: resolveAvatarUrl(st.user.avatarUrl || st.mediaUrl),
    mediaUrl: st.mediaUrl,
  }));

  const openCount = restaurants.filter((r) => r.isOpen).length;
  const storesWithNewDishes = restaurants.filter((r) => r.menu.length > 0).length;
  const totalFollowing = restaurants.filter((r) => r.isFollowing).length;

  return {
    newDishesSummary: `${storesWithNewDishes} quán có món mới hôm nay`,
    totalFollowing,
    openStoresCount: openCount,
    stories,
    restaurants,
  };
}

export async function getStores(req: AuthRequest, res: Response): Promise<void> {
  const keyword = typeof req.query.keyword === 'string' ? req.query.keyword.trim() : '';
  const payload = await buildRestaurantsPayload(req.user?.userId, keyword, false);
  successResponse(res, payload, 'Lấy danh sách quán ăn thành công');
}

export async function getFollowingRestaurants(req: AuthRequest, res: Response): Promise<void> {
  const keyword = typeof req.query.keyword === 'string' ? req.query.keyword.trim() : '';
  const payload = await buildRestaurantsPayload(req.user?.userId, keyword, true);
  successResponse(res, payload, 'Lấy danh sách quán theo dõi thành công');
}

// ─────────────────────────────────────────
// 3. DELIVERY ADDRESSES
// ─────────────────────────────────────────

export async function getAddresses(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const addresses = await prisma.deliveryAddress.findMany({
    where: { userId },
    orderBy: [{ isDefault: 'desc' }, { createdAt: 'asc' }],
  });
  successResponse(res, addresses, 'Lấy danh sách địa chỉ thành công');
}

export async function addAddress(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const { label, recipientName, phoneNumber, detailAddress, notes, isDefault } = req.body;

  if (!recipientName || !phoneNumber || !detailAddress) {
    errorResponse(res, 'Vui lòng nhập đầy đủ họ tên, số điện thoại và địa chỉ chi tiết', 400);
    return;
  }

  const existingCount = await prisma.deliveryAddress.count({ where: { userId } });
  const makeDefault = Boolean(isDefault) || existingCount === 0;

  const newAddress = await prisma.$transaction(async (tx) => {
    if (makeDefault) {
      await tx.deliveryAddress.updateMany({
        where: { userId, isDefault: true },
        data: { isDefault: false },
      });
    }
    return tx.deliveryAddress.create({
      data: {
        userId,
        label: (label && String(label).trim()) || 'Nhà riêng',
        recipientName: String(recipientName).trim(),
        phoneNumber: String(phoneNumber).trim(),
        detailAddress: String(detailAddress).trim(),
        notes: notes ? String(notes).trim() : null,
        isDefault: makeDefault,
      },
    });
  });

  successResponse(res, newAddress, 'Thêm địa chỉ giao hàng thành công', 201);
}

export async function updateAddress(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const addressId = parsePositiveInt(req.params.id);
  if (!addressId) {
    errorResponse(res, 'ID địa chỉ không hợp lệ', 400);
    return;
  }

  const existing = await prisma.deliveryAddress.findFirst({ where: { id: addressId, userId } });
  if (!existing) {
    errorResponse(res, 'Không tìm thấy địa chỉ', 404);
    return;
  }

  const { label, recipientName, phoneNumber, detailAddress, notes, isDefault } = req.body;

  const updated = await prisma.$transaction(async (tx) => {
    if (isDefault === true) {
      await tx.deliveryAddress.updateMany({
        where: { userId, isDefault: true },
        data: { isDefault: false },
      });
    }
    return tx.deliveryAddress.update({
      where: { id: addressId },
      data: {
        label: label !== undefined ? String(label).trim() : undefined,
        recipientName: recipientName !== undefined ? String(recipientName).trim() : undefined,
        phoneNumber: phoneNumber !== undefined ? String(phoneNumber).trim() : undefined,
        detailAddress: detailAddress !== undefined ? String(detailAddress).trim() : undefined,
        notes: notes !== undefined ? (notes ? String(notes).trim() : null) : undefined,
        isDefault: typeof isDefault === 'boolean' ? isDefault : undefined,
      },
    });
  });

  successResponse(res, updated, 'Cập nhật địa chỉ thành công');
}

export async function setDefaultAddress(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const addressId = parsePositiveInt(req.params.id);
  if (!addressId) {
    errorResponse(res, 'ID địa chỉ không hợp lệ', 400);
    return;
  }

  const existing = await prisma.deliveryAddress.findFirst({ where: { id: addressId, userId } });
  if (!existing) {
    errorResponse(res, 'Không tìm thấy địa chỉ', 404);
    return;
  }

  const updated = await prisma.$transaction(async (tx) => {
    await tx.deliveryAddress.updateMany({
      where: { userId, isDefault: true },
      data: { isDefault: false },
    });
    return tx.deliveryAddress.update({
      where: { id: addressId },
      data: { isDefault: true },
    });
  });

  successResponse(res, updated, 'Đã đặt làm địa chỉ mặc định');
}

export async function deleteAddress(req: AuthRequest, res: Response): Promise<void> {
  const userId = req.user!.userId;
  const addressId = parsePositiveInt(req.params.id);
  if (!addressId) {
    errorResponse(res, 'Invalid address ID', 400);
    return;
  }

  const existing = await prisma.deliveryAddress.findFirst({ where: { id: addressId, userId } });
  if (!existing) {
    errorResponse(res, 'Không tìm thấy địa chỉ', 404);
    return;
  }

  await prisma.deliveryAddress.delete({ where: { id: addressId } });

  if (existing.isDefault) {
    const next = await prisma.deliveryAddress.findFirst({
      where: { userId },
      orderBy: { createdAt: 'asc' },
    });
    if (next) {
      await prisma.deliveryAddress.update({
        where: { id: next.id },
        data: { isDefault: true },
      });
    }
  }

  successResponse(res, null, 'Xóa địa chỉ thành công');
}
