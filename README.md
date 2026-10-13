# vodaiminhtri - Chiều Thứ 3 Kết Thúc Môn Học

Ứng dụng Android quản lý và đặt món ăn (Food Ordering & Admin Management).

## 🚀 Tính năng chính
- **Danh sách món ăn:** Xem thực đơn theo danh mục (Món chính, Món phụ, Đồ uống), tìm kiếm món ăn nhanh chóng.
- **Giỏ hàng & Thanh toán:** Thêm/sửa số lượng món trong giỏ hàng, hỗ trợ thanh toán qua mã QR.
- **AI Chatbot hỗ trợ:** Tư vấn thực đơn, gợi ý món ăn thông minh.
- **Quản lý Admin:** Đăng nhập quản trị viên, thêm/sửa/xóa món ăn, quản lý danh sách đơn hàng.

## 🛠️ Công nghệ sử dụng
- **Ngôn ngữ:** Kotlin
- **Build tool:** Gradle (Kotlin DSL)
- **UI & Architecture:** Android ViewBinding, RecyclerView, Material Design Components

## 📂 Cấu trúc mã nguồn
```
├── app/
│   ├── src/main/java/com/example/chieuthu3_ketthucmonhoc/
│   │   ├── adapters/     # Adapter cho RecyclerView (Food, Cart, Chat, Admin)
│   │   ├── models/       # Data classes (FoodItem, CartItem, Order, ChatMessage)
│   │   ├── MainActivity.kt
│   │   └── AdminActivity.kt
│   └── src/main/res/     # Layout XML, Drawables, Themes, Values
├── gradle/               # Gradle wrapper
├── build.gradle.kts      # Project build configuration
└── settings.gradle.kts   # Project settings
```
