# Hệ Thống Quản Lý Đặt Món Nhà Hàng Ẩm Thực Việt (Vietnamese Restaurant Ordering & Admin System)

> **Dự án ứng dụng Android thương mại chuyên nghiệp** phục vụ quy trình gọi món tại bàn, thanh toán VietQR Napas247, trợ lý thông minh tư vấn thực đơn và cổng quản trị vận hành nhà hàng toàn diện.

---

## 🌟 Tổng Quan Hệ Thống

Ứng dụng được thiết kế và hiện thực hóa dựa trên bài toán vận hành thực tế tại các nhà hàng ẩm thực Việt Nam quy mô vừa và lớn. Hệ thống cung cấp trải nghiệm số hóa trọn vẹn từ lúc thực khách bước vào bàn, quét thực đơn, nhận tư vấn khẩu vị thông minh, đặt món, thanh toán không tiền mặt qua mã VietQR chuẩn ngân hàng đến quản lý chế biến tại bếp và báo cáo doanh thu quản trị.

Toàn bộ ứng dụng được xây dựng **100% bằng Java thuần (Pure Java 11)** theo kiến trúc hướng đối tượng chuẩn mực (**Layered Architecture**, **SOLID principles**, **Interface-driven Design**), loại bỏ hoàn toàn các cấu hình phụ thuộc không cần thiết để tối ưu hóa hiệu năng và độ ổn định.

---

## 🚀 Các Tính Năng Nghiệp Vụ Chính

### 1. Phục Vụ Khách Hàng & Đặt Món Tại Bàn
- **Thực đơn phong phú phân loại khoa học:**
  - Danh mục trực quan: Món chính (*Main Dish*), Món phụ / Ăn kèm (*Side Dish*), Nước uống (*Beverage*).
  - Thuộc tính vị giác chi tiết (*Taste Attributes*): Chua, Cay, Mặn, Ngọt, Giòn, Chiên, Nướng, Thanh mát giúp thực khách dễ dàng lựa chọn theo gu ẩm thực.
  - Xếp hạng sao đánh giá thực khách (*Rating*) và số lượt đặt thực tế (*Order Count*).
- **Bộ công cụ tìm kiếm thông minh:** Hỗ trợ tìm kiếm theo tên món, mô tả món, tự động xử lý tiếng Việt có dấu/không dấu chuẩn Telex (`VietnameseTelexHelper`).
- **Gợi ý Combo Thông Minh (*Smart Combo*):** Tự động phát hiện các món ăn kết hợp hoàn hảo (ví dụ: món nướng đậm vị kèm nước có ga thanh mát, món chiên giòn kèm gỏi chua ngọt chống ngấy).
- **Sơ đồ bàn ăn linh hoạt:** Quản lý và chọn bàn từ Bàn 01 đến Bàn 12, hiển thị trạng thái bàn trống/đang phục vụ và yêu cầu hỗ trợ (gọi phục vụ, xin hóa đơn).

### 2. Giỏ Hàng & Quy Trình Thanh Toán VietQR Napas247
- **Giỏ hàng Session-scoped:** Lưu trữ trong bộ nhớ theo phiên làm việc, hỗ trợ tăng giảm số lượng, thêm ghi chú chế biến (ít cay, không hành, v.v.), tự động tính tổng tiền tức thì.
- **Thanh toán VietQR chuẩn Napas247:**
  - Tích hợp sinh mã QR thanh toán động VietinBank/Napas247 theo chuẩn ngân hàng quốc gia (`https://api.vietqr.io/image/...`).
  - Hỗ trợ công cụ sinh ảnh Bitmap QR trực tiếp tại thiết bị (`QrBitmapGenerator`) đảm bảo luôn hiển thị ngay cả khi mạng chập chờn.
  - Tiện ích một chạm sao chép số tài khoản, số tiền và nội dung chuyển khoản (Order ID).
  - Xác nhận thanh toán và tự động đồng bộ trạng thái đơn hàng vào cơ sở dữ liệu Room.

### 3. Trợ Lý Chatbot & Tư Vấn Ẩm Thực
- **Smart Recommendation Engine:** Phân tích khẩu vị ưa thích của khách hàng để gợi ý món ăn phù hợp với độ tương đồng khẩu vị cao nhất.
- **Chatbot Tư vấn 24/7:** Mô hình phân loại ý định (Intent Classifier) xử lý ngôn ngữ tự nhiên tiếng Việt, hỗ trợ giải đáp thực đơn, tư vấn món ăn thanh đạm/cay nồng/đồ uống giải nhiệt, và gửi phản hồi kèm danh thiếp món ăn tương tác trực tiếp.

