# Dishcision — Test Cases cho Auth, User Profile & Trang Home

Tài liệu hướng dẫn kiểm thử chức năng **Authentication (Xác thực & Bảo mật)**, **User Profile (Hồ sơ người dùng)** và **Trang Home (Bảng tin & Tương tác bài viết)** trên ứng dụng Android Dishcision và Backend API.

---

## 1. Môi trường & Tài khoản kiểm thử

### 1.1. Môi trường
- **Backend API (Production):** `https://dishcision.duckdns.org/api/v1`
- **App Android:** Bản build từ nhánh `main` (commit mới nhất đã đồng bộ luồng Email OTP 2FA, Google Sign-In không dùng 2FA, User Profile, Home lướt nhiều ảnh & xem ảnh tràn màn hình).
- *(Mẹo kiểm tra server trên app)*: Tại màn hình **Đăng nhập**, bấm vào logo **Dishcision** để kiểm tra/đổi URL API nếu cần.

### 1.2. Tài khoản mẫu
| Loại tài khoản | Email / Username | Mật khẩu | Mục đích test |
| :--- | :--- | :--- | :--- |
| Seed Account 1 | `tester@dishcision.com` (`@tester_foodie`) | `Password@123` | Đăng nhập thường, tương tác chéo (Follow, xem Profile, Chat, Chặn) |
| Seed Account 2 | `tester2@dishcision.com` (`@tester2_foodie`) | `Password@123` | Đăng nhập thường, sửa Profile, test luồng nhiều thiết bị |
| Gmail thật của Tester | `<gmail-thật-của-bạn>` | Tự đặt khi đăng ký | **Bắt buộc** dùng cho các case nhận OTP qua email (2FA, Quên mật khẩu) và Đăng nhập Google |
| Tài khoản phụ (tự tạo) | `<tạo mới khi test>` | Tự đặt | Dùng riêng cho case **Xóa tài khoản** |

### 1.3. Quy tắc quan trọng trước khi test
1. Các email đuôi `@dishcision.com` là tài khoản seed **không có hộp thư thật** — chỉ dùng test đăng nhập không bật OTP, Profile, Follow, Quyền riêng tư...
2. Khi test **Bật 2FA / Đăng nhập 2FA / Quên mật khẩu**, bắt buộc dùng **Gmail thật** có thể mở hộp thư (kiểm tra cả mục **Spam / Quảng cáo** nếu chưa thấy mail trong Hộp thư đến).
3. **Không xóa** 2 tài khoản seed (`tester@dishcision.com`, `tester2@dishcision.com`). Test xóa tài khoản bằng tài khoản tự đăng ký mới.
4. **Không khóa nhầm 2FA** trên tài khoản seed không có hộp thư thật — nếu lỡ bật, không lấy được OTP để đăng nhập lại qua app.

---

## 2. Test Cases — Authentication (Auth)

### 2.1. Đăng ký tài khoản (`RegisterActivity` · `POST /auth/register`)

| Mã TC | Tên kịch bản | Tiền điều kiện | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_AUTH_01` | Đăng ký bằng Email thành công | Chưa đăng nhập | 1. Mở màn **Đăng ký**<br>2. Nhập Họ tên (`1–100` ký tự), Email mới hợp lệ, Mật khẩu `>= 6` ký tự, Nhập lại mật khẩu khớp<br>3. Bấm **Đăng ký** | API trả `201`; app lưu token, tạo phiên đăng nhập và vào thẳng **Trang chủ**; 2FA mặc định **TẮT** | [ ] |
| `TC_AUTH_02` | Đăng ký trùng Email hoặc Username | Đã có `tester@dishcision.com` | 1. Nhập `tester@dishcision.com` vào form Đăng ký (hoặc gọi API với `username` đã tồn tại)<br>2. Bấm **Đăng ký** | API trả `409 Conflict` (`Email already exists` / `Username already exists`); app báo lỗi rõ, không tạo tài khoản mới | [ ] |
| `TC_AUTH_03` | Kiểm tra validation form Đăng ký | Đang ở màn **Đăng ký** | Thử lần lượt:<br>- Để trống Họ tên<br>- Nhập Email sai định dạng (`abc@`)<br>- Mật khẩu `< 6` ký tự<br>- Xác nhận mật khẩu không khớp | App chặn ngay ở client và hiển thị Toast nhắc nhở từng lỗi, không gửi request tạo tài khoản (nếu gọi API trực tiếp trả `400`) | [ ] |
| `TC_AUTH_04` | Đăng ký mới bằng Google | Thiết bị có tài khoản Gmail chưa từng đăng ký trên app | 1. Tại màn **Đăng ký**, bấm **Đăng ký bằng Google**<br>2. Chọn Gmail chưa từng có trên hệ thống | API `/auth/google` trả `200` + JWT (`isGoogleAccount: true`), vào thẳng **Trang chủ**, **không** yêu cầu 2FA | [ ] |

---

### 2.2. Đăng nhập thường & Google (`LoginActivity` · `POST /auth/login`, `POST /auth/google`)

| Mã TC | Tên kịch bản | Tiền điều kiện | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_AUTH_05` | Đăng nhập bằng Email (2FA đang TẮT) | Tài khoản có 2FA đang TẮT | Nhập `tester2@dishcision.com` + `Password@123` → bấm **Đăng nhập** | API trả `200` + JWT, vào thẳng **Trang chủ** | [ ] |
| `TC_AUTH_06` | Đăng nhập bằng Username (2FA đang TẮT) | Tài khoản có 2FA đang TẮT | Nhập `tester2_foodie` + `Password@123` → bấm **Đăng nhập** | API trả `200` + JWT, vào thẳng **Trang chủ** | [ ] |
| `TC_AUTH_07` | Đăng nhập sai mật khẩu / sai tài khoản | Chưa đăng nhập | Thử nhập:<br>- Sai mật khẩu<br>- Email không tồn tại<br>- Email của tài khoản tạo bằng Google thuần | API trả `401 Unauthorized` (`Invalid credentials`); app báo sai Email/Tên đăng nhập hoặc mật khẩu | [ ] |
| `TC_AUTH_08` | Tài khoản bị khóa / vô hiệu hóa | Có tài khoản trạng thái `Banned` hoặc `Suspended` | Thử đăng nhập thường hoặc đăng nhập Google bằng tài khoản đó | API trả `403 Forbidden`; app từ chối đăng nhập và thông báo tài khoản bị khóa/chưa kích hoạt | [ ] |
| `TC_AUTH_09` | Đăng nhập lại bằng Google | Gmail đã từng đăng nhập/đăng ký bằng Google | Tại màn **Đăng nhập**, bấm **Tiếp tục với Google** → chọn lại Gmail đó | API trả `200` + JWT ngay lập tức, vào thẳng **Trang chủ** (không qua bước OTP 2FA) | [ ] |
| `TC_AUTH_10` | Giao diện bảo mật riêng cho tài khoản Google thuần | Đã đăng nhập bằng Google (`passwordHash === ''`) | Vào **Hồ sơ → Cài đặt → Bảo mật và đăng nhập** | - Ở Cài đặt & Bảo mật: **ẩn** mục `Đổi mật khẩu`<br>- Thẻ 2FA hiện màu xanh `"Bảo vệ qua Google"` và **ẩn nút Bật 2FA**<br>- Gọi API `GET /auth/2fa/setup` trả `400` | [ ] |

