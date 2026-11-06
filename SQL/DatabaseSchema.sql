IF OBJECT_ID(N'[__EFMigrationsHistory]') IS NULL
BEGIN
    CREATE TABLE [__EFMigrationsHistory] (
        [MigrationId] nvarchar(150) NOT NULL,
        [ProductVersion] nvarchar(32) NOT NULL,
        CONSTRAINT [PK___EFMigrationsHistory] PRIMARY KEY ([MigrationId])
    );
END;
GO

BEGIN TRANSACTION;
CREATE TABLE [MenuItems] (
    [Id] int NOT NULL IDENTITY,
    [Loai] nvarchar(max) NOT NULL,
    [Ten] nvarchar(max) NOT NULL,
    [Gia] decimal(18,2) NOT NULL,
    [Hinh] nvarchar(max) NOT NULL,
    CONSTRAINT [PK_MenuItems] PRIMARY KEY ([Id])
);

CREATE TABLE [Orders] (
    [Id] nvarchar(450) NOT NULL,
    [CustomerName] nvarchar(max) NOT NULL,
    [OrderDate] datetime2 NOT NULL,
    [Total] decimal(18,2) NOT NULL,
    [Status] nvarchar(max) NOT NULL,
    CONSTRAINT [PK_Orders] PRIMARY KEY ([Id])
);

CREATE TABLE [OrderItems] (
    [InternalId] int NOT NULL IDENTITY,
    [Cart_Id] nvarchar(max) NOT NULL,
    [Id] int NOT NULL,
    [Loai] nvarchar(max) NOT NULL,
    [Ten] nvarchar(max) NOT NULL,
    [Gia] decimal(18,2) NOT NULL,
    [Hinh] nvarchar(max) NOT NULL,
    [OrderId] nvarchar(450) NOT NULL,
    CONSTRAINT [PK_OrderItems] PRIMARY KEY ([InternalId]),
    CONSTRAINT [FK_OrderItems_Orders_OrderId] FOREIGN KEY ([OrderId]) REFERENCES [Orders] ([Id]) ON DELETE CASCADE
);

IF EXISTS (SELECT * FROM [sys].[identity_columns] WHERE [name] IN (N'Id', N'Gia', N'Hinh', N'Loai', N'Ten') AND [object_id] = OBJECT_ID(N'[MenuItems]'))
    SET IDENTITY_INSERT [MenuItems] ON;
INSERT INTO [MenuItems] ([Id], [Gia], [Hinh], [Loai], [Ten])
VALUES (1, 120000.0, N'/images/monchinh/chan-giou-muoi-chien-gion.png', N'mon_chinh', N'Chân Giò Muối Chiên Giòn'),
(2, 85000.0, N'/images/monchinh/ech-nuong-muoi-ot-la-chanh.png', N'mon_chinh', N'Ếch Nướng Muối Ớt Lá Chanh'),
(3, 150000.0, N'/images/monchinh/ga-nuong-mat-ong.png', N'mon_chinh', N'Gà Nướng Mật Ong'),
(4, 160000.0, N'/images/monchinh/ga-quay-banh-bao.png', N'mon_chinh', N'Gà Quay Bánh Bao'),
(5, 95000.0, N'/images/monchinh/tom-chien-xu.png', N'mon_chinh', N'Tôm Chiên Xù'),
(6, 105000.0, N'/images/monchinh/tom-nuong-muoi-ot.png', N'mon_chinh', N'Tôm Nướng Muối Ớt'),
(7, 60000.0, N'/images/monphu/Chan-ga-sa-tac.png', N'mon_phu', N'Chân Gà Sả Tắc'),
(8, 65000.0, N'/images/monphu/chan-ga-sot-thai.png', N'mon_phu', N'Chân Gà Sốt Thái'),
(9, 55000.0, N'/images/monphu/goi-ngo-sen.png', N'mon_phu', N'Gỏi Ngó Sen'),
(10, 70000.0, N'/images/monphu/goi-xoai-tom-kho.png', N'mon_phu', N'Gỏi Xoài Tôm Khô'),
(11, 80000.0, N'/images/monphu/goi-xoai-tom-thit.png', N'mon_phu', N'Gỏi Xoài Tôm Thịt'),
(12, 15000.0, N'/images/nuocuong/7up.png', N'nuoc_uong', N'7Up'),
(13, 15000.0, N'/images/nuocuong/cocacola.png', N'nuoc_uong', N'Coca Cola'),
(14, 15000.0, N'/images/nuocuong/pepsi.png', N'nuoc_uong', N'Pepsi'),
(15, 15000.0, N'/images/nuocuong/sting.png', N'nuoc_uong', N'Sting');
IF EXISTS (SELECT * FROM [sys].[identity_columns] WHERE [name] IN (N'Id', N'Gia', N'Hinh', N'Loai', N'Ten') AND [object_id] = OBJECT_ID(N'[MenuItems]'))
    SET IDENTITY_INSERT [MenuItems] OFF;

CREATE INDEX [IX_OrderItems_OrderId] ON [OrderItems] ([OrderId]);

INSERT INTO [__EFMigrationsHistory] ([MigrationId], [ProductVersion])
VALUES (N'20260403041616_InitialSqlServer', N'10.0.5');

COMMIT;
GO

BEGIN TRANSACTION;
ALTER TABLE [OrderItems] DROP CONSTRAINT [FK_OrderItems_Orders_OrderId];

DECLARE @var nvarchar(max);
SELECT @var = QUOTENAME([d].[name])
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[Orders]') AND [c].[name] = N'Status');
IF @var IS NOT NULL EXEC(N'ALTER TABLE [Orders] DROP CONSTRAINT ' + @var + ';');
ALTER TABLE [Orders] ALTER COLUMN [Status] nvarchar(max) NULL;

