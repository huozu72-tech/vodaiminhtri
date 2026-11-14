-- ====================================================================
-- CSDL QUẢN LÝ ĐẶT MÓN NHÀ HÀNG (RESTAURANT ORDERING SYSTEM)
-- Database Engine: SQLite 3 / Android Room Compatible
-- File: restaurant.sql (Đồng bộ 100% với InitialDataFactory.java)
-- Chuẩn thương mại: Nhà Hàng Ẩm Thực Việt - 36 Món, 14 Khách VIP, 28 Đơn Hàng
-- Doanh thu tích lũy demo: 16,949,000 VNĐ
-- ====================================================================

PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `menu_items`;
DROP TABLE IF EXISTS `customers`;
DROP TABLE IF EXISTS `admins`;

CREATE TABLE IF NOT EXISTS `admins` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `username` TEXT NOT NULL,
    `passwordHash` TEXT NOT NULL,
    `fullName` TEXT NOT NULL,
    `role` TEXT NOT NULL,
    `createdAt` INTEGER NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS `index_admins_username` ON `admins` (`username`);

CREATE TABLE IF NOT EXISTS `customers` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `fullName` TEXT NOT NULL,
    `email` TEXT,
    `phone` TEXT NOT NULL,
    `passwordHash` TEXT NOT NULL,
    `address` TEXT,
    `createdAt` INTEGER NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS `index_customers_phone` ON `customers` (`phone`);
CREATE UNIQUE INDEX IF NOT EXISTS `index_customers_email` ON `customers` (`email`);