---

### 2.3. Xác thực 2 yếu tố (2FA) qua Email *(chỉ áp dụng cho tài khoản Email + Mật khẩu)*

> Đường dẫn màn hình: **Hồ sơ → Cài đặt → Bảo mật và đăng nhập** (`SecurityLoginActivity` → `Setup2faStep2Activity` → `VerifyOtpActivity`).

| Mã TC | Tên kịch bản | Tiền điều kiện | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_AUTH_11` | Bật 2FA thành công | Đang đăng nhập bằng tài khoản **Gmail thật + Mật khẩu** (2FA đang tắt) | 1. Vào **Bảo mật và đăng nhập** → bấm **Bật xác thực hai yếu tố**<br>2. Mở hộp thư Gmail kiểm tra email mã 6 số<br>3. Nhập đúng mã → bấm xác nhận | Nhận được email mã OTP 6 chữ số (hiệu lực 10 phút); API `/auth/2fa/verify` trả `200`, thẻ 2FA chuyển sang màu xanh **`Đã bật`** | [ ] |
| `TC_AUTH_12` | Validation & khóa sai quá 5 lần khi bật 2FA | Đang ở màn nhập mã bật 2FA (`Setup2faStep2Activity`) | 1. Nhập thiếu 6 số hoặc ký tự chữ<br>2. Nhập sai OTP 6 số liên tiếp 5 lần | - Sai định dạng: app chặn ngay ở client<br>- Sai lần 1–4: API trả `400`<br>- Sai lần 5: API trả `429`, hủy phiên OTP setup bắt bấm **Gửi lại mã** | [ ] |
| `TC_AUTH_13` | Gửi lại mã khi bật 2FA | Đang ở màn bật 2FA | 1. Vừa nhận mã xong, bấm **Gửi lại mã** ngay (< 15s)<br>2. Đợi > 15s rồi bấm **Gửi lại mã** | Trong 15s đầu backend không gửi trùng spam; sau 15s tạo và gửi OTP mới về Gmail | [ ] |
| `TC_AUTH_14` | Đăng nhập khi 2FA đang BẬT — đúng OTP | Tài khoản Gmail thật vừa bật 2FA thành công ở `TC_AUTH_11` | 1. Đăng xuất khỏi app<br>2. Đăng nhập lại bằng Email + Mật khẩu<br>3. Kiểm tra Gmail lấy OTP 6 số<br>4. Nhập mã vào màn xác thực 2FA | Bước 2 không vào thẳng Trang chủ mà chuyển sang `VerifyOtpActivity` (hiện email đã che dạng `ab***@gmail.com`); nhập đúng OTP `/auth/login/verify-2fa` trả JWT và vào Trang chủ | [ ] |
| `TC_AUTH_15` | Đăng nhập 2FA — sai OTP & sai quá 5 lần | Đang ở màn OTP đăng nhập 2FA | Nhập sai mã OTP 6 số liên tiếp 5 lần | Lần 1–4 báo lỗi `400` cho nhập lại; lần thứ 5 API trả `429`, hủy `tempToken`, app đóng màn OTP quay về **Đăng nhập** | [ ] |
| `TC_AUTH_16` | Gửi lại mã OTP khi đăng nhập 2FA | Đang ở màn OTP đăng nhập 2FA | 1. Bấm **Gửi lại mã** ngay trong vòng `< 20 giây`<br>2. Đợi qua `20 giây`, bấm **Gửi lại mã**<br>3. Thử nhập mã cũ trước đó rồi nhập mã mới | `< 20s` API trả `429` báo đợi; `> 20s` gửi OTP mới vào Gmail, mã cũ mất hiệu lực, chỉ mã mới đăng nhập được | [ ] |
| `TC_AUTH_17` | Tắt 2FA | Đang đăng nhập bằng tài khoản đã bật 2FA | 1. Vào **Bảo mật và đăng nhập** → bấm **Tắt xác thực hai yếu tố**<br>2. Đăng xuất rồi đăng nhập lại | Trạng thái 2FA về **`Chưa thiết lập`**; lần đăng nhập sau vào thẳng Trang chủ không cần OTP | [ ] |

---

### 2.4. Quên mật khẩu & Đổi mật khẩu (`ForgotPasswordActivity` · `ResetPasswordActivity` · `ChangePasswordActivity`)

| Mã TC | Tên kịch bản | Tiền điều kiện | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_AUTH_18` | Quên mật khẩu & đặt lại thành công | Tài khoản Gmail thật đã đăng ký trên hệ thống | 1. Ở màn Đăng nhập, bấm **Quên mật khẩu**<br>2. Nhập Gmail thật → Gửi mã<br>3. Lấy OTP 6 số từ mail → xác thực (`/verify-otp`)<br>4. Nhập mật khẩu mới `>= 8` ký tự & xác nhận khớp → Lưu | Chuyển qua từng bước mượt mà; API `/reset-password` trả `200`; đăng nhập lại bằng mật khẩu mới thành công, mật khẩu cũ lỗi `401` | [ ] |
| `TC_AUTH_19` | Quên mật khẩu — Email không tồn tại / OTP sai | Ở màn Quên mật khẩu | 1. Nhập email sai định dạng → kiểm tra báo lỗi client<br>2. Nhập email đúng định dạng nhưng chưa đăng ký<br>3. Nhập OTP sai ở bước xác nhận | - Sai định dạng: chặn ở client<br>- Email không tồn tại: API vẫn trả `200` (không gửi mail, chống dò email)<br>- OTP sai: API `/verify-otp` trả `400` | [ ] |
| `TC_AUTH_20` | Mật khẩu mới < 8 ký tự / tái sử dụng token reset | Đang ở màn Đặt lại mật khẩu (`ResetPasswordActivity`) | 1. Nhập mật khẩu mới `6–7` ký tự<br>2. Đặt lại thành công xong thử gọi lại API `/reset-password` với cùng `tokenHash` | - `< 8` ký tự: client/server chặn (`400`)<br>- Dùng lại `tokenHash` đã sử dụng: API trả `400 Invalid or expired token` | [ ] |
| `TC_AUTH_21` | Đổi mật khẩu trong Cài đặt | Đăng nhập bằng tài khoản Email + Mật khẩu | 1. Vào **Cài đặt → Đổi mật khẩu** (hoặc trong **Bảo mật**)<br>2. Thử nhập sai mật khẩu hiện tại<br>3. Thử mật khẩu mới `< 8` ký tự<br>4. Nhập đúng mật khẩu cũ + mật khẩu mới `>= 8` ký tự | - Sai pass cũ: API trả `401`<br>- Pass mới `< 8` ký tự: chặn client (`400`)<br>- Đổi thành công (`200`): đăng xuất và đăng nhập lại bằng pass mới thành công | [ ] |

