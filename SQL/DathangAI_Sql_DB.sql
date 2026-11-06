-- ============================================================
--  DatHangAI - Restaurant Ordering System
--  File     : database_setup.sql
--  Mô tả    : Script tạo và seed dữ liệu cho toàn bộ database
--  Cập nhật : 2026-04-12
-- ============================================================
-- Chạy script này để khởi tạo lại database từ đầu.
-- Lệnh chạy bằng SSMS hoặc sqlcmd:
--   sqlcmd -S (localdb)\mssqllocaldb -d RestaurantDB -i database_setup.sql
-- ============================================================


-- ============================================================
-- PHẦN 1: TẠO DATABASE (bỏ comment nếu muốn tạo mới)
-- ============================================================
-- IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'RestaurantDB')
-- BEGIN
--     CREATE DATABASE RestaurantDB;
-- END
-- GO
-- USE RestaurantDB;
-- GO


-- ============================================================
-- PHẦN 2: XÓA BẢNG CŨ (nếu cần reset)
-- ============================================================
-- Bỏ comment phần này nếu muốn xóa và tạo lại toàn bộ bảng
/*
IF OBJECT_ID('OrderItems',    'U') IS NOT NULL DROP TABLE OrderItems;
IF OBJECT_ID('Orders',        'U') IS NOT NULL DROP TABLE Orders;
IF OBJECT_ID('MenuItems',     'U') IS NOT NULL DROP TABLE MenuItems;
IF OBJECT_ID('Customers',     'U') IS NOT NULL DROP TABLE Customers;
IF OBJECT_ID('AdminAccounts', 'U') IS NOT NULL DROP TABLE AdminAccounts;
*/


-- ============================================================
-- PHẦN 3: TẠO BẢNG
-- ============================================================

-- ------------------------------------------------------------
-- Bảng: MenuItems (Danh sách món ăn)
-- ------------------------------------------------------------
IF OBJECT_ID('MenuItems', 'U') IS NULL
BEGIN
    CREATE TABLE MenuItems (
        Id    INT            NOT NULL IDENTITY(1,1),
        Loai  NVARCHAR(MAX)  NULL,          -- 'mon_chinh', 'mon_phu', 'nuoc_uong'
        Ten   NVARCHAR(MAX)  NULL,          -- Tên món
        Gia   DECIMAL(18,2)  NOT NULL,      -- Giá (VNĐ)
        Hinh  NVARCHAR(MAX)  NULL,          -- Đường dẫn hình ảnh
        CONSTRAINT PK_MenuItems PRIMARY KEY (Id)
    );
    PRINT '>> Đã tạo bảng MenuItems';
END
ELSE
    PRINT '>> Bảng MenuItems đã tồn tại, bỏ qua.';
GO

-- ------------------------------------------------------------
-- Bảng: Orders (Đơn hàng)
-- ------------------------------------------------------------
IF OBJECT_ID('Orders', 'U') IS NULL
BEGIN
    CREATE TABLE Orders (
        Id           NVARCHAR(450)  NOT NULL,   -- GUID dạng chuỗi
        CustomerName NVARCHAR(MAX)  NULL,        -- Tên khách đặt hàng
        OrderDate    DATETIME2      NOT NULL,    -- Thời gian đặt hàng
        Total        DECIMAL(18,2)  NOT NULL,    -- Tổng tiền
        Status       NVARCHAR(MAX)  NULL,        -- 'Pending', 'Confirmed', 'Completed', 'Cancelled'
        CONSTRAINT PK_Orders PRIMARY KEY (Id)
    );
    PRINT '>> Đã tạo bảng Orders';
END
ELSE
    PRINT '>> Bảng Orders đã tồn tại, bỏ qua.';
GO