DECLARE @var1 nvarchar(max);
SELECT @var1 = QUOTENAME([d].[name])
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[Orders]') AND [c].[name] = N'CustomerName');
IF @var1 IS NOT NULL EXEC(N'ALTER TABLE [Orders] DROP CONSTRAINT ' + @var1 + ';');
ALTER TABLE [Orders] ALTER COLUMN [CustomerName] nvarchar(max) NULL;

DECLARE @var2 nvarchar(max);
SELECT @var2 = QUOTENAME([d].[name])
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[OrderItems]') AND [c].[name] = N'Ten');
IF @var2 IS NOT NULL EXEC(N'ALTER TABLE [OrderItems] DROP CONSTRAINT ' + @var2 + ';');
ALTER TABLE [OrderItems] ALTER COLUMN [Ten] nvarchar(max) NULL;

DECLARE @var3 nvarchar(max);
SELECT @var3 = QUOTENAME([d].[name])
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[OrderItems]') AND [c].[name] = N'OrderId');
IF @var3 IS NOT NULL EXEC(N'ALTER TABLE [OrderItems] DROP CONSTRAINT ' + @var3 + ';');
ALTER TABLE [OrderItems] ALTER COLUMN [OrderId] nvarchar(450) NULL;

DECLARE @var4 nvarchar(max);
SELECT @var4 = QUOTENAME([d].[name])
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[OrderItems]') AND [c].[name] = N'Loai');
IF @var4 IS NOT NULL EXEC(N'ALTER TABLE [OrderItems] DROP CONSTRAINT ' + @var4 + ';');
ALTER TABLE [OrderItems] ALTER COLUMN [Loai] nvarchar(max) NULL;

DECLARE @var5 nvarchar(max);
SELECT @var5 = QUOTENAME([d].[name])
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[OrderItems]') AND [c].[name] = N'Hinh');
IF @var5 IS NOT NULL EXEC(N'ALTER TABLE [OrderItems] DROP CONSTRAINT ' + @var5 + ';');
ALTER TABLE [OrderItems] ALTER COLUMN [Hinh] nvarchar(max) NULL;

DECLARE @var6 nvarchar(max);
SELECT @var6 = QUOTENAME([d].[name])
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[OrderItems]') AND [c].[name] = N'Cart_Id');
IF @var6 IS NOT NULL EXEC(N'ALTER TABLE [OrderItems] DROP CONSTRAINT ' + @var6 + ';');
ALTER TABLE [OrderItems] ALTER COLUMN [Cart_Id] nvarchar(max) NULL;

ALTER TABLE [OrderItems] ADD [SoLuong] int NOT NULL DEFAULT 0;

DECLARE @var7 nvarchar(max);
SELECT @var7 = QUOTENAME([d].[name])
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[MenuItems]') AND [c].[name] = N'Ten');
IF @var7 IS NOT NULL EXEC(N'ALTER TABLE [MenuItems] DROP CONSTRAINT ' + @var7 + ';');
ALTER TABLE [MenuItems] ALTER COLUMN [Ten] nvarchar(max) NULL;

DECLARE @var8 nvarchar(max);
SELECT @var8 = QUOTENAME([d].[name])
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[MenuItems]') AND [c].[name] = N'Loai');
IF @var8 IS NOT NULL EXEC(N'ALTER TABLE [MenuItems] DROP CONSTRAINT ' + @var8 + ';');
ALTER TABLE [MenuItems] ALTER COLUMN [Loai] nvarchar(max) NULL;

DECLARE @var9 nvarchar(max);
SELECT @var9 = QUOTENAME([d].[name])
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[MenuItems]') AND [c].[name] = N'Hinh');
IF @var9 IS NOT NULL EXEC(N'ALTER TABLE [MenuItems] DROP CONSTRAINT ' + @var9 + ';');
ALTER TABLE [MenuItems] ALTER COLUMN [Hinh] nvarchar(max) NULL;

ALTER TABLE [OrderItems] ADD CONSTRAINT [FK_OrderItems_Orders_OrderId] FOREIGN KEY ([OrderId]) REFERENCES [Orders] ([Id]);

INSERT INTO [__EFMigrationsHistory] ([MigrationId], [ProductVersion])
VALUES (N'20260404055840_AddSoLuong', N'10.0.5');

COMMIT;
GO

BEGIN TRANSACTION;
CREATE TABLE [Customers] (
    [Id] int NOT NULL IDENTITY,
    [HoTen] nvarchar(max) NULL,
    [SoDienThoai] nvarchar(max) NULL,
    [MatKhau] nvarchar(max) NULL,
    [CreatedAt] datetime2 NOT NULL,
    CONSTRAINT [PK_Customers] PRIMARY KEY ([Id])
);

INSERT INTO [__EFMigrationsHistory] ([MigrationId], [ProductVersion])
VALUES (N'20260404101831_AddCustomerTable', N'10.0.5');

COMMIT;
GO

BEGIN TRANSACTION;
CREATE TABLE [AdminAccounts] (
    [Id] int NOT NULL IDENTITY,
    [HoTen] nvarchar(max) NULL,
    [SoDienThoai] nvarchar(max) NULL,
    [MatKhau] nvarchar(max) NULL,
    [CreatedAt] datetime2 NOT NULL,
    CONSTRAINT [PK_AdminAccounts] PRIMARY KEY ([Id])
);

INSERT INTO [__EFMigrationsHistory] ([MigrationId], [ProductVersion])
VALUES (N'20260411094209_AddAdminAccounts', N'10.0.5');

COMMIT;
GO

