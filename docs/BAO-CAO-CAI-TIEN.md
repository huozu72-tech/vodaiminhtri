# Báo cáo rà soát & cải tiến — Ứng dụng đặt đồ ăn Android (QL_Order_Androi)

| | |
|---|---|
| **Ngày lập** | 25/10/2026 |
| **Phạm vi** | Toàn bộ mã nguồn module `:app` — package `com.example.chieuthu3_ketthucmonhoc` (UI View-based + ViewBinding, dữ liệu in-memory, không mạng/DB) |
| **Vùng rà soát** | ① Bảo mật & xác thực · ② Đặt hàng & thanh toán · ③ Trợ lý AI · ④ Kiến trúc & kiểm thử |
| **Trạng thái mã nguồn** | Nhánh `phong`, nền commit `a2cb0db`; 12 nhóm nâng cấp nằm trong working tree — 11 file sửa + 4 lớp test mới, **chưa commit** |
| **Kết quả kiểm thử** | ✅ **67/67 unit test ĐẠT (0 lỗi, 0 bỏ qua) và build APK debug ĐẠT** — tôi đã chạy lại trực tiếp ngày 25/10/2026 để xác minh (chi tiết Mục 5) |

> **Ghi chú về số dòng:** các vị trí `file:line` ở **Mục 3 (Phát hiện)** phản ánh mã nguồn *tại thời điểm rà soát, trước khi nâng cấp*. Sau khi áp dụng các nâng cấp ở Mục 4, số dòng đã dịch chuyển; các vị trí mới được ghi kèm ở Mục 4 (tôi đã đọc lại mã nguồn hiện hành để đối chiếu từng nâng cấp).

> **Nguồn thông tin:** (1) dữ liệu rà soát 30 phát hiện và 12 thay đổi do quy trình cải tiến cung cấp; (2) mã nguồn hiện hành đọc trực tiếp để đối chiếu; (3) các lệnh kiểm thử tôi tự chạy trong phiên này (ghi rõ lệnh và kết quả ở Mục 5).

---

## 1. Tổng quan

Đợt cải tiến gồm hai giai đoạn nối tiếp:

1. **Rà soát** mã nguồn ghi nhận **30 phát hiện** trên 4 vùng, gồm **27 vấn đề** (12 mức **trung bình**, 15 mức **thấp**) và **3 điểm mạnh đã kiểm chứng** cần giữ nguyên.
2. **Hiện thực 12 nhóm nâng cấp** khắc phục trực tiếp **9 phát hiện**, cải thiện một phần **5 phát hiện**, kèm **39 unit test mới** (trong đó 4 lớp test hoàn toàn mới: `SecurityHelperTest`, `InitialDataFactoryTest`, `MenuRepositoryImplTest`, `UserRepositoryImplTest`), đưa bộ test từ **28 lên 67 test**.

Các vấn đề trọng yếu nhất đã được xử lý:

- **Cổng đăng nhập quản trị 2 lớp** — trước đây bất kỳ ai cũng vào thẳng màn CRUD món/hủy đơn/xem doanh thu;
- **Mã đơn hàng duy nhất, tăng dần** — hết nguy cơ trùng mã đơn seed và ghi đè im lặng làm mất dữ liệu đơn;
- **Kiểm soát chuyển trạng thái đơn hợp lệ** theo luồng PENDING → PREPARING → COMPLETED / CANCELLED;
- **Hiển thị URL QR VietQR thật** (trước đây dialog thanh toán chỉ có ảnh placeholder) và **bỏ hard-code "Bàn 12"**;
- **4 sửa biên cho Trợ lý AI**: ngân sách không món hợp, số VND 7 chữ số, chuỗi rỗng, ảnh không khớp từ khóa.

Kết quả kiểm thử sau nâng cấp: **67/67 test ĐẠT, build APK debug ĐẠT** (chạy xác minh lại lần cuối lúc 10:01–10:04 ngày 25/10/2026, xem Mục 5). Cổng kiểm thử độc lập của quy trình cũng xác nhận xanh (`gateGreen=true`, `baselineGreen=true` theo dữ liệu đầu vào).

---

## 2. Điểm mạnh đã kiểm chứng (cần giữ nguyên)

1. **Kiến trúc phân lớp & DI gần như hoàn hảo** (phát hiện KT-8): cả 2 Activity và 6 adapter chỉ lấy dependency từ `AppServiceLocator` qua kiểu `I*Service`; grep `import android` trong `domain/` + `data/` trả về **0 kết quả** → service thuần Kotlin, unit-testable đúng chuẩn AGENTS.md. Vị trí: `di/AppServiceLocator.kt:27-80`, `MainActivity.kt:39-44`, `AdminActivity.kt:32-35`.
2. **Hàng rào manifest + tách phiên hai vai trò** (BC-7): `AdminActivity` khai báo `android:exported="false"` (`AndroidManifest.xml:24-27`), `currentCustomer`/`currentAdmin` là 2 biến riêng (`AuthenticationServiceImpl.kt:11-12`), toàn bộ auth đi qua interface `IAuthenticationService` với `AuthResult` sealed class — nền sẵn có, nay được tận dụng cho cổng xác thực mới (U1, U2).
3. **Validation nghiệp vụ nằm đúng tầng service** (ĐH-7): `require(tableNumber.isNotBlank())`, `require(cartItems.isNotEmpty())`, snapshot `unitPrice` tại thời điểm đặt (`OrderServiceImpl.kt:23-32` khi rà soát); UI chặn giỏ rỗng ở cả checkout lẫn mở dialog (`MainActivity.kt:231-236, 248-251`); URL VietQR sinh đúng tham số `amount` + `addInfo` (URLEncoder) và có test.
4. **Chuẩn hóa input ở repository** (bằng chứng của BC-5): email/username tra cứu **trim + không phân biệt hoa thường** (`UserRepositoryImpl.kt:26, 47`) — nay được khóa bằng `UserRepositoryImplTest` (U12).
5. **Seed data lưu hash, không lưu plaintext** (bằng chứng của BC-4): `InitialDataFactory.kt:21, 28, 42` đều gọi `SecurityHelper.hashPassword` — được khóa bằng `InitialDataFactoryTest` (U10) và `SecurityHelperTest` (U9).
6. **Hành vi "số lượng về 0 thì xóa món khỏi giỏ" có regression test** (phần điểm mạnh của KT-3): `CartServiceTest.kt:57-64` assert giỏ rỗng sau `updateQuantity(id, 0)` — nút trừ ở UI (`MainActivity.kt:211`) đang dựa vào hành vi này.
7. **Thread-safety cơ bản:** `OrderRepositoryImpl.kt:14` và `CartRepositoryImpl.kt:9` dùng `CopyOnWriteArrayList`.
8. **Bộ test đầu vào hoàn toàn xanh** (bằng chứng KT-1): `./gradlew testDebugUnitTest --console=plain` → BUILD SUCCESSFUL, 28 test / 0 failure / 0 skip.

---

## 3. Phát hiện theo vùng

### Bảng tổng hợp 30 phát hiện

Chú thích trạng thái: ✅ đã sửa hoàn toàn · 🔶 sửa một phần · ❌ chưa sửa · 💪 điểm mạnh giữ nguyên. Mã U# tham chiếu nâng cấp ở Mục 4.