-- ------------------------------------------------------------
-- Bảng: OrderItems (Chi tiết đơn hàng)
-- ------------------------------------------------------------
IF OBJECT_ID('OrderItems', 'U') IS NULL
BEGIN
    CREATE TABLE OrderItems (
        InternalId  INT            NOT NULL IDENTITY(1,1),
        Cart_Id     NVARCHAR(MAX)  NULL,          -- ID giỏ hàng tạm
        Id          INT            NOT NULL,       -- ID gốc của MenuItem
        Loai        NVARCHAR(MAX)  NULL,
        Ten         NVARCHAR(MAX)  NULL,
        Gia         DECIMAL(18,2)  NOT NULL,
        Hinh        NVARCHAR(MAX)  NULL,
        SoLuong     INT            NOT NULL DEFAULT 1,
        OrderId     NVARCHAR(450)  NULL,           -- FK -> Orders.Id
        CONSTRAINT PK_OrderItems        PRIMARY KEY (InternalId),
        CONSTRAINT FK_OrderItems_Orders FOREIGN KEY (OrderId)
            REFERENCES Orders (Id)
            ON DELETE SET NULL  -- Giữ lại lịch sử khi xóa đơn
    );
    CREATE INDEX IX_OrderItems_OrderId ON OrderItems (OrderId);
    PRINT '>> Đã tạo bảng OrderItems';
END
ELSE
    PRINT '>> Bảng OrderItems đã tồn tại, bỏ qua.';
GO

-- ------------------------------------------------------------
-- Bảng: Customers (Khách hàng)
-- ------------------------------------------------------------
IF OBJECT_ID('Customers', 'U') IS NULL
BEGIN
    CREATE TABLE Customers (
        Id            INT            NOT NULL IDENTITY(1,1),
        HoTen         NVARCHAR(MAX)  NULL,
        SoDienThoai   NVARCHAR(MAX)  NULL,
        MatKhau       NVARCHAR(MAX)  NULL,       -- Lưu hash mật khẩu
        CreatedAt     DATETIME2      NOT NULL DEFAULT GETDATE(),
        CONSTRAINT PK_Customers PRIMARY KEY (Id)
    );
    PRINT '>> Đã tạo bảng Customers';
END
ELSE
    PRINT '>> Bảng Customers đã tồn tại, bỏ qua.';
GO

-- ------------------------------------------------------------
-- Bảng: AdminAccounts (Tài khoản quản trị)
-- ------------------------------------------------------------
IF OBJECT_ID('AdminAccounts', 'U') IS NULL
BEGIN
    CREATE TABLE AdminAccounts (
        Id            INT            NOT NULL IDENTITY(1,1),
        HoTen         NVARCHAR(MAX)  NULL,
        SoDienThoai   NVARCHAR(MAX)  NULL,
        MatKhau       NVARCHAR(MAX)  NULL,       -- Lưu hash mật khẩu
        CreatedAt     DATETIME2      NOT NULL DEFAULT GETDATE(),
        CONSTRAINT PK_AdminAccounts PRIMARY KEY (Id)
    );
    PRINT '>> Đã tạo bảng AdminAccounts';
END
ELSE
    PRINT '>> Bảng AdminAccounts đã tồn tại, bỏ qua.';
GO


-- ============================================================
-- PHẦN 4: DỮ LIỆU MẪU (Seed Data)
-- ============================================================

-- ------------------------------------------------------------
-- Seed: MenuItems
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM MenuItems)
BEGIN
    SET IDENTITY_INSERT MenuItems ON;

    INSERT INTO MenuItems (Id, Loai, Ten, Gia, Hinh) VALUES
    -- Món chính
    (1,  N'mon_chinh', N'Chân Giò Muối Chiên Giòn',    120000, N'/images/monchinh/chan-giou-muoi-chien-gion.png'),
    (2,  N'mon_chinh', N'Ếch Nướng Muối Ớt Lá Chanh',   85000, N'/images/monchinh/ech-nuong-muoi-ot-la-chanh.png'),
    (3,  N'mon_chinh', N'Gà Nướng Mật Ong',             150000, N'/images/monchinh/ga-nuong-mat-ong.png'),
    (4,  N'mon_chinh', N'Gà Quay Bánh Bao',             160000, N'/images/monchinh/ga-quay-banh-bao.png'),
    (5,  N'mon_chinh', N'Tôm Chiên Xù',                  95000, N'/images/monchinh/tom-chien-xu.png'),
    (6,  N'mon_chinh', N'Tôm Nướng Muối Ớt',            105000, N'/images/monchinh/tom-nuong-muoi-ot.png'),

    -- Món phụ
    (7,  N'mon_phu',   N'Chân Gà Sả Tắc',               60000, N'/images/monphu/Chan-ga-sa-tac.png'),
    (8,  N'mon_phu',   N'Chân Gà Sốt Thái',              65000, N'/images/monphu/chan-ga-sot-thai.png'),
    (9,  N'mon_phu',   N'Gỏi Ngó Sen',                   55000, N'/images/monphu/goi-ngo-sen.png'),
    (10, N'mon_phu',   N'Gỏi Xoài Tôm Khô',              70000, N'/images/monphu/goi-xoai-tom-kho.png'),
    (11, N'mon_phu',   N'Gỏi Xoài Tôm Thịt',             80000, N'/images/monphu/goi-xoai-tom-thit.png'),

    -- Nước uống
    (12, N'nuoc_uong', N'7Up',        15000, N'/images/nuocuong/7up.png'),
    (13, N'nuoc_uong', N'Coca Cola',  15000, N'/images/nuocuong/cocacola.png'),
    (14, N'nuoc_uong', N'Pepsi',      15000, N'/images/nuocuong/pepsi.png'),
    (15, N'nuoc_uong', N'Sting',      15000, N'/images/nuocuong/sting.png');

    SET IDENTITY_INSERT MenuItems OFF;
    PRINT '>> Đã thêm dữ liệu mẫu vào MenuItems (15 món)';