---

### 2.5. Phiên đăng nhập & Đăng xuất (`LoginSessionActivity` · `POST /auth/logout`, `/sessions`, `/sessions/revoke`)

| Mã TC | Tên kịch bản | Tiền điều kiện | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_AUTH_22` | Quản lý & thu hồi phiên đăng nhập khác | Cùng 1 tài khoản đăng nhập trên **2 thiết bị/trình duyệt** khác nhau | 1. Trên máy A vào **Bảo mật → Nơi bạn đã đăng nhập**<br>2. Kiểm tra mục phiên hiện tại và danh sách phiên khác<br>3. Bấm thu hồi phiên máy B (hoặc đăng xuất tất cả phiên khác) | Hiển thị đúng tên thiết bị, badge `Thiết bị này` cho máy A; thu hồi xong máy B biến mất khỏi danh sách phiên đang hoạt động trên DB | [ ] |
| `TC_AUTH_23` | Đăng xuất & vô hiệu hóa JWT cũ | Đang đăng nhập | 1. Bấm **Đăng xuất**<br>2. (API) Lấy lại JWT trước khi đăng xuất để gọi `GET /api/v1/users/me` | App xóa session cục bộ, quay về **Đăng nhập**; JWT cũ bị đưa vào blacklist Redis nên gọi API trả `401 Unauthorized` | [ ] |

---

## 3. Test User Profile thì test những gì?

Khi kiểm thử phần **User Profile**, tester cần kiểm tra đủ **6 mảng chức năng** sau:
1. **Xem hồ sơ cá nhân (`ProfileActivity`)**: thông tin hiển thị, role badge, thống kê số bài viết/followers/following, chia sẻ/QR, xử lý hết phiên `401`.
2. **Chỉnh sửa hồ sơ & Upload Avatar (`EditProfileActivity`)**: cập nhật trường hợp lệ, upload ảnh đại diện, ràng buộc ký tự/độ dài/trùng username, cảnh báo rời trang khi chưa lưu, xóa cache profile.
3. **Xem hồ sơ người dùng khác (`UserProfileActivity`)**: dữ liệu công khai, không lộ bài viết đang lưu trữ (`Hidden`), tự chuyển hướng khi xem chính mình, nút Nhắn tin & Báo cáo.
4. **Follow / Unfollow & Danh sách theo dõi (`FollowersActivity`)**: tăng/giảm số đếm, tạo thông báo Follow, chặn tự follow/follow trùng.
5. **Khẩu vị, Quyền riêng tư, Chặn/Hạn chế & Nhật ký hoạt động (`SettingsActivity` và các màn con)**: lưu bền xuống server, lọc đúng nhật ký.
6. **Xóa / Vô hiệu hóa tài khoản (`DeleteAccountActivity`)**: xác nhận 2 bước, thu hồi quyền truy cập ngay lập tức.

### 3.1. Xem hồ sơ cá nhân (`ProfileActivity` · `GET /users/me`, `GET /users/me/posts`)

| Mã TC | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| `TC_PROF_01` | Hiển thị đầy đủ thông tin hồ sơ | Mở tab **Hồ sơ** sau khi đăng nhập | Hiển thị đúng Avatar, Họ tên, `@username`, Tiểu sử (nếu trống hiện `"Chưa có tiểu sử giới thiệu."`), tag khẩu vị, danh sách bài đăng của mình, số đếm Bài viết / Followers / Following | [ ] |
| `TC_PROF_02` | Đếm đúng bài `Active` (không đếm bài ẩn) | Lưu trữ (Archive/Hidden) 1 bài viết rồi quay lại **Hồ sơ** | `postsCount` giảm đi 1, lưới bài viết trên hồ sơ không hiện bài đã lưu trữ | [ ] |
| `TC_PROF_03` | Hiển thị huy hiệu theo Role | Mở hồ sơ bằng tài khoản `RestaurantAccount` / `Admin` / `User` | - `RestaurantAccount`: hiện `"🏪 Đối tác Nhà hàng"` + tích xanh<br>- `Admin`: hiện `"🛡️ Quản trị viên"` + tích xanh<br>- `User`: ẩn huy hiệu & tích xanh | [ ] |
| `TC_PROF_04` | Share hồ sơ & Mã hồ sơ | Bấm biểu tượng **Chia sẻ** và nút **QR** trên Profile | - Share mở bảng chia sẻ kèm tên + `@handle` + link `/users/<id>`<br>- QR mở popup hiển thị đúng tên và `@username` | [ ] |
| `TC_PROF_05` | Tự đá về Login khi token hết hạn | Vô hiệu hóa token (logout từ nguồn khác / hết hạn), quay lại hoặc mở **Hồ sơ** | Khi API `/users/me` trả `401`, app xóa cache cục bộ và đẩy về **Đăng nhập** | [ ] |

---

### 3.2. Chỉnh sửa hồ sơ & Upload Avatar (`EditProfileActivity` · `PUT /users/me`)

| Mã TC | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| `TC_PROF_06` | Cập nhật thông tin hồ sơ thành công | Bấm **Chỉnh sửa hồ sơ** → đổi Họ tên, `@username` hợp lệ, Bio (`<= 255` ký tự), Thành phố, thêm/xóa tag khẩu vị → bấm **Lưu** | API `PUT /users/me` trả `200`; quay lại **Hồ sơ** & **Trang chủ** thấy dữ liệu mới ngay; tài khoản khác mở xem trang cá nhân cũng thấy tên/bio mới (cache Redis đã được xóa) | [ ] |
| `TC_PROF_07` | Đổi Avatar & kiểm tra trạng thái đang tải | Bấm **Đổi ảnh đại diện** → chọn ảnh từ thư viện:<br>1. Bấm **Lưu** ngay khi chữ đang là `"Đang tải ảnh..."`<br>2. Đợi tải lên xong báo thành công rồi bấm **Lưu** | 1. App chặn bấm Lưu sớm, báo `"Ảnh đại diện đang tải lên, vui lòng đợi..."`<br>2. Lưu thành công, thoát ra vào lại avatar mới vẫn giữ nguyên | [ ] |
| `TC_PROF_08` | Validation Họ tên, Username & Bio | Thử lần lượt:<br>- Để trống Họ tên<br>- Để trống Username / `< 3` ký tự / `> 50` ký tự / chứa dấu cách hoặc ký tự đặc biệt (`@user!#`)<br>- Nhập Bio `> 255` ký tự | App chặn ở client với thông báo rõ ràng; bộ đếm Bio đổi màu cam khi vượt `255/255`; API trả `400` nếu gửi dữ liệu sai hoặc `dateOfBirth`/`gender` không hợp lệ | [ ] |
| `TC_PROF_09` | Trùng Username với người khác | Đổi Username thành `tester_foodie` (hoặc username của acc khác đang dùng) → bấm **Lưu** | API trả `409 Conflict` (`Tên người dùng này đã được sử dụng`), app báo lỗi và không ghi đè | [ ] |
| `TC_PROF_10` | Cảnh báo hủy thay đổi khi thoát | 1. Sửa 1 trường bất kỳ → bấm **Hủy** hoặc nút Back<br>2. Không sửa gì → bấm **Hủy** / Back | 1. Hiện hộp thoại `"Hủy thay đổi?"` để tránh mất dữ liệu<br>2. Đóng màn hình ngay lập tức | [ ] |