| ID | Vùng | Mức độ | Tóm tắt | Trạng thái |
|----|------|--------|---------|------------|
| BC-1 | Bảo mật | Trung bình | Màn quản trị không có cổng xác thực | ✅ Đã sửa (U1, U2) |
| BC-2 | Bảo mật | Trung bình | Đăng ký tạo được tài khoản không SĐT lẫn email | ❌ Chưa sửa |
| BC-3 | Bảo mật | Trung bình | Không có đường đăng xuất trong UI | ❌ Chưa sửa |
| BC-4 | Bảo mật | Thấp | Băm mật khẩu SHA-256 không salt | ❌ Chưa sửa (U9 làm nền) |
| BC-5 | Bảo mật | Thấp | Thông báo lỗi đăng nhập lộ danh sách tài khoản | ❌ Chưa sửa |
| BC-6 | Bảo mật | Thấp | UI trim khoảng trắng mật khẩu âm thầm | ❌ Chưa sửa |
| BC-7 | Bảo mật | — | 💪 exported=false, tách phiên khách/quản trị | Giữ nguyên |
| ĐH-1 | Đặt hàng | Trung bình | Dialog thanh toán chỉ hiện ảnh placeholder, QR thật không render | 🔶 Sửa một phần (U4) |
| ĐH-2 | Đặt hàng | Trung bình | Hard-code "Bàn 12" cho mọi đơn | ✅ Đã sửa (U3) |
| ĐH-3 | Đặt hàng | Trung bình | Admin đặt được trạng thái tùy ý cho đơn (kể cả quay lui) | 🔶 Sửa ở service (U5), UI còn |
| ĐH-4 | Đặt hàng | Trung bình | Mã đơn ngẫu nhiên có thể trùng → ghi đè mất đơn cũ | ✅ Đã sửa (U6) |
| ĐH-5 | Đặt hàng | Thấp | Khách không xem lại được đơn đã đặt, không hủy được đơn | ❌ Chưa sửa |
| ĐH-6 | Đặt hàng | Thấp | Dialog trạng thái map index→enum thủ công | ❌ Chưa sửa |
| ĐH-7 | Đặt hàng | — | 💪 Phân lớp sạch, guard nghiệp vụ đúng chỗ | Giữ nguyên |
| AI-1 | Trợ lý AI | Trung bình | Nhánh ngân sách không có else → gợi ý món đắt hơn ngân sách | ✅ Đã sửa (U7) |
| AI-2 | Trợ lý AI | Thấp | Regex parse sai VND từ 7 chữ số ("1000000" → 1.000đ) | ✅ Đã sửa (U7) |
| AI-3 | Trợ lý AI | Trung bình | Chatbot không khớp từ khóa tiếng Việt không dấu | ❌ Chưa sửa |
| AI-4 | Trợ lý AI | Trung bình | Test AI chỉ phủ happy path | 🔶 Bổ sung một phần (U7, U8) |
| AI-5 | Trợ lý AI | Thấp | sendMessage nhận chuỗi rỗng ở tầng service | ✅ Đã sửa (U7) |
| AI-6 | Trợ lý AI | Thấp | Ảnh không khớp vẫn khẳng định "nhận diện thành công" | ✅ Đã sửa (U7) |
| AI-7 | Trợ lý AI | Thấp | clearChat() là API chết, lịch sử chat tăng vô hạn | ❌ Chưa sửa |
| AI-8 | Trợ lý AI | Thấp | getSimilarDishes chưa wiring UI; score có thể vượt 1.0 | 🔶 Sửa score + reason (U8) |
| KT-1 | Kiến trúc & kiểm thử | Trung bình | Nhiều nhánh nghiệp vụ trọng yếu chưa có test | 🔶 Bổ sung một phần (U5–U12) |
| KT-2 | Kiến trúc & kiểm thử | Trung bình | Mã đơn trùng/ghi đè (cùng gốc với ĐH-4) | ✅ Đã sửa (U6) |
| KT-3 | Kiến trúc & kiểm thử | Thấp | Guard addItem/updateQuantity bất đối xứng | ❌ Chưa sửa (giữ hành vi chủ ý) |
| KT-4 | Kiến trúc & kiểm thử | Thấp | Filter isAvailable đặt sai tầng một cách bất nhất | ❌ Chưa sửa |
| KT-5 | Kiến trúc & kiểm thử | Thấp | "Món bán chạy" tính cả đơn đã hủy | ❌ Chưa sửa (cần quyết định nghiệp vụ) |
| KT-6 | Kiến trúc & kiểm thử | Thấp | So sánh mã đơn không nhất quán (ignoreCase vs `==`) | ✅ Đã sửa (U6) |
| KT-7 | Kiến trúc & kiểm thử | Thấp | Default args repository tự gọi InitialDataFactory | ❌ Chưa sửa |
| KT-8 | Kiến trúc & kiểm thử | — | 💪 Tuân thủ layered architecture & DI | Giữ nguyên |

### 3.1 Vùng Bảo mật & xác thực (7 phát hiện)

**BC-1 · Trung bình · ✅ ĐÃ SỬA (U1, U2) — Màn quản trị hoàn toàn không có cổng xác thực**
- **Vấn đề:** Nút trong dialog đăng nhập điều hướng thẳng vào `AdminActivity`, trong khi `loginAdmin()` đã triển khai và có test nhưng chưa bao giờ được UI gọi — bất kỳ ai cũng vào được khu CRUD món/hủy đơn/xem doanh thu.
- **Vị trí:** `MainActivity.kt:394-397`, `AdminActivity.kt:37-54` (khi rà soát).
- **Bằng chứng:** UI chỉ gọi `startActivity(Intent(this, AdminActivity::class.java))`; grep toàn bộ `app/src/main`: `loginAdmin` chỉ xuất hiện ở `IAuthenticationService.kt:14`, `AuthenticationServiceImpl.kt:80` và `AuthenticationServiceTest.kt:87` — không có call site UI nào. Phân tích đã chạy `./gradlew testDebugUnitTest --tests "com.example.chieuthu3_ketthucmonhoc.services.AuthenticationServiceTest"` → BUILD SUCCESSFUL, XML: 6 test / 0 failure / 0 error (đăng nhập admin/admin123 seeded hoạt động, `AuthenticationServiceTest.kt:83-90`) — service sẵn sàng, chỉ thiếu wiring UI. Khớp thẻ "Điểm cần lưu ý" của sơ đồ kiến trúc archify (candidate.json:661).

**BC-2 · Trung bình · ❌ CHƯA SỬA — Đăng ký tạo được tài khoản "ma" không SĐT lẫn email**
- **Vấn đề:** Khi ô SĐT/email để trống, UI tự sinh số điện thoại giả làm tràn qua validation của service, tạo ra tài khoản **không bao giờ đăng nhập lại được** và có thể va chạm SĐT với tài khoản thật.
- **Vị trí:** `MainActivity.kt:378-379` (khi rà soát; tôi xác nhận dòng này vẫn còn ở `MainActivity.kt:443-444` hiện hành).
- **Bằng chứng:** `val phone = if (!phoneOrEmail.contains("@")) phoneOrEmail else "090" + System.currentTimeMillis() % 10000000` — ô trống cho phone giả "090…" đủ 10 ký tự, qua được kiểm tra độ dài tối thiểu 9 ở `AuthenticationServiceImpl.kt:27-29`; lần đăng nhập sau với ô trống bị chặn ở `AuthenticationServiceImpl.kt:58-60` ("Vui lòng nhập đầy đủ…"); giá trị giả trùng không gian SĐT thật nên có thể gặp "Số điện thoại đã được đăng ký" (`AuthenticationServiceImpl.kt:37-39`).
- **Đề xuất:** Trong `showLoginDialog()`, chặn trước khi gọi service khi `phoneOrEmail.isBlank()` (nhấn Đăng Ký); chỉ sinh phone thay thế khi người dùng đã nhập email.

**BC-3 · Trung bình · ❌ CHƯA SỬA — Không có đường đăng xuất trong UI**
- **Vấn đề:** `logout()` đã có ở service nhưng không screen nào gọi; phiên khách hàng gắn vĩnh viễn tới khi tắt app — đơn tiếp theo vẫn được gán cho khách trước đó trên thiết bị dùng chung.
- **Vị trí:** `AuthenticationServiceImpl.kt:97-100`; `MainActivity.kt:350-400` (khi rà soát).
- **Bằng chứng:** grep `logout` toàn `app/src/main`: chỉ có định nghĩa `IAuthenticationService.kt:15` và triển khai `AuthenticationServiceImpl.kt:97`, không call site; `MainActivity.kt:187-194` hiển thị tên khách đã đăng nhập, `MainActivity.kt:149/173-175` mở lại dialog đăng nhập, `MainActivity.kt:269-273` gắn `customerId` hiện tại vào mọi đơn mới tạo.
- **Đề xuất:** Trong `showLoginDialog()`, nếu `authService.getCurrentCustomer() != null` thì đổi nội dung dialog thành trạng thái tài khoản + nút "Đăng xuất" gọi `authService.logout()`, `updateUserHeader()`, `refreshAiRecommendations()`.