CREATE TABLE IF NOT EXISTS `menu_items` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `name` TEXT NOT NULL,
    `description` TEXT,
    `price` INTEGER NOT NULL,
    `category` TEXT NOT NULL,
    `tasteAttributes` TEXT,
    `isAvailable` INTEGER NOT NULL DEFAULT 1,
    `rating` REAL NOT NULL DEFAULT 5.0,
    `orderCount` INTEGER NOT NULL DEFAULT 0,
    `image_res_id` INTEGER NOT NULL DEFAULT 0,
    `image_path` TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS `orders` (
    `order_id` TEXT PRIMARY KEY NOT NULL,
    `tableNumber` TEXT,
    `customer_id` INTEGER,
    `totalAmount` INTEGER NOT NULL,
    `status` TEXT NOT NULL,
    `paymentMethod` TEXT,
    `qrPayloadUrl` TEXT,
    `created_at` INTEGER NOT NULL,
    FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS `index_orders_customer_id` ON `orders` (`customer_id`);

CREATE TABLE IF NOT EXISTS `order_items` (
    `id` INTEGER NOT NULL,
    `order_id` TEXT NOT NULL,
    `menu_item_id` INTEGER NOT NULL,
    `quantity` INTEGER NOT NULL,
    `unitPrice` INTEGER NOT NULL,
    PRIMARY KEY (`order_id`, `id`),
    FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE,
    FOREIGN KEY (`menu_item_id`) REFERENCES `menu_items` (`id`) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS `index_order_items_order_id` ON `order_items` (`order_id`);
CREATE INDEX IF NOT EXISTS `index_order_items_menu_item_id` ON `order_items` (`menu_item_id`);
CREATE UNIQUE INDEX IF NOT EXISTS `index_order_items_order_id_menu_item_id` ON `order_items` (`order_id`, `menu_item_id`);


-- 1. Dữ liệu Quản trị & Nhân sự (6 tài khoản phân quyền, mật khẩu băm SHA-256)
INSERT INTO `admins` (`id`, `username`, `passwordHash`, `fullName`, `role`, `createdAt`) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Võ Văn Hoàng (Tổng Quản Trị Hệ Thống)', 'SUPER_ADMIN', 1700000000000),
(2, '0900000000', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Quản Trị Hotline Tiếp Nhận (SĐT)', 'SUPER_ADMIN', 1700000000000),
(3, 'manager', '866485796cfa8d7c0cf7111640205b83076433547577511d81f8030ae99ecea5', 'Trần Minh Quân (Quản Lý Trưởng Nhà Hàng)', 'MANAGER', 1700000000000),
(4, 'beptruong', '866485796cfa8d7c0cf7111640205b83076433547577511d81f8030ae99ecea5', 'Nguyễn Hữu Tài (Bếp Trưởng Điều Phối)', 'MANAGER', 1700000000000),
(5, 'cashier', 'b4c94003c562bb0d89535eca77f07284fe560fd48a7cc1ed99f0a56263d616ba', 'Lê Thị Thảo (Thu Ngân Trưởng Ca Sáng)', 'STAFF', 1700000000000),
(6, 'thungan02', 'b4c94003c562bb0d89535eca77f07284fe560fd48a7cc1ed99f0a56263d616ba', 'Nguyễn Thúy Vy (Thu Ngân Ca Tối)', 'STAFF', 1700000000000);

-- 2. Dữ liệu Khách hàng thực tế (14 khách hàng phân khúc VIP/Gia đình/Doanh nghiệp)
INSERT INTO `customers` (`id`, `fullName`, `email`, `phone`, `passwordHash`, `address`, `createdAt`) VALUES
(1, 'Nguyễn Hoàng Nam (VIP Diamond)', 'khachhang@restaurant.com', '0901234567', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn VIP 01 - Tầng 2 (Khu VIP Diamond)', 1700000000000),
(2, 'Trần Anh Tuấn (VIP Platinum)', 'anhtuan.vip@gmail.com', '0912345678', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn 12 - Tầng 2 (Khu Máy Lạnh)', 1700000000000),
(3, 'Chị Lê Mai Phương (Khách Gia Đình)', 'maiphuong.family@gmail.com', '0987654321', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn 05 - Tầng 1 (Phòng Gia Đình Family-01)', 1700000000000),
(4, 'Anh Vũ Đình Trọng (Khách Gia Đình)', 'trongvu.family@gmail.com', '0934567890', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn 02 - Tầng 1 (Khu Trẻ Em & Gia Đình)', 1700000000000),
(5, 'Phạm Quốc Hưng (Tập Đoàn SunGroup)', 'quochung.sungroup@corp.vn', '0945678901', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'VIP 02 - Tầng 2 (Phòng Tiệc Doanh Nghiệp)', 1700000000000),
(6, 'Đặng Thùy Dung (FPT Software)', 'thuydung.fpt@corp.vn', '0967890123', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn 04 - Tầng 1 (Khu Hội Nghị Doanh Nghiệp)', 1700000000000),
(7, 'Hoàng Minh Trí (Viettel Telecom)', 'minhtri.viettel@corp.vn', '0978901234', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn 10 - Tầng 2 (Khu VIP)', 1700000000000),
(8, 'Vũ Bích Ngọc (VIP Gold)', 'bichngoc.gold@gmail.com', '0909876543', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn 07 - Tầng 1 (Khu Sân Vườn)', 1700000000000),
(9, 'Đỗ Thành Đạt (Sinh Viên Công Nghệ)', 'datdo.student@gmail.com', '0923456789', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn 03 - Tầng 1 (Khu Giới Trẻ)', 1700000000000),
(10, 'Nguyễn Thu Trang (Khách Vãng Lai Thân Thiết)', 'thutrang.travel@gmail.com', '0938112233', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn 09 - Tầng 1 (Khu Cửa Sổ)', 1700000000000),
(11, 'Bùi Đức Mạnh (VIP Silver)', 'ducmanh.silver@gmail.com', '0915667788', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn 14 - Tầng 2 (Khu Ban Công)', 1700000000000),
(12, 'Trịnh Hải Yến (Khách Tiệc Sinh Nhật)', 'haiyen.party@gmail.com', '0982334455', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn VIP 03 - Tầng 2 (Phòng Tiệc Sinh Nhật)', 1700000000000),
(13, 'Lâm Quốc Bảo (Khách Đặt Bàn Trưa)', 'quocbao.lunch@gmail.com', '0903445566', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn 06 - Tầng 1 (Khu Sân Vườn)', 1700000000000),
(14, 'Phan Tuyết Mai (Vingroup)', 'tuyetmai.vin@corp.vn', '0971223344', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bàn 15 - Tầng 2 (Khu Tiếp Khách)', 1700000000000);

-- 3. Thực đơn 36 món ẩm thực Việt (18 Món chính, 10 Món phụ, 8 Nước uống & Tráng miệng)
INSERT INTO `menu_items` (`id`, `name`, `description`, `price`, `category`, `tasteAttributes`, `isAvailable`, `rating`, `orderCount`, `image_res_id`) VALUES
(1, 'Chân Giò Muối Chiên Giòn', 'Chân Giò Muối Chiên Giòn da giòn rụm, thịt mềm thơm đậm đà hương vị truyền thống', 120000, 'MAIN_DISH', 'CRISPY,SAVORY,FRIED', 1, 4.9, 145, 0),
(2, 'Ếch Nướng Muối Ớt Lá Chanh', 'Ếch đồng tươi nướng muối ớt cay nồng thơm lừng hương lá chanh miền Tây', 85000, 'MAIN_DISH', 'SPICY,SAVORY,GRILLED', 1, 4.7, 98, 0),
(3, 'Gà Nướng Mật Ong Hoa Rừng', 'Gà ta ướp mật ong hoa rừng nướng than hồng vàng óng, thịt ngọt thơm đậm vị', 150000, 'MAIN_DISH', 'SWEET,SAVORY,GRILLED', 1, 4.9, 212, 0),
(4, 'Gà Quay Thảo Mộc Bánh Bao', 'Gà quay thảo mộc da giòn rụm ăn kèm bánh bao chiên vàng óng giải ngấy', 160000, 'MAIN_DISH', 'SAVORY,CRISPY', 1, 4.8, 175, 0),
(5, 'Tôm Sú Chiên Xù Giòn Rụm', 'Tôm sú tươi tẩm bột chiên xù vàng ruộm, chấm kèm sốt tương ớt mayonnaise', 95000, 'MAIN_DISH', 'CRISPY,SAVORY,FRIED', 1, 4.7, 136, 0),
(6, 'Tôm Càng Nướng Muối Ớt Sa Tế', 'Tôm càng xanh nướng muối ớt cay xè, giữ trọn vị ngọt tự nhiên của thịt tôm', 115000, 'MAIN_DISH', 'SPICY,SAVORY,GRILLED', 1, 4.8, 168, 0),
(7, 'Chân Gà Rút Xương Sả Tắc', 'Chân gà rút xương ngâm sả tắc chua cay thanh mát giòn sần sật khai vị', 60000, 'SIDE_DISH', 'SOUR,SPICY,REFRESHING,CRISPY', 1, 4.8, 240, 0),
(8, 'Chân Gà Sốt Thái Chua Cay', 'Chân gà ngập sốt Thái chua cay đậm vị thơm mùi xoài non và cóc bao tử', 65000, 'SIDE_DISH', 'SPICY,SOUR,SWEET', 1, 4.9, 285, 0),
(9, 'Gỏi Ngó Sen Tôm Thịt Thanh Đạm', 'Gỏi ngó sen tôm thịt thanh đạm giải ngấy, chua ngọt vừa miệng rắc đậu phộng', 55000, 'SIDE_DISH', 'SOUR,SWEET,REFRESHING,CRISPY', 1, 4.6, 120, 0),
(10, 'Gỏi Xoài Tôm Khô Cay Nồng', 'Xoài keo xanh băm sợi trộn tôm khô thơm bùi nước mắm ớt chua cay đậm đà', 70000, 'SIDE_DISH', 'SOUR,SPICY,CRISPY', 1, 4.7, 105, 0),
(11, 'Gỏi Xoài Tôm Thịt Đậu Phộng', 'Gỏi xoài tôm thịt tươi đầy đặn, rắc đậu phộng rang giòn thơm nức mũi', 80000, 'SIDE_DISH', 'SOUR,SPICY,SAVORY', 1, 4.8, 154, 0),
(12, '7Up Chanh Tươi Mát Lạnh', 'Nước giải khát 7Up có ga hương chanh thanh mát sảng khoái ướp lạnh', 15000, 'BEVERAGE', 'SWEET,REFRESHING', 1, 4.9, 380, 0),
(13, 'Coca Cola Ướp Lạnh', 'Nước ngọt có ga Coca Cola mát lạnh giải nhiệt tức thì trong bữa ăn', 15000, 'BEVERAGE', 'SWEET,REFRESHING', 1, 4.9, 450, 0),
(14, 'Pepsi Có Ga Sảng Khoái', 'Nước giải khát Pepsi mát lạnh sảng khoái kích thích vị giác ngon miệng', 15000, 'BEVERAGE', 'SWEET,REFRESHING', 1, 4.8, 310, 0),
(15, 'Sting Dâu Tăng Lực', 'Nước tăng lực Sting hương dâu tây thơm ngon tiếp thêm năng lượng', 15000, 'BEVERAGE', 'SWEET,REFRESHING', 1, 4.8, 290, 0),
(16, 'Lẩu Gà Lá Giang Chua Cay', 'Lẩu gà lá giang chua cay đậm đà, thịt gà ta dai ngon nước lẩu thanh tao', 180000, 'MAIN_DISH', 'SOUR,SAVORY,SPICY', 1, 4.9, 215, 0),
(17, 'Lẩu Hải Sản Chua Cay Thập Cẩm', 'Lẩu hải sản chua cay phong cách Thái Lan với tôm sú, mực trứng, nghêu biển', 240000, 'MAIN_DISH', 'SOUR,SPICY,SAVORY', 1, 4.9, 165, 0),
(18, 'Lẩu Riêu Cua Bắp Bò Sườn Sụn', 'Lẩu riêu cua đồng béo ngậy kèm bắp bò hoa và sườn non giòn sần sật', 260000, 'MAIN_DISH', 'SAVORY,SOUR,SWEET', 1, 4.9, 188, 0),
(19, 'Bò Lúc Lắc Khoai Tây Chiên', 'Thịt bò thăn cắt vuông xào lửa lớn mềm ngọt ăn kèm khoai tây chiên vàng', 135000, 'MAIN_DISH', 'SAVORY,CRISPY', 1, 4.8, 142, 0),
(20, 'Bò Né Hoa Thiên Lý Sốt Tiêu', 'Bò né chảo gang xèo xèo cùng hoa thiên lý thơm nồng sốt tiêu đen thượng hạng', 145000, 'MAIN_DISH', 'SAVORY,SPICY', 1, 4.7, 118, 0),
(21, 'Sườn Heo Nướng Sốt BBQ Mật Ong', 'Dẻ sườn heo nướng than hoa sốt BBQ mật ong óng ả mềm róc xương', 130000, 'MAIN_DISH', 'SWEET,SAVORY,GRILLED', 1, 4.8, 156, 0),
(22, 'Mực Trứng Nướng Muối Ớt', 'Mực trứng Phan Thiết nướng muối ớt cay nồng đậm đà béo ngậy trứng mực', 125000, 'MAIN_DISH', 'SPICY,SAVORY,GRILLED', 1, 4.8, 134, 0),
(23, 'Mực Chiên Giòn Sốt Mayonnaise', 'Khoanh mực tươi tẩm bột chiên xù vàng ruộm chấm sốt mayonnaise béo ngậy', 110000, 'MAIN_DISH', 'CRISPY,SAVORY,FRIED', 1, 4.7, 95, 0),
(24, 'Vịt Quay Bắc Kinh Bánh Kẹp', 'Vịt quay da giòn rụm bóng mượt ăn kèm bánh tráng kẹp dưa leo và đầu hành', 190000, 'MAIN_DISH', 'SAVORY,CRISPY', 1, 4.9, 112, 0),
(25, 'Cá Lóc Nướng Trui Rơm Vàng', 'Cá lóc đồng nướng trui rơm vàng thơm lừng cuốn bánh tráng rau rừng', 140000, 'MAIN_DISH', 'SAVORY,GRILLED', 1, 4.7, 86, 0),
(26, 'Giò Heo Hầm Nấm Đông Cô', 'Giò heo hầm mềm rục cùng nấm đông cô và táo đỏ bổ dưỡng ngọt nước', 165000, 'MAIN_DISH', 'SAVORY,SWEET', 1, 4.8, 74, 0),
(27, 'Cơm Chiên Hải Sản Hoàng Kim', 'Cơm chiên hạt tơi vàng óng ánh với tôm, mực, trứng muối đậm đà mỹ vị', 85000, 'MAIN_DISH', 'SAVORY,CRISPY', 1, 4.8, 195, 0),
(28, 'Chả Giò Hải Sản Bắp Ngọt', 'Chả giò cuốn tôm thịt và bắp ngọt giòn tan chấm sốt tương xí muội', 50000, 'SIDE_DISH', 'CRISPY,SAVORY,FRIED', 1, 4.8, 185, 0),
(29, 'Khoai Tây Chiên Lắc Phô Mai', 'Khoai tây chiên giòn rụm lắc bột phô mai béo ngọt thơm ngậy', 40000, 'SIDE_DISH', 'CRISPY,SWEET,SAVORY', 1, 4.7, 210, 0),
(30, 'Đậu Hũ Lướt Ván Giòn Tan', 'Đậu hũ non chiên lướt ván ngoài giòn trong mềm chấm mắm tôm hoặc xì dầu', 45000, 'SIDE_DISH', 'CRISPY,SAVORY', 1, 4.6, 130, 0),
(31, 'Salad Ức Gà Dầu Giấm', 'Xà lách tươi sạch trộn ức gà xé sốt dầu giấm thanh mát giải ngấy hữu hiệu', 55000, 'SIDE_DISH', 'REFRESHING,SOUR,SAVORY', 1, 4.7, 115, 0),
(32, 'Rau Muống Xào Tỏi Xanh Mướt', 'Rau muống giòn ngọt xào tỏi đập dập thơm nức mũi món ăn dân dã', 35000, 'SIDE_DISH', 'SAVORY,REFRESHING', 1, 4.6, 260, 0),
(33, 'Trà Đào Cam Sả Hạt Chia', 'Trà đào thơm nồng hòa quyện cam tươi và sả thanh mát kèm hạt chia bổ dưỡng', 35000, 'BEVERAGE', 'SWEET,REFRESHING,SOUR', 1, 4.9, 320, 0),
(34, 'Trà Vải Hạt Sen Thanh Nhiệt', 'Trà lài ướp quả vải mọng nước và hạt sen bùi béo thanh lọc cơ thể', 38000, 'BEVERAGE', 'SWEET,REFRESHING', 1, 4.8, 230, 0),
(35, 'Nước Ép Dưa Hấu Tươi', 'Nước ép dưa hấu nguyên chất mát lạnh giải nhiệt mùa hè không đường hóa học', 30000, 'BEVERAGE', 'SWEET,REFRESHING', 1, 4.8, 190, 0),
(36, 'Cà Phê Sữa Đá Sài Gòn', 'Cà phê Robusta pha phin đậm đặc hòa quyện sữa đặc béo ngậy đá mát lạnh', 25000, 'BEVERAGE', 'SWEET,SAVORY', 1, 4.9, 275, 0);

-- 4. Lịch sử 28 đơn hàng quy mô thực tế (Tổng doanh thu demo: 16,949,000 VNĐ)
INSERT INTO `orders` (`order_id`, `tableNumber`, `customer_id`, `totalAmount`, `status`, `paymentMethod`, `qrPayloadUrl`, `created_at`) VALUES
('#ORD-1001', 'Bàn 01 - Tầng 1', 1, 570000, 'COMPLETED', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=570000&addInfo=#ORD-1001', 1700003600000),
('#ORD-1002', 'Bàn 02 - Tầng 1', 4, 625000, 'COMPLETED', 'Tiền mặt tại quầy', NULL, 1700007200000),
('#ORD-1003', 'Bàn 05 - Tầng 1', 3, 750000, 'COMPLETED', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=750000&addInfo=#ORD-1003', 1700010800000),
('#ORD-1004', 'VIP 01 - Tầng 2', 1, 1558000, 'COMPLETED', 'Chuyển khoản VietQR Napas247 (Mã VIP50K)', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=1558000&addInfo=#ORD-1004', 1700014400000),
('#ORD-1005', 'VIP 02 - Tầng 2', 5, 2155000, 'COMPLETED', 'Thẻ tín dụng Visa/MasterCard POS', NULL, 1700018000000),
('#ORD-1006', 'Bàn 04 - Tầng 1', 6, 900000, 'COMPLETED', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=900000&addInfo=#ORD-1006', 1700021600000),
('#ORD-1007', 'Bàn 07 - Tầng 1', 8, 420000, 'COMPLETED', 'Tiền mặt tại quầy', NULL, 1700025200000),
('#ORD-1008', 'Bàn 03 - Tầng 1', 9, 445000, 'COMPLETED', 'Tiền mặt tại quầy (Mã CHIEUTHU3)', NULL, 1700028800000),
('#ORD-1009', 'Bàn 12 - Tầng 2', 2, 780000, 'COMPLETED', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=780000&addInfo=#ORD-1009', 1700032400000),
('#ORD-1010', 'Bàn 10 - Tầng 2', 7, 1350000, 'COMPLETED', 'Thẻ tín dụng Visa/MasterCard POS', NULL, 1700036000000),
('#ORD-1011', 'Bàn 08 - Tầng 1', 10, 490000, 'COMPLETED', 'Tiền mặt tại quầy', NULL, 1700039600000),
('#ORD-1012', 'Bàn 14 - Tầng 2', 11, 720000, 'COMPLETED', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=720000&addInfo=#ORD-1012', 1700043200000),
('#ORD-1013', 'VIP 03 - Tầng 2', 12, 2094000, 'COMPLETED', 'Chuyển khoản VietQR Napas247 (Mã VIP50K)', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=2094000&addInfo=#ORD-1013', 1700046800000),
('#ORD-1014', 'Bàn 06 - Tầng 1', 13, 600000, 'COMPLETED', 'Tiền mặt tại quầy', NULL, 1700050400000),
('#ORD-1015', 'Bàn 15 - Tầng 2', 14, 1340000, 'COMPLETED', 'Thẻ tín dụng Visa/MasterCard POS', NULL, 1700054000000),
('#ORD-1016', 'Bàn 01 - Tầng 1', 1, 645000, 'COMPLETED', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=645000&addInfo=#ORD-1016', 1700057600000),
('#ORD-1017', 'Bàn 05 - Tầng 1', 3, 695000, 'COMPLETED', 'Tiền mặt tại quầy', NULL, 1700061200000),
('#ORD-1018', 'Bàn 12 - Tầng 2', 2, 812000, 'COMPLETED', 'Chuyển khoản VietQR Napas247 (Mã CHIEUTHU3)', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=812000&addInfo=#ORD-1018', 1700064800000),
('#ORD-1019', 'Bàn 02 - Tầng 1', 4, 335000, 'PREPARING', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=335000&addInfo=#ORD-1019', 1700068400000),
('#ORD-1020', 'VIP 01 - Tầng 2', 1, 665000, 'PREPARING', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=665000&addInfo=#ORD-1020', 1700072000000),
('#ORD-1021', 'Bàn 07 - Tầng 1', 8, 405000, 'PREPARING', 'Tiền mặt tại quầy', NULL, 1700075600000),
('#ORD-1022', 'Bàn 04 - Tầng 1', 6, 410000, 'PREPARING', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=410000&addInfo=#ORD-1022', 1700079200000),
('#ORD-1023', 'Bàn 09 - Tầng 1', 10, 285000, 'PREPARING', 'Tiền mặt tại quầy', NULL, 1700082800000),
('#ORD-1024', 'Bàn 10 - Tầng 2', 7, 285000, 'PREPARING', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=285000&addInfo=#ORD-1024', 1700086400000),
('#ORD-1025', 'Bàn 03 - Tầng 1', 9, 215000, 'PENDING', 'Tiền mặt tại quầy', NULL, 1700090000000),
('#ORD-1026', 'Bàn 14 - Tầng 2', 11, 395000, 'PENDING', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=395000&addInfo=#ORD-1026', 1700093600000),
('#ORD-1027', 'VIP 02 - Tầng 2', 5, 1205000, 'PENDING', 'Chuyển khoản VietQR Napas247', 'https://img.vietqr.io/image/970422-0901234567-compact.png?amount=1205000&addInfo=#ORD-1027', 1700097200000),
('#ORD-1028', 'Bàn 06 - Tầng 1', 13, 180000, 'CANCELLED', 'Tiền mặt tại quầy', NULL, 1700100800000);

-- 5. Chi tiết 109 món trong 28 đơn hàng
INSERT INTO `order_items` (`id`, `order_id`, `menu_item_id`, `quantity`, `unitPrice`) VALUES
(1, '#ORD-1001', 1, 2, 120000),
(2, '#ORD-1001', 12, 4, 15000),
(3, '#ORD-1001', 28, 2, 50000),
(4, '#ORD-1001', 27, 2, 85000),
(1, '#ORD-1002', 3, 2, 150000),
(2, '#ORD-1002', 29, 3, 40000),
(3, '#ORD-1002', 13, 4, 15000),
(4, '#ORD-1002', 20, 1, 145000),
(1, '#ORD-1003', 16, 2, 180000),
(2, '#ORD-1003', 7, 2, 60000),
(3, '#ORD-1003', 33, 4, 35000),
(4, '#ORD-1003', 21, 1, 130000),
(1, '#ORD-1004', 18, 2, 260000),
(2, '#ORD-1004', 6, 4, 115000),
(3, '#ORD-1004', 11, 2, 80000),
(4, '#ORD-1004', 34, 6, 38000),
(5, '#ORD-1004', 24, 1, 190000),
(1, '#ORD-1005', 17, 3, 240000),
(2, '#ORD-1005', 24, 2, 190000),
(3, '#ORD-1005', 21, 4, 130000),
(4, '#ORD-1005', 33, 8, 35000),
(5, '#ORD-1005', 27, 3, 85000),
(1, '#ORD-1006', 19, 3, 135000),
(2, '#ORD-1006', 27, 3, 85000),
(3, '#ORD-1006', 14, 6, 15000),
(4, '#ORD-1006', 28, 3, 50000),
(1, '#ORD-1007', 2, 2, 85000),
(2, '#ORD-1007', 9, 2, 55000),
(3, '#ORD-1007', 15, 4, 15000),
(4, '#ORD-1007', 29, 2, 40000),
(1, '#ORD-1008', 8, 3, 65000),
(2, '#ORD-1008', 29, 2, 40000),
(3, '#ORD-1008', 12, 4, 15000),
(4, '#ORD-1008', 23, 1, 110000),
(1, '#ORD-1009', 4, 2, 160000),
(2, '#ORD-1009', 22, 2, 125000),
(3, '#ORD-1009', 36, 4, 25000),
(4, '#ORD-1009', 31, 2, 55000),
(1, '#ORD-1010', 18, 2, 260000),
(2, '#ORD-1010', 5, 4, 95000),
(3, '#ORD-1010', 32, 2, 35000),
(4, '#ORD-1010', 13, 6, 15000),
(5, '#ORD-1010', 20, 2, 145000),
(1, '#ORD-1011', 1, 2, 120000),
(2, '#ORD-1011', 30, 2, 45000),
(3, '#ORD-1011', 35, 2, 30000),
(4, '#ORD-1011', 28, 2, 50000),
(1, '#ORD-1012', 20, 2, 145000),
(2, '#ORD-1012', 27, 2, 85000),
(3, '#ORD-1012', 33, 4, 35000),
(4, '#ORD-1012', 7, 2, 60000),
(1, '#ORD-1013', 17, 2, 240000),
(2, '#ORD-1013', 21, 4, 130000),
(3, '#ORD-1013', 6, 4, 115000),
(4, '#ORD-1013', 34, 8, 38000),
(5, '#ORD-1013', 26, 2, 165000),
(1, '#ORD-1014', 3, 2, 150000),
(2, '#ORD-1014', 31, 2, 55000),
(3, '#ORD-1014', 36, 2, 25000),
(4, '#ORD-1014', 25, 1, 140000),
(1, '#ORD-1015', 24, 2, 190000),
(2, '#ORD-1015', 26, 2, 165000),
(3, '#ORD-1015', 11, 2, 80000),
(4, '#ORD-1015', 33, 6, 35000),
(5, '#ORD-1015', 18, 1, 260000),
(1, '#ORD-1016', 5, 3, 95000),
(2, '#ORD-1016', 8, 2, 65000),
(3, '#ORD-1016', 12, 4, 15000),
(4, '#ORD-1016', 27, 2, 85000),
(1, '#ORD-1017', 16, 2, 180000),
(2, '#ORD-1017', 28, 3, 50000),
(3, '#ORD-1017', 15, 4, 15000),
(4, '#ORD-1017', 22, 1, 125000),
(1, '#ORD-1018', 19, 2, 135000),
(2, '#ORD-1018', 22, 2, 125000),
(3, '#ORD-1018', 34, 4, 38000),
(4, '#ORD-1018', 10, 2, 70000),
(1, '#ORD-1019', 4, 1, 160000),
(2, '#ORD-1019', 29, 2, 40000),
(3, '#ORD-1019', 13, 3, 15000),
(4, '#ORD-1019', 28, 1, 50000),
(1, '#ORD-1020', 18, 1, 260000),
(2, '#ORD-1020', 6, 2, 115000),
(3, '#ORD-1020', 10, 1, 70000),
(4, '#ORD-1020', 33, 3, 35000),
(1, '#ORD-1021', 21, 2, 130000),
(2, '#ORD-1021', 27, 1, 85000),
(3, '#ORD-1021', 35, 2, 30000),
(1, '#ORD-1022', 20, 1, 145000),
(2, '#ORD-1022', 23, 2, 110000),
(3, '#ORD-1022', 14, 3, 15000),
(1, '#ORD-1023', 1, 1, 120000),
(2, '#ORD-1023', 7, 2, 60000),
(3, '#ORD-1023', 12, 3, 15000),
(1, '#ORD-1024', 25, 1, 140000),
(2, '#ORD-1024', 32, 2, 35000),
(3, '#ORD-1024', 36, 3, 25000),
(1, '#ORD-1025', 2, 1, 85000),
(2, '#ORD-1025', 28, 2, 50000),
(3, '#ORD-1025', 15, 2, 15000),
(1, '#ORD-1026', 16, 1, 180000),
(2, '#ORD-1026', 9, 2, 55000),
(3, '#ORD-1026', 33, 3, 35000),
(1, '#ORD-1027', 17, 2, 240000),
(2, '#ORD-1027', 6, 3, 115000),
(3, '#ORD-1027', 24, 1, 190000),
(4, '#ORD-1027', 34, 5, 38000),
(1, '#ORD-1028', 3, 1, 150000),
(2, '#ORD-1028', 13, 2, 15000);
