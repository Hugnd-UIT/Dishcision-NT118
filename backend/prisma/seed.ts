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
  Gender,
} from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

function mapExternalToLocalUrl(url: string | null | undefined, fallback: string): string | null {
  if (!url || !url.includes('images.unsplash.com')) {
    return null;
  }
  return fallback;
}

interface SampleUserInput {
  username: string;
  email: string;
  passwordHash: string;
  fullName: string;
  role: Role;
  avatarUrl: string;
  bio: string;
  gender?: Gender;
  dateOfBirth?: Date;
}

async function ensureSampleUser(input: SampleUserInput) {
  const existing = await prisma.user.findFirst({
    where: {
      OR: [{ username: input.username }, { email: input.email }],
    },
  });

  if (existing) {
    const needsAvatarUpdate = !existing.avatarUrl || existing.avatarUrl.startsWith('https://images.unsplash.com/');
    if (needsAvatarUpdate) {
      return prisma.user.update({
        where: { id: existing.id },
        data: { avatarUrl: input.avatarUrl },
      });
    }
    return existing;
  }

  return prisma.user.create({
    data: {
      username: input.username,
      email: input.email,
      passwordHash: input.passwordHash,
      fullName: input.fullName,
      role: input.role,
      accountStatus: AccountStatus.Active,
      avatarUrl: input.avatarUrl,
      bio: input.bio,
      gender: input.gender,
      dateOfBirth: input.dateOfBirth,
    },
  });
}