---

### 3.3. Xem hồ sơ người khác (`UserProfileActivity` · `GET /users/:userId`, `GET /users/:userId/posts`)

| Mã TC | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| `TC_PROF_11` | Xem đúng thông tin & bài công khai của người khác | Bấm vào tên/avatar của 1 tài khoản khác trên bảng tin | Hiển thị đúng tên, `@username`, bio, số Followers/Following, trạng thái nút Theo dõi, lưới bài viết `Active` (không hiện bài `Hidden` của họ) | [ ] |
| `TC_PROF_12` | Mở hồ sơ với ID của chính mình | Chuyển hướng tới `UserProfileActivity` bằng chính `userId` đang đăng nhập | App tự nhận diện và chuyển sang `ProfileActivity` (có nút **Chỉnh sửa hồ sơ**) | [ ] |
| `TC_PROF_13` | User không tồn tại hoặc bị khóa | Mở trang cá nhân của `userId` không tồn tại hoặc tài khoản đang `Banned`/`Suspended` | API trả `400`/`404`, app thông báo không tìm thấy người dùng và đóng màn hình an toàn | [ ] |
| `TC_PROF_14` | Nhắn tin, Sao chép link & Báo cáo | Trên trang người khác: bấm **Nhắn tin**; mở menu 3 chấm chọn **Sao chép liên kết hồ sơ** và **Báo cáo người dùng** | - Nhắn tin mở đúng khung chat với người đó<br>- Sao chép link lưu URL `/users/<id>` vào clipboard<br>- Báo cáo mở `ReportContentActivity` đúng thông tin target | [ ] |