**BC-4 · Thấp · ❌ CHƯA SỬA (U9 đã làm nền) — Băm mật khẩu bằng SHA-256 không salt**
- **Vấn đề:** Hash nhanh, dò trực tiếp/bảng cầu vồng được; hai người cùng mật khẩu có hash giống hệt nhau (dễ thấy ở seed "123456").
- **Vị trí:** `SecurityHelper.kt:6-10` (tôi xác nhận chưa đổi: vẫn `MessageDigest.getInstance("SHA-256")` hex hóa, không salt, không stretch).
- **Bằng chứng:** Điểm mạnh liên quan: seed lưu hash chứ không plaintext (`InitialDataFactory.kt:21, 28, 42`), và `verifyPassword` (dòng 12-14) so sánh hex ignoreCase nhất quán với đầu ra.
- **Đề xuất:** Nâng cấp lên PBKDF2WithHmacSHA256 (`java.crypto` sẵn có) với salt ngẫu nhiên + số vòng lặp, lưu dạng `salt:hash` — cả seed lẫn verify đều đi qua `SecurityHelper` nên đổi một chỗ là nhất quán; `SecurityHelperTest` (U9) được thiết kế không khóa chi tiết triển khai nên nâng cấp không vỡ test.

**BC-5 · Thấp · ❌ CHƯA SỬA — Thông báo lỗi đăng nhập lộ danh sách tài khoản (user enumeration)**
- **Vấn đề:** Phân biệt "tài khoản không tồn tại" và "mật khẩu không chính xác" cho phép dò SĐT/email/username đã đăng ký.
- **Vị trí:** `AuthenticationServiceImpl.kt:68-74` và `87-91`.
- **Bằng chứng:** Dòng 68-70 trả "Tài khoản không tồn tại trên hệ thống" khi không tìm thấy khách, dòng 72-74 trả "Mật khẩu không chính xác" khi sai mật khẩu; admin tương tự ở dòng 87 và 89-91.
- **Đề xuất:** Đổi cả hai nhánh về cùng thông báo chung, ví dụ "Tài khoản hoặc mật khẩu không chính xác" — sửa 4 xâu là xong.

**BC-6 · Thấp · ❌ CHƯA SỬA — UI trim khoảng trắng đầu/cuối mật khẩu trước khi băm**
- **Vấn đề:** Nhất quán nên không gây lệch đăng nhập, nhưng âm thầm thay đổi mật khẩu người dùng tự chọn (mất ký tự space đầu/cuối) thay vì từ chối và báo lỗi.
- **Vị trí:** `MainActivity.kt:358, 376` (khi rà soát; hiện hành tương đương ở `MainActivity.kt:423, 441` — tôi xác nhận vẫn `.trim()`).
- **Bằng chứng:** Hai nhánh login/register trim giống hệt nhau; `AuthenticationServiceImpl.kt:30` kiểm tra độ dài trên chuỗi đã trim; layout đã đúng chuẩn cơ bản: `inputType="textPassword"` + toggle (`dialog_login.xml:63-69`).
- **Đề xuất:** Bỏ `.trim()` cho mật khẩu ở cả hai nhánh, chỉ trim trường định danh; nếu muốn chặn space đầu/cuối thì báo lỗi rõ ràng thay vì cắt im lặng.

**BC-7 · 💪 ĐIỂM MẠNH GIỮ NGUYÊN — Hàng rào manifest, tách phiên hai vai trò, auth qua interface**
- **Nội dung:** `AdminActivity` `exported=false` chặn truy cập từ ứng dụng ngoài; phiên khách/quản trị tách bạch trong singleton service; toàn bộ auth đi qua `IAuthenticationService` với `AuthResult` sealed class.
- **Vị trí:** `AndroidManifest.xml:24-27`; `AuthenticationServiceImpl.kt:11-12`; `AppServiceLocator.kt:53-55`.
- **Bằng chứng:** Chỉ `MainActivity` (dòng 14-23) là launcher/exported; đăng nhập khách (dòng 76) không cấp quyền quản trị và ngược lại (dòng 93); `AppServiceLocator.kt:29-55` wire lazy singleton qua interface đúng quy tắc phân lớp trong AGENTS.md. Cổng xác thực mới (U1, U2) đã tận dụng đúng nền này.

### 3.2 Vùng Đặt hàng & thanh toán (7 phát hiện)

**ĐH-1 · Trung bình · 🔶 SỬA MỘT PHẦN (U4) — Dialog thanh toán chỉ hiển thị ảnh placeholder, URL QR thật không bao giờ được render**
- **Vấn đề:** `showPaymentDialog()` không tham chiếu `imgQrCode` hay `qrPayloadUrl`; URL VietQR thật trong `Order.qrPayloadUrl` (chứa amount + addInfo) không được hiển thị.
- **Vị trí:** `MainActivity.kt:247-289` (khi rà soát).
- **Bằng chứng:** `dialog_payment_qr.xml:42-48` đặt ImageView `src="@drawable/ic_restaurant"`; `OrderServiceImpl.kt:45, 55-61` sinh URL img.vietqr.io; grep toàn tree: `generatePaymentQrUrl` chỉ xuất hiện ở `IOrderService.kt:15` và `OrderServiceImpl.kt:51` — không có caller UI nào.
- **Phần còn lại:** U4 hiển thị URL dạng text + nút mở trình duyệt; chưa render **ảnh** QR trong app vì thiếu quyền INTERNET trong AndroidManifest và chưa có thư viện ảnh trong version catalog (chi tiết Mục 6.1).

**ĐH-2 · Trung bình · ✅ ĐÃ SỬA (U3) — Số bàn bị hard-code "Bàn 12"**
- **Vấn đề:** Mọi đơn của mọi khách đều ghi "Bàn 12" ở cả nhãn hóa đơn lẫn đơn tạo ra, trong khi service hỗ trợ bàn bất kỳ và có validate không rỗng.
- **Vị trí:** `MainActivity.kt:259, 271` (khi rà soát).
- **Bằng chứng:** Chuỗi format "Hóa đơn Bàn 12 • …" và `tableNumber = "Bàn 12"`; không có input số bàn nào trong `dialog_payment_qr.xml` (đọc toàn bộ layout 1-83); `OrderServiceImpl.kt:23` đã có guard yêu cầu bàn không rỗng — service sẵn sàng nhận bàn bất kỳ.

**ĐH-3 · Trung bình · 🔶 SỬA Ở SERVICE (U5), UI CÒN — Chuyển trạng thái đơn không được kiểm soát**
- **Vấn đề:** Admin đặt được bất kỳ trạng thái nào cho bất kỳ đơn nào (kể cả COMPLETED → PENDING, CANCELLED → PENDING); rule "không hủy đơn hoàn thành" chỉ nằm trong `cancelOrder` vốn không bao giờ được UI gọi.
- **Vị trí:** `AdminActivity.kt:219-244` (khi rà soát).
- **Bằng chứng:** `AdminActivity.kt:229-237` luôn hiển thị đủ 4 trạng thái và gọi `updateOrderStatus` cho mọi lựa chọn; `OrderServiceImpl.kt:75-77` thuần ủy quyền không kiểm tra; `OrderRepositoryImpl.kt:36-41` chỉ `order.copy(status = newStatus)`; guard COMPLETED ở `OrderServiceImpl.kt:81-83` nằm trong `cancelOrder` nhưng grep toàn tree cho thấy `cancelOrder` không được gọi từ bất kỳ Activity/Adapter nào.
- **Phần còn lại:** U5 đã chặn ở service; dialog admin vẫn hiện đủ 4 lựa chọn và toast thành công bất kể service trả `false` (ngoài phạm vi lượt sửa — Mục 6.1).

**ĐH-4 · Trung bình · ✅ ĐÃ SỬA (U6) — Mã đơn ngẫu nhiên có thể trùng, khi trùng thì ghi đè im lặng**
- **Vấn đề:** Mã đơn chỉ có 9000 giá trị có thể trùng mã seed và trùng nhau; khi trùng, `saveOrder` ghi đè im lặng lên đơn cũ — mất dữ liệu đơn đã tồn tại.
- **Vị trí:** `OrderServiceImpl.kt:26` (khi rà soát).
- **Bằng chứng:** `val orderCode = "#ORD-${1000 + random.nextInt(9000)}"` — không kiểm tra trùng; seed có `#ORD-1024`, `#ORD-1025`, `#ORD-1023` cùng định dạng (`InitialDataFactory.kt:207, 218, 229`); `OrderRepositoryImpl.kt:26-34`: tìm thấy mã trùng thì `orderList[existingIdx] = order` — đơn cũ bị thay thế hoàn toàn, không toast/log.

