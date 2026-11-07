# BỘ ẢNH CHỤP MÀN HÌNH GIAO DIỆN ỨNG DỤNG NHÀ HÀNG (PHỤC VỤ BÁO CÁO TIỂU LUẬN)

Thư mục này chứa đầy đủ toàn bộ ảnh chụp màn hình (screenshots) thực tế từ máy ảo Android (Pixel 8 / độ phân giải 1080x2400) cho từng chức năng, phân hệ và luồng người dùng (Actor: Khách Hàng, Quản Trị Viên, Hệ Thống AI).

---

## 1. Màn Hình Khởi Động & Giới Thiệu (Intro / Splash)
- **File**: `01_intro_screen.png`
- **Mô tả**: Màn hình giới thiệu nhận diện thương hiệu nhà hàng ẩm thực Việt, hiển thị Logo, slogan giới thiệu, hiệu ứng animation và nút *"Bắt Đầu Trải Nghiệm"*.

---

## 2. Phân Hệ Khách Hàng (Customer Module - `MainActivity`)
- **File**: `02_main_home.png`
  - **Mô tả**: Màn hình chính thực đơn gồm Header hiển thị bàn & tài khoản, Banner *"Gợi ý AI Dành Cho Bạn"*, thanh tìm kiếm thông minh và danh mục món ăn dạng lưới (Grid).
- **File**: `03_loc_sap_xep_thuc_don.png`
  - **Mô tả**: Bộ lọc danh mục (Món chính, Món phụ, Nước uống) kết hợp thanh Chip sắp xếp đa tiêu chí (*Mặc định, Bán chạy nhất, Đánh giá cao, Giá thấp → cao, Giá cao → thấp*).
- **File**: `04_chi_tiet_mon_an_danh_gia.png`
  - **Mô tả**: Hộp thoại Chi tiết món ăn với ảnh chất lượng cao, nhãn hương vị đặc trưng, tính năng **Đánh giá chất lượng món 1–5 sao (⭐)** và danh sách **Món tương đồng do AI gợi ý**.
- **File**: `05_tro_ly_ai_chatbot.png`
  - **Mô tả**: Trợ lý ẩm thực AI Chatbot tích hợp Machine Learning (Multinomial Naive Bayes + TF-IDF) hỗ trợ gợi ý món cay dưới 100k, tư vấn khẩu vị và nhận diện ảnh thực đơn.
- **File**: `06_gio_hang_va_ai_upsell.png`
  - **Mô tả**: BottomSheet Giỏ hàng với chi tiết đơn giá × số lượng, tính năng **Ghi chú bếp từng món** (Ít cay, không hành...), nút xóa nhanh 1 chạm và **Khu vực AI Gợi Ý Gọi Kèm Cho Trọn Vị (AI Cross-Sell)**.

---

## 3. Cổng Thanh Toán & Hóa Đơn Điện Tử
- **File**: `07_thanh_toan_vietqr.png`
  - **Mô tả**: Phương thức Chuyển khoản QR với mã VietQR Napas247 tạo theo chuẩn ISO/IEC 18004 và checksum CRC-16, hiển thị STK ngân hàng và nội dung chuyển khoản.
- **File**: `08_thanh_toan_vi_dien_tu.png`
  - **Mô tả**: Phương thức Ví điện tử MoMo / ZaloPay với nút sao chép 1 chạm cho Số tài khoản và Nội dung chuyển tiền.
- **File**: `09_thanh_toan_tien_mat.png`
  - **Mô tả**: Phương thức Tiền mặt tại bàn kèm các phím tắt chọn mệnh giá (200k, 500k, 1000k) và công cụ tính toán tiền thừa trả khách thời gian thực.
- **File**: `10_hoa_don_thanh_toan.png`
  - **Mô tả**: Hóa đơn điện tử itemized chi tiết xác nhận đặt món và thanh toán thành công, có nút chuyển tiếp theo dõi tiến độ đơn hàng.

---

## 4. Hồ Sơ Khách Hàng & Quản Trị Đơn Hàng Của Khách
- **File**: `11_dang_nhap_khach_hang.png`
  - **Mô tả**: Hộp thoại Đăng nhập và Đăng ký tài khoản khách hàng bằng Số điện thoại và Mật khẩu.