---

### 3.4. Follow / Unfollow & Danh sách Followers / Following (`FollowersActivity`)

| Mã TC | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| `TC_PROF_15` | Follow & Unfollow người khác | 1. Mở hồ sơ acc B → bấm **Theo dõi** (`POST /users/:id/follow`)<br>2. Mở tab **Following** của mình & **Followers** của B<br>3. Bấm **Đang theo dõi** để hủy (`DELETE /users/:id/follow`) | - Follow: nút đổi sang `Đang theo dõi`, `followersCount` của B & `followingCount` của mình tăng +1, B nhận thông báo Follow<br>- Unfollow: số đếm giảm về đúng ban đầu | [ ] |
| `TC_PROF_16` | Case biên Follow (tự follow / follow trùng) | Gọi API `POST /users/<id-của-mình>/follow` và gọi Follow 2 lần liên tiếp | - Tự follow trả `400 Cannot follow yourself`<br>- Follow trùng trả `409 Already following` | [ ] |

---

### 3.5. Khẩu vị, Quyền riêng tư, Chặn / Hạn chế & Nhật ký hoạt động

| Mã TC | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| `TC_PROF_17` | Cập nhật Khẩu vị & Dị ứng (`TastePreferencesActivity`) | Chọn/bỏ chọn các chip khẩu vị có sẵn, thêm khẩu vị tùy chỉnh, tick các ô dị ứng → bấm Lưu → thoát ra vào lại | API `PUT /users/me/preferences` lưu thành công; khẩu vị đồng bộ với hồ sơ cá nhân; trạng thái dị ứng được nhớ theo đúng tài khoản đang đăng nhập | [ ] |
| `TC_PROF_18` | Cài đặt Quyền riêng tư (`PrivacySettingsActivity`) | Đổi giá trị 7 mục (`Bài đăng mới`, `Thông tin cá nhân`, `Danh sách đang theo dõi`, `Tìm bạn bằng email`, `Lời mời kết bạn`, `Tin nhắn`, `Bình luận`) → thoát ra vào lại | API `PUT /users/me/privacy` lưu xuống DB; quay lại vẫn hiển thị đúng các lựa chọn mới | [ ] |
| `TC_PROF_19` | Chặn & Hạn chế tài khoản (`BlockedAccountsActivity`, `RestrictedAccountsActivity`) | 1. Thêm 1 tài khoản khác vào danh sách Đã chặn / Bị hạn chế<br>2. Tìm kiếm trong danh sách<br>3. Bỏ chặn / Bỏ hạn chế<br>4. Gọi API chặn chính mình hoặc ID không tồn tại | Thêm/lọc/gỡ cập nhật đúng trên UI & DB; tự chặn chính mình trả `400`, ID không tồn tại trả `404` | [ ] |
| `TC_PROF_20` | Xem & lọc Nhật ký hoạt động (`ActivityLogActivity`) | Mở **Nhật ký hoạt động** → lọc theo thời gian (`Tất cả`, `Hôm nay`, `7 ngày qua`, `30 ngày qua`) và loại (`Bài viết`, `Bình luận`, `Lượt thích`, `Bài đánh giá`, `Bài đã lưu`); bấm shortcut **Kho lưu trữ** | Danh sách thay đổi đúng theo filter (`GET /users/me/activity`); bấm **Kho lưu trữ** mở đúng `ArchiveActivity` | [ ] |