**ĐH-5 · Thấp · ❌ CHƯA SỬA — Hành trình khách "chết" ngay sau Toast thanh toán**
- **Vấn đề:** Khách không xem được đơn vừa đặt, trạng thái đơn, lịch sử đơn, và không có đường hủy đơn phía khách.
- **Vị trí:** `IOrderService.kt:17` (khai báo `getCustomerOrders`).
- **Bằng chứng:** Grep toàn tree: `getCustomerOrders` chỉ xuất hiện ở `IOrderService.kt:17` và `OrderServiceImpl.kt:67-69` — không Activity/Adapter nào gọi; sau thanh toán chỉ có Toast (`MainActivity.kt:281-285` khi rà soát) rồi luồng kết thúc; `cancelOrder` cũng không reachable từ phía khách.
- **Đề xuất:** Thêm bottom sheet "Đơn của tôi" gọi `getCustomerOrders(authService.getCurrentCustomer()?.id)` hiển thị mã đơn, tổng tiền, giờ, chip trạng thái; khách chưa đăng nhập thì hiện các đơn seed hoặc thông báo đăng nhập.

**ĐH-6 · Thấp · ❌ CHƯA SỬA — Dialog đổi trạng thái map index → enum thủ công**
- **Vấn đề:** Dễ lệch nếu thay đổi enum `OrderStatus` — cơ sở đã có (`OrderStatus.entries` / `fromDisplayName`) nhưng bị bỏ qua.
- **Vị trí:** `AdminActivity.kt:220-235` (khi rà soát).
- **Bằng chứng:** Dòng 220-225 khai báo `statusList` thủ công đủ 4 displayName; dòng 230-235 `when(which)` map 0..3 sang enum lặp lại thứ tự đó; `OrderStatus.kt:10-12` đã có `fromDisplayName` sẵn dùng.
- **Đề xuất:** Dùng `OrderStatus.entries.map { it.displayName }` cho `setItems` và lấy `OrderStatus.entries[which]`.

**ĐH-7 · 💪 ĐIỂM MẠNH GIỮ NGUYÊN — Phân lớp sạch và guard nghiệp vụ đúng chỗ**
- **Nội dung:** Mọi finding của vùng đều sửa được mà không phá kiến trúc: validation nằm ở service layer, Activity chỉ gọi qua `I*Service`.
- **Vị trí:** `OrderServiceImpl.kt:23-32` (khi rà soát).
- **Bằng chứng:** `MainActivity.kt:39-44` chỉ giữ `I*Service` từ `AppServiceLocator`; `require(tableNumber.isNotBlank())` và `require(cartItems.isNotEmpty())` ở dòng 23-24; snapshot `unitPrice` tại thời điểm đặt (dòng 32); URL VietQR đúng tham số (dòng 55-61, có test `OrderServiceTest.kt:46-48`); chặn giỏ rỗng ở UI (`MainActivity.kt:231-236, 248-251`); `clearCart + updateCartBadge + refreshAiRecommendations` sau đặt hàng (dòng 276-278); `OrderRepositoryImpl.kt:14`, `CartRepositoryImpl.kt:9` dùng `CopyOnWriteArrayList`.

### 3.3 Vùng Trợ lý AI (8 phát hiện)

**AI-1 · Trung bình · ✅ ĐÃ SỬA (U7) — Nhánh ngân sách không có nhánh else**
- **Vấn đề:** Khi không món nào ≤ ngân sách, bot bỏ qua ràng buộc giá và rơi về gợi ý mặc định (đều đắt hơn ngân sách), không có câu trả lời kiểu "không có món phù hợp".
- **Vị trí:** `AIChatbotServiceImpl.kt:85-97` (khi rà soát).
- **Bằng chứng:** `val affordable = allItems.filter { it.price <= budget }…` rồi `if (affordable.isNotEmpty()) { … return }` — không có nhánh else; dòng 135-140 là default `allItems.sortedByDescending { it.orderCount }.take(3)`. Kiểm chứng bằng Node với logic tương đương: "dưới 1k" → budget = 1.000đ, trong khi món rẻ nhất seed là 15.000đ (`InitialDataFactory.kt:164` và 3 món nước kế tiếp đều 15.000) → affordable rỗng → bot trả về các món 60.000–160.000đ.

**AI-2 · Thấp · ✅ ĐÃ SỬA (U7) — Regex ngân sách parse sai số VND từ 7 chữ số trở lên**
- **Vấn đề:** "1000000" bị hiểu là 1.000đ thay vì 1.000.000đ.
- **Vị trí:** `AIChatbotServiceImpl.kt:84-87` (khi rà soát).
- **Bằng chứng:** Regex cũ `(\d+)\s*(k|nghìn|nghin|000)`: backtracking khiến group số khớp "1000" và để "000" làm hậu tố. Đã chạy Node kiểm chứng: "1000000" → num=1000, budget=1.000đ; đối chiếu "100000" → 100.000đ (đúng), "dưới 100k" → 100.000đ (đúng).

**AI-3 · Trung bình · ❌ CHƯA SỬA — Chatbot khớp từ khóa tiếng Việt có dấu nguyên văn**
- **Vấn đề:** Input không dấu ("nuoc", "ga nuong", "dem da") không khớp taste lẫn category nên luôn rơi vào default, trong khi tìm kiếm thực đơn chính lại đã normalize dấu.
- **Vị trí:** `TasteAttribute.kt:14-17`.
- **Bằng chứng:** `entries.filter { lower.contains(it.tag) }` — 6/8 tag có dấu ("ngọt", "giòn", "đậm đà", "thanh mát", "nướng", "chiên"); `AIChatbotServiceImpl.kt:117` `lower.contains("nước")`. Đối chiếu `MenuRepositoryImpl.kt:47, 58-62` đã dùng `removeDiacritics` + đ→d cho `searchMenuItems`. Chính regex budget (dòng 84) có sẵn lựa chọn "nghin" không dấu, chứng tỏ input không dấu là kịch bản được kỳ vọng nhưng chỉ budget xử lý.
- **Đề xuất:** Nâng `removeDiacritics` thành util dùng chung; normalize query + tag + keyword trước khi `contains`, đồng bộ với `searchMenuItems`. (U11 đã khóa hành vi chuẩn của tìm kiếm ở repository; chatbot vẫn chưa đổi.)

**AI-4 · Trung bình · 🔶 BỔ SUNG MỘT PHẦN (U7, U8) — Lớp test AI chỉ phủ happy path**
- **Vấn đề:** Thiếu đúng các biên: ngân sách không món nào hợp, chuỗi rỗng, từ khóa không khớp, clearChat, fallback ảnh.
- **Vị trí:** `AIChatbotServiceTest.kt:29-59` (khi rà soát).
- **Bằng chứng:** Đọc toàn bộ 4 test của `AIChatbotServiceTest.kt:14-60` và 3 test của `AIRecommendationServiceTest.kt:37-76`; không test nào cho các biên trên. Phân tích đã chạy `./gradlew testDebugUnitTest --tests "...AIChatbotServiceTest" --tests "...AIRecommendationServiceTest"` → BUILD SUCCESSFUL (baseline xanh).
- **Phần đã bổ sung:** U7 thêm 4 test (ngân sách không món hợp, VND 7 chữ số, chuỗi rỗng, ảnh không khớp), U8 thêm 2 test (chặn điểm 1.0, lý do fallback). **Còn thiếu:** test hint ảnh khớp từ khóa có dấu ("tôm", "gà"… — nhánh `AIChatbotServiceImpl.kt:46-56` khi rà soát), test nhánh query không khớp → default, test `clearChat()`.

**AI-5 · Thấp · ✅ ĐÃ SỬA (U7) — sendMessage không chặn chuỗi rỗng ở tầng service**
- **Vấn đề:** Vẫn thêm bubble user rỗng vào history rồi trả lời default; UI đã chặn (điểm mạnh, `MainActivity.kt:319-320` khi rà soát) nhưng service pure-Kotlin được test độc lập nên contract chưa tự bảo vệ.
- **Vị trí:** `AIChatbotServiceImpl.kt:26-34` (khi rà soát).