- **File**: `11_ho_so_khach_hang_lich_su_don.png`
  - **Mô tả**: Hồ sơ khách hàng cho phép cập nhật thông tin cá nhân; lịch sử đơn hàng với **Thanh tiến trình 3 bước trực quan (`Chờ xác nhận ──▶ Bếp đang nấu ──▶ Hoàn thành`)** và nút **`🔄 Đặt Lại Đơn Này` (1-tap Re-order)**.

---

## 5. Phân Hệ Quản Trị Viên (Admin Module - `AdminActivity`)
- **File**: `12_dang_nhap_admin.png`
  - **Mô tả**: Cổng đăng nhập dành riêng cho Quản trị viên nhà hàng với tài khoản mẫu SQL (`admin / admin123`).
- **File**: `13_admin_tong_quan_dashboard.png`
  - **Mô tả**: Bảng điều khiển Thống kê & Tổng quan Doanh thu (Tổng đơn hàng, Đơn đang xử lý, Tổng sản phẩm, Tổng doanh thu, Tỷ lệ hoàn thành đơn và Top 5 món bán chạy nhất).
- **File**: `14_admin_quan_ly_thuc_don.png`
  - **Mô tả**: Tab Quản lý thực đơn món ăn (danh sách món, giá bán, công tắc bật/tắt còn hàng - hết hàng, nút sửa và xóa món).
- **File**: `15_admin_them_sua_mon_an.png`
  - **Mô tả**: Hộp thoại Thêm/Sửa món ăn với lựa chọn danh mục và **Bộ Chip chọn thuộc tính hương vị (Taste Attributes: Đậm đà, Giòn, Cay, Ngọt, Chua, Thanh mát, Nướng, Chiên)** phục vụ mô hình máy học AI.
- **File**: `16_admin_quan_ly_don_hang.png`
  - **Mô tả**: Tab Quản lý đơn hàng thời gian thực với bộ lọc trạng thái bếp (Tất cả, Chờ xác nhận, Đang chế biến, Đã hoàn thành) và nút cập nhật trạng thái đơn.
- **File**: `17_admin_chi_tiet_don_hang.png`
  - **Mô tả**: Hộp thoại Chi tiết đơn hàng Admin hiển thị hóa đơn thu ngân chi tiết và chức năng in hóa đơn.
- **File**: `18_admin_quan_ly_khach_hang.png`
  - **Mô tả**: Tab Quản lý khách hàng hiển thị danh sách toàn bộ khách hàng đăng ký, số điện thoại, email và ngày tham gia.


---

## 6. Bộ Tính Năng Mở Rộng Chuyên Sâu (Smart Restaurant Operations)
- **File**: `20_main_quick_smart_utility_bar.png`
  - **Mô tả**: Thanh tiện ích thông minh nhanh trên màn hình chính (`🪑 Sơ đồ bàn`, `🍱 Combo AI`, `🎟️ Voucher`, `🛎️ Gọi phục vụ`) và Chip lọc `❤️ Yêu Thích`.
- **File**: `21_dialog_table_map_reservation.png`
  - **Mô tả**: Hộp thoại Sơ đồ bàn trực quan theo khu vực (Khu A Sân Vườn, Khu B Máy Lạnh, Khu C Phòng VIP) và Đặt bàn trước (Table Reservation).
- **File**: `22_dialog_ai_smart_combo_builder.png`
  - **Mô tả**: Trình Thiết Kế Mâm Tiệc / Combo AI Thông Minh theo số người, ngân sách và phong cách ẩm thực, tự động tính tổng kcal và ưu đãi combo.
- **File**: `23_dialog_voucher_wallet.png`
  - **Mô tả**: Ví Mã Giảm Giá / Voucher Khuyến Mãi (`CHIEUTHU3`, `TANBINH30`, `COMBOAI20`, `FREEDRINK`) áp dụng trực tiếp vào giỏ hàng và thanh toán.
- **File**: `24_dialog_smart_waiter_call.png`
  - **Mô tả**: Chuông Gọi Nhân Viên Phục Vụ Tại Bàn (Smart Waiter Call: Thêm đá/trà đá, thêm bát đũa, khăn giấy, yêu cầu thanh toán tại bàn) đồng bộ trực tiếp lên Dashboard Quản trị viên.