---

### 3.6. Xóa / Vô hiệu hóa tài khoản (`DeleteAccountActivity` · `DELETE /users/me`)

| Mã TC | Tên kịch bản | Tiền điều kiện | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_PROF_21` | Chưa tick ô xác nhận xóa | Đang mở **Cài đặt → Xóa tài khoản** | Không tick ô checkbox xác nhận, bấm nút **Xóa tài khoản** | Nút xóa ở trạng thái chưa kích hoạt / hiện thông báo yêu cầu tick xác nhận trước | [ ] |
| `TC_PROF_22` | Xóa / Vô hiệu hóa tài khoản thành công | Dùng **tài khoản phụ mới tạo** (không dùng 2 acc seed) | 1. Tick checkbox xác nhận → bấm **Xóa tài khoản** → xác nhận trong hộp thoại<br>2. Thử đăng nhập lại bằng tài khoản vừa xóa<br>3. Thử gọi API với token cũ | - Tài khoản mới chưa phát sinh ràng buộc FK bị xóa hẳn (`deleted`), tài khoản đã có dữ liệu liên kết chuyển sang `Suspended` (`deactivated`)<br>- Token cũ bị blacklist & dữ liệu cache Redis bị xóa<br>- Không đăng nhập lại được | [ ] |

---

## 4. Test Cases — Trang Home (`HomeActivity` & Bảng tin)

### 4.1. Tải bảng tin, Header, Sidebar & Điều hướng (`GET /api/v1/posts`)

| Mã TC | Tên kịch bản | Tiền điều kiện | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_HOME_01` | Hiển thị bảng tin (Newsfeed) | Đã đăng nhập vào Trang chủ | 1. Mở màn **Trang chủ**<br>2. Cuộn xem các bài viết trên feed | Tải và hiển thị danh sách bài `Active` từ server (avatar, tên tác giả, `@handle`, tích xanh cho Nhà hàng/Admin, địa điểm + thời gian, nội dung, ảnh, voucher, hashtag); **không** hiển thị bài `Hidden` hoặc đã xóa | [ ] |
| `TC_HOME_02` | Xử lý mất mạng / lỗi API trên Home | Tắt Wi-Fi/4G rồi mở app hoặc làm mới | 1. Tắt mạng khi chưa có cache bài nào → mở Home<br>2. Đang có bài trên Home → tắt mạng rồi quay lại Home từ màn khác | 1. Hiện thông báo `"Không thể kết nối máy chủ."`<br>2. Giữ bài cũ đang hiển thị và hiện Toast `"Không thể làm mới bảng tin"` | [ ] |
| `TC_HOME_03` | Thanh tìm kiếm trực tiếp trên Home | Đang ở Trang chủ có nhiều bài | 1. Gõ từng từ khóa vào ô tìm kiếm (`phở`, `@tên`, tên quán, `#hashtag`, mã voucher)<br>2. Gõ chuỗi không tồn tại<br>3. Xóa trống ô tìm kiếm | 1. Bảng tin lọc realtime theo nội dung, tác giả, địa điểm, voucher, hashtag (không phân biệt hoa/thường)<br>2. Hiện `"Không tìm thấy bài viết phù hợp."`<br>3. Khôi phục toàn bộ bài | [ ] |
| `TC_HOME_04` | Chuyển tới Tìm kiếm nâng cao / Bộ lọc / Hashtag | Đang ở Trang chủ | 1. Bấm icon **Kính lúp** hoặc nút **Bộ lọc**<br>2. Bấm trực tiếp vào một `#hashtag` màu cam trên card bài viết | 1. Mở màn **Tìm kiếm** (`SearchActivity`)<br>2. Mở **Kết quả tìm kiếm** (`SearchResultsActivity`) lọc đúng hashtag vừa bấm | [ ] |
| `TC_HOME_05` | Mở Sidebar Drawer & điều hướng menu bên | Đang ở Trang chủ | 1. Bấm vào logo **Dishcision** góc trên trái để mở Sidebar<br>2. Kiểm tra thông tin user trên Sidebar<br>3. Bấm từng mục: `Dành cho bạn`, `Món ăn yêu thích`, `Quán ăn yêu thích`, `Đánh giá của tôi`, `Bài viết của tôi` | Drawer mở mượt; hiện đúng avatar, `fullName`, `@username` đang đăng nhập; mỗi mục đóng drawer và mở đúng màn hình tương ứng | [ ] |
| `TC_HOME_06` | Chuông thông báo & chấm đỏ chưa đọc | Tài khoản vừa nhận được Like / Comment / Follow mới | 1. Quan sát icon chuông góc trên phải Home<br>2. Bấm vào chuông → xem & đọc hết thông báo → quay lại Home | Khi có thông báo chưa đọc, chấm đỏ nhỏ hiển thị trên chuông; sau khi đọc hết và quay lại Home, chấm đỏ tự ẩn | [ ] |
| `TC_HOME_07` | Thanh điều hướng dưới đáy (Bottom Nav) | Đang ở Trang chủ | Cuộn xuống giữa bảng tin rồi bấm **Trang chủ**; sau đó thử bấm `Quyết định`, `Tạo bài`, `Tin nhắn`, `Cá nhân` | Bấm `Trang chủ` cuộn mượt lên đầu trang; 4 nút còn lại mở đúng từng màn hình tương ứng | [ ] |