**AI-6 · Thấp · ✅ ĐÃ SỬA (U7) — analyzeFoodImage fallback "nói dối"**
- **Vấn đề:** Khi không khớp từ khóa nào: fallback lặng lẽ lấy 2 món đầu tiên và message vẫn khẳng định "hệ thống nhận diện thấy các món rất khớp"; nếu thực đơn trống/toàn unavailable thì `suggestedItems` rỗng nhưng văn bản vẫn báo nhận diện thành công.
- **Vị trí:** `AIChatbotServiceImpl.kt:53-62` (khi rà soát).
- **Bằng chứng:** Dòng 53-56 `.ifEmpty { allItems.take(2) }` lấy 2 món đầu theo thứ tự menu (không phải món liên quan); dòng 59 text cố định; `allItems` (dòng 45, filter `isAvailable`) có thể rỗng.

**AI-7 · Thấp · ❌ CHƯA SỬA — clearChat() là API chết, lịch sử chat tăng vô hạn**
- **Vấn đề:** Không UI nào gọi; history là singleton tăng vô hạn trong phiên ứng dụng.
- **Vị trí:** `AIChatbotServiceImpl.kt:69-77` (khi rà soát).
- **Bằng chứng:** Grep toàn `app/src/main/java`: `clearChat` chỉ xuất hiện tại `IAIChatbotService.kt:9` (khai báo) và `AIChatbotServiceImpl.kt:69` (override); `showAiChatDialog` (`MainActivity.kt:291-329` khi rà soát) không có control làm mới; history là `CopyOnWriteArrayList` singleton lazy qua `AppServiceLocator.kt:73-75`.
- **Đề xuất:** Thêm nút "Làm mới" trong dialog chat gọi `clearChat()` rồi `chatAdapter.updateList`; hoặc giới hạn history ~50 tin cuối trong `sendMessage`.

**AI-8 · Thấp · 🔶 SỬA SCORE + REASON (U8) — getSimilarDishes chưa wiring, lý do rỗng, điểm vượt thang**
- **Vấn đề:** `getSimilarDishes` chưa được wiring vào UI nào (chỉ test gọi); `reason` của nó có thể là chuỗi rỗng; score cá nhân hóa có thể vượt 1.0 (tối đa 1.10) — hiện chưa lộ vì adapter không hiển thị score.
- **Vị trí:** `AIRecommendationServiceImpl.kt:69-93` (khi rà soát).
- **Bằng chứng:** Grep `getSimilarDishes` trong main: chỉ thấy trong test (`AIRecommendationServiceTest.kt:71`) và định nghĩa impl; dòng 77-89: nếu khác category và không taste chung thì reasons rỗng → `joinToString(" • ")` trả ""; dòng 37-60: tổng trọng số tối đa 0.35+0.45+0.20+0.10 = 1.10; `AIRecommendationAdapter.kt:28-30` chỉ bind reason/tên/giá.
- **Phần còn lại:** wiring UI (dialog "Món tương tự" khi click món) chưa làm.

### 3.4 Vùng Kiến trúc & kiểm thử (8 phát hiện)

**KT-1 · Trung bình · 🔶 BỔ SUNG MỘT PHẦN (U5–U12) — Nhiều nhánh nghiệp vụ trọng yếu chưa được test**
- **Vấn đề:** Mọi service đều có lớp test và 28/28 pass ở đầu vào, nhưng nhiều nhánh trọng yếu chưa phủ.
- **Vị trí:** `app/src/test/java/.../services/` (7 lớp test khi rà soát).
- **Bằng chứng:** Phân tích đã chạy `./gradlew testDebugUnitTest --console=plain` → BUILD SUCCESSFUL, XML tổng hợp: 1+4+3+6+5+2+5+2 = 28 test, 0 failure/0 skip. Khoảng trống đối chiếu test với source: (1) `OrderServiceTest.kt` chỉ 2 test — cancelOrder chỉ test nhánh "không hủy được đơn COMPLETED" (dòng 63-66), chưa test PENDING→cancel=true hay `require` ở `OrderServiceImpl.kt:23-24`, `getCustomerOrders` không test; (2) `MenuServiceTest.kt` (5 test) không có test `updateMenuItem` dù `AdminActivity.kt:184` gọi nó, `require` ở `MenuServiceImpl.kt:39-40, 55-56` chưa phủ; (3) `CartServiceTest.kt` không test guard `require` ở `CartServiceImpl.kt:12-13`; (4) `AuthenticationServiceTest.kt` chưa test đăng nhập bằng email (`AuthenticationServiceImpl.kt:62-66`) và các nhánh lỗi loginAdmin; (5) `AIChatbotServiceTest.kt:55` dùng hint không chứa từ khóa có dấu nên chỉ đi nhánh fallback — các nhánh khớp "tôm/gà/ếch…" chưa từng được test.
- **Phần đã bổ sung:** +39 test (chi tiết Mục 4, 5). **Còn thiếu:** cancelOrder PENDING→true, `updateMenuItem` (đổi giá + giá ≤0 ném lỗi), guard `addItem` (quantity 0, món hết hàng), loginCustomer bằng email, analyzeFoodImage với hint chứa "tôm" có dấu.

**KT-2 · Trung bình · ✅ ĐÃ SỬA (U6) — Mã đơn trùng/ghi đè (cùng gốc với ĐH-4)**
- **Vị trí:** `OrderServiceImpl.kt:26` và `OrderRepositoryImpl.kt:27-29` (khi rà soát).
- **Bằng chứng:** `#ORD-1000..9999` chứa gọn 3 đơn seed `#ORD-1023/1024/1025`; `OrderRepositoryImpl.kt:27-29` `if (existingIdx >= 0) { orderList[existingIdx] = order }` — thay thế đơn cũ (khác khách, khác tổng tiền) mà `createOrder` không kiểm tra, dashboard đổi số liệu không giải thích được.

**KT-3 · Thấp · ❌ CHƯA SỬA (giữ hành vi chủ ý) — Guard addItem/updateQuantity bất đối xứng**
- **Vấn đề:** Câu hỏi trong đề bài đã được **VERIFY ĐÚNG**: `updateQuantity` về 0 (hoặc âm) còn **xóa** item khỏi giỏ — hành vi này đã có regression test (điểm mạnh) và UI nút trừ đang dựa vào. Khoảng trống: `addItem` có `require()` còn `updateQuantity` thì không — quantity âm cũng thành xóa item thay vì ném lỗi.
- **Vị trí:** `CartRepositoryImpl.kt:25-32`.
- **Bằng chứng:** Dòng 26-29 `if (quantity <= 0) { removeItem(menuItemId); return }`; `CartServiceTest.kt:57-64` test `updateQuantity(food1.id, 0)` xong assert `assertTrue(cartService.isEmpty())` — pass; ngược lại `CartServiceImpl.kt:12-13` có `require(quantity > 0)` / `require(item.isAvailable)` cho addItem nhưng `updateQuantity` (dòng 17-19) truyền thẳng số bất kỳ xuống repo.
- **Đề xuất:** Giữ nguyên hành vi qty ≤ 0 → xóa; bổ sung test rõ ràng nhánh quantity âm và test cho 2 guard `require` của addItem.

**KT-4 · Thấp · ❌ CHƯA SỬA — Filter isAvailable đặt sai tầng một cách bất nhất**
- **Vấn đề:** `getMenuItemsByCategory` lọc sẵn món khả dụng trong service nhưng `searchMenuItems` thì để Activity tự lọc — logic nghiệp vụ rơi ra ngoài service layer.
- **Vị trí:** `MenuServiceImpl.kt:24-26` so với `MainActivity.kt:127` (khi rà soát).
- **Bằng chứng:** `MenuServiceImpl.kt:21` có `.filter { it.isAvailable }` cho category, còn dòng 24-26 search ủy thẳng `menuRepository.searchMenuItems(query)`; `MainActivity.kt:127` phải tự bù `menuService.searchMenuItems(query).filter { it.isAvailable }`.
- **Đề xuất:** Đẩy filter vào `MenuServiceImpl.searchMenuItems`, bỏ filter trùng ở Activity; thêm test chứng minh search không trả món hết hàng.

**KT-5 · Thấp · ❌ CHƯA SỬA — getTopSellingMenuItems cộng số lượng từ cả đơn đã HỦY**
- **Vấn đề:** "Món bán chạy" bị thổi phồng bởi đơn CANCELLED, trong khi doanh thu cùng hàm `getDashboardMetrics` lại đã lọc đúng chỉ COMPLETED.
- **Vị trí:** `DashboardServiceImpl.kt:39-44`.
- **Bằng chứng:** Vòng `for (order in allOrders)` không lọc status (so với dòng 17 `allOrders.filter { it.status == OrderStatus.COMPLETED }` cho revenue); `DashboardServiceTest.kt:28-32` có đơn #ORD-3 CANCELLED chứa dish2 x1 và assertion dòng 54-59 kỳ vọng dish2 = 3 (2 từ đơn hoàn tất + 1 từ đơn hủy) — test đang "baked in" hành vi đáng nghi.
- **Đề xuất:** Quyết định nghiệp vụ: nếu đơn hủy không tính bán chạy, thêm filter `status != CANCELLED` và sửa kỳ vọng test (dish2 = 2); nếu giữ nguyên, thêm comment giải thích ở cả service lẫn test.