END
ELSE
    PRINT '>> MenuItems đã có dữ liệu, bỏ qua seed.';
GO

-- ------------------------------------------------------------
-- Seed: AdminAccounts (tài khoản admin mặc định)
-- QUAN TRỌNG: Thay MatKhau bằng giá trị hash thực trong production!
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM AdminAccounts)
BEGIN
    INSERT INTO AdminAccounts (HoTen, SoDienThoai, MatKhau, CreatedAt) VALUES
    (N'Quản Trị Viên', N'0900000000', N'admin123', GETDATE());
    PRINT '>> Đã thêm tài khoản admin mặc định';
END
ELSE
    PRINT '>> AdminAccounts đã có dữ liệu, bỏ qua seed.';
GO


-- ============================================================
-- PHẦN 5: CÁC CÂU QUERY HỮU ÍCH (để kiểm tra dữ liệu)
-- ============================================================

-- Xem menu theo loại
-- SELECT Loai, COUNT(*) AS SoMon, MIN(Gia) AS GiaThap, MAX(Gia) AS GiaCao
-- FROM MenuItems GROUP BY Loai ORDER BY Loai;

-- Xem đơn hàng gần đây
-- SELECT TOP 20 o.Id, o.CustomerName, o.OrderDate, o.Total, o.Status,
--        COUNT(oi.InternalId) AS SoMon
-- FROM Orders o
-- LEFT JOIN OrderItems oi ON oi.OrderId = o.Id
-- GROUP BY o.Id, o.CustomerName, o.OrderDate, o.Total, o.Status
-- ORDER BY o.OrderDate DESC;

-- Thống kê doanh thu theo ngày
-- SELECT CAST(OrderDate AS DATE) AS Ngay,
--        COUNT(*) AS SoDon,
--        SUM(Total) AS DoanhThu
-- FROM Orders
-- WHERE Status = 'Completed'
-- GROUP BY CAST(OrderDate AS DATE)
-- ORDER BY Ngay DESC;

-- Xem danh sách khách hàng
-- SELECT Id, HoTen, SoDienThoai, CreatedAt FROM Customers ORDER BY CreatedAt DESC;


-- ============================================================
-- PHẦN 6: CẬP NHẬT MENU (thêm/sửa/xóa món ăn)
-- ============================================================

-- Thêm món mới:
-- INSERT INTO MenuItems (Loai, Ten, Gia, Hinh)
-- VALUES (N'mon_chinh', N'Tên Món Mới', 99000, N'/images/monchinh/ten-mon-moi.png');

-- Cập nhật giá món:
-- UPDATE MenuItems SET Gia = 130000 WHERE Ten = N'Chân Giò Muối Chiên Giòn';

-- Xóa món:
-- DELETE FROM MenuItems WHERE Id = 99;

PRINT '============================================================';
PRINT 'Hoàn tất! Database DatHangAI đã sẵn sàng sử dụng.';
PRINT '============================================================';
GO