---

### 4.2. Xem & Lướt nhiều ảnh + Xem ảnh tràn màn hình (`ViewPager` · `FullscreenImageActivity`)

| Mã TC | Tên kịch bản | Tiền điều kiện | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_HOME_08` | Lướt nhiều ảnh ngay trên card ở Home | Có bài viết chứa từ **2 ảnh trở lên** (VD: bài có badge `1 / 2`) | 1. Quan sát góc ảnh trên card bài viết ở Trang chủ<br>2. Vuốt ngang sang trái/phải để chuyển sang ảnh thứ 2 và vuốt ngược lại | Card hiện badge `1 / 2` (bài 1 ảnh ẩn badge); vuốt ngang chuyển ảnh mượt mà và badge cập nhật thành `2 / 2` → `1 / 2` | [ ] |
| `TC_HOME_09` | Bấm vào ảnh mở chế độ tràn màn hình | Có bài viết 1 ảnh và bài 2 ảnh trên Home | 1. Bấm vào ảnh đầu tiên (hoặc vuốt sang ảnh `2 / 2` rồi bấm vào ảnh)<br>2. Quan sát viền trên, tai thỏ/status bar | Mở màn ảnh toàn màn hình nền đen (`FullscreenImageActivity`), **không** bị dính thanh header chữ Dishcision đè ở đầu trang, **không** bị thanh trạng thái che cắt ở đỉnh | [ ] |
| `TC_HOME_10` | Vuốt đổi ảnh & đóng màn hình Fullscreen | Đang mở ảnh từ bài có nhiều ảnh ở `TC_HOME_09` | 1. Kiểm tra ảnh mở ra đúng vị trí vừa bấm (VD: `2 / 2`) và caption/tác giả bên dưới<br>2. Vuốt ngang sang ảnh khác<br>3. Bấm nút `✕` hoặc chạm vào nền/ảnh | Bộ đếm trên đầu cập nhật khi vuốt (`1 / 2` ↔ `2 / 2`); bấm `✕` hoặc chạm ảnh đóng gọn màn hình về đúng vị trí đang đọc trên Home | [ ] |

---

### 4.3. Tương tác trên bài viết: Thả tim, Bình luận, Lưu, Chia sẻ, Voucher

| Mã TC | Tên kịch bản | Tiền điều kiện | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_HOME_11` | Thả tim (`Like`) & Hủy tim (`Unlike`) | Đang ở Trang chủ | 1. Bấm **Thích** ở 1 bài viết chưa thả tim<br>2. Thoát app mở lại kiểm tra bài đó<br>3. Bấm **Thích** thêm lần nữa | 1. Tim chuyển màu đỏ ngay, số thích tăng +1 (tác giả bài nhận thông báo `Like`)<br>2. Vào lại vẫn giữ trạng thái tim đỏ<br>3. Hủy tim về màu đen, số thích giảm -1 | [ ] |
| `TC_HOME_12` | Mở & gửi Bình luận mới (`Comment`) | Đang ở Trang chủ | 1. Bấm **Bình luận** trên card bài viết<br>2. Thử gửi bình luận rỗng<br>3. Chọn emoji từ thanh emoji + nhập nội dung → Gửi<br>4. Đổi sắp xếp `Mới nhất` ↔ `Cũ nhất` | - Gửi rỗng bị chặn bằng Toast<br>- Gửi thành công (`201`): bình luận mới hiện ngay trong danh sách, tiêu đề `Bình luận (N)` và số đếm trên card Home tăng +1<br>- Sắp xếp đảo chiều đúng thứ tự | [ ] |
| `TC_HOME_13` | Trả lời (`Reply`) & Xóa bình luận của mình | Đang mở BottomSheet bình luận | 1. Bấm **Trả lời** dưới 1 bình luận → gửi nội dung<br>2. Kiểm tra hủy trả lời (chạm vào dòng `"Đang trả lời ... • Nhấn để hủy"`)<br>3. Bấm **Xóa** (màu đỏ) trên bình luận do mình viết | - Trả lời hiển thị thụt lề ngay dưới bình luận cha; có thể bấm hủy để về viết bình luận gốc<br>- Nút `Xóa` chỉ hiện ở bình luận của mình; xóa xong danh sách & số đếm cập nhật giảm đúng | [ ] |
| `TC_HOME_14` | Lưu bài viết (`Bookmark`) | Đang ở Trang chủ | 1. Bấm icon **Bookmark** trên bài chưa lưu<br>2. Vào **Món ăn yêu thích** (`FavoriteDishesActivity`) kiểm tra<br>3. Bấm lại Bookmark để bỏ lưu | Icon đổi trạng thái đã lưu; bài xuất hiện trong danh sách đã lưu (`GET /users/me/saved-posts`); bấm lại bỏ lưu thành công | [ ] |
| `TC_HOME_15` | Sao chép Mã ưu đãi (`Voucher`) | Tìm bài viết có badge `🎟 Mã ưu đãi: ...` | Bấm trực tiếp vào badge Mã ưu đãi trên card bài viết | Copy đúng mã ưu đãi vào clipboard và hiện Toast `"Đã sao chép mã ưu đãi: ..."` | [ ] |
| `TC_HOME_16` | Chia sẻ bài viết qua Chat & Copy link | Đang ở Trang chủ | 1. Bấm **Chia sẻ** trên bài viết<br>2. Bấm **Sao chép liên kết**<br>3. Mở lại **Chia sẻ**, tìm & chọn 1–2 bạn bè trong danh sách → bấm Gửi trên Dishcision | - Copy link chép đúng URL `/posts/<id>` vào clipboard<br>- Gửi cho bạn bè tạo tin nhắn chứa link + preview bài trong **Tin nhắn** và gọi `/posts/:id/shares` ghi nhận lượt chia sẻ | [ ] |
| `TC_HOME_17` | Bấm Avatar / Tên tác giả trên bài viết | Có bài của người khác và bài của chính mình trên Home | 1. Bấm avatar/tên ở bài người khác<br>2. Bấm avatar/tên ở bài chính mình | 1. Mở `UserProfileActivity` của người đó<br>2. Mở `ProfileActivity` của bản thân | [ ] |