**KT-6 · Thấp · ✅ ĐÃ SỬA (U6) — Không nhất quán so sánh mã đơn**
- **Vấn đề:** `getOrderById` dùng equals ignoreCase nhưng `saveOrder` khớp `==` phân biệt hoa/thường — tiềm ẩn tạo đơn trùng lặp nếu saveOrder với mã khác case.
- **Vị trí:** `OrderRepositoryImpl.kt:19` so với dòng 27 (khi rà soát).
- **Bằng chứng:** Luồng `updateOrderStatus` (dòng 36-41) lấy order từ `getOrderById` rồi `copy(status=…)` giữ nguyên orderId nên không lộ bug hôm nay — ghi nhận là latent, chưa xác nhận có path sản xuất nào truyền mã khác case.

**KT-7 · Thấp · ❌ CHƯA SỬA — Constructor repository có default args tự gọi InitialDataFactory**
- **Vấn đề:** Tạo đường wiring dữ liệu thứ hai song song với `AppServiceLocator`, làm yếu nguyên tắc "chỉ AppServiceLocator biết concrete implementation và nguồn seed".
- **Vị trí:** `OrderRepositoryImpl.kt:10-11` (tương tự `MenuRepositoryImpl.kt:10`, `UserRepositoryImpl.kt:9-10`).
- **Bằng chứng:** `menuItems: List<MenuItem> = InitialDataFactory.createInitialMenuItems(), initialOrders = ...`; `AppServiceLocator.kt:42-47` vẫn truyền explicit nên production không dính, nhưng `AuthenticationServiceTest.kt:84` đang dựa vào default `UserRepositoryImpl()` — ai quên truyền args sẽ nhận seed data im lặng thay vì lỗi rõ ràng.
- **Đề xuất:** Bỏ default args (bắt buộc truyền; test tự gọi InitialDataFactory) hoặc giữ nhưng thêm KDoc ghi rõ default chỉ dành cho test.

**KT-8 · 💪 ĐIỂM MẠNH GIỮ NGUYÊN — Tuân thủ layered architecture & DI gần như hoàn hảo**
- **Nội dung:** Cả 2 Activity và toàn bộ 6 adapter chỉ lấy dependency từ `AppServiceLocator` qua kiểu `I*Service`; domain/data layer không import android nào → service thuần Kotlin, unit-testable đúng chuẩn AGENTS.md.
- **Vị trí:** `AppServiceLocator.kt:27-80`, `MainActivity.kt:39-44`, `AdminActivity.kt:32-35`.
- **Bằng chứng:** Grep import trên 6 file adapters chỉ ra R, databinding và domain.models; grep `import android` trong `domain/` + `data/` trả về 0 kết quả (exit=1); `AppServiceLocator.kt:29-79` wire 4 repository + 7 service đều lazy singleton qua interface.
- **Đề xuất:** Giữ nguyên làm chuẩn cho code mới; cân nhắc thêm architectural test chặn regression (kiểm tra package adapters/activities không import data.repository).

---

## 4. Các nâng cấp đã hiện thực (12 nhóm, 15 file)

Mỗi nâng cấp ghi kèm phát hiện được khắc phục và vị trí mã nguồn **hiện hành** (tôi đã đọc lại để đối chiếu).

### U1. Cổng đăng nhập quản trị trước khi vào AdminActivity — lớp 1: UI gọi loginAdmin
**Khắc phục:** BC-1. **File:** `dialog_login.xml`, `MainActivity.kt`
- `dialog_login.xml` thêm mục "Đăng Nhập Quản Trị" (`etAdminUsername`/`etAdminPassword` + hint tài khoản mẫu admin/admin123, manager/manager123 từ `InitialDataFactory.kt:16-33`, bọc ScrollView).
- `MainActivity.showLoginDialog()` (`MainActivity.kt:459-477`) thay vì `startActivity` trực tiếp giờ gọi `authService.loginAdmin()` — chỉ chuyển sang `AdminActivity` khi `AuthResult.Success` (dòng 463-472), lỗi service hiện Toast.
- ✅ Đối chiếu mã nguồn: đã xác nhận `btnSwitchToAdmin` gọi `loginAdmin` và `startActivity` chỉ nằm trong nhánh `Success`.

### U2. Hàng rào phiên quản trị trong AdminActivity — lớp 2: kiểm tra getCurrentAdmin
**Khắc phục:** BC-1. **File:** `AdminActivity.kt`
- `AdminActivity.onCreate()` (`AdminActivity.kt:41-45`): `getCurrentAdmin() == null` thì Toast + `finish()` + return, chặn truy cập khi chưa đăng nhập quản trị; an toàn với lateinit adapter vì `finish()` trong onCreate khiến hệ thống bỏ qua onStart/onResume.
- ✅ Đối chiếu mã nguồn: cổng kiểm tra nằm đúng vị trí dòng 41-45, trước mọi khởi tạo UI.

### U3. Bỏ hard-code "Bàn 12": người dùng nhập số bàn trong dialog thanh toán
**Khắc phục:** ĐH-2. **File:** `dialog_payment_qr.xml`, `MainActivity.kt`
- Layout thêm TextInputLayout số bàn (inputType number, maxLength 3). `showPaymentDialog()` (`MainActivity.kt:261-287`): mặc định "12" (dòng 278), TextWatcher cập nhật nhãn "Hóa đơn Bàn N" trực tiếp; truyền "Bàn \<n\>" vào `orderService.createOrder` (dòng 304-308); để trống thì hiện lỗi trên `tilTableNumber` (dòng 299-301) và chặn tạo đơn.
- ✅ Đối chiếu mã nguồn: đủ `tableLabelFromInput()`, TextWatcher và nhánh lỗi như mô tả.

### U4. Hiển thị URL QR VietQR thật (order.qrPayloadUrl) trong dialog thanh toán
**Khắc phục:** ĐH-1 (một phần). **File:** `dialog_payment_qr.xml`, `MainActivity.kt`
- Nút "Tạo Đơn & Hiện Mã QR" (`MainActivity.kt:296-322`) tạo đơn PENDING (chưa xóa giỏ), hiển thị `order.qrPayloadUrl` thật (URL img.vietqr.io chứa amount + addInfo) trong `tvQrUrl` selectable (dòng 312-313) + nút "Mở Mã QR VietQR" mở bằng `ACTION_VIEW`, bắt `ActivityNotFoundException` (dòng 324-336); khóa số bàn sau khi tạo đơn (dòng 311); nút "Xác Nhận Đã Thanh Toán" khi đó chỉ clearCart + refresh + Toast (dòng 338-351).
- Không render **ảnh** QR bằng thư viện mạng vì thiếu quyền INTERNET trong AndroidManifest và không có lib ảnh trong version catalog (cả hai file ngoài phạm vi được giao) — hiển thị URL dạng text + nút mở trình duyệt không cần quyền.
- ⚠️ Hành vi còn lại: khách tạo đơn rồi đóng dialog không xác nhận sẽ để lại đơn PENDING; lần mở sau có thể tạo đơn khác (Mục 6.1).

### U5. Cổng kiểm soát chuyển trạng thái đơn hàng hợp lệ trong OrderServiceImpl
**Khắc phục:** ĐH-3 (tầng service). **File:** `OrderServiceImpl.kt`, `OrderServiceTest.kt`
- `updateOrderStatus` (`OrderServiceImpl.kt:93-116` hiện hành) giờ kiểm tra luồng hợp lệ PENDING→PREPARING→COMPLETED và PENDING/PREPARING→CANCELLED: chặn nhảy bước (PENDING→COMPLETED), chặn quay lui và "hồi sinh" đơn đã COMPLETED/CANCELLED; `cancelOrder` (dòng 101-107) ngoài COMPLETED giờ còn chặn cả đơn đã CANCELLED trước đó (trước đây trả true).
- Thêm 4 test: nhảy bước bị chặn, COMPLETED là trạng thái cuối, CANCELLED không hủy lại/khôi phục được, orderId không tồn tại trả false.
- ⚠️ Phạm vi còn lại: `AdminActivity` vẫn hiển thị đủ 4 lựa chọn và toast thành công bất kể service trả false — UI ngoài phạm vi được giao (Mục 6.1).

