import {
  PrismaClient,
  Role,
  AccountStatus,
  PostStatus,
  NotificationType,
  ReportReason,
  ReportStatus,
  ReportPriority,
  AuditTargetType,
} from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function main() {
  console.log('🌱 Starting comprehensive database seeding...');

  // ─────────────────────────────────────────
  // 1. PASSWORDS
  // ─────────────────────────────────────────
  const adminPasswordHash = await bcrypt.hash('Admin@Dishcision2026', 12);
  const userPasswordHash = await bcrypt.hash('Password@123', 12);

  // ─────────────────────────────────────────
  // 2. SEED USERS
  // ─────────────────────────────────────────
  console.log('👤 Seeding users...');

  // Admin
  const admin = await prisma.user.upsert({
    where: { username: 'admin' },
    update: {},
    create: {
      username: 'admin',
      email: 'admin@dishcision.com',
      passwordHash: adminPasswordHash,
      fullName: 'System Administrator',
      role: Role.Admin,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400',
      bio: 'Dishcision Head Administrator',
    },
  });

  // Restaurant Accounts
  const phothin = await prisma.user.upsert({
    where: { username: 'phothin' },
    update: {},
    create: {
      username: 'phothin',
      email: 'phothin@dishcision.com',
      passwordHash: userPasswordHash,
      fullName: 'Phở Thìn Lò Đúc',
      role: Role.RestaurantAccount,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=400',
      bio: 'Phở bò tái lăn gia truyền số 13 Lò Đúc từ năm 1979 🍜',
    },
  });

  const banhmi25 = await prisma.user.upsert({
    where: { username: 'banhmi25' },
    update: {},
    create: {
      username: 'banhmi25',
      email: 'banhmi25@dishcision.com',
      passwordHash: userPasswordHash,
      fullName: 'Bánh Mì 25',
      role: Role.RestaurantAccount,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=400',
      bio: 'Bánh mì truyền thống phố cổ Hà Nội số 25 Hàng Cá 🥖',
    },
  });

  const bunchahuonglien = await prisma.user.upsert({
    where: { username: 'bunchahuonglien' },
    update: {},
    create: {
      username: 'bunchahuonglien',
      email: 'bunchahuonglien@dishcision.com',
      passwordHash: userPasswordHash,
      fullName: 'Bún Chả Hương Liên',
      role: Role.RestaurantAccount,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1559847844-5315695dadae?w=400',
      bio: 'Bún chả Obama gia truyền tại 24 Lê Văn Hưu, Hà Nội 🇻🇳🇺🇸',
    },
  });

  // Normal Users
  const userA = await prisma.user.upsert({
    where: { username: 'nguyenvana' },
    update: {},
    create: {
      username: 'nguyenvana',
      email: 'nguyenvana@gmail.com',
      passwordHash: userPasswordHash,
      fullName: 'Nguyễn Văn A',
      role: Role.User,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400',
      bio: 'Đam mê ẩm thực đường phố và săn quán ngon Hà Nội 🍜🌶️',
      gender: 'Male',
      dateOfBirth: new Date('1998-05-15'),
    },
  });

  const userB = await prisma.user.upsert({
    where: { username: 'tranthib' },
    update: {},
    create: {
      username: 'tranthib',
      email: 'tranthib@gmail.com',
      passwordHash: userPasswordHash,
      fullName: 'Trần Thị B',
      role: Role.User,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400',
      bio: 'Food reviewer thích bánh ngọt & cà phê chill cuối tuần 🍰☕',
      gender: 'Female',
      dateOfBirth: new Date('2001-09-20'),
    },
  });

  const userC = await prisma.user.upsert({
    where: { username: 'lequangc' },
    update: {},
    create: {
      username: 'lequangc',
      email: 'lequangc@gmail.com',
      passwordHash: userPasswordHash,
      fullName: 'Lê Quang C',
      role: Role.User,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400',
      bio: 'Tín đồ ẩm thực cay nóng & lẩu nướng sinh viên 🍲🔥',
      gender: 'Male',
      dateOfBirth: new Date('2002-12-10'),
    },
  });

  // Mock Users for Mobile App Home Screen & Discussions
  const mayaLin = await prisma.user.upsert({
    where: { username: 'mayafoodie' },
    update: {},
    create: {
      username: 'mayafoodie',
      email: 'mayafoodie@dishcision.com',
      passwordHash: userPasswordHash,
      fullName: 'Maya Lin',
      role: Role.User,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400',
      bio: 'Foodie đam mê các món chay thanh đạm 🥗✨',
      gender: 'Female',
      dateOfBirth: new Date('2000-03-12'),
    },
  });

  const julianCole = await prisma.user.upsert({
    where: { username: 'juliancole' },
    update: {},
    create: {
      username: 'juliancole',
      email: 'juliancole@dishcision.com',
      passwordHash: userPasswordHash,
      fullName: 'Julian Cole',
      role: Role.User,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400',
      bio: 'Pasta lover & Italian cuisine enthusiast 🍝🇮🇹',
      gender: 'Male',
      dateOfBirth: new Date('1997-08-25'),
    },
  });

  const anNhien = await prisma.user.upsert({
    where: { username: 'annhien_food' },
    update: {},
    create: {
      username: 'annhien_food',
      email: 'annhien@dishcision.com',
      passwordHash: userPasswordHash,
      fullName: 'An Nhiên',
      role: Role.User,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400',
      bio: 'Tiệm bánh ngọt và những tách trà chiều 🍰🍓',
      gender: 'Female',
      dateOfBirth: new Date('2001-11-05'),
    },
  });

  const minhAnh = await prisma.user.upsert({
    where: { username: 'minhanh' },
    update: {},
    create: {
      username: 'minhanh',
      email: 'minhanh@dishcision.com',
      passwordHash: userPasswordHash,
      fullName: 'Minh Anh',
      role: Role.User,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400',
      bio: 'Thực khách sành ăn Hà Nội 🥗',
      gender: 'Female',
      dateOfBirth: new Date('2002-04-18'),
    },
  });

  const linhHa = await prisma.user.upsert({
    where: { username: 'linhha' },
    update: {},
    create: {
      username: 'linhha',
      email: 'linhha@dishcision.com',
      passwordHash: userPasswordHash,
      fullName: 'Linh Hà',
      role: Role.User,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400',
      bio: 'Mê đồ chay và trà thảo mộc 🍵',
      gender: 'Female',
      dateOfBirth: new Date('1999-07-22'),
    },
  });

  const quangTran = await prisma.user.upsert({
    where: { username: 'quangtran' },
    update: {},
    create: {
      username: 'quangtran',
      email: 'quangtran@dishcision.com',
      passwordHash: userPasswordHash,
      fullName: 'Quang Trần',
      role: Role.User,
      accountStatus: AccountStatus.Active,
      avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400',
      bio: 'Đam mê ẩm thực Địa Trung Hải & nấm truffle 🍄',
      gender: 'Male',
      dateOfBirth: new Date('1996-10-30'),
    },
  });

  // ─────────────────────────────────────────
  // 3. SEED FOOD PREFERENCES
  // ─────────────────────────────────────────
  console.log('🌶️ Seeding food preferences...');

  const preferenceData = [
    { name: 'Cay nồng', icon: '🌶️', description: 'Các món ăn đậm đà, vị cay ấm nóng kích thích vị giác' },
    { name: 'Ngọt ngào', icon: '🍰', description: 'Đồ ngọt, tráng miệng, chè và bánh ngọt các loại' },
    { name: 'Ăn chay', icon: '🥗', description: 'Món ăn thuần chay thanh đạm từ rau củ và nấm' },
    { name: 'Ăn vặt đường phố', icon: '🍢', description: 'Các món ăn nhanh, đồ ăn vặt vỉa hè quen thuộc' },
    { name: 'Hải sản tươi sống', icon: '🦐', description: 'Tôm, cua, cá, ốc chế biến hấp dẫn' },
    { name: 'Healthy / Ăn sạch', icon: '🥑', description: 'Món ăn ít calo, giàu protein và chất xơ' },
    { name: 'Món nước nóng hổi', icon: '🍜', description: 'Phở, bún, miến, hủ tiếu với nước dùng thơm ngọt' },
    { name: 'Lẩu & Nướng', icon: '🍲', description: 'Thích hợp cho tụ tập bạn bè và gia đình' },
  ];

  const preferences: any[] = [];
  for (const p of preferenceData) {
    let pref = await prisma.foodPreference.findFirst({ where: { name: p.name } });
    if (!pref) {
      pref = await prisma.foodPreference.create({ data: p });
    }
    preferences.push(pref);
  }

  // Assign user preferences
  await prisma.userPreference.deleteMany({
    where: { userId: { in: [userA.id, userB.id, userC.id, mayaLin.id, julianCole.id, anNhien.id] } },
  });

  await prisma.userPreference.createMany({
    data: [
      { userId: userA.id, preferenceId: preferences[0].id }, // Cay nồng
      { userId: userA.id, preferenceId: preferences[3].id }, // Ăn vặt
      { userId: userA.id, preferenceId: preferences[6].id }, // Món nước
      { userId: userB.id, preferenceId: preferences[1].id }, // Ngọt ngào
      { userId: userB.id, preferenceId: preferences[5].id }, // Healthy
      { userId: userC.id, preferenceId: preferences[0].id }, // Cay nồng
      { userId: userC.id, preferenceId: preferences[7].id }, // Lẩu & nướng
      { userId: mayaLin.id, preferenceId: preferences[2].id }, // Ăn chay
      { userId: mayaLin.id, preferenceId: preferences[5].id }, // Healthy
      { userId: julianCole.id, preferenceId: preferences[0].id }, // Cay nồng
      { userId: anNhien.id, preferenceId: preferences[1].id }, // Ngọt ngào
    ],
  });

  // ─────────────────────────────────────────
  // 4. SEED FOOD CATEGORIES
  // ─────────────────────────────────────────
  console.log('🏷️ Seeding food categories...');

  const categoryData = [
    { name: 'Phở & Bún', icon: '🍜' },
    { name: 'Cơm tấm & Cơm phần', icon: '🍚' },
    { name: 'Bánh mì & Xôi', icon: '🥖' },
    { name: 'Lẩu & Nướng', icon: '🍲' },
    { name: 'Hải sản & Ốc', icon: '🦀' },
    { name: 'Trà & Cà phê', icon: '☕' },
    { name: 'Tráng miệng & Chè', icon: '🍧' },
    { name: 'Món chay', icon: '🥗' },
  ];

  for (const c of categoryData) {
    const existing = await prisma.foodCategory.findFirst({ where: { name: c.name } });
    if (!existing) {
      await prisma.foodCategory.create({ data: c });
    }
  }

  // ─────────────────────────────────────────
  // 5. SEED POSTS & POST IMAGES
  // ─────────────────────────────────────────
  console.log('📝 Seeding posts...');

  // Clean up existing posts and post relations for idempotency
  await prisma.report.deleteMany({});
  await prisma.commentLike.deleteMany({});
  await prisma.comment.deleteMany({});
  await prisma.postLike.deleteMany({});
  await prisma.postShare.deleteMany({});
  await prisma.savedPost.deleteMany({});
  await prisma.postImage.deleteMany({});
  await prisma.post.deleteMany({});

  const now = new Date();
  const postsData = [
    // 1. Post by Maya Lin (Home Card 1)
    {
      authorId: mayaLin.id,
      content:
        'Tìm thấy tô salad ngũ sắc tươi ngon nhất phố! Từng hạt đậu gà rang giòn ngấm vị ớt paprika xông khói kết hợp xốt tahini béo bùi chuẩn vị. Cực kỳ đáng thử! 🥗✨',
      locationUrl: 'https://maps.google.com/?q=The+Green+Fork+Bistro',
      voucherCode: 'SALADVEG15',
      hashtags: ['MonChay', 'AnLanhManh', 'AmThucXanh'],
      createdAt: new Date(now.getTime() - 2 * 3600 * 1000), // 2 hours ago
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=800', displayOrder: 1 },
      ],
    },
    // 2. Post by Julian Cole (Home Card 2)
    {
      authorId: julianCole.id,
      content:
        'Món pasta thủ công tươi mới vừa ra lò chiều nay! Sốt kem béo mịn thơm lừng mùi nấm truffle thượng hạng. Mời cả nhà cùng thưởng thức! 🍝🇮🇹',
      locationUrl: 'https://maps.google.com/?q=Trattoria+Bella',
      voucherCode: 'TRUFFLE20',
      hashtags: ['ItalianFood', 'Pasta', 'TruffleLover'],
      createdAt: new Date(now.getTime() - 4 * 3600 * 1000), // 4 hours ago
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=800', displayOrder: 1 },
      ],
    },
    // 3. Post by An Nhiên (Home Card 3)
    {
      authorId: anNhien.id,
      content:
        'Bánh mousse dâu tây mọng nước ngọt thanh nhẹ dịu, lớp kem mềm mịn tan ngay trong miệng. Rất hợp cho buổi trà chiều thư giãn cùng bạn bè! 🍰🍓',
      locationUrl: 'https://maps.google.com/?q=Tiem+Banh+Ngot+Nha+An',
      voucherCode: 'MOUSSE10',
      hashtags: ['BanhNgot', 'DessertLover', 'TraChieu'],
      createdAt: new Date(now.getTime() - 6 * 3600 * 1000), // 6 hours ago
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=800', displayOrder: 1 },
      ],
    },
    // 4. Restaurant & Community Posts
    {
      authorId: phothin.id,
      content:
        'Phở Thìn Lò Đúc chào tuần mới! Bát phở bò tái lăn bốc khói nghi ngút, hành hoa tươi xanh, thịt bò xào lăn thơm lừng tỏi ớt. Kính mời quý thực khách ghé thưởng thức!',
      locationUrl: 'https://maps.google.com/?q=Pho+Thin+13+Lo+Duc+Hanoi',
      voucherCode: 'PHOTHIN10',
      hashtags: ['phothin', 'phohanoi', 'phobotailan', 'monngonhanoi'],
      createdAt: new Date(now.getTime() - 24 * 3600 * 1000),
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800', displayOrder: 1 },
        { imageUrl: 'https://images.unsplash.com/photo-1569058242253-92a9c755a0ec?w=800', displayOrder: 2 },
      ],
    },
    {
      authorId: banhmi25.id,
      content:
        'Bánh mì giòn rụm vừa ra lò! Pate gan béo ngậy, sốt trứng thơm lừng kèm xá xíu đậm vị. Thưởng thức cùng một ly trà đá phố cổ mát lạnh nhé cả nhà!',
      locationUrl: 'https://maps.google.com/?q=Banh+Mi+25+Hang+Ca',
      voucherCode: 'BANHMI25K',
      hashtags: ['banhmi', 'banhmi25', 'streetfood', 'vietnamesebread'],
      createdAt: new Date(now.getTime() - 48 * 3600 * 1000),
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800', displayOrder: 1 },
      ],
    },
    {
      authorId: bunchahuonglien.id,
      content:
        'Bún chả gia truyền Hương Liên từng vinh dự được cựu Tổng thống Obama và cố đầu bếp Anthony Bourdain ghé thăm. Thịt nướng than hoa vàng rộm, nước mắm thơm lừng vị đu đủ xanh.',
      locationUrl: 'https://maps.google.com/?q=Bun+Cha+Huong+Lien+24+Le+Van+Huu',
      voucherCode: 'OBAMASET',
      hashtags: ['bunchahuonglien', 'bunchaobama', 'hanoicuisine', 'amthucviet'],
      createdAt: new Date(now.getTime() - 72 * 3600 * 1000),
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1559847844-5315695dadae?w=800', displayOrder: 1 },
      ],
    },
    {
      authorId: userA.id,
      content:
        'Sáng nay ghé làm bát bún ốc cô Huệ phố Hàng Giầy. Ốc to giòn sần sật, nước dùng chua thanh vị giấm bỗng và cay the của ớt chưng. Đúng chuẩn vị ốc Hà Nội xưa!',
      locationUrl: 'https://maps.google.com/?q=Bun+Oc+Co+Hue+Hang+Giay',
      hashtags: ['bunoc', 'hanggiay', 'hanoifoodie', 'monngonmoingay'],
      createdAt: new Date(now.getTime() - 96 * 3600 * 1000),
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1541832676-9b763b0239ab?w=800', displayOrder: 1 },
      ],
    },
    {
      authorId: userB.id,
      content:
        'Cuối tuần ghé quán cafe tone trắng phong cách Nhật Bản nhẹ nhàng cực chill ☕ Bánh matcha crepe ở đây mềm mịn, vị ngọt vừa vặn không hề bị gắt. Rất đáng thử nha!',
      locationUrl: 'https://maps.google.com/?q=Cafe+Chill+Hoan+Kiem',
      hashtags: ['coffee', 'cafehanoi', 'matcha', 'weekendchill'],
      createdAt: new Date(now.getTime() - 120 * 3600 * 1000),
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800', displayOrder: 1 },
        { imageUrl: 'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800', displayOrder: 2 },
      ],
    },
  ];

  const createdPosts: any[] = [];
  for (const postItem of postsData) {
    const { images, ...data } = postItem;
    const post = await prisma.post.create({
      data: {
        ...data,
        images: { create: images },
      },
    });
    createdPosts.push(post);
  }

  // ─────────────────────────────────────────
  // 6. SEED LIKES, SHARES & BOOKMARKS
  // ─────────────────────────────────────────
  console.log('❤️ Seeding post likes & shares...');

  // Likes
  await prisma.postLike.createMany({
    data: [
      // Post 1 (Maya Lin)
      { userId: userA.id, postId: createdPosts[0].id },
      { userId: userB.id, postId: createdPosts[0].id },
      { userId: userC.id, postId: createdPosts[0].id },
      { userId: julianCole.id, postId: createdPosts[0].id },
      { userId: anNhien.id, postId: createdPosts[0].id },
      { userId: minhAnh.id, postId: createdPosts[0].id },
      { userId: linhHa.id, postId: createdPosts[0].id },
      { userId: quangTran.id, postId: createdPosts[0].id },
      // Post 2 (Julian Cole)
      { userId: mayaLin.id, postId: createdPosts[1].id },
      { userId: userA.id, postId: createdPosts[1].id },
      { userId: userB.id, postId: createdPosts[1].id },
      { userId: anNhien.id, postId: createdPosts[1].id },
      { userId: minhAnh.id, postId: createdPosts[1].id },
      { userId: quangTran.id, postId: createdPosts[1].id },
      // Post 3 (An Nhiên)
      { userId: mayaLin.id, postId: createdPosts[2].id },
      { userId: julianCole.id, postId: createdPosts[2].id },
      { userId: userB.id, postId: createdPosts[2].id },
      { userId: userC.id, postId: createdPosts[2].id },
      { userId: linhHa.id, postId: createdPosts[2].id },
      // Other posts
      { userId: userA.id, postId: createdPosts[3].id },
      { userId: userB.id, postId: createdPosts[3].id },
      { userId: userC.id, postId: createdPosts[3].id },
      { userId: userA.id, postId: createdPosts[4].id },
      { userId: userC.id, postId: createdPosts[4].id },
      { userId: userB.id, postId: createdPosts[5].id },
      { userId: userC.id, postId: createdPosts[6].id },
    ],
    skipDuplicates: true,
  });

  // Shares
  await prisma.postShare.createMany({
    data: [
      { userId: userA.id, postId: createdPosts[0].id },
      { userId: userB.id, postId: createdPosts[0].id },
      { userId: minhAnh.id, postId: createdPosts[1].id },
      { userId: userA.id, postId: createdPosts[3].id },
      { userId: userB.id, postId: createdPosts[4].id },
    ],
    skipDuplicates: true,
  });

  // Saved Posts (Bookmarks)
  await prisma.savedPost.createMany({
    data: [
      { userId: userA.id, postId: createdPosts[0].id },
      { userId: quangTran.id, postId: createdPosts[0].id },
      { userId: mayaLin.id, postId: createdPosts[1].id },
      { userId: userB.id, postId: createdPosts[2].id },
      { userId: userA.id, postId: createdPosts[3].id },
      { userId: userA.id, postId: createdPosts[5].id },
      { userId: userB.id, postId: createdPosts[7].id },
    ],
    skipDuplicates: true,
  });

  // ─────────────────────────────────────────
  // 7. SEED COMMENTS & REPLIES
  // ─────────────────────────────────────────
  console.log('💬 Seeding comments...');

  // Comments for Post 1 (Maya Lin)
  const commentMaya1 = await prisma.comment.create({
    data: {
      postId: createdPosts[0].id,
      authorId: minhAnh.id,
      content: 'Nhìn tươi ngon quá chị ơi! Quán này có bán mang về không ạ? 🥗',
    },
  });

  await prisma.comment.create({
    data: {
      postId: createdPosts[0].id,
      authorId: linhHa.id,
      content: 'Có nha bạn, mình vừa đặt qua app tuần trước ship nhanh lắm nè! ✨',
      parentCommentId: commentMaya1.id,
    },
  });

  await prisma.comment.create({
    data: {
      postId: createdPosts[0].id,
      authorId: quangTran.id,
      content: 'Đậu gà ở đây làm đỉnh thật sự, ăn giòn bùi không bị khô chút nào 💯',
    },
  });

  // Comments for Post 2 (Julian Cole)
  await prisma.comment.create({
    data: {
      postId: createdPosts[1].id,
      authorId: mayaLin.id,
      content: 'Pasta thủ công nhìn hấp dẫn quá anh! Sốt nấm truffle chuẩn phong cách Ý 🍝🇮🇹',
    },
  });

  await prisma.comment.create({
    data: {
      postId: createdPosts[1].id,
      authorId: quangTran.id,
      content: 'Sốt kem béo mịn ngậy mùi truffle, đĩa này chuẩn 10/10 luôn!',
    },
  });

  // Comments for Post 3 (An Nhiên)
  await prisma.comment.create({
    data: {
      postId: createdPosts[2].id,
      authorId: minhAnh.id,
      content: 'Bánh mousse dâu tây xinh xỉu! Cuối tuần nhất định phải ghé thử cùng hội bạn 🍰🍓',
    },
  });

  // Existing comments for other posts
  const comment1 = await prisma.comment.create({
    data: {
      postId: createdPosts[3].id,
      authorId: userA.id,
      content: 'Phở Thìn ăn từ nhỏ đến lớn vẫn mê nhất là nước dùng nhiều béo và hành!',
    },
  });

  await prisma.comment.create({
    data: {
      postId: createdPosts[3].id,
      authorId: phothin.id,
      content: 'Cảm ơn bạn đã luôn ủng hộ Phở Thìn suốt nhiều năm qua ạ! ❤️',
      parentCommentId: comment1.id,
    },
  });

  await prisma.comment.create({
    data: {
      postId: createdPosts[4].id,
      authorId: userB.id,
      content: 'Bánh mì pate ở đây ngon số dzách luôn, khách Tây khách Việt lúc nào cũng đông!',
    },
  });

  // ─────────────────────────────────────────
  // 8. SEED REVIEWS & REVIEW IMAGES
  // ─────────────────────────────────────────
  console.log('⭐ Seeding reviews...');

  await prisma.review.create({
    data: {
      authorId: userA.id,
      restaurantName: 'Phở Thìn Lò Đúc',
      locationUrl: 'https://maps.google.com/?q=Pho+Thin+13+Lo+Duc',
      rating: 5,
      content:
        'Hương vị tuyệt hảo! Nước phở đậm đà, thịt bò tái lăn xào xém cạnh thơm phức. Không gian quán mang nét cổ kính của Hà Nội.',
      tags: ['Phở ngon', 'Đậm vị', 'Nổi tiếng'],
      images: {
        create: [{ imageUrl: 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800', displayOrder: 1 }],
      },
    },
  });

  await prisma.review.create({
    data: {
      authorId: userB.id,
      restaurantName: 'Bánh Mì 25 Hàng Cá',
      locationUrl: 'https://maps.google.com/?q=Banh+Mi+25+Hang+Ca',
      rating: 5,
      content:
        'Bánh mì cực kỳ thơm và giòn tan. Đồ chua ăn kèm chống ngấy rất tốt, nhân viên thân thiện và order nhanh.',
      tags: ['Bánh mì ngon', 'Phố cổ', 'Giá tốt'],
      images: {
        create: [{ imageUrl: 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800', displayOrder: 1 }],
      },
    },
  });

  await prisma.review.create({
    data: {
      authorId: userC.id,
      restaurantName: 'Bún Chả Hương Liên',
      locationUrl: 'https://maps.google.com/?q=Bun+Cha+Huong+Lien',
      rating: 4,
      content:
        'Chả nướng thơm ngon, nem cua bể giòn rụm. Buổi trưa quán hơi đông nên phải chờ bàn một chút nhưng rất xứng đáng.',
      tags: ['Bún chả', 'Nem cua bể', 'Quán đông'],
    },
  });

  // ─────────────────────────────────────────
  // 9. SEED FOLLOWS
  // ─────────────────────────────────────────
  console.log('🤝 Seeding follows...');

  await prisma.follow.createMany({
    data: [
      { followerId: userA.id, followingId: phothin.id },
      { followerId: userA.id, followingId: banhmi25.id },
      { followerId: userA.id, followingId: userB.id },
      { followerId: userB.id, followingId: bunchahuonglien.id },
      { followerId: userB.id, followingId: userA.id },
      { followerId: userC.id, followingId: userA.id },
      { followerId: userC.id, followingId: phothin.id },
    ],
    skipDuplicates: true,
  });

  // ─────────────────────────────────────────
  // 10. SEED MESSAGES (CHATS)
  // ─────────────────────────────────────────
  console.log('✉️ Seeding chat messages...');

  await prisma.message.createMany({
    data: [
      {
        senderId: userA.id,
        recipientId: phothin.id,
        content: 'Dạ quán cho em hỏi trưa nay nhóm 6 người còn bàn không ạ?',
        isRead: true,
      },
      {
        senderId: phothin.id,
        recipientId: userA.id,
        content: 'Chào bạn, trưa nay quán vẫn còn bàn phục vụ nhóm 6 người bạn nhé. Hân hạnh đón tiếp bạn!',
        isRead: true,
      },
      {
        senderId: userB.id,
        recipientId: banhmi25.id,
        content: 'Bên mình có nhận đặt ship qua GrabFood không quán ơi?',
        isRead: false,
      },
    ],
  });

  // ─────────────────────────────────────────
  // 11. SEED NOTIFICATIONS
  // ─────────────────────────────────────────
  console.log('🔔 Seeding notifications...');

  await prisma.notification.createMany({
    data: [
      {
        userId: phothin.id,
        type: NotificationType.Like,
        message: 'Nguyễn Văn A đã thích bài viết của bạn',
        relatedPostId: createdPosts[0].id,
      },
      {
        userId: phothin.id,
        type: NotificationType.Comment,
        message: 'Nguyễn Văn A đã bình luận vào bài viết của bạn',
        relatedPostId: createdPosts[0].id,
      },
      {
        userId: userB.id,
        type: NotificationType.Follow,
        message: 'Nguyễn Văn A đã bắt đầu theo dõi bạn',
      },
      {
        userId: userA.id,
        type: NotificationType.System,
        message: '[Hệ thống] Chào mừng bạn gia nhập cộng đồng sành ăn Dishcision! Khám phá ngay các ưu đãi hôm nay.',
      },
      {
        userId: userB.id,
        type: NotificationType.System,
        message: '[Hệ thống] Cập nhật phiên bản mới: Đã có tính năng Vòng quay ngẫu nhiên món ăn.',
      },
    ],
  });

  // ─────────────────────────────────────────
  // 12. SEED AI REQUESTS & FEEDBACK
  // ─────────────────────────────────────────
  console.log('🧠 Seeding AI requests...');

  await prisma.aIRequest.create({
    data: {
      userId: userA.id,
      mood: 'Hôm nay hơi mệt, cần món gì ấm bụng và bổ dưỡng',
      budget: '60000',
      companions: 1,
      craving: 'Món nước nóng hổi',
      resultJson: [
        { rank: 1, dishName: 'Phở bò tái nạm', reason: 'Nước dùng ninh xương ấm nóng bổ sung năng lượng tức thì', estimatedPrice: '55,000 VND' },
        { rank: 2, dishName: 'Bún bò Huế', reason: 'Vị cay nồng ấm bụng, kích thích vị giác giải cảm', estimatedPrice: '60,000 VND' },
        { rank: 3, dishName: 'Cháo sườn quẩy', reason: 'Thanh nhẹ, dễ tiêu hóa cho ngày mỏi mệt', estimatedPrice: '35,000 VND' },
      ],
      modelUsed: 'gemini-2.0-flash',
      feedbackRating: 5,
      feedbackComment: 'Gợi ý phở bò cực kỳ chuẩn tâm trạng!',
    },
  });

  await prisma.aIRequest.create({
    data: {
      userId: userB.id,
      mood: 'Vui vẻ, muốn tụ tập cùng 3 người bạn thân',
      budget: '200000',
      companions: 4,
      craving: 'Đồ nướng hoặc lẩu',
      resultJson: [
        { rank: 1, dishName: 'Lẩu nấm gà ác', reason: 'Thanh đạm, bổ dưỡng, rất hợp ngồi trò chuyện rôm rả', estimatedPrice: '180,000 VND/người' },
        { rank: 2, dishName: 'Lẩu riêu cua bắp bò', reason: 'Đậm đà hương vị truyền thống Hà Nội', estimatedPrice: '150,000 VND/người' },
      ],
      modelUsed: 'gemini-2.0-flash',
      feedbackRating: 4,
      feedbackComment: 'Quán lẩu gợi ý rất ngon.',
    },
  });

  // ─────────────────────────────────────────
  // 13. SEED AI CONFIGS & PROMPTS
  // ─────────────────────────────────────────
  console.log('⚙️ Seeding AI configuration & prompts...');

  const aiConfigs = [
    {
      modelName: 'gemini-2.0-flash',
      provider: 'Google',
      isActive: true,
      maxTokens: 1024,
      temperature: 0.7,
    },
    {
      modelName: 'gpt-4o-mini',
      provider: 'OpenAI',
      isActive: false,
      maxTokens: 1024,
      temperature: 0.7,
    },
  ];

  for (const config of aiConfigs) {
    const existing = await prisma.aIConfig.findFirst({ where: { modelName: config.modelName } });
    if (!existing) {
      await prisma.aIConfig.create({ data: config });
    }
  }

  const existingPrompt = await prisma.aIPrompt.findFirst({ where: { name: 'Food Suggestion System Prompt' } });
  if (!existingPrompt) {
    await prisma.aIPrompt.create({
      data: {
        name: 'Food Suggestion System Prompt',
        content:
          'Bạn là chuyên gia tư vấn ẩm thực Việt Nam thông thái và am hiểu tâm lý của ứng dụng Dishcision. Dựa trên tâm trạng (mood), ngân sách (budget), số người cùng đi (companions) và sở thích cá nhân, hãy đề xuất 3 món ăn chuẩn vị kèm quán ăn tiêu biểu.',
        isActive: true,
      },
    });
  }

  // ─────────────────────────────────────────
  // 14. SEED REPORTS (FOR ADMIN MODERATION TEST)
  // ─────────────────────────────────────────
  console.log('🚨 Seeding sample reports for admin...');

  await prisma.report.create({
    data: {
      reporterId: userB.id,
      postId: createdPosts[1].id,
      reason: ReportReason.Spam,
      description: 'Bài viết quảng cáo lặp đi lặp lại nhiều lần trong ngày',
      status: ReportStatus.Pending,
      priority: ReportPriority.Medium,
    },
  });

  // ─────────────────────────────────────────
  // 15. SEED AUDIT LOGS
  // ─────────────────────────────────────────
  console.log('📋 Seeding admin audit logs...');

  await prisma.auditLog.createMany({
    data: [
      {
        adminId: admin.id,
        action: 'INITIALIZE_SYSTEM',
        targetType: AuditTargetType.User,
        targetId: admin.id,
        ipAddress: '127.0.0.1',
        details: 'Khởi tạo hệ thống Dishcision và dữ liệu nền tảng ban đầu',
      },
      {
        adminId: admin.id,
        action: 'SWITCH_AI_CONFIG',
        targetType: AuditTargetType.AIConfig,
        targetId: 1,
        ipAddress: '127.0.0.1',
        details: 'Đặt model gemini-2.0-flash làm mô hình AI hoạt động chính',
      },
    ],
  });

  console.log('🎉 Comprehensive database seeding completed successfully!');
}

main()
  .catch((e) => {
    console.error('❌ Seeding failed:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