### 4. Chương Trình Khách Hàng Thân Thiết (Loyalty & VIP)
- Hệ thống tích điểm chi tiêu: Tự động cộng điểm thưởng trên từng đơn hàng hoàn tất.
- Cấp bậc hội viên: Đồng, Bạc, Vàng, Kim Cương, Khách VIP với các đặc quyền ưu đãi riêng biệt.
- Áp dụng Voucher khuyến mãi (*Promo Voucher*): Giảm giá trực tiếp theo phần trăm hoặc số tiền cố định.

### 5. Cổng Quản Trị Nhà Hàng Chuyên Nghiệp (Admin Management Portal)
- **Bảo mật & Phân quyền đăng nhập:**
  - Cơ chế kiểm soát phiên truy cập nghiêm ngặt (`authService.getCurrentAdmin()`).
  - Phân quyền vai trò: `SUPER_ADMIN` (Tổng quản lý), `MANAGER` (Quản lý chi nhánh), `STAFF` (Nhân viên thu ngân / phục vụ).
  - Toàn bộ mật khẩu lưu trữ trong cơ sở dữ liệu được mã hóa băm một chiều an toàn bằng thuật toán **SHA-256** (`SecurityHelper`).
- **Quản lý Thực đơn (Menu Management):**
  - Thêm món mới, cập nhật giá, chỉnh sửa danh mục và thuộc tính vị giác.
  - Cơ chế xóa an toàn (**Soft Delete**): Nếu món ăn đã có trong lịch sử hóa đơn, hệ thống tự động chuyển trạng thái `isAvailable = false` thay vì xóa cứng, bảo toàn 100% tính toàn vẹn dữ liệu kế toán và báo cáo doanh thu.
- **Quản lý Đơn hàng & Điều phối Bếp (Kitchen & Order Tracking):**
  - Giám sát trạng thái đơn hàng thời gian thực: `PENDING` (Chờ tiếp nhận) -> `PREPARING` (Đang chế biến) -> `READY_FOR_PICKUP` (Đã sẵn sàng lên món) -> `COMPLETED` (Hoàn thành) / `CANCELLED` (Hủy bỏ).
  - Xem chi tiết từng món ăn, đơn giá, số lượng, thời gian đặt và thông tin bàn.
- **Báo cáo Thống Kê & Doanh Thu (Dashboard Analytics):**
  - Tổng doanh thu tích lũy và doanh thu trong ngày.
  - Tổng số đơn hàng thành công.
  - Danh sách các món ăn bán chạy nhất (*Top Selling Items*) kèm số lượng và tỷ trọng doanh thu.
- **Quản lý Khách hàng:** Xem danh sách khách hàng, số điện thoại liên hệ, tổng chi tiêu và cấp độ thân thiết.

---

## 🏗️ Kiến Trúc Hệ Thống (Architecture & Design)

Hệ thống tuân thủ mô hình kiến trúc phân tầng chuẩn mực (**Layered Architecture**), phân tách rõ ràng trách nhiệm giữa các tầng:

```
app/src/main/java/com/example/chieuthu3_ketthucmonhoc/
├── App.java                              # Điểm khởi chạy: khởi tạo AppServiceLocator
├── IntroActivity.java                    # Màn hình Splash / Giới thiệu ứng dụng
├── MainActivity.java                     # Màn hình chính phục vụ thực khách (Menu, Cart, QR, Tư vấn)
├── AdminActivity.java                    # Màn hình Cổng quản trị nhà hàng (Admin Portal)
│
├── domain/                               # Lớp Nghiệp Vụ Cốt Lõi (Domain Layer - Pure Java)
│   ├── models/                           # Thực thể & Đối tượng nghiệp vụ (MenuItem, Order, Customer,...)
│   │   ├── MenuItem.java                 # Thông tin món ăn, giá, phân loại, thuộc tính vị giác
│   │   ├── Order.java / OrderItem.java   # Hóa đơn đặt món và chi tiết dòng món ăn
│   │   ├── Customer.java                 # Thông tin khách hàng, hạng thẻ, tích điểm
│   │   ├── AdminAccount.java             # Tài khoản quản trị & vai trò
│   │   ├── CartItem.java                 # Dòng món ăn trong giỏ hàng phiên
│   │   ├── LoyaltyProfile.java           # Hồ sơ điểm thưởng & hạng thành viên
│   │   ├── RestaurantTable.java          # Thực thể bàn ăn & trạng thái phục vụ
│   │   ├── SmartComboProposal.java       # Đề xuất món ăn kèm thông minh
│   │   ├── TableServiceRequest.java      # Yêu cầu phục vụ tại bàn
│   │   ├── FoodImageResolver.java        # Bộ ánh xạ tài nguyên hình ảnh món ăn
│   │   └── enums/                        # FoodCategory, OrderStatus, TableStatus, TasteAttribute, UserRole
│   │
│   └── services/                         # Interface & Implementation các dịch vụ nghiệp vụ
│       ├── IMenuService.java             # Quản lý thực đơn, tìm kiếm, lọc danh mục
│       ├── ICartService.java             # Quản lý giỏ hàng phiên làm việc
│       ├── IOrderService.java            # Xử lý đặt hàng, chuyển trạng thái, tính tiền
│       ├── IAuthenticationService.java   # Xác thực tài khoản khách hàng và nhân viên
│       ├── IDashboardService.java        # Thống kê số liệu doanh thu, top món bán chạy
│       ├── IRecommendationService.java   # Thuật toán gợi ý món ăn theo khẩu vị
│       ├── IChatbotService.java          # Xử lý ngôn ngữ tự nhiên và tư vấn thực đơn
│       ├── IRestaurantOperationsService.java # Quản lý bàn ăn, yêu cầu dịch vụ
│       ├── SecurityHelper.java           # Mã hóa mật khẩu SHA-256
│       ├── VietnameseTelexHelper.java    # Chuẩn hóa tiếng Việt Telex & bỏ dấu
│       └── QrBitmapGenerator.java        # Bộ sinh ảnh mã QR nội bộ
│
├── data/                                 # Lớp Dữ Liệu & Lưu Trữ (Data Layer)
│   ├── repository/                       # Hợp đồng kho lưu trữ (Repository Interfaces)
│   │   ├── IMenuRepository.java
│   │   ├── IOrderRepository.java
│   │   ├── IUserRepository.java
│   │   ├── ICartRepository.java
│   │   └── *RepositoryImpl.java          # Bộ nhớ In-Memory phục vụ Unit Test độc lập
│   │
│   ├── database/                         # Lưu trữ SQLite qua Android Room
│   │   ├── AppDatabase.java              # Cấu hình Room Database (restaurant.db)
│   │   ├── DatabaseSeeder.java           # Cơ chế khởi tạo dữ liệu mẫu và nạp SQL
│   │   ├── RoomMenuRepository.java       # Hiện thực IMenuRepository trên Room SQLite
│   │   ├── RoomOrderRepository.java      # Hiện thực IOrderRepository trên Room SQLite
│   │   ├── RoomUserRepository.java       # Hiện thực IUserRepository trên Room SQLite
│   │   ├── dao/                          # Data Access Objects (MenuItemDao, OrderDao, UserDao)
│   │   └── entity/                       # Bảng cơ sở dữ liệu Room Entity
│   │
│   └── datasource/
│       └── InitialDataFactory.java       # Dữ liệu khởi tạo chuẩn thương mại
│
├── di/
│   └── AppServiceLocator.java            # Manual Dependency Injection (Singleton Service Locator)
│
└── adapters/                             # Bộ điều hợp RecyclerView hiển thị dữ liệu
    ├── FoodAdapter.java                  # Danh sách món ăn trang chủ
    ├── CartAdapter.java                  # Danh sách món trong giỏ hàng
    ├── RecommendationAdapter.java        # Thẻ món ăn gợi ý thông minh
    ├── ChatAdapter.java                  # Bong bóng hội thoại tin nhắn Chatbot
    ├── AdminFoodAdapter.java             # Quản lý danh sách món cho Admin
    ├── AdminOrderAdapter.java            # Danh sách đơn hàng cho Admin
    └── AdminCustomerAdapter.java         # Danh sách khách hàng thân thiết
```

### Các Nguyên Tắc Thiết Kế Trọng Tâm:
1. **Dependency Inversion Principle (DIP):** Activities và Adapters phụ thuộc hoàn toàn vào `I*Service` thông qua `AppServiceLocator`, không bao giờ khởi tạo trực tiếp lớp cụ thể.
2. **Interface Segregation:** Mỗi dịch vụ đảm nhiệm đúng phạm vi chức năng (Menu, Cart, Order, Auth, Operations, Dashboard).
3. **Data Integrity & Safety:** Ràng buộc toàn vẹn khóa ngoại (Foreign Keys) được kiểm tra chặt chẽ ở cấp độ SQLite database; cơ chế Soft Delete bảo vệ lịch sử hóa đơn.
4. **100% Pure Java:** Không sử dụng Kotlin trong source code Java, không dùng KSP dư thừa, biên dịch thuần Java 11 chuẩn xác.

---

## 💾 Thiết Kế Cơ Sở Dữ Liệu (SQLite Room Database)

Cơ sở dữ liệu được đặt tên `restaurant.db` với 5 bảng quan hệ được thiết kế chuẩn hóa:

| Tên Bảng | Vai Trò & Ràng Buộc |
|---|---|
| `admins` | Lưu tài khoản quản trị viên. Cột `username` có chỉ mục `UNIQUE`. Mật khẩu băm SHA-256. |
| `customers` | Lưu tài khoản khách hàng. Cột `phone` và `email` có chỉ mục `UNIQUE`. |
| `menu_items` | Lưu danh mục món ăn, đơn giá, đánh giá sao, lượt đặt, thuộc tính vị giác, cờ `isAvailable`. |
| `orders` | Lưu đơn hàng, bàn ăn, tổng tiền, phương thức thanh toán, mã đơn `#ORD-xxxx`, liên kết `customer_id` (`ON DELETE SET NULL`). |
| `order_items` | Lưu chi tiết dòng món trong đơn. Khóa chính phức hợp `(order_id, id)`. Khóa ngoại `order_id` (`CASCADE`), `menu_item_id` (`RESTRICT`). |

Tệp kịch bản SQL độc lập được duy trì tại:
- `restaurant.sql` (ở thư mục gốc)
- `app/src/main/assets/restaurant.sql` (trong tài nguyên ứng dụng)

---

## 🛠️ Công Nghệ & Thư Viện Sử Dụng

- **Ngôn ngữ:** Java 11 (Pure Java)
- **Hệ điều hành mục tiêu:** Android API 24 (Android 7.0) trở lên, `targetSdk = 35`, `compileSdk = 35`
- **Build System:** Gradle 9.5.0, Android Gradle Plugin (AGP) 9.3.3, Gradle Version Catalog (`gradle/libs.versions.toml`)
- **Giao diện người dùng (UI):** Android ViewBinding, Material Design 3 Components, Edge-to-Edge (`ViewCompat`, `WindowInsetsCompat`)
- **Cơ sở dữ liệu:** Android Room 2.8.5 (`room-runtime`, `room-compiler` annotation processor)
- **Kiểm thử tự động:** JUnit 4.13.2, AndroidX Test Runner, Espresso Core

---

## 🔑 Tài Khoản Quản Trị & Khách Hàng Thử Nghiệm

Hệ thống được khởi tạo sẵn các tài khoản mẫu chuẩn để kiểm thử toàn diện:

### 1. Tài Khoản Quản Trị Viên (Admin Portal)
| Vai trò | Tên đăng nhập (hoặc SĐT) | Mật khẩu mặc định | Quyền hạn |
|---|---|---|---|
| **Tổng Quản Trị (Super Admin)** | `admin` hoặc `0900000000` | `admin123` | Toàn quyền quản trị, thêm/sửa/xóa món, xem doanh thu, xem đơn |
| **Quản Lý Nhà Hàng (Manager)** | `manager` | `manager123` | Quản lý thực đơn, điều phối đơn hàng, xem báo cáo thống kê |

### 2. Tài Khoản Khách Hàng (Customer)
| Họ và tên | Số điện thoại | Mật khẩu | Hạng thành viên |
|---|---|---|---|
| Khách Hàng Thân Thiết | `0901234567` | `123456` | Khách VIP (Bàn 12 - Khu Vực A) |

---

## 💻 Hướng Dẫn Biên Dịch & Chạy Kiểm Thử (Build & Run)

Ứng dụng hỗ trợ biên dịch nhanh chóng thông qua Gradle Wrapper (sử dụng `./gradlew` trên Linux/macOS hoặc `.\gradlew.bat` trên Windows PowerShell / Git Bash):

### 1. Chạy Toàn Bộ Bộ Kiểm Thử Tự Động (Unit Tests)
```bash
./gradlew testDebugUnitTest
```
*Tất cả 12 test suites (CartServiceTest, OrderServiceTest, MenuServiceTest, AuthenticationServiceTest, DashboardServiceTest, RestaurantOperationsServiceTest, SecurityHelperTest, VietnameseTelexHelperTest, FoodImageResolverTest,...) đều chạy độc lập và vượt qua 100% trong ~5-8 giây.*

### 2. Kiểm Tra Riêng Từng Lớp Test
```bash
./gradlew testDebugUnitTest --tests "com.example.chieuthu3_ketthucmonhoc.services.CartServiceTest"
./gradlew testDebugUnitTest --tests "com.example.chieuthu3_ketthucmonhoc.services.OrderServiceTest"
```

### 3. Đóng Gói File Cài Đặt Debug APK
```bash
./gradlew assembleDebug
```
*Tệp APK thành phẩm sẽ được tạo tại thư mục:* `app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 Bản Quyền & Giấy Phép
Dự án được phát triển phục vụ mục đích học tập, nghiên cứu và ứng dụng quản lý nhà hàng thực tế. Toàn bộ mã nguồn tuân thủ tiêu chuẩn lập trình Java chuyên nghiệp và bảo mật thông tin.