### U6. Sinh mã đơn duy nhất tăng dần, hết nguy cơ trùng/ghi đè đơn
**Khắc phục:** ĐH-4, KT-2, KT-6. **File:** `OrderServiceImpl.kt`, `OrderRepositoryImpl.kt`, `OrderServiceTest.kt`
- `createOrder` bỏ mã ngẫu nhiên #ORD-1000..9999, thay bằng `generateUniqueOrderCode()` (`OrderServiceImpl.kt:64-73` hiện hành): max số hiện có + 1 kèm vòng while kiểm tra `getOrderById` nên không bao giờ trùng.
- `OrderRepositoryImpl.saveOrder` đổi sang so sánh ignoreCase đồng bộ với `getOrderById` (`OrderRepositoryImpl.kt:19`) để mã khác case vẫn là cập nhật chứ không tạo bản trùng.
- Thêm 2 test: mã mới = #ORD-1026 khi seed sẵn 1023/1025 và 3 đơn liên tiếp không trùng/không mất đơn; saveOrder khác case cập nhật thay vì nhân bản.
- ✅ Đối chiếu mã nguồn: cả hai thay đổi xác nhận có mặt.

### U7. Xử lý biên Trợ lý AI: ngân sách không món hợp, VND 7 chữ số, chuỗi rỗng, ảnh không khớp
**Khắc phục:** AI-1, AI-2, AI-5, AI-6. **File:** `AIChatbotServiceImpl.kt`, `AIChatbotServiceTest.kt`
- (1) Nhánh ngân sách có else (`AIChatbotServiceImpl.kt:117-131` hiện hành): khi không món nào ≤ ngân sách trả lời thẳng "không có món dưới Xk" kèm món rẻ nhất, thay vì rơi về gợi ý mặc định đắt hơn ngân sách.
- (2) `parseBudget` mới (dòng 186-195): hậu tố k/nghìn/nghin nhân 1000 có negative lookahead `(?!\w)` để chữ "k" đầu của "không" không bị nuốt; số thường chỉ nhận khi đủ 4 chữ số trở lên — "1000000" giờ là 1.000.000đ thay vì 1.000đ.
- (3) `sendMessage` với chuỗi rỗng/toàn khoảng trắng (dòng 26-37) không thêm bubble người dùng rỗng vào history, chỉ trả lời nhắc nhập nội dung.
- (4) `analyzeFoodImage` khi không khớp từ khóa nói thật "chưa nhận diện được" (dòng 74-78), khi thực đơn trống báo rõ (dòng 68-70).
- Thêm 4 test mới; 4 test cũ giữ nguyên và vẫn khớp hành vi mới. ✅ Đối chiếu mã nguồn: cả 4 nhánh xác nhận có mặt đúng vị trí.

### U8. Chặn điểm gợi ý AI vượt thang 1.0 và lý do rỗng ở getSimilarDishes
**Khắc phục:** AI-8 (một phần). **File:** `AIRecommendationServiceImpl.kt`, `AIRecommendationServiceTest.kt`
- Điểm cá nhân hóa bị `coerceAtMost(1.0)` trước khi làm tròn (tổng trọng số thô tối đa 0.35+0.45+0.20+0.10 = 1.10 nên trước đây có thể vượt thang) — hiện hành qua `MAX_SCORE = 1.0` ở `AIRecommendationServiceImpl.kt:62-63, 89-90, 110-113`.
- `getSimilarDishes` có reason fallback "Món được khách yêu thích tại nhà hàng" khi món không cùng nhóm và không chung vị (trước đây `joinToString` của list rỗng trả chuỗi rỗng).
- Thêm 2 test mới (test dựng đúng ca 1.092 → chặn còn 1.0); 3 test cũ không đổi và vẫn pass.

### U9. Bổ sung SecurityHelperTest — lớp test đầu tiên cho helper băm mật khẩu
**Hỗ trợ:** BC-4 (làm nền cho nâng PBKDF2). **File:** `SecurityHelperTest.kt` (mới)
- 4 test (đếm từ kết quả chạy thực tế): round-trip verify đúng (4 mật khẩu gồm cả tiếng Việt UTF-8, theo `SecurityHelper.kt:6-14`), sai/rỗng mật khẩu bị từ chối, hash không bao giờ trả plaintext và không rỗng, hai mật khẩu khác nhau ra hash khác nhau.
- Cố tình **không** khóa chi tiết triển khai (độ dài hex, tính tất định) để không cản việc nâng cấp sang PBKDF2 có salt — mọi assertion đều đúng với cả SHA-256 lẫn PBKDF2 dạng `salt:hash`.

### U10. Bổ sung InitialDataFactoryTest — khóa bất biến toàn bộ seed data
**File:** `InitialDataFactoryTest.kt` (mới). 6 test theo phương pháp property-based:
- id món duy nhất & dương, giá dương, tên/mô tả không rỗng; đủ món cho cả 3 FoodCategory; SĐT/email khách và username admin duy nhất (email/username theo lowercase); mật khẩu seed luôn là hash chứ không plaintext (chặn "123456"/"admin123"/"manager123"); mã đơn khớp Regex `#ORD-\d+`, không trùng, tableNumber/món không rỗng và mọi OrderItem trỏ tới món có trong thực đơn (phát hiện hồi quy nếu findItem fallback lặng lẽ tại `InitialDataFactory.kt:204`); totalAmount bằng tổng subtotal.
- Test viết theo thuộc tính (không khóa số lượng/mã cụ thể) nên không vỡ khi seed được bổ sung.

### U11. Bổ sung MenuRepositoryImplTest — khóa chuẩn hóa dấu của tìm kiếm thực đơn
**Liên quan:** AI-3 (khóa hợp đồng chuẩn hóa đã có). **File:** `MenuRepositoryImplTest.kt` (mới)
- **10 test** dùng fixture tường minh (không dựa default seed) — đếm trực tiếp từ file (10 hàm `@Test`) và kết quả chạy; *lưu ý: mô tả thay đổi ghi "11 test", đối chiếu thực tế là 10*:
  - search không dấu khớp tên có dấu ("ga nuong" → Gà Nướng), đ→d ("banh duc" → Bánh Đúc), query có dấu vẫn khớp, khớp theo mô tả ("mat lanh") và displayName danh mục ("nuoc uong"), query blank trả tất cả / không khớp trả rỗng, lọc danh mục, `getMenuItemById` null với id lạ, `saveMenuItem` cấp id tự tăng max+1 khi id ≤ 0 và cập nhật tại chỗ khi id đã có, `deleteMenuItem` true-then-false.
- Logic chuẩn hóa đã được kiểm chứng bằng Node mô phỏng đúng `removeDiacritics` (NFD + strip combining marks + đ→d tương đương `MenuRepositoryImpl.kt:58-62`): cả 7 kỳ vọng tìm kiếm đều khớp.

### U12. Bổ sung UserRepositoryImplTest — khóa tra cứu ignoreCase/trim và cấp id
**Liên quan:** BC-5 (khóa hợp đồng chuẩn hóa input). **File:** `UserRepositoryImplTest.kt` (mới). 7 test:
- `getCustomerByEmail`/`getAdminByUsername` bỏ hoa-thường và trim (`UserRepositoryImpl.kt:26, 47`), `getCustomerByPhone` trim (dòng 22), tra cứu lạ trả null, `saveCustomer` cấp id = max+1 (fixture id 1,3 → id mới 4, chứng minh theo max chứ không theo số phần tử), save id đã có cập nhật tại chỗ không nhân bản, `getAllAdmins` phản chiếu seed. Fixture email "A@Gmail.com" khớp query " a@GMAIL.COM ".

---

## 5. Trạng thái kiểm thử

### 5.1 Đầu vào (baseline, trước nâng cấp)

Pha phân tích chạy `./gradlew testDebugUnitTest --console=plain` → **BUILD SUCCESSFUL**, báo cáo XML tổng hợp **28 test / 0 failure / 0 skip** trên 8 suite (1+4+3+6+5+2+5+2 — bằng chứng phát hiện KT-1). Các lần chạy theo lớp (AuthenticationServiceTest 6/6; AIChatbotServiceTest + AIRecommendationServiceTest) cũng BUILD SUCCESSFUL.