async function main() {
  console.log('Starting non-destructive database seeding...');

  if (process.env.SEED_RESET === 'true') {
    console.log('SEED_RESET=true: clearing sample content tables...');
    await prisma.report.deleteMany({});
    await prisma.commentLike.deleteMany({});
    await prisma.comment.deleteMany({});
    await prisma.postLike.deleteMany({});
    await prisma.postShare.deleteMany({});
    await prisma.savedPost.deleteMany({});
    await prisma.postImage.deleteMany({});
    await prisma.post.deleteMany({});
  }

  const adminPasswordHash = await bcrypt.hash('Admin@Dishcision2026', 12);
  const userPasswordHash = await bcrypt.hash('Password@123', 12);

  const admin = await ensureSampleUser({
    username: 'admin',
    email: 'admin@dishcision.com',
    passwordHash: adminPasswordHash,
    fullName: 'Quản trị viên Dishcision',
    role: Role.Admin,
    avatarUrl: '/uploads/images/sample-avatar-admin.jpg',
    bio: 'Dishcision Head Administrator',
  });

  const tester = await ensureSampleUser({
    username: 'tester_foodie',
    email: 'tester@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Minh Khôi (Tester)',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-tester.jpg',
    bio: 'Tài khoản kiểm thử đầy đủ tính năng Dishcision 🍜🍕☕',
    gender: Gender.Male,
    dateOfBirth: new Date('2000-06-15'),
  });

  const tester2 = await ensureSampleUser({
    username: 'tester2_foodie',
    email: 'tester2@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Khánh Linh (Tester 2)',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-minhanh.jpg',
    bio: 'Tài khoản kiểm thử 2 — Ẩm thực Sài Gòn & Hà Nội 🍲🍰☕',
    gender: Gender.Female,
    dateOfBirth: new Date('2001-08-22'),
  });

  const googleDemo = await ensureSampleUser({
    username: 'google_foodie',
    email: 'google_demo@dishcision.com',
    passwordHash: '',
    fullName: 'Thảo Vy (Google Demo)',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-google.jpg',
    bio: 'Tài khoản Google mẫu không dùng mật khẩu 🌟',
    gender: Gender.Female,
    dateOfBirth: new Date('2001-04-20'),
  });

  const phothin = await ensureSampleUser({
    username: 'phothin',
    email: 'phothin@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Phở Thìn Lò Đúc',
    role: Role.RestaurantAccount,
    avatarUrl: '/uploads/images/sample-food-pho1.jpg',
    bio: 'Phở bò tái lăn gia truyền số 13 Lò Đúc từ năm 1979 🍜',
  });

  const banhmi25 = await ensureSampleUser({
    username: 'banhmi25',
    email: 'banhmi25@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Bánh Mì 25',
    role: Role.RestaurantAccount,
    avatarUrl: '/uploads/images/sample-food-banhmi.jpg',
    bio: 'Bánh mì truyền thống phố cổ Hà Nội số 25 Hàng Cá 🥖',
  });

  const bunchahuonglien = await ensureSampleUser({
    username: 'bunchahuonglien',
    email: 'bunchahuonglien@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Bún Chả Hương Liên',
    role: Role.RestaurantAccount,
    avatarUrl: '/uploads/images/sample-food-buncha.jpg',
    bio: 'Bún chả Obama gia truyền tại 24 Lê Văn Hưu, Hà Nội',
  });

  const comtamBaNghien = await ensureSampleUser({
    username: 'comtam_banghien',
    email: 'comtam_banghien@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Cơm Tấm Ba Nghiền',
    role: Role.RestaurantAccount,
    avatarUrl: '/uploads/images/sample-food-comtam.jpg',
    bio: 'Cơm tấm sườn nướng than hoa trứ danh Phú Nhuận từ 1995 🔥',
  });

  const tocotoco = await ensureSampleUser({
    username: 'tocotoco_cg',
    email: 'tocotoco_cg@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Trà Sữa TocoToco',
    role: Role.RestaurantAccount,
    avatarUrl: '/uploads/images/sample-food-milktea.jpg',
    bio: 'Trà sữa nông sản Việt — 156 Cầu Giấy, Hà Nội 🧋',
  });

  const userA = await ensureSampleUser({
    username: 'nguyenvana',
    email: 'nguyenvana@gmail.com',
    passwordHash: userPasswordHash,
    fullName: 'Nguyễn Văn A',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-usera.jpg',
    bio: 'Đam mê ẩm thực đường phố và săn quán ngon Hà Nội 🍜🌶️',
    gender: Gender.Male,
    dateOfBirth: new Date('1998-05-15'),
  });

  const userB = await ensureSampleUser({
    username: 'tranthib',
    email: 'tranthib@gmail.com',
    passwordHash: userPasswordHash,
    fullName: 'Trần Thị B',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-tranthib.jpg',
    bio: 'Food reviewer thích bánh ngọt & cà phê chill cuối tuần 🍰☕',
    gender: Gender.Female,
    dateOfBirth: new Date('2001-09-20'),
  });

  const userC = await ensureSampleUser({
    username: 'lequangc',
    email: 'lequangc@gmail.com',
    passwordHash: userPasswordHash,
    fullName: 'Lê Quang C',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-userc.jpg',
    bio: 'Tín đồ ẩm thực cay nóng & lẩu nướng sinh viên 🍲🔥',
    gender: Gender.Male,
    dateOfBirth: new Date('2002-12-10'),
  });

  const mayaLin = await ensureSampleUser({
    username: 'mayafoodie',
    email: 'mayafoodie@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Maya Lin',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-maya.jpg',
    bio: 'Foodie đam mê các món chay thanh đạm & ẩm thực xanh 🥗✨',
    gender: Gender.Female,
    dateOfBirth: new Date('2000-03-12'),
  });

  const julianCole = await ensureSampleUser({
    username: 'juliancole',
    email: 'juliancole@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Julian Cole',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-julian.jpg',
    bio: 'Pasta lover & Italian cuisine enthusiast 🍝🇮🇹',
    gender: Gender.Male,
    dateOfBirth: new Date('1997-08-25'),
  });

  const anNhien = await ensureSampleUser({
    username: 'annhien_food',
    email: 'annhien@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'An Nhiên',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-annhien.jpg',
    bio: 'Tiệm bánh ngọt và những tách trà chiều 🍰🍓',
    gender: Gender.Female,
    dateOfBirth: new Date('2001-11-05'),
  });

  const minhAnh = await ensureSampleUser({
    username: 'minhanh',
    email: 'minhanh@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Minh Anh',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-minhanh.jpg',
    bio: 'Thực khách sành ăn Hà Nội 🥗',
    gender: Gender.Female,
    dateOfBirth: new Date('2002-04-18'),
  });

  const linhHa = await ensureSampleUser({
    username: 'linhha',
    email: 'linhha@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Linh Hà',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-linhha.jpg',
    bio: 'Mê đồ chay và trà thảo mộc 🍵',
    gender: Gender.Female,
    dateOfBirth: new Date('1999-07-22'),
  });

  const quangTran = await ensureSampleUser({
    username: 'quangtran',
    email: 'quangtran@dishcision.com',
    passwordHash: userPasswordHash,
    fullName: 'Quang Trần',
    role: Role.User,
    avatarUrl: '/uploads/images/sample-avatar-quangtran.jpg',
    bio: 'Đam mê ẩm thực Địa Trung Hải & nấm truffle 🍄',
    gender: Gender.Male,
    dateOfBirth: new Date('1996-10-30'),
  });

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

  const preferences: { id: number; name: string }[] = [];
  for (const p of preferenceData) {
    let pref = await prisma.foodPreference.findFirst({ where: { name: p.name } });
    if (!pref) {
      pref = await prisma.foodPreference.create({ data: p });
    }
    preferences.push(pref);
  }

  await prisma.userPreference.createMany({
    data: [
      { userId: tester.id, preferenceId: preferences[0].id },
      { userId: tester.id, preferenceId: preferences[3].id },
      { userId: tester.id, preferenceId: preferences[6].id },
      { userId: tester2.id, preferenceId: preferences[1].id },
      { userId: tester2.id, preferenceId: preferences[2].id },
      { userId: tester2.id, preferenceId: preferences[4].id },
      { userId: tester2.id, preferenceId: preferences[7].id },
      { userId: googleDemo.id, preferenceId: preferences[1].id },
      { userId: googleDemo.id, preferenceId: preferences[5].id },
      { userId: userA.id, preferenceId: preferences[0].id },
      { userId: userA.id, preferenceId: preferences[3].id },
      { userId: userA.id, preferenceId: preferences[6].id },
      { userId: userB.id, preferenceId: preferences[1].id },
      { userId: userB.id, preferenceId: preferences[5].id },
      { userId: userC.id, preferenceId: preferences[0].id },
      { userId: userC.id, preferenceId: preferences[7].id },
      { userId: mayaLin.id, preferenceId: preferences[2].id },
      { userId: mayaLin.id, preferenceId: preferences[5].id },
      { userId: julianCole.id, preferenceId: preferences[0].id },
      { userId: anNhien.id, preferenceId: preferences[1].id },
    ],
    skipDuplicates: true,
  });

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

  const now = new Date();
  const postsData = [
    {
      authorId: mayaLin.id,
      content: 'Tìm thấy tô salad ngũ sắc tươi ngon nhất phố! Từng hạt đậu gà rang giòn ngấm vị ớt paprika xông khói kết hợp xốt tahini béo bùi chuẩn vị. Cực kỳ đáng thử! 🥗✨',
      locationUrl: 'https://maps.google.com/?q=The+Green+Fork+Bistro',
      voucherCode: 'SALADVEG15',
      hashtags: ['MonChay', 'AnLanhManh', 'AmThucXanh'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 2 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-salad.jpg', displayOrder: 1 }],
    },
    {
      authorId: julianCole.id,
      content: 'Món pasta thủ công tươi mới vừa ra lò chiều nay! Sốt kem béo mịn thơm lừng mùi nấm truffle thượng hạng. Mời cả nhà cùng thưởng thức! 🍝🇮🇹',
      locationUrl: 'https://maps.google.com/?q=Trattoria+Bella',
      voucherCode: 'TRUFFLE20',
      hashtags: ['ItalianFood', 'Pasta', 'TruffleLover'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 4 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-pasta.jpg', displayOrder: 1 }],
    },
    {
      authorId: anNhien.id,
      content: 'Bánh mousse dâu tây mọng nước ngọt thanh nhẹ dịu, lớp kem mềm mịn tan ngay trong miệng. Rất hợp cho buổi trà chiều thư giãn cùng bạn bè! 🍰🍓',
      locationUrl: 'https://maps.google.com/?q=Tiem+Banh+Ngot+Nha+An',
      voucherCode: 'MOUSSE10',
      hashtags: ['BanhNgot', 'DessertLover', 'TraChieu'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 6 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-mousse.jpg', displayOrder: 1 }],
    },
    {
      authorId: phothin.id,
      content: 'Phở Thìn Lò Đúc chào tuần mới! Bát phở bò tái lăn bốc khói nghi ngút, hành hoa tươi xanh, thịt bò xào lăn thơm lừng tỏi ớt. Kính mời quý thực khách ghé thưởng thức!',
      locationUrl: 'https://maps.google.com/?q=Pho+Thin+13+Lo+Duc+Hanoi',
      voucherCode: 'PHOTHIN10',
      hashtags: ['phothin', 'phohanoi', 'phobotailan', 'monngonhanoi'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 24 * 3600 * 1000),
      images: [
        { imageUrl: '/uploads/images/sample-food-pho1.jpg', displayOrder: 1 },
        { imageUrl: '/uploads/images/sample-food-pho2.jpg', displayOrder: 2 },
      ],
    },
    {
      authorId: banhmi25.id,
      content: 'Bánh mì giòn rụm vừa ra lò! Pate gan béo ngậy, sốt trứng thơm lừng kèm xá xíu đậm vị. Thưởng thức cùng một ly trà đá phố cổ mát lạnh nhé cả nhà!',
      locationUrl: 'https://maps.google.com/?q=Banh+Mi+25+Hang+Ca',
      voucherCode: 'BANHMI25K',
      hashtags: ['banhmi', 'banhmi25', 'streetfood', 'vietnamesebread'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 48 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-banhmi.jpg', displayOrder: 1 }],
    },
    {
      authorId: bunchahuonglien.id,
      content: 'Bún chả gia truyền Hương Liên từng vinh dự được cựu Tổng thống Obama và cố đầu bếp Anthony Bourdain ghé thăm. Thịt nướng than hoa vàng rộm, nước mắm thơm lừng vị đu đủ xanh.',
      locationUrl: 'https://maps.google.com/?q=Bun+Cha+Huong+Lien+24+Le+Van+Huu',
      voucherCode: 'OBAMASET',
      hashtags: ['bunchahuonglien', 'bunchaobama', 'hanoicuisine', 'amthucviet'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 72 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-buncha.jpg', displayOrder: 1 }],
    },
    {
      authorId: comtamBaNghien.id,
      content: 'Sườn nướng mật ong vàng ươm vừa ra khỏi bếp than hoa! Miếng sườn dày mềm mọng ăn kèm bì thính và chả trứng hấp béo bùi tại Cơm Tấm Ba Nghiền.',
      locationUrl: 'https://maps.google.com/?q=Com+Tam+Ba+Nghien+84+Dang+Van+Ngu',
      voucherCode: 'COMTAM20',
      hashtags: ['comtam', 'saigonfood', 'suonbicha'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 80 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-comtam.jpg', displayOrder: 1 }],
    },
    {
      authorId: tocotoco.id,
      content: 'Buổi chiều nạp lại năng lượng với ly trà sữa trân châu sợi dai giòn và matcha kem cheese mát lạnh tại TocoToco Cầu Giấy 🧋✨',
      locationUrl: 'https://maps.google.com/?q=TocoToco+156+Cau+Giay+Hanoi',
      voucherCode: 'TOCOTOCO15',
      hashtags: ['trasua', 'tocotoco', 'hanoi'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 90 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-milktea.jpg', displayOrder: 1 }],
    },
    {
      authorId: userA.id,
      content: 'Sáng nay ghé làm bát bún ốc cô Huệ phố Hàng Giầy. Ốc to giòn sần sật, nước dùng chua thanh vị giấm bỗng và cay the của ớt chưng. Đúng chuẩn vị ốc Hà Nội xưa!',
      locationUrl: 'https://maps.google.com/?q=Bun+Oc+Co+Hue+Hang+Giay',
      hashtags: ['bunoc', 'hanggiay', 'hanoifoodie', 'monngonmoingay'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 96 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-bunoc.jpg', displayOrder: 1 }],
    },
    {
      authorId: userB.id,
      content: 'Cuối tuần ghé quán cafe tone trắng phong cách Nhật Bản nhẹ nhàng cực chill ☕ Bánh matcha crepe ở đây mềm mịn, vị ngọt vừa vặn không hề bị gắt. Rất đáng thử nha!',
      locationUrl: 'https://maps.google.com/?q=Cafe+Chill+Hoan+Kiem',
      hashtags: ['coffee', 'cafehanoi', 'matcha', 'weekendchill'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 120 * 3600 * 1000),
      images: [
        { imageUrl: '/uploads/images/sample-food-cafe1.jpg', displayOrder: 1 },
        { imageUrl: '/uploads/images/sample-food-cafe2.jpg', displayOrder: 2 },
      ],
    },
    {
      authorId: userC.id,
      content: 'Trời chuyển se lạnh rủ hội bạn thân làm nồi lẩu nóng hổi nghi ngút khói! Nước lẩu chua cay đậm đà, topping nhúng thả ga 🍲🔥',
      locationUrl: 'https://maps.google.com/?q=Lau+Thai+Binh+Dan+Dong+Da',
      voucherCode: 'HOTPOT15',
      hashtags: ['launuong', 'amthucsinhvien', 'hanoifood'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 130 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-hotpot.jpg', displayOrder: 1 }],
    },
    {
      authorId: tester.id,
      content: 'Bữa tối hoàn hảo tại Bistro trung tâm với pizza lò củi viền giòn xốp và phô mai kéo sợi béo ngậy 🍕✨ Ai có gợi ý quán pizza ngon ở Quận 1 cho mình xin thêm nha!',
      locationUrl: 'https://maps.google.com/?q=Bistro+Sai+Gon+Quan+1',
      voucherCode: 'TESTER20',
      hashtags: ['pizza', 'bistro', 'saigonfood'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 3 * 3600 * 1000),
      images: [
        { imageUrl: '/uploads/images/sample-food-pizza.jpg', displayOrder: 1 },
        { imageUrl: '/uploads/images/sample-food-bistro.jpg', displayOrder: 2 },
      ],
    },
    {
      authorId: tester.id,
      content: 'Hôm nay mọi người ăn trưa món gì thế? Mình đang phân vân giữa bún chả nướng than hoa và cơm tấm sườn bì chả — ai vote giúp mình với!',
      locationUrl: 'https://maps.google.com/?q=Ho+Chi+Minh+City',
      hashtags: ['homnayangi', 'comtam', 'buncha'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 18 * 3600 * 1000),
      images: [],
    },
    {
      authorId: tester.id,
      content: '[Bài viết nháp] Ghi chú riêng những quán ăn đêm đáng quay lại cuối tháng này ở Hà Nội và Sài Gòn.',
      locationUrl: 'https://maps.google.com/?q=Hanoi+Old+Quarter',
      hashtags: ['notes', 'andem'],
      status: PostStatus.Hidden,
      createdAt: new Date(now.getTime() - 140 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-pho2.jpg', displayOrder: 1 }],
    },
    {
      authorId: tester.id,
      content: '[Lưu trữ] Nhật ký thử Matcha & Tiramisu cuối tuần cùng hội bạn tại quán cafe yên tĩnh.',
      locationUrl: 'https://maps.google.com/?q=Hoan+Kiem+Cafe',
      hashtags: ['matcha', 'archive'],
      status: PostStatus.Hidden,
      createdAt: new Date(now.getTime() - 170 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-cafe1.jpg', displayOrder: 1 }],
    },
    {
      authorId: tester2.id,
      content: 'Tối nay tụ tập làm nồi lẩu Thái chua cay và tráng miệng bánh Matcha kem béo cực đã! 🍲🍵 Có mã ưu đãi TESTER2VIP giảm 15% cho hóa đơn nhóm nhé!',
      locationUrl: 'https://maps.google.com/?q=Lau+Thai+Saigon+Quan+3',
      voucherCode: 'TESTER2VIP',
      hashtags: ['lauthaichua', 'saigonfood', 'weekend'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 1 * 3600 * 1000),
      images: [
        { imageUrl: '/uploads/images/sample-food-hotpot.jpg', displayOrder: 1 },
        { imageUrl: '/uploads/images/sample-food-cafe2.jpg', displayOrder: 2 },
      ],
    },
    {
      authorId: tester2.id,
      content: 'Buổi trưa nạp vitamin cùng phần bún chả nướng than hoa vàng ươm, nước mắm chua ngọt vừa miệng. Mọi người hay ăn bún chả ở quán nào ngon? 🥢🔥',
      locationUrl: 'https://maps.google.com/?q=Bun+Cha+Ha+Noi+Quan+1',
      voucherCode: 'LINHFOOD15',
      hashtags: ['buncha', 'hanoifood', 'foodreview'],
      status: PostStatus.Active,
      createdAt: new Date(now.getTime() - 10 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-buncha.jpg', displayOrder: 1 }],
    },
    {
      authorId: tester2.id,
      content: '[Lưu trữ] Ghi chú công thức bánh mousse dâu tây ít đường làm tặng sinh nhật bạn thân.',
      locationUrl: 'https://maps.google.com/?q=Tiem+Banh+Ngot+Saigon',
      hashtags: ['mousse', 'archive'],
      status: PostStatus.Hidden,
      createdAt: new Date(now.getTime() - 150 * 3600 * 1000),
      images: [{ imageUrl: '/uploads/images/sample-food-mousse.jpg', displayOrder: 1 }],
    },
  ];

  const createdPosts: any[] = [];
  for (const postItem of postsData) {
    const { images, ...data } = postItem;
    let post = await prisma.post.findFirst({
      where: { authorId: data.authorId, content: data.content },
      include: { images: true },
    });

    if (!post) {
      post = await prisma.post.create({
        data: {
          ...data,
          images: images.length > 0 ? { create: images } : undefined,
        },
        include: { images: true },
      });
    } else if (post.images && post.images.length > 0) {
      for (const existingImg of post.images) {
        const mapped = mapExternalToLocalUrl(existingImg.imageUrl, '/uploads/images/sample-food-bistro.jpg');
        if (mapped) {
          await prisma.postImage.update({
            where: { id: existingImg.id },
            data: { imageUrl: mapped },
          });
        }
      }
    }
    createdPosts.push(post);
  }

  const unsplashImages = await prisma.postImage.findMany({
    where: { imageUrl: { startsWith: 'https://images.unsplash.com/' } },
  });
  for (const img of unsplashImages) {
    const mapped = mapExternalToLocalUrl(img.imageUrl, '/uploads/images/sample-food-bistro.jpg');
    if (mapped) {
      await prisma.postImage.update({ where: { id: img.id }, data: { imageUrl: mapped } });
    }
  }

  const testerPost = createdPosts[11];
  const tester2Post = createdPosts[15];

  await prisma.postLike.createMany({
    data: [
      { userId: tester.id, postId: createdPosts[0].id },
      { userId: tester.id, postId: createdPosts[3].id },
      { userId: tester.id, postId: tester2Post.id },
      { userId: tester2.id, postId: testerPost.id },
      { userId: tester2.id, postId: createdPosts[0].id },
      { userId: tester2.id, postId: createdPosts[5].id },
      { userId: tester2.id, postId: createdPosts[7].id },
      { userId: userA.id, postId: createdPosts[0].id },
      { userId: userB.id, postId: createdPosts[0].id },
      { userId: userC.id, postId: createdPosts[0].id },
      { userId: julianCole.id, postId: createdPosts[0].id },
      { userId: anNhien.id, postId: createdPosts[0].id },
      { userId: minhAnh.id, postId: createdPosts[0].id },
      { userId: linhHa.id, postId: createdPosts[0].id },
      { userId: quangTran.id, postId: createdPosts[0].id },
      { userId: mayaLin.id, postId: createdPosts[1].id },
      { userId: userA.id, postId: createdPosts[1].id },
      { userId: userB.id, postId: createdPosts[1].id },
      { userId: anNhien.id, postId: createdPosts[1].id },
      { userId: minhAnh.id, postId: createdPosts[1].id },
      { userId: quangTran.id, postId: createdPosts[1].id },
      { userId: mayaLin.id, postId: createdPosts[2].id },
      { userId: julianCole.id, postId: createdPosts[2].id },
      { userId: userB.id, postId: createdPosts[2].id },
      { userId: userC.id, postId: createdPosts[2].id },
      { userId: linhHa.id, postId: createdPosts[2].id },
      { userId: userA.id, postId: createdPosts[3].id },
      { userId: userB.id, postId: createdPosts[3].id },
      { userId: userC.id, postId: createdPosts[3].id },
      { userId: userA.id, postId: createdPosts[4].id },
      { userId: userC.id, postId: createdPosts[4].id },
      { userId: userB.id, postId: createdPosts[5].id },
      { userId: userC.id, postId: createdPosts[6].id },
      { userId: mayaLin.id, postId: testerPost.id },
      { userId: julianCole.id, postId: testerPost.id },
      { userId: userA.id, postId: testerPost.id },
      { userId: mayaLin.id, postId: tester2Post.id },
      { userId: anNhien.id, postId: tester2Post.id },
      { userId: userA.id, postId: tester2Post.id },
    ],
    skipDuplicates: true,
  });

  await prisma.postShare.createMany({
    data: [
      { userId: tester.id, postId: createdPosts[0].id },
      { userId: tester.id, postId: tester2Post.id },
      { userId: tester2.id, postId: testerPost.id },
      { userId: tester2.id, postId: createdPosts[3].id },
      { userId: userA.id, postId: createdPosts[0].id },
      { userId: userB.id, postId: createdPosts[0].id },
      { userId: minhAnh.id, postId: createdPosts[1].id },
      { userId: userA.id, postId: createdPosts[3].id },
      { userId: userB.id, postId: createdPosts[4].id },
    ],
    skipDuplicates: true,
  });

  await prisma.savedPost.createMany({
    data: [
      { userId: tester.id, postId: createdPosts[0].id },
      { userId: tester.id, postId: createdPosts[1].id },
      { userId: tester.id, postId: createdPosts[3].id },
      { userId: tester.id, postId: createdPosts[4].id },
      { userId: tester.id, postId: tester2Post.id },
      { userId: tester2.id, postId: testerPost.id },
      { userId: tester2.id, postId: createdPosts[0].id },
      { userId: tester2.id, postId: createdPosts[5].id },
      { userId: tester2.id, postId: createdPosts[7].id },
      { userId: userA.id, postId: createdPosts[0].id },
      { userId: quangTran.id, postId: createdPosts[0].id },
      { userId: mayaLin.id, postId: createdPosts[1].id },
      { userId: userB.id, postId: createdPosts[2].id },
      { userId: userA.id, postId: createdPosts[3].id },
      { userId: userA.id, postId: createdPosts[5].id },
      { userId: userB.id, postId: createdPosts[9].id },
    ],
    skipDuplicates: true,
  });

  async function ensureComment(postId: number, authorId: number, content: string, parentCommentId?: number) {
    const existing = await prisma.comment.findFirst({
      where: { postId, authorId, content, parentCommentId: parentCommentId ?? null },
    });
    if (existing) return existing;
    return prisma.comment.create({
      data: { postId, authorId, content, parentCommentId },
    });
  }

  const cMaya1 = await ensureComment(createdPosts[0].id, minhAnh.id, 'Nhìn tươi ngon quá chị ơi! Quán này có bán mang về không ạ? 🥗');
  await ensureComment(createdPosts[0].id, linhHa.id, 'Có nha bạn, mình vừa đặt qua app tuần trước ship nhanh lắm nè! ✨', cMaya1.id);
  await ensureComment(createdPosts[0].id, quangTran.id, 'Đậu gà ở đây làm đỉnh thật sự, ăn giòn bùi không bị khô chút nào 💯');
  await ensureComment(createdPosts[1].id, mayaLin.id, 'Pasta thủ công nhìn hấp dẫn quá anh! Sốt nấm truffle chuẩn phong cách Ý 🍝🇮🇹');
  await ensureComment(createdPosts[1].id, quangTran.id, 'Sốt kem béo mịn ngậy mùi truffle, đĩa này chuẩn 10/10 luôn!');
  await ensureComment(createdPosts[2].id, minhAnh.id, 'Bánh mousse dâu tây xinh xỉu! Cuối tuần nhất định phải ghé thử cùng hội bạn 🍰🍓');

  const cPho = await ensureComment(createdPosts[3].id, userA.id, 'Phở Thìn ăn từ nhỏ đến lớn vẫn mê nhất là nước dùng nhiều béo và hành!');
  await ensureComment(createdPosts[3].id, phothin.id, 'Cảm ơn bạn đã luôn ủng hộ Phở Thìn suốt nhiều năm qua ạ! ❤️', cPho.id);
  await ensureComment(createdPosts[4].id, userB.id, 'Bánh mì pate ở đây ngon số dzách luôn, khách Tây khách Việt lúc nào cũng đông!');

  const cTester = await ensureComment(testerPost.id, mayaLin.id, 'Nhìn pizza phô mai kéo sợi hấp dẫn quá! Quán này gần phố đi bộ Nguyễn Huệ nè 🍕');
  await ensureComment(testerPost.id, tester.id, 'Đúng rồi bạn ơi, tối cuối tuần ghé ngồi ban công siêu chill luôn!', cTester.id);
  const cTesterFromT2 = await ensureComment(testerPost.id, tester2.id, 'Pizza lò củi nhìn giòn rụm thích quá, tối nay tụi mình ghé thử luôn nha Khôi! 🍕🔥');
  await ensureComment(testerPost.id, tester.id, 'Chốt đơn nha Linh ơi, nhớ nhập mã TESTER20 đó!', cTesterFromT2.id);

  const cTester2 = await ensureComment(tester2Post.id, tester.id, 'Nồi lẩu Thái nhìn hấp dẫn xỉu! Nước lẩu cay vừa hay cay nồng vậy Linh ơi? 🍲');
  await ensureComment(tester2Post.id, tester2.id, 'Cay nhẹ thơm sả ớt vừa miệng lắm, quán còn tặng thêm bánh Matcha nữa đó!', cTester2.id);

  async function ensureReview(authorId: number, restaurantName: string, locationUrl: string, rating: number, content: string, tags: string[], imageUrl?: string) {
    const existing = await prisma.review.findFirst({
      where: { authorId, restaurantName },
      include: { images: true },
    });
    if (!existing) {
      return prisma.review.create({
        data: {
          authorId,
          restaurantName,
          locationUrl,
          rating,
          content,
          tags,
          images: imageUrl ? { create: [{ imageUrl, displayOrder: 1 }] } : undefined,
        },
      });
    }
    if (imageUrl && existing.images && existing.images.length > 0) {
      for (const img of existing.images) {
        if (img.imageUrl.startsWith('https://images.unsplash.com/')) {
          await prisma.reviewImage.update({ where: { id: img.id }, data: { imageUrl } });
        }
      }
    }
    return existing;
  }

  await ensureReview(
    userA.id,
    'Phở Thìn Lò Đúc',
    'https://maps.google.com/?q=Pho+Thin+13+Lo+Duc',
    5,
    'Hương vị tuyệt hảo! Nước phở đậm đà, thịt bò tái lăn xào xém cạnh thơm phức. Không gian quán mang nét cổ kính của Hà Nội.',
    ['Phở ngon', 'Đậm vị', 'Nổi tiếng'],
    '/uploads/images/sample-food-pho1.jpg',
  );
  await ensureReview(
    userB.id,
    'Bánh Mì 25 Hàng Cá',
    'https://maps.google.com/?q=Banh+Mi+25+Hang+Ca',
    5,
    'Bánh mì cực kỳ thơm và giòn tan. Đồ chua ăn kèm chống ngấy rất tốt, nhân viên thân thiện và order nhanh.',
    ['Bánh mì ngon', 'Phố cổ', 'Giá tốt'],
    '/uploads/images/sample-food-banhmi.jpg',
  );
  await ensureReview(
    userC.id,
    'Bún Chả Hương Liên',
    'https://maps.google.com/?q=Bun+Cha+Huong+Lien',
    4,
    'Chả nướng thơm ngon, nem cua bể giòn rụm. Buổi trưa quán hơi đông nên phải chờ bàn một chút nhưng rất xứng đáng.',
    ['Bún chả', 'Nem cua bể', 'Quán đông'],
    '/uploads/images/sample-food-buncha.jpg',
  );
  await ensureReview(
    tester.id,
    'Cơm Tấm Ba Nghiền',
    'https://maps.google.com/?q=Com+Tam+Ba+Nghien+84+Dang+Van+Ngu',
    5,
    'Miếng sườn cọng nướng mật ong to bằng bàn tay, mềm thơm cực kỳ vừa miệng. Phục vụ rất nhanh dù quán đông khách!',
    ['Cơm tấm', 'Sườn nướng', 'Đáng thử'],
    '/uploads/images/sample-food-comtam.jpg',
  );
  await ensureReview(
    tester.id,
    'Phở Thìn Lò Đúc',
    'https://maps.google.com/?q=Pho+Thin+13+Lo+Duc',
    5,
    'Phở tái lăn xào lửa lớn thơm ngào ngạt, nước dùng béo vừa đủ rất hợp bữa sáng Hà Nội.',
    ['Phở bò', 'Tái lăn', 'Buổi sáng'],
    '/uploads/images/sample-food-pho2.jpg',
  );
  await ensureReview(
    mayaLin.id,
    'Trà Sữa TocoToco',
    'https://maps.google.com/?q=TocoToco+156+Cau+Giay+Hanoi',
    5,
    'Trà sữa vị trà rõ, trân châu sợi dai ngon và giá sinh viên rất hợp lý.',
    ['Trà sữa', 'Đồ uống', 'Cầu Giấy'],
    '/uploads/images/sample-food-milktea.jpg',
  );
  await ensureReview(
    tester2.id,
    'Bún Chả Hương Liên',
    'https://maps.google.com/?q=Bun+Cha+Huong+Lien+24+Le+Van+Huu',
    5,
    'Thịt chả nướng than thơm lừng, nem cua bể đầy đặn giòn rụm, phục vụ rất nhanh nhẹn.',
    ['Bún chả', 'Truyền thống', 'Đáng thử'],
    '/uploads/images/sample-food-buncha.jpg',
  );
  await ensureReview(
    tester2.id,
    'Trà Sữa TocoToco',
    'https://maps.google.com/?q=TocoToco+156+Cau+Giay+Hanoi',
    5,
    'Trà sữa đậm vị trà, topping trân châu và pudding trứng rất vừa miệng cho buổi chiều.',
    ['Trà sữa', 'Matcha', 'Chill'],
    '/uploads/images/sample-food-milktea.jpg',
  );

  const unsplashReviewImages = await prisma.reviewImage.findMany({
    where: { imageUrl: { startsWith: 'https://images.unsplash.com/' } },
  });
  for (const img of unsplashReviewImages) {
    const mapped = mapExternalToLocalUrl(img.imageUrl, '/uploads/images/sample-food-pho1.jpg');
    if (mapped) {
      await prisma.reviewImage.update({ where: { id: img.id }, data: { imageUrl: mapped } });
    }
  }

  await prisma.follow.createMany({
    data: [
      { followerId: tester.id, followingId: phothin.id },
      { followerId: tester.id, followingId: banhmi25.id },
      { followerId: tester.id, followingId: mayaLin.id },
      { followerId: tester.id, followingId: julianCole.id },
      { followerId: tester.id, followingId: userA.id },
      { followerId: tester.id, followingId: tester2.id },
      { followerId: tester2.id, followingId: tester.id },
      { followerId: tester2.id, followingId: phothin.id },
      { followerId: tester2.id, followingId: bunchahuonglien.id },
      { followerId: tester2.id, followingId: tocotoco.id },
      { followerId: tester2.id, followingId: comtamBaNghien.id },
      { followerId: tester2.id, followingId: anNhien.id },
      { followerId: tester2.id, followingId: mayaLin.id },
      { followerId: mayaLin.id, followingId: tester.id },
      { followerId: phothin.id, followingId: tester.id },
      { followerId: anNhien.id, followingId: tester.id },
      { followerId: userB.id, followingId: tester.id },
      { followerId: phothin.id, followingId: tester2.id },
      { followerId: anNhien.id, followingId: tester2.id },
      { followerId: mayaLin.id, followingId: tester2.id },
      { followerId: userA.id, followingId: tester2.id },
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

  async function ensureMessage(senderId: number, recipientId: number, content: string, isRead: boolean, mediaUrl?: string) {
    const existing = await prisma.message.findFirst({ where: { senderId, recipientId, content, mediaUrl: mediaUrl ?? null } });
    if (!existing) {
      await prisma.message.create({ data: { senderId, recipientId, content, mediaUrl, isRead } });
    }
  }

  await ensureMessage(userA.id, phothin.id, 'Dạ quán cho em hỏi trưa nay nhóm 6 người còn bàn không ạ?', true);
  await ensureMessage(phothin.id, userA.id, 'Chào bạn, trưa nay quán vẫn còn bàn phục vụ nhóm 6 người bạn nhé. Hân hạnh đón tiếp bạn!', true);
  await ensureMessage(userB.id, banhmi25.id, 'Bên mình có nhận đặt ship qua GrabFood không quán ơi?', false);
  await ensureMessage(mayaLin.id, tester.id, 'Tối nay đi ăn thử quán pizza hôm trước bạn review không nè?', false);
  await ensureMessage(tester.id, mayaLin.id, 'Oke luôn bạn ơi, tầm 7h tối mình qua đón nha!', true);
  await ensureMessage(tester.id, phothin.id, 'Chào quán, cuối tuần sáng Chủ Nhật quán mở cửa từ mấy giờ vậy ạ?', true);
  await ensureMessage(phothin.id, tester.id, 'Quán mở từ 6h sáng đến 21h tối mỗi ngày ạ, mời bạn ghé thưởng thức phở tái lăn nha!', false);
  await ensureMessage(userA.id, tester.id, 'Sáng mai rảnh không qua Bánh Mì 25 Hàng Cá ăn sáng chung nhé!', false);
  await ensureMessage(tester2.id, tester.id, 'Chào Khôi, tối nay nhóm mình đi ăn ở Bistro hay lẩu Thái vậy?', true);
  await ensureMessage(tester.id, tester2.id, 'Pizza lò củi quán này đỉnh lắm nè, có mã TESTER20 nữa!', true, '/uploads/images/sample-food-pizza.jpg');
  await ensureMessage(tester2.id, tester.id, 'Nhìn ngon xỉu! Hay tụi mình chốt đi ăn lẩu Thái ở Quận 3 luôn nhé?', true, '/uploads/images/sample-food-hotpot.jpg');
  await ensureMessage(tester.id, tester2.id, 'Gửi bạn link địa điểm và voucher quán nè: https://dishcision.duckdns.org/posts — 19h30 gặp ở cổng nhé!', false);
  await ensureMessage(tester2.id, tester.id, 'Okela luôn! Xem thêm thực đơn quán tại https://dishcision.duckdns.org/ rồi tối chốt món nhé!', false);
  await ensureMessage(tester2.id, tocotoco.id, 'Chào quán, mã ưu đãi TOCOTOCO15 áp dụng cho đơn đặt tại quầy không ạ?', true);
  await ensureMessage(tocotoco.id, tester2.id, 'Dạ có ạ, bạn chỉ cần đọc mã TOCOTOCO15 khi gọi món tại quầy là được giảm ngay 15% nha!', false);
  await ensureMessage(anNhien.id, tester2.id, 'Cuối tuần rảnh qua tiệm bánh nhà An thử mousse dâu mới nha Linh ơi 🍓', false);

  async function ensureNotification(userId: number, type: NotificationType, message: string, relatedPostId?: number) {
    const existing = await prisma.notification.findFirst({ where: { userId, type, message } });
    if (!existing) {
      await prisma.notification.create({ data: { userId, type, message, relatedPostId } });
    }
  }

  await ensureNotification(phothin.id, NotificationType.Like, 'Nguyễn Văn A đã thích bài viết của bạn', createdPosts[3].id);
  await ensureNotification(phothin.id, NotificationType.Comment, 'Nguyễn Văn A đã bình luận vào bài viết của bạn', createdPosts[3].id);
  await ensureNotification(userB.id, NotificationType.Follow, 'Nguyễn Văn A đã bắt đầu theo dõi bạn');
  await ensureNotification(userA.id, NotificationType.System, '[Hệ thống] Chào mừng bạn gia nhập cộng đồng sành ăn Dishcision! Khám phá ngay các ưu đãi hôm nay.');
  await ensureNotification(userB.id, NotificationType.System, '[Hệ thống] Cập nhật phiên bản mới: Đã có tính năng Vòng quay ngẫu nhiên món ăn.');
  await ensureNotification(tester.id, NotificationType.Like, 'Maya Lin đã thích bài viết của bạn', testerPost.id);
  await ensureNotification(tester.id, NotificationType.Comment, 'Maya Lin đã bình luận vào bài viết của bạn', testerPost.id);
  await ensureNotification(tester.id, NotificationType.Follow, 'Maya Lin đã bắt đầu theo dõi bạn');
  await ensureNotification(tester.id, NotificationType.Follow, 'Khánh Linh (Tester 2) đã bắt đầu theo dõi bạn');
  await ensureNotification(tester.id, NotificationType.Comment, 'Khánh Linh (Tester 2) đã bình luận vào bài viết của bạn', testerPost.id);
  await ensureNotification(tester.id, NotificationType.System, '[Hệ thống] Tài khoản kiểm thử Dishcision đã sẵn sàng trải nghiệm đầy đủ tính năng.');
  await ensureNotification(tester2.id, NotificationType.Like, 'Minh Khôi (Tester) đã thích bài viết của bạn', tester2Post.id);
  await ensureNotification(tester2.id, NotificationType.Comment, 'Minh Khôi (Tester) đã bình luận vào bài viết của bạn', tester2Post.id);
  await ensureNotification(tester2.id, NotificationType.Follow, 'Minh Khôi (Tester) đã bắt đầu theo dõi bạn');
  await ensureNotification(tester2.id, NotificationType.System, '[Hệ thống] Tài khoản kiểm thử 2 đã sẵn sàng trải nghiệm đầy đủ tính năng Dishcision.');

  async function ensureAiRequest(userId: number, mood: string, budget: string, companions: number, craving: string, resultJson: any, feedbackRating: number, feedbackComment: string) {
    const existing = await prisma.aIRequest.findFirst({ where: { userId, mood } });
    if (!existing) {
      await prisma.aIRequest.create({
        data: {
          userId,
          mood,
          budget,
          companions,
          craving,
          resultJson,
          modelUsed: 'gemini-2.0-flash',
          feedbackRating,
          feedbackComment,
        },
      });
    }
  }

  await ensureAiRequest(
    userA.id,
    'Hôm nay hơi mệt, cần món gì ấm bụng và bổ dưỡng',
    '60000',
    1,
    'Món nước nóng hổi',
    [
      { rank: 1, dishName: 'Phở bò tái nạm', reason: 'Nước dùng ninh xương ấm nóng bổ sung năng lượng tức thì', estimatedPrice: '55,000 VND' },
      { rank: 2, dishName: 'Bún bò Huế', reason: 'Vị cay nồng ấm bụng, kích thích vị giác giải cảm', estimatedPrice: '60,000 VND' },
      { rank: 3, dishName: 'Cháo sườn quẩy', reason: 'Thanh nhẹ, dễ tiêu hóa cho ngày mỏi mệt', estimatedPrice: '35,000 VND' },
    ],
    5,
    'Gợi ý phở bò cực kỳ chuẩn tâm trạng!',
  );

  await ensureAiRequest(
    userB.id,
    'Vui vẻ, muốn tụ tập cùng 3 người bạn thân',
    '200000',
    4,
    'Đồ nướng hoặc lẩu',
    [
      { rank: 1, dishName: 'Lẩu nấm gà ác', reason: 'Thanh đạm, bổ dưỡng, rất hợp ngồi trò chuyện rôm rả', estimatedPrice: '180,000 VND/người' },
      { rank: 2, dishName: 'Lẩu riêu cua bắp bò', reason: 'Đậm đà hương vị truyền thống Hà Nội', estimatedPrice: '150,000 VND/người' },
    ],
    4,
    'Quán lẩu gợi ý rất ngon.',
  );

  await ensureAiRequest(
    tester.id,
    'Hào hứng cuối tuần, muốn thử món mới lạ ở trung tâm',
    '150000',
    2,
    'Đồ Âu hoặc fusion',
    [
      { rank: 1, dishName: 'Pasta sốt nấm Truffle', reason: 'Hương vị Ý béo mịn sang trọng cho buổi tối cuối tuần', estimatedPrice: '145,000 VND' },
      { rank: 2, dishName: 'Pizza lò củi Hải sản', reason: 'Vỏ bánh giòn xốp thơm lừng phô mai nướng', estimatedPrice: '160,000 VND' },
    ],
    5,
    'Gợi ý hợp gu quá!',
  );

  await ensureAiRequest(
    tester2.id,
    'Thư giãn cùng bạn bè, thích đồ ngọt hoặc lẩu nhẹ',
    '180000',
    3,
    'Lẩu Thái hoặc tráng miệng matcha',
    [
      { rank: 1, dishName: 'Lẩu Thái hải sản chua cay', reason: 'Hương sả ớt ấm bụng, rất hợp ngồi tụ tập bạn bè', estimatedPrice: '185,000 VND' },
      { rank: 2, dishName: 'Matcha Latte & Bánh Crepe trà xanh', reason: 'Thanh nhẹ ít ngọt để tráng miệng cuối bữa', estimatedPrice: '48,000 VND' },
    ],
    5,
    'Đúng gu luôn!',
  );

  const aiConfigs = [
    { modelName: 'gemini-2.0-flash', provider: 'Google', isActive: true, maxTokens: 1024, temperature: 0.7 },
    { modelName: 'gpt-4o-mini', provider: 'OpenAI', isActive: false, maxTokens: 1024, temperature: 0.7 },
  ];
  for (const config of aiConfigs) {
    const existing = await prisma.aIConfig.findFirst({ where: { modelName: config.modelName } });
    if (!existing) await prisma.aIConfig.create({ data: config });
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

  const existingReport = await prisma.report.findFirst({
    where: { reporterId: userB.id, postId: createdPosts[1].id },
  });
  if (!existingReport) {
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
  }

  const auditEntries = [
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
  ];
  for (const entry of auditEntries) {
    const existing = await prisma.auditLog.findFirst({
      where: { adminId: entry.adminId, action: entry.action },
    });
    if (!existing) await prisma.auditLog.create({ data: entry });
  }

  const storeProfilesData = [
    {
      userId: phothin.id,
      coverUrl: '/uploads/images/sample-food-pho1.jpg',
      address: '13 Lò Đúc, Hai Bà Trưng, Hà Nội',
      openHours: '06:00 – 21:30',
      isOpen: true,
      rating: 4.8,
      highlightDish: 'Phở bò tái lăn gia truyền',
      dishPrice: '65.000 đ',
      dishDescription: 'Thịt bò xào lửa lớn thơm nức với tỏi hành, nước dùng ninh xương đậm đà.',
    },
    {
      userId: banhmi25.id,
      coverUrl: '/uploads/images/sample-food-banhmi.jpg',
      address: '25 Hàng Cá, Hoàn Kiếm, Hà Nội',
      openHours: '07:00 – 21:00',
      isOpen: true,
      rating: 4.9,
      highlightDish: 'Bánh mì pate xá xíu đặc biệt',
      dishPrice: '35.000 đ',
      dishDescription: 'Vỏ bánh giòn rụm, pate gan nhà làm béo ngậy cùng sốt bơ trứng.',
    },
    {
      userId: bunchahuonglien.id,
      coverUrl: '/uploads/images/sample-food-buncha.jpg',
      address: '24 Lê Văn Hưu, Hai Bà Trưng, Hà Nội',
      openHours: '08:00 – 20:30',
      isOpen: true,
      rating: 4.7,
      highlightDish: 'Bún chả đặc biệt & Nem cua bể',
      dishPrice: '60.000 đ',
      dishDescription: 'Chả nướng than hoa vàng rộm kết hợp nem hải sản giòn tan.',
    },
    {
      userId: comtamBaNghien.id,
      coverUrl: '/uploads/images/sample-food-comtam.jpg',
      address: '84 Đặng Văn Ngữ, Phú Nhuận, TP.HCM',
      openHours: '07:00 – 21:30',
      isOpen: true,
      rating: 4.9,
      highlightDish: 'Cơm tấm sườn bì chả đặc biệt',
      dishPrice: '75.000 đ',
      dishDescription: 'Sườn cọng nướng mật ong mềm mọng, bì thính và chả trứng hấp.',
    },
    {
      userId: tocotoco.id,
      coverUrl: '/uploads/images/sample-food-milktea.jpg',
      address: '156 Cầu Giấy, Hà Nội',
      openHours: '08:30 – 22:30',
      isOpen: true,
      rating: 4.7,
      highlightDish: 'Trà sữa Ba Anh Em (Size L)',
      dishPrice: '42.000 đ',
      dishDescription: 'Đủ bộ trân châu hoàng gia, thạch sương sáo và pudding béo mịn.',
    },
  ];

  for (const sp of storeProfilesData) {
    await prisma.storeProfile.upsert({
      where: { userId: sp.userId },
      create: sp,
      update: {},
    });
  }

  const dishesSeedData = [
    {
      storeUserId: phothin.id,
      name: 'Phở bò tái lăn gia truyền',
      price: 65000,
      formattedPrice: '65.000 đ',
      description: 'Thịt bò tươi xào lăn xém cạnh với tỏi hành thơm phức, nước dùng ninh xương đậm đà.',
      imageUrl: '/uploads/images/sample-food-pho1.jpg',
      category: 'Phở & Bún',
      rating: 4.9,
      reviewsCount: 248,
    },
    {
      storeUserId: phothin.id,
      name: 'Phở bò chín nạm gầu',
      price: 60000,
      formattedPrice: '60.000 đ',
      description: 'Nạm gầu giòn ngọt kết hợp bánh phở dẻo mềm truyền thống.',
      imageUrl: '/uploads/images/sample-food-pho2.jpg',
      category: 'Phở & Bún',
      rating: 4.8,
      reviewsCount: 164,
    },
    {
      storeUserId: phothin.id,
      name: 'Bún ốc sườn chua cay',
      price: 55000,
      formattedPrice: '55.000 đ',
      description: 'Ốc nhồi giòn sần sật, sườn sụn ninh mềm cùng nước giấm bỗng chua thanh.',
      imageUrl: '/uploads/images/sample-food-bunoc.jpg',
      category: 'Phở & Bún',
      rating: 4.6,
      reviewsCount: 92,
    },
    {
      storeUserId: banhmi25.id,
      name: 'Bánh mì pate xá xíu đặc biệt',
      price: 35000,
      formattedPrice: '35.000 đ',
      description: 'Vỏ bánh mì giòn rụm, pate gan béo ngậy, thịt xá xíu đậm vị phố cổ.',
      imageUrl: '/uploads/images/sample-food-banhmi.jpg',
      category: 'Bánh mì & Xôi',
      rating: 4.9,
      reviewsCount: 310,
    },
    {
      storeUserId: banhmi25.id,
      name: 'Bánh mì gà nướng mật ong',
      price: 40000,
      formattedPrice: '40.000 đ',
      description: 'Thịt gà ướp mật ong nướng thơm kết hợp rau răm đồ chua thanh mát.',
      imageUrl: '/uploads/images/sample-food-banhmi.jpg',
      category: 'Bánh mì & Xôi',
      rating: 4.8,
      reviewsCount: 175,
    },
    {
      storeUserId: bunchahuonglien.id,
      name: 'Bún chả đặc biệt & Nem cua bể',
      price: 60000,
      formattedPrice: '60.000 đ',
      description: 'Chả viên và chả miếng nướng than hoa ăn kèm nem cua bể giòn tan.',
      imageUrl: '/uploads/images/sample-food-buncha.jpg',
      category: 'Phở & Bún',
      rating: 4.8,
      reviewsCount: 245,
    },
    {
      storeUserId: bunchahuonglien.id,
      name: 'Lẩu Thái hải sản chua cay',
      price: 185000,
      formattedPrice: '185.000 đ',
      description: 'Nồi lẩu nóng hổi nghi ngút khói với tôm, mực, thịt bò và nấm tươi.',
      imageUrl: '/uploads/images/sample-food-hotpot.jpg',
      category: 'Lẩu & Nướng',
      rating: 4.7,
      reviewsCount: 112,
    },
    {
      storeUserId: comtamBaNghien.id,
      name: 'Cơm tấm sườn bì chả đặc biệt',
      price: 75000,
      formattedPrice: '75.000 đ',
      description: 'Sườn cọng nướng mật ong mềm mọng ăn kèm bì thính và chả trứng hấp.',
      imageUrl: '/uploads/images/sample-food-comtam.jpg',
      category: 'Cơm tấm & Cơm phần',
      rating: 4.9,
      reviewsCount: 412,
    },
    {
      storeUserId: comtamBaNghien.id,
      name: 'Salad ngũ sắc đậu gà xốt tahini',
      price: 58000,
      formattedPrice: '58.000 đ',
      description: 'Salad rau củ hữu cơ thanh mát kết hợp đậu gà rang giòn xốt mè béo bùi.',
      imageUrl: '/uploads/images/sample-food-salad.jpg',
      category: 'Món chay',
      rating: 4.7,
      reviewsCount: 84,
    },
    {
      storeUserId: comtamBaNghien.id,
      name: 'Pizza lò củi phô mai hải sản',
      price: 165000,
      formattedPrice: '165.000 đ',
      description: 'Đế bánh lên men tự nhiên nướng lò củi giòn xốp cùng phô mai kéo sợi.',
      imageUrl: '/uploads/images/sample-food-pizza.jpg',
      category: 'Hải sản & Ốc',
      rating: 4.8,
      reviewsCount: 138,
    },
    {
      storeUserId: comtamBaNghien.id,
      name: 'Pasta thủ công sốt nấm Truffle',
      price: 145000,
      formattedPrice: '145.000 đ',
      description: 'Mì tươi làm trong ngày phủ sốt kem nấm truffle béo mịn thơm ngậy.',
      imageUrl: '/uploads/images/sample-food-pasta.jpg',
      category: 'Cơm tấm & Cơm phần',
      rating: 4.8,
      reviewsCount: 95,
    },
    {
      storeUserId: tocotoco.id,
      name: 'Trà sữa Ba Anh Em (Size L)',
      price: 42000,
      formattedPrice: '42.000 đ',
      description: 'Hồng trà sữa thơm béo với trân châu sợi, sương sáo và pudding trứng.',
      imageUrl: '/uploads/images/sample-food-milktea.jpg',
      category: 'Trà & Cà phê',
      rating: 4.7,
      reviewsCount: 289,
    },
    {
      storeUserId: tocotoco.id,
      name: 'Matcha Latte & Bánh Crepe trà xanh',
      price: 48000,
      formattedPrice: '48.000 đ',
      description: 'Trà xanh Nhật Bản thanh mát dùng kèm bánh crepe mềm mịn ít ngọt.',
      imageUrl: '/uploads/images/sample-food-cafe1.jpg',
      category: 'Trà & Cà phê',
      rating: 4.8,
      reviewsCount: 120,
    },
    {
      storeUserId: tocotoco.id,
      name: 'Bánh mousse dâu tây tươi',
      price: 45000,
      formattedPrice: '45.000 đ',
      description: 'Bánh kem mousse mềm mịn vị dâu tây ngọt dịu tan ngay trong miệng.',
      imageUrl: '/uploads/images/sample-food-mousse.jpg',
      category: 'Tráng miệng & Chè',
      rating: 4.9,
      reviewsCount: 142,
    },
  ];

  for (const dish of dishesSeedData) {
    const existing = await prisma.dish.findFirst({
      where: { storeUserId: dish.storeUserId, name: dish.name },
    });
    if (!existing) {
      await prisma.dish.create({ data: dish });
    }
  }

  const storiesSeedData = [
    {
      userId: phothin.id,
      mediaUrl: '/uploads/images/sample-food-pho1.jpg',
      caption: 'Bát phở tái lăn buổi sáng nóng hổi thơm nức lòng thực khách!',
    },
    {
      userId: banhmi25.id,
      mediaUrl: '/uploads/images/sample-food-banhmi.jpg',
      caption: 'Bánh mì pate giòn rụm vừa ra lò mẻ mới! 🥖',
    },
    {
      userId: bunchahuonglien.id,
      mediaUrl: '/uploads/images/sample-food-buncha.jpg',
      caption: 'Chả nướng than hoa vừa lên vỉ thơm lừng góc phố!',
    },
    {
      userId: comtamBaNghien.id,
      mediaUrl: '/uploads/images/sample-food-comtam.jpg',
      caption: 'Sườn nướng mật ong vàng ươm nóng hổi sẵn sàng phục vụ bữa trưa!',
    },
    {
      userId: tocotoco.id,
      mediaUrl: '/uploads/images/sample-food-milktea.jpg',
      caption: 'Giảm 20% trà sữa Size L khung giờ vàng chiều nay 🧋',
    },
    {
      userId: userB.id,
      mediaUrl: '/uploads/images/sample-food-cafe2.jpg',
      caption: 'Chill cuối tuần cùng bạn bè tại quán cafe quen ☕',
    },
    {
      userId: mayaLin.id,
      mediaUrl: '/uploads/images/sample-food-salad.jpg',
      caption: 'Bữa trưa thanh nhẹ cùng salad ngũ sắc & nước ép táo xanh 🥗',
    },
    {
      userId: tester.id,
      mediaUrl: '/uploads/images/sample-food-pizza.jpg',
      caption: 'Pizza lò củi nóng hổi cuối ngày 🍕',
    },
    {
      userId: tester2.id,
      mediaUrl: '/uploads/images/sample-food-hotpot.jpg',
      caption: 'Nồi lẩu Thái chua cay tối nay cùng bạn bè 🍲🔥',
    },
  ];

  for (const st of storiesSeedData) {
    const existing = await prisma.story.findFirst({
      where: { userId: st.userId, caption: st.caption },
    });
    if (!existing) {
      await prisma.story.create({ data: st });
    }
  }

  const addressesSeedData = [
    {
      userId: tester.id,
      label: 'Nhà riêng',
      recipientName: 'Minh Khôi',
      phoneNumber: '0912345678',
      detailAddress: 'Số 25 Lý Tự Trọng, Bến Nghé, Quận 1, TP.HCM',
      notes: 'Gọi khi đến cổng',
      isDefault: true,
    },
    {
      userId: tester.id,
      label: 'Công ty',
      recipientName: 'Minh Khôi (Văn phòng)',
      phoneNumber: '0912345678',
      detailAddress: 'Tầng 12 Tòa nhà Bitexco, 2 Hải Triều, Quận 1, TP.HCM',
      notes: 'Gửi ở quầy lễ tân sảnh chính',
      isDefault: false,
    },
    {
      userId: userA.id,
      label: 'Nhà riêng',
      recipientName: 'Nguyễn Văn A',
      phoneNumber: '0987654321',
      detailAddress: 'Số 12 ngõ 45 phố Chùa Bộc, Đống Đa, Hà Nội',
      notes: null,
      isDefault: true,
    },
    {
      userId: tester2.id,
      label: 'Nhà riêng',
      recipientName: 'Khánh Linh',
      phoneNumber: '0934567890',
      detailAddress: '142 Võ Văn Tần, Phường 6, Quận 3, TP.HCM',
      notes: 'Gọi trước khi giao 5 phút',
      isDefault: true,
    },
    {
      userId: tester2.id,
      label: 'Trường học',
      recipientName: 'Khánh Linh (KTX)',
      phoneNumber: '0934567890',
      detailAddress: 'Ký túc xá Khu B, Linh Trung, Thủ Đức, TP.HCM',
      notes: 'Giao tại cổng bảo vệ',
      isDefault: false,
    },
  ];

  for (const addr of addressesSeedData) {
    const existing = await prisma.deliveryAddress.findFirst({
      where: { userId: addr.userId, detailAddress: addr.detailAddress },
    });
    if (!existing) {
      await prisma.deliveryAddress.create({ data: addr });
    }
  }

  const ticketsSeedData = [
    {
      userId: tester.id,
      type: 'Support',
      subject: 'Đã lưu & theo dõi',
      title: 'Hỏi về cách lọc ưu đãi đã lưu theo quán ăn',
      description: 'Mình đã lưu nhiều bài viết có mã giảm giá, mong muốn ứng dụng hiển thị mã ưu đãi rõ ràng trong mục Ưu đãi đã lưu.',
      contactEmail: 'tester@dishcision.com',
      status: 'Đã tiếp nhận',
      attachments: [],
      timeline: [
        { step: 'Đã gửi yêu cầu', date: '08/11/2026', desc: 'Qua biểu mẫu hỗ trợ', isDone: true },
        { step: 'Đã tiếp nhận', date: '08/11/2026', desc: 'Đội ngũ kỹ thuật đang xem xét yêu cầu của bạn.', isDone: false },
      ],
    },
    {
      userId: tester.id,
      type: 'Support',
      subject: 'Bài viết & đánh giá',
      title: 'Cập nhật vị trí quán trên bản đồ',
      description: 'Nhờ đội ngũ kiểm tra lại liên kết Google Maps cho bài viết đánh giá gần đây.',
      contactEmail: 'tester@dishcision.com',
      status: 'Đã xử lý',
      attachments: [],
      timeline: [
        { step: 'Đã gửi yêu cầu', date: '05/11/2026', desc: 'Qua biểu mẫu hỗ trợ', isDone: true },
        { step: 'Đã xử lý', date: '06/11/2026', desc: 'Liên kết quán đã được cập nhật chính xác.', isDone: true },
      ],
    },
    {
      userId: userA.id,
      type: 'Support',
      subject: 'Tài khoản & bảo mật',
      title: 'Hỗ trợ bật xác thực hai lớp',
      description: 'Xin hướng dẫn chi tiết cách liên kết ứng dụng xác thực cho tài khoản.',
      contactEmail: 'nguyenvana@gmail.com',
      status: 'Đã tiếp nhận',
      attachments: [],
      timeline: [
        { step: 'Đã gửi yêu cầu', date: '07/11/2026', desc: 'Qua biểu mẫu hỗ trợ', isDone: true },
        { step: 'Đã tiếp nhận', date: '07/11/2026', desc: 'Đã gửi hướng dẫn qua email đăng ký.', isDone: false },
      ],
    },
    {
      userId: tester2.id,
      type: 'Support',
      subject: 'Đã lưu & theo dõi',
      title: 'Góp ý lọc nhanh quán có ưu đãi gần tôi',
      description: 'Mong muốn màn hình Khám phá hỗ trợ lọc riêng các quán đang có mã giảm giá.',
      contactEmail: 'tester2@dishcision.com',
      status: 'Đã tiếp nhận',
      attachments: [],
      timeline: [
        { step: 'Đã gửi yêu cầu', date: '10/11/2026', desc: 'Qua biểu mẫu hỗ trợ', isDone: true },
        { step: 'Đã tiếp nhận', date: '10/11/2026', desc: 'Đội ngũ phát triển đã ghi nhận đề xuất.', isDone: false },
      ],
    },
  ];

  for (const ticket of ticketsSeedData) {
    const existing = await prisma.supportTicket.findFirst({
      where: { userId: ticket.userId, title: ticket.title },
    });
    if (!existing) {
      await prisma.supportTicket.create({ data: ticket });
    }
  }

  await prisma.blockedAccount.upsert({
    where: { userId_blockedUserId: { userId: tester.id, blockedUserId: userC.id } },
    create: { userId: tester.id, blockedUserId: userC.id },
    update: {},
  });

  await prisma.blockedAccount.upsert({
    where: { userId_blockedUserId: { userId: tester2.id, blockedUserId: userC.id } },
    create: { userId: tester2.id, blockedUserId: userC.id },
    update: {},
  });

  await prisma.restrictedAccount.upsert({
    where: { userId_restrictedUserId: { userId: tester.id, restrictedUserId: quangTran.id } },
    create: { userId: tester.id, restrictedUserId: quangTran.id },
    update: {},
  });

  await prisma.restrictedAccount.upsert({
    where: { userId_restrictedUserId: { userId: tester2.id, restrictedUserId: quangTran.id } },
    create: { userId: tester2.id, restrictedUserId: quangTran.id },
    update: {},
  });

  await prisma.userPrivacy.upsert({
    where: { userId: tester.id },
    create: { userId: tester.id },
    update: {},
  });

  await prisma.userPrivacy.upsert({
    where: { userId: tester2.id },
    create: { userId: tester2.id },
    update: {},
  });

  await prisma.userPrivacy.upsert({
    where: { userId: userA.id },
    create: { userId: userA.id },
    update: {},
  });

  console.log('Non-destructive comprehensive database seeding completed successfully!');
}

main()
  .catch((e) => {
    console.error('Seeding failed:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