---

### 4.4. Tạo, Sửa, Lưu trữ, Xóa & Báo cáo bài viết từ Home (`navCreate`, `⋮` Options)

| Mã TC | Tên kịch bản | Tiền điều kiện | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_HOME_18` | Đăng bài mới từ nút `+ Tạo bài` | Đang ở Trang chủ | 1. Bấm **Tạo bài** ở thanh dưới<br>2. Nhập nội dung, chọn **2 ảnh** (thử chọn thêm ảnh thứ 3), nhập địa điểm/voucher/hashtag → Đăng<br>3. Quay về Trang chủ | Tối đa 2 ảnh/bài ở màn Tạo bài (ảnh 3 bị chặn báo tối đa 2 ảnh); đăng thành công (`201`), Home tự làm mới và bài mới hiện lên đầu bảng tin | [ ] |
| `TC_HOME_19` | Menu `⋮` bài của chính mình — Sửa bài | Trên Home có bài do tài khoản đang đăng nhập tạo | Bấm icon `⋮` góc phải card → kiểm tra các mục → bấm **Chỉnh sửa bài viết** → sửa nội dung/ảnh/voucher → Lưu | Chỉ bài của mình mới hiện `Chỉnh sửa`, `Lưu trữ`, `Xóa` (ẩn `Báo cáo`); lưu sửa xong (`PUT /posts/:id`) Home cập nhật nội dung mới ngay | [ ] |
| `TC_HOME_20` | Menu `⋮` bài của chính mình — Lưu trữ (`Hidden`) | Trên Home có bài của mình | Bấm `⋮` → chọn **Lưu trữ bài viết** → vào **Cài đặt → Kho lưu trữ** (`ArchiveActivity`) | Bài biến mất khỏi bảng tin Home ngay lập tức, chuyển trạng thái `Hidden` và hiển thị trong Kho lưu trữ | [ ] |
| `TC_HOME_21` | Menu `⋮` bài của chính mình — Xóa bài | Dùng bài vừa tạo ở `TC_HOME_18` (tránh xóa bài seed) | Bấm `⋮` → chọn **Xóa bài viết** → xác nhận `Xóa` trên dialog | API `DELETE /posts/:id` trả `200`; bài biến mất khỏi Home & Profile; gọi `GET /posts/:id` trả `404` | [ ] |
| `TC_HOME_22` | Menu `⋮` bài của người khác — Báo cáo bài viết | Trên Home có bài của người khác | Bấm `⋮` trên bài người khác → kiểm tra menu → bấm **Báo cáo bài viết** | Chỉ hiện `Sao chép liên kết`, `Chia sẻ`, `Báo cáo` (ẩn `Sửa/Lưu trữ/Xóa`); mở đúng màn `ReportContentActivity` điền sẵn thông tin bài bị báo cáo | [ ] |

---

## 5. Mẫu báo cáo lỗi (Bug Report Template) cho Tester

Khi phát hiện lỗi, tester ghi lại theo mẫu sau để dev tái hiện nhanh:

```text
- Mã Test Case: (VD: TC_AUTH_14 / TC_PROF_07 / TC_HOME_09)
- Thiết bị & Phiên bản app: (VD: Android 14 / commit f02c9c0)
- Tài khoản test: (Email/username, là tài khoản Google hay Email+Pass)
- Các bước tái hiện:
  1. ...
  2. ...
- Kết quả thực tế: (Kèm ảnh chụp màn hình / mã lỗi HTTP nếu có)
- Kết quả mong đợi: ...
```