### 5.2 Sau nâng cấp — tôi chạy lại để xác minh (25/10/2026)

| # | Lệnh đã chạy | Kết quả |
|---|--------------|---------|
| 1 | `./gradlew testDebugUnitTest assembleDebug --console=plain` | **BUILD SUCCESSFUL** in 838ms (43/43 task up-to-date — xác nhận trạng thái build xanh hiện hành) |
| 2 | `./gradlew testDebugUnitTest --rerun assembleDebug --rerun --console=plain` | **BUILD SUCCESSFUL**; `testDebugUnitTest` **thực thi lại thật** (1 executed): 12 suite, **67 test, 0 failure, 0 error, 0 skip** (XML timestamp 2026-10-25T03:01:32Z) |
| 3 | `rm app/build/outputs/apk/debug/app-debug.apk && ./gradlew assembleDebug --console=plain` | **BUILD SUCCESSFUL**; `packageDebug` + `assembleDebug` **thực thi thật** (1 executed), APK mới sinh `app/build/outputs/apk/debug/app-debug.apk` (6,43 MB, 10:04 ngày 25/10) |

Kết quả chi tiết theo lớp (đọc trực tiếp từ XML `app/build/test-results/testDebugUnitTest/` của lần chạy #2):

| Lớp test | Số test | Lỗi | Bỏ qua | Ghi chú |
|---|---|---|---|---|
| AIChatbotServiceTest | 8 | 0 | 0 | 4 cũ + 4 mới (U7) |
| AIRecommendationServiceTest | 5 | 0 | 0 | 3 cũ + 2 mới (U8) |
| AuthenticationServiceTest | 6 | 0 | 0 | |
| CartServiceTest | 5 | 0 | 0 | |
| DashboardServiceTest | 2 | 0 | 0 | |
| MenuServiceTest | 5 | 0 | 0 | |
| OrderServiceTest | 8 | 0 | 0 | 2 cũ + 6 mới (U5, U6) |
| MenuRepositoryImplTest | 10 | 0 | 0 | mới (U11) — mô tả thay đổi ghi 11, thực tế 10 |
| UserRepositoryImplTest | 7 | 0 | 0 | mới (U12) |
| InitialDataFactoryTest | 6 | 0 | 0 | mới (U10) |
| SecurityHelperTest | 4 | 0 | 0 | mới (U9) |
| ExampleUnitTest | 1 | 0 | 0 | lớp template mẫu của dự án |
| **Tổng** | **67** | **0** | **0** | baseline 28 + **39 test mới** |

### 5.3 Kết luận và ghi chú

- **Unit test: ĐẠT** — 67/67 test pass (tăng từ 28 lên 67, thêm 39 test và 4 lớp test mới).
- **Build: ĐẠT** — `assembleDebug` build thành công APK debug (chạy thực thi lại từ đầu artifact).
- Cổng kiểm thử độc lập của quy trình xác nhận: `gateGreen=true` (test + build sau nâng cấp) và `baselineGreen=true` (test trước nâng cấp) — theo dữ liệu đầu vào của báo cáo này.
- Các kỹ sư sửa U5–U8 desk-check logic theo chỉ dẫn (không tự chạy gradle), việc chạy kiểm thử do cổng kiểm thử thực hiện; các số liệu ở Mục 5.2 là kết quả **tôi chạy trực tiếp** trong phiên lập báo cáo.
- **Chưa chạy:** `connectedAndroidTest` (cần emulator/thiết bị) và `lint` — ngoài phạm vi đợt này.

---

## 6. Việc chưa làm được và đề xuất tiếp theo

### 6.1 Việc chưa làm được (kèm lý do)

1. **Render ảnh QR trong app** (phần còn lại của ĐH-1): AndroidManifest chưa khai báo quyền INTERNET và version catalog (`gradle/libs.versions.toml`) chưa có thư viện tải ảnh — cả hai file ngoài phạm vi được giao của lượt sửa. Giải pháp tạm thời đã làm: hiển thị URL dạng text + nút mở trình duyệt (U4).
2. **UI dialog trạng thái ở AdminActivity** (phần còn lại của ĐH-3): lượt sửa chỉ được giao tầng service (U5); dialog vẫn hiện đủ 4 lựa chọn và toast "thành công" bất kể service trả `false`.
3. **Đơn PENDING sót lại** (hành vi mới phát sinh từ U4): khách bấm "Tạo Đơn & Hiện Mã QR" rồi đóng dialog không xác nhận sẽ để lại đơn PENDING; lần mở sau có thể tạo đơn khác.
4. **13 phát hiện chưa được sửa:** BC-2, BC-3, BC-4, BC-5, BC-6, ĐH-5, ĐH-6, AI-3, AI-7, KT-3, KT-4, KT-5, KT-7. **Phần dở của 5 phát hiện sửa một phần:** wiring UI getSimilarDishes (AI-8), dialog admin (ĐH-3), render ảnh QR (ĐH-1), các test biên còn thiếu của AI-4/KT-1 (chi tiết ở từng phát hiện Mục 3).
5. **Chưa commit:** toàn bộ 12 nâng cấp đang nằm trong working tree nhánh `phong` (11 file sửa + 4 file test mới) — nên xem lại và commit để không mất.

### 6.2 Đề xuất tiếp theo (theo thứ tự ưu tiên)

1. **UI chuyển trạng thái đơn (ĐH-3 phần UI + ĐH-6):** ẩn các lựa chọn không hợp lệ theo trạng thái hiện tại (lấy từ `isValidStatusTransition`), hiển thị lỗi khi service trả false; thay map index→enum thủ công bằng `OrderStatus.entries`.
2. **Đăng xuất + "Đơn của tôi" (BC-3 + ĐH-5):** dialog tài khoản có nút "Đăng xuất" gọi `authService.logout()`; bottom sheet "Đơn của tôi" dùng `getCustomerOrders` hiển thị mã đơn/tổng tiền/chip trạng thái, kèm đường hủy đơn phía khách (đi qua `cancelOrder` để hưởng guard đã có).
3. **Nâng PBKDF2 có salt (BC-4):** `SecurityHelperTest` (U9) đã được thiết kế không khóa chi tiết triển khai nên nâng cấp không vỡ test — đổi một chỗ, nhất quán trong một lần chạy.
4. **Chuỗi đăng nhập/đăng ký (BC-2, BC-5, BC-6):** chặn đăng ký khi để trống SĐT/email; thống nhất thông báo lỗi đăng nhập; bỏ trim mật khẩu (hoặc báo lỗi rõ ràng).
5. **Chatbot không dấu + làm mới chat (AI-3, AI-7):** nâng `removeDiacritics` thành util dùng chung, normalize query + tag + keyword; thêm nút "Làm mới" gọi `clearChat()`.
6. **Wiring getSimilarDishes (AI-8):** gắn vào click món (dialog "Món tương tự").
7. **Dọn tầng nghiệp vụ (KT-4, KT-5, KT-7):** đẩy filter `isAvailable` vào `MenuServiceImpl.searchMenuItems`; quyết định đơn hủy có tính vào "bán chạy" không rồi sửa service + test tương ứng; bỏ default args repository hoặc ghi KDoc.
8. **Bổ sung test còn thiếu (KT-1, AI-4, KT-3):** cancelOrder PENDING→true + orderId lạ, createOrder giỏ rỗng/bàn trống ném IllegalArgumentException, updateMenuItem đổi giá + giá ≤0 ném lỗi, addItem quantity 0/món hết hàng, loginCustomer bằng email, analyzeFoodImage hint chứa "tôm" có dấu, nhánh query không khớp → default, clearChat còn đúng 1 tin welcome.
9. **Render QR thật (ĐH-1 trọn vẹn):** thêm quyền INTERNET + thư viện ảnh (Coil) qua version catalog, rồi nạp `order.qrPayloadUrl` vào `imgQrCode`; cân nhắc dọn đơn PENDING bỏ hoang khi mở lại dialog thanh toán.

---

*Tổng hợp từ dữ liệu rà soát của quy trình cải tiến; mọi số liệu kiểm thử trong Mục 5.2 do người lập báo cáo chạy trực tiếp trên máy (Windows, Gradle 9.5.0) ngày 25/10/2026.*
