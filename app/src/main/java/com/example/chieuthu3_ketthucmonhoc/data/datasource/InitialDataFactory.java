package com.example.chieuthu3_ketthucmonhoc.data.datasource;

import com.example.chieuthu3_ketthucmonhoc.R;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;
import com.example.chieuthu3_ketthucmonhoc.domain.models.UserRole;
import com.example.chieuthu3_ketthucmonhoc.domain.services.SecurityHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Nguồn dữ liệu khởi tạo chuẩn thương mại (Production / Commercial Grade Seed Data)
 * Dành cho hệ thống quản lý và gọi món Nhà Hàng Ẩm Thực Việt.
 * Quy mô: 36 Món ăn, 14 Khách hàng VIP/Doanh nghiệp, 6 Nhân sự, 28 Đơn hàng thực tế.
 * Đồng bộ 100% với restaurant.sql và app/src/main/assets/restaurant.sql.
 */
public final class InitialDataFactory {

    private InitialDataFactory() {
        // Utility factory
    }

    /**
     * Danh sách tài khoản quản trị và nhân sự vận hành nhà hàng
     */
    public static List<AdminAccount> createInitialAdmins() {
        List<AdminAccount> list = new ArrayList<>();

        list.add(new AdminAccount(
                1,
                "admin",
                "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9",
                "Võ Văn Hoàng (Tổng Quản Trị Hệ Thống)",
                UserRole.SUPER_ADMIN,
                1700000000000L
        ));
        list.add(new AdminAccount(
                2,
                "0900000000",
                "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9",
                "Quản Trị Hotline Tiếp Nhận (SĐT)",
                UserRole.SUPER_ADMIN,
                1700000000000L
        ));
        list.add(new AdminAccount(
                3,
                "manager",
                "866485796cfa8d7c0cf7111640205b83076433547577511d81f8030ae99ecea5",
                "Trần Minh Quân (Quản Lý Trưởng Nhà Hàng)",
                UserRole.MANAGER,
                1700000000000L
        ));
        list.add(new AdminAccount(
                4,
                "beptruong",
                "866485796cfa8d7c0cf7111640205b83076433547577511d81f8030ae99ecea5",
                "Nguyễn Hữu Tài (Bếp Trưởng Điều Phối)",
                UserRole.MANAGER,
                1700000000000L
        ));
        list.add(new AdminAccount(
                5,
                "cashier",
                "b4c94003c562bb0d89535eca77f07284fe560fd48a7cc1ed99f0a56263d616ba",
                "Lê Thị Thảo (Thu Ngân Trưởng Ca Sáng)",
                UserRole.STAFF,
                1700000000000L
        ));
        list.add(new AdminAccount(
                6,
                "thungan02",
                "b4c94003c562bb0d89535eca77f07284fe560fd48a7cc1ed99f0a56263d616ba",
                "Nguyễn Thúy Vy (Thu Ngân Ca Tối)",
                UserRole.STAFF,
                1700000000000L
        ));

        return list;
    }

    /**
     * Danh sách khách hàng thực tế thuộc các nhóm: VIP Diamond/Platinum/Gold/Silver, Gia Đình, Doanh Nghiệp
     */
    public static List<Customer> createInitialCustomers() {
        List<Customer> list = new ArrayList<>();

        list.add(new Customer(
                1,
                "Nguyễn Hoàng Nam (VIP Diamond)",
                "khachhang@restaurant.com",
                "0901234567",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn VIP 01 - Tầng 2 (Khu VIP Diamond)",
                1700000000000L
        ));
        list.add(new Customer(
                2,
                "Trần Anh Tuấn (VIP Platinum)",
                "anhtuan.vip@gmail.com",
                "0912345678",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn 12 - Tầng 2 (Khu Máy Lạnh)",
                1700000000000L
        ));
        list.add(new Customer(
                3,
                "Chị Lê Mai Phương (Khách Gia Đình)",
                "maiphuong.family@gmail.com",
                "0987654321",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn 05 - Tầng 1 (Phòng Gia Đình Family-01)",
                1700000000000L
        ));
        list.add(new Customer(
                4,
                "Anh Vũ Đình Trọng (Khách Gia Đình)",
                "trongvu.family@gmail.com",
                "0934567890",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn 02 - Tầng 1 (Khu Trẻ Em & Gia Đình)",
                1700000000000L
        ));
        list.add(new Customer(
                5,
                "Phạm Quốc Hưng (Tập Đoàn SunGroup)",
                "quochung.sungroup@corp.vn",
                "0945678901",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "VIP 02 - Tầng 2 (Phòng Tiệc Doanh Nghiệp)",
                1700000000000L
        ));
        list.add(new Customer(
                6,
                "Đặng Thùy Dung (FPT Software)",
                "thuydung.fpt@corp.vn",
                "0967890123",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn 04 - Tầng 1 (Khu Hội Nghị Doanh Nghiệp)",
                1700000000000L
        ));
        list.add(new Customer(
                7,
                "Hoàng Minh Trí (Viettel Telecom)",
                "minhtri.viettel@corp.vn",
                "0978901234",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn 10 - Tầng 2 (Khu VIP)",
                1700000000000L
        ));
        list.add(new Customer(
                8,
                "Vũ Bích Ngọc (VIP Gold)",
                "bichngoc.gold@gmail.com",
                "0909876543",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn 07 - Tầng 1 (Khu Sân Vườn)",
                1700000000000L
        ));
        list.add(new Customer(
                9,
                "Đỗ Thành Đạt (Sinh Viên Công Nghệ)",
                "datdo.student@gmail.com",
                "0923456789",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn 03 - Tầng 1 (Khu Giới Trẻ)",
                1700000000000L
        ));
        list.add(new Customer(
                10,
                "Nguyễn Thu Trang (Khách Vãng Lai Thân Thiết)",
                "thutrang.travel@gmail.com",
                "0938112233",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn 09 - Tầng 1 (Khu Cửa Sổ)",
                1700000000000L
        ));
        list.add(new Customer(
                11,
                "Bùi Đức Mạnh (VIP Silver)",
                "ducmanh.silver@gmail.com",
                "0915667788",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn 14 - Tầng 2 (Khu Ban Công)",
                1700000000000L
        ));
        list.add(new Customer(
                12,
                "Trịnh Hải Yến (Khách Tiệc Sinh Nhật)",
                "haiyen.party@gmail.com",
                "0982334455",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn VIP 03 - Tầng 2 (Phòng Tiệc Sinh Nhật)",
                1700000000000L
        ));
        list.add(new Customer(
                13,
                "Lâm Quốc Bảo (Khách Đặt Bàn Trưa)",
                "quocbao.lunch@gmail.com",
                "0903445566",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn 06 - Tầng 1 (Khu Sân Vườn)",
                1700000000000L
        ));
        list.add(new Customer(
                14,
                "Phan Tuyết Mai (Vingroup)",
                "tuyetmai.vin@corp.vn",
                "0971223344",
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                "Bàn 15 - Tầng 2 (Khu Tiếp Khách)",
                1700000000000L
        ));

        return list;
    }

    /**
     * Thực đơn 36 món ẩm thực Việt phong phú đa phân loại
     */
    public static List<MenuItem> createInitialMenuItems() {
        List<MenuItem> list = new ArrayList<>();

        list.add(new MenuItem(
                1,
                "Chân Giò Muối Chiên Giòn",
                "Chân Giò Muối Chiên Giòn da giòn rụm, thịt mềm thơm đậm đà hương vị truyền thống",
                120000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.CRISPY, TasteAttribute.SAVORY, TasteAttribute.FRIED),
                true,
                4.9,
                145,
                R.drawable.food_chan_gio_muoi_chien_gion
        ));

        list.add(new MenuItem(
                2,
                "Ếch Nướng Muối Ớt Lá Chanh",
                "Ếch đồng tươi nướng muối ớt cay nồng thơm lừng hương lá chanh miền Tây",
                85000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SPICY, TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                true,
                4.7,
                98,
                R.drawable.food_ech_nuong_muoi_ot_la_chanh
        ));

        list.add(new MenuItem(
                3,
                "Gà Nướng Mật Ong Hoa Rừng",
                "Gà ta ướp mật ong hoa rừng nướng than hồng vàng óng, thịt ngọt thơm đậm vị",
                150000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                true,
                4.9,
                212,
                R.drawable.food_ga_nuong_mat_ong
        ));

        list.add(new MenuItem(
                4,
                "Gà Quay Thảo Mộc Bánh Bao",
                "Gà quay thảo mộc da giòn rụm ăn kèm bánh bao chiên vàng óng giải ngấy",
                160000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SAVORY, TasteAttribute.CRISPY),
                true,
                4.8,
                175,
                R.drawable.food_ga_quay_banh_bao
        ));

        list.add(new MenuItem(
                5,
                "Tôm Sú Chiên Xù Giòn Rụm",
                "Tôm sú tươi tẩm bột chiên xù vàng ruộm, chấm kèm sốt tương ớt mayonnaise",
                95000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.CRISPY, TasteAttribute.SAVORY, TasteAttribute.FRIED),
                true,
                4.7,
                136,
                R.drawable.food_tom_chien_xu
        ));

        list.add(new MenuItem(
                6,
                "Tôm Càng Nướng Muối Ớt Sa Tế",
                "Tôm càng xanh nướng muối ớt cay xè, giữ trọn vị ngọt tự nhiên của thịt tôm",
                115000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SPICY, TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                true,
                4.8,
                168,
                R.drawable.food_tom_nuong_muoi_ot
        ));

        list.add(new MenuItem(
                7,
                "Chân Gà Rút Xương Sả Tắc",
                "Chân gà rút xương ngâm sả tắc chua cay thanh mát giòn sần sật khai vị",
                60000L,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SPICY, TasteAttribute.REFRESHING, TasteAttribute.CRISPY),
                true,
                4.8,
                240,
                R.drawable.food_chan_ga_sa_tac
        ));

        list.add(new MenuItem(
                8,
                "Chân Gà Sốt Thái Chua Cay",
                "Chân gà ngập sốt Thái chua cay đậm vị thơm mùi xoài non và cóc bao tử",
                65000L,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.SPICY, TasteAttribute.SOUR, TasteAttribute.SWEET),
                true,
                4.9,
                285,
                R.drawable.food_chan_ga_sot_thai
        ));

        list.add(new MenuItem(
                9,
                "Gỏi Ngó Sen Tôm Thịt Thanh Đạm",
                "Gỏi ngó sen tôm thịt thanh đạm giải ngấy, chua ngọt vừa miệng rắc đậu phộng",
                55000L,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SWEET, TasteAttribute.REFRESHING, TasteAttribute.CRISPY),
                true,
                4.6,
                120,
                R.drawable.food_goi_ngo_sen
        ));

        list.add(new MenuItem(
                10,
                "Gỏi Xoài Tôm Khô Cay Nồng",
                "Xoài keo xanh băm sợi trộn tôm khô thơm bùi nước mắm ớt chua cay đậm đà",
                70000L,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SPICY, TasteAttribute.CRISPY),
                true,
                4.7,
                105,
                R.drawable.food_goi_xoai_tom_kho
        ));

        list.add(new MenuItem(
                11,
                "Gỏi Xoài Tôm Thịt Đậu Phộng",
                "Gỏi xoài tôm thịt tươi đầy đặn, rắc đậu phộng rang giòn thơm nức mũi",
                80000L,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SPICY, TasteAttribute.SAVORY),
                true,
                4.8,
                154,
                R.drawable.food_goi_xoai_tom_thit
        ));

        list.add(new MenuItem(
                12,
                "7Up Chanh Tươi Mát Lạnh",
                "Nước giải khát 7Up có ga hương chanh thanh mát sảng khoái ướp lạnh",
                15000L,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                true,
                4.9,
                380,
                R.drawable.food_7up
        ));

        list.add(new MenuItem(
                13,
                "Coca Cola Ướp Lạnh",
                "Nước ngọt có ga Coca Cola mát lạnh giải nhiệt tức thì trong bữa ăn",
                15000L,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                true,
                4.9,
                450,
                R.drawable.food_cocacola
        ));

        list.add(new MenuItem(
                14,
                "Pepsi Có Ga Sảng Khoái",
                "Nước giải khát Pepsi mát lạnh sảng khoái kích thích vị giác ngon miệng",
                15000L,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                true,
                4.8,
                310,
                R.drawable.food_pepsi
        ));

        list.add(new MenuItem(
                15,
                "Sting Dâu Tăng Lực",
                "Nước tăng lực Sting hương dâu tây thơm ngon tiếp thêm năng lượng",
                15000L,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                true,
                4.8,
                290,
                R.drawable.food_sting
        ));

        list.add(new MenuItem(
                16,
                "Lẩu Gà Lá Giang Chua Cay",
                "Lẩu gà lá giang chua cay đậm đà, thịt gà ta dai ngon nước lẩu thanh tao",
                180000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SAVORY, TasteAttribute.SPICY),
                true,
                4.9,
                215,
                R.drawable.food_lau_ga_la_giang
        ));

        list.add(new MenuItem(
                17,
                "Lẩu Hải Sản Chua Cay Thập Cẩm",
                "Lẩu hải sản chua cay phong cách Thái Lan với tôm sú, mực trứng, nghêu biển",
                240000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SPICY, TasteAttribute.SAVORY),
                true,
                4.9,
                165,
                R.drawable.food_lau_ga_la_giang
        ));

        list.add(new MenuItem(
                18,
                "Lẩu Riêu Cua Bắp Bò Sườn Sụn",
                "Lẩu riêu cua đồng béo ngậy kèm bắp bò hoa và sườn non giòn sần sật",
                260000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SAVORY, TasteAttribute.SOUR, TasteAttribute.SWEET),
                true,
                4.9,
                188,
                R.drawable.food_lau_ga_la_giang
        ));

        list.add(new MenuItem(
                19,
                "Bò Lúc Lắc Khoai Tây Chiên",
                "Thịt bò thăn cắt vuông xào lửa lớn mềm ngọt ăn kèm khoai tây chiên vàng",
                135000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SAVORY, TasteAttribute.CRISPY),
                true,
                4.8,
                142,
                R.drawable.food_ga_nuong_mat_ong
        ));

        list.add(new MenuItem(
                20,
                "Bò Né Hoa Thiên Lý Sốt Tiêu",
                "Bò né chảo gang xèo xèo cùng hoa thiên lý thơm nồng sốt tiêu đen thượng hạng",
                145000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SAVORY, TasteAttribute.SPICY),
                true,
                4.7,
                118,
                R.drawable.food_ga_nuong_mat_ong
        ));

        list.add(new MenuItem(
                21,
                "Sườn Heo Nướng Sốt BBQ Mật Ong",
                "Dẻ sườn heo nướng than hoa sốt BBQ mật ong óng ả mềm róc xương",
                130000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                true,
                4.8,
                156,
                R.drawable.food_ga_nuong_mat_ong
        ));

        list.add(new MenuItem(
                22,
                "Mực Trứng Nướng Muối Ớt",
                "Mực trứng Phan Thiết nướng muối ớt cay nồng đậm đà béo ngậy trứng mực",
                125000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SPICY, TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                true,
                4.8,
                134,
                R.drawable.food_tom_nuong_muoi_ot
        ));

        list.add(new MenuItem(
                23,
                "Mực Chiên Giòn Sốt Mayonnaise",
                "Khoanh mực tươi tẩm bột chiên xù vàng ruộm chấm sốt mayonnaise béo ngậy",
                110000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.CRISPY, TasteAttribute.SAVORY, TasteAttribute.FRIED),
                true,
                4.7,
                95,
                R.drawable.food_tom_chien_xu
        ));

        list.add(new MenuItem(
                24,
                "Vịt Quay Bắc Kinh Bánh Kẹp",
                "Vịt quay da giòn rụm bóng mượt ăn kèm bánh tráng kẹp dưa leo và đầu hành",
                190000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SAVORY, TasteAttribute.CRISPY),
                true,
                4.9,
                112,
                R.drawable.food_ga_quay_banh_bao
        ));

        list.add(new MenuItem(
                25,
                "Cá Lóc Nướng Trui Rơm Vàng",
                "Cá lóc đồng nướng trui rơm vàng thơm lừng cuốn bánh tráng rau rừng",
                140000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                true,
                4.7,
                86,
                R.drawable.food_ech_nuong_muoi_ot_la_chanh
        ));

        list.add(new MenuItem(
                26,
                "Giò Heo Hầm Nấm Đông Cô",
                "Giò heo hầm mềm rục cùng nấm đông cô và táo đỏ bổ dưỡng ngọt nước",
                165000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SAVORY, TasteAttribute.SWEET),
                true,
                4.8,
                74,
                R.drawable.food_chan_gio_muoi_chien_gion
        ));

        list.add(new MenuItem(
                27,
                "Cơm Chiên Hải Sản Hoàng Kim",
                "Cơm chiên hạt tơi vàng óng ánh với tôm, mực, trứng muối đậm đà mỹ vị",
                85000L,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SAVORY, TasteAttribute.CRISPY),
                true,
                4.8,
                195,
                R.drawable.food_tom_chien_xu
        ));

        list.add(new MenuItem(
                28,
                "Chả Giò Hải Sản Bắp Ngọt",
                "Chả giò cuốn tôm thịt và bắp ngọt giòn tan chấm sốt tương xí muội",
                50000L,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.CRISPY, TasteAttribute.SAVORY, TasteAttribute.FRIED),
                true,
                4.8,
                185,
                R.drawable.food_tom_chien_xu
        ));

        list.add(new MenuItem(
                29,
                "Khoai Tây Chiên Lắc Phô Mai",
                "Khoai tây chiên giòn rụm lắc bột phô mai béo ngọt thơm ngậy",
                40000L,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.CRISPY, TasteAttribute.SWEET, TasteAttribute.SAVORY),
                true,
                4.7,
                210,
                R.drawable.food_chan_gio_muoi_chien_gion
        ));

        list.add(new MenuItem(
                30,
                "Đậu Hũ Lướt Ván Giòn Tan",
                "Đậu hũ non chiên lướt ván ngoài giòn trong mềm chấm mắm tôm hoặc xì dầu",
                45000L,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.CRISPY, TasteAttribute.SAVORY),
                true,
                4.6,
                130,
                R.drawable.food_goi_ngo_sen
        ));

        list.add(new MenuItem(
                31,
                "Salad Ức Gà Dầu Giấm",
                "Xà lách tươi sạch trộn ức gà xé sốt dầu giấm thanh mát giải ngấy hữu hiệu",
                55000L,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.REFRESHING, TasteAttribute.SOUR, TasteAttribute.SAVORY),
                true,
                4.7,
                115,
                R.drawable.food_chan_ga_sa_tac
        ));

        list.add(new MenuItem(
                32,
                "Rau Muống Xào Tỏi Xanh Mướt",
                "Rau muống giòn ngọt xào tỏi đập dập thơm nức mũi món ăn dân dã",
                35000L,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.SAVORY, TasteAttribute.REFRESHING),
                true,
                4.6,
                260,
                R.drawable.food_goi_ngo_sen
        ));

        list.add(new MenuItem(
                33,
                "Trà Đào Cam Sả Hạt Chia",
                "Trà đào thơm nồng hòa quyện cam tươi và sả thanh mát kèm hạt chia bổ dưỡng",
                35000L,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.REFRESHING, TasteAttribute.SOUR),
                true,
                4.9,
                320,
                R.drawable.food_7up
        ));

        list.add(new MenuItem(
                34,
                "Trà Vải Hạt Sen Thanh Nhiệt",
                "Trà lài ướp quả vải mọng nước và hạt sen bùi béo thanh lọc cơ thể",
                38000L,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                true,
                4.8,
                230,
                R.drawable.food_7up
        ));

        list.add(new MenuItem(
                35,
                "Nước Ép Dưa Hấu Tươi",
                "Nước ép dưa hấu nguyên chất mát lạnh giải nhiệt mùa hè không đường hóa học",
                30000L,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                true,
                4.8,
                190,
                R.drawable.food_sting
        ));

        list.add(new MenuItem(
                36,
                "Cà Phê Sữa Đá Sài Gòn",
                "Cà phê Robusta pha phin đậm đặc hòa quyện sữa đặc béo ngậy đá mát lạnh",
                25000L,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.SAVORY),
                true,
                4.9,
                275,
                R.drawable.food_cocacola
        ));

        return list;
    }

    /**
     * Lịch sử 28 đơn hàng quy mô thực tế, đầy đủ các bàn, các phân khúc và trạng thái
     */
    public static List<Order> createInitialOrders(List<MenuItem> menuItems) {
        MenuItem defaultItem = (menuItems != null && !menuItems.isEmpty()) ? menuItems.get(0) : null;
        List<Order> list = new ArrayList<>();

        List<OrderItem> items1001 = new ArrayList<>();
        items1001.add(new OrderItem(1, findMenuItem(menuItems, 1, defaultItem), 2));
        items1001.add(new OrderItem(2, findMenuItem(menuItems, 12, defaultItem), 4));
        items1001.add(new OrderItem(3, findMenuItem(menuItems, 28, defaultItem), 2));
        items1001.add(new OrderItem(4, findMenuItem(menuItems, 27, defaultItem), 2));
        list.add(new Order(
                "#ORD-1001",
                "Bàn 01 - Tầng 1",
                1,
                items1001,
                570000L,
                OrderStatus.COMPLETED,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=570000&addInfo=#ORD-1001",
                1700003600000L
        ));

        List<OrderItem> items1002 = new ArrayList<>();
        items1002.add(new OrderItem(1, findMenuItem(menuItems, 3, defaultItem), 2));
        items1002.add(new OrderItem(2, findMenuItem(menuItems, 29, defaultItem), 3));
        items1002.add(new OrderItem(3, findMenuItem(menuItems, 13, defaultItem), 4));
        items1002.add(new OrderItem(4, findMenuItem(menuItems, 20, defaultItem), 1));
        list.add(new Order(
                "#ORD-1002",
                "Bàn 02 - Tầng 1",
                4,
                items1002,
                625000L,
                OrderStatus.COMPLETED,
                "Tiền mặt tại quầy",
                null,
                1700007200000L
        ));

        List<OrderItem> items1003 = new ArrayList<>();
        items1003.add(new OrderItem(1, findMenuItem(menuItems, 16, defaultItem), 2));
        items1003.add(new OrderItem(2, findMenuItem(menuItems, 7, defaultItem), 2));
        items1003.add(new OrderItem(3, findMenuItem(menuItems, 33, defaultItem), 4));
        items1003.add(new OrderItem(4, findMenuItem(menuItems, 21, defaultItem), 1));
        list.add(new Order(
                "#ORD-1003",
                "Bàn 05 - Tầng 1",
                3,
                items1003,
                750000L,
                OrderStatus.COMPLETED,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=750000&addInfo=#ORD-1003",
                1700010800000L
        ));

        List<OrderItem> items1004 = new ArrayList<>();
        items1004.add(new OrderItem(1, findMenuItem(menuItems, 18, defaultItem), 2));
        items1004.add(new OrderItem(2, findMenuItem(menuItems, 6, defaultItem), 4));
        items1004.add(new OrderItem(3, findMenuItem(menuItems, 11, defaultItem), 2));
        items1004.add(new OrderItem(4, findMenuItem(menuItems, 34, defaultItem), 6));
        items1004.add(new OrderItem(5, findMenuItem(menuItems, 24, defaultItem), 1));
        list.add(new Order(
                "#ORD-1004",
                "VIP 01 - Tầng 2",
                1,
                items1004,
                1558000L,
                OrderStatus.COMPLETED,
                "Chuyển khoản VietQR Napas247 (Mã VIP50K)",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=1558000&addInfo=#ORD-1004",
                1700014400000L
        ));

        List<OrderItem> items1005 = new ArrayList<>();
        items1005.add(new OrderItem(1, findMenuItem(menuItems, 17, defaultItem), 3));
        items1005.add(new OrderItem(2, findMenuItem(menuItems, 24, defaultItem), 2));
        items1005.add(new OrderItem(3, findMenuItem(menuItems, 21, defaultItem), 4));
        items1005.add(new OrderItem(4, findMenuItem(menuItems, 33, defaultItem), 8));
        items1005.add(new OrderItem(5, findMenuItem(menuItems, 27, defaultItem), 3));
        list.add(new Order(
                "#ORD-1005",
                "VIP 02 - Tầng 2",
                5,
                items1005,
                2155000L,
                OrderStatus.COMPLETED,
                "Thẻ tín dụng Visa/MasterCard POS",
                null,
                1700018000000L
        ));

        List<OrderItem> items1006 = new ArrayList<>();
        items1006.add(new OrderItem(1, findMenuItem(menuItems, 19, defaultItem), 3));
        items1006.add(new OrderItem(2, findMenuItem(menuItems, 27, defaultItem), 3));
        items1006.add(new OrderItem(3, findMenuItem(menuItems, 14, defaultItem), 6));
        items1006.add(new OrderItem(4, findMenuItem(menuItems, 28, defaultItem), 3));
        list.add(new Order(
                "#ORD-1006",
                "Bàn 04 - Tầng 1",
                6,
                items1006,
                900000L,
                OrderStatus.COMPLETED,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=900000&addInfo=#ORD-1006",
                1700021600000L
        ));

        List<OrderItem> items1007 = new ArrayList<>();
        items1007.add(new OrderItem(1, findMenuItem(menuItems, 2, defaultItem), 2));
        items1007.add(new OrderItem(2, findMenuItem(menuItems, 9, defaultItem), 2));
        items1007.add(new OrderItem(3, findMenuItem(menuItems, 15, defaultItem), 4));
        items1007.add(new OrderItem(4, findMenuItem(menuItems, 29, defaultItem), 2));
        list.add(new Order(
                "#ORD-1007",
                "Bàn 07 - Tầng 1",
                8,
                items1007,
                420000L,
                OrderStatus.COMPLETED,
                "Tiền mặt tại quầy",
                null,
                1700025200000L
        ));

        List<OrderItem> items1008 = new ArrayList<>();
        items1008.add(new OrderItem(1, findMenuItem(menuItems, 8, defaultItem), 3));
        items1008.add(new OrderItem(2, findMenuItem(menuItems, 29, defaultItem), 2));
        items1008.add(new OrderItem(3, findMenuItem(menuItems, 12, defaultItem), 4));
        items1008.add(new OrderItem(4, findMenuItem(menuItems, 23, defaultItem), 1));
        list.add(new Order(
                "#ORD-1008",
                "Bàn 03 - Tầng 1",
                9,
                items1008,
                445000L,
                OrderStatus.COMPLETED,
                "Tiền mặt tại quầy (Mã CHIEUTHU3)",
                null,
                1700028800000L
        ));

        List<OrderItem> items1009 = new ArrayList<>();
        items1009.add(new OrderItem(1, findMenuItem(menuItems, 4, defaultItem), 2));
        items1009.add(new OrderItem(2, findMenuItem(menuItems, 22, defaultItem), 2));
        items1009.add(new OrderItem(3, findMenuItem(menuItems, 36, defaultItem), 4));
        items1009.add(new OrderItem(4, findMenuItem(menuItems, 31, defaultItem), 2));
        list.add(new Order(
                "#ORD-1009",
                "Bàn 12 - Tầng 2",
                2,
                items1009,
                780000L,
                OrderStatus.COMPLETED,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=780000&addInfo=#ORD-1009",
                1700032400000L
        ));

        List<OrderItem> items1010 = new ArrayList<>();
        items1010.add(new OrderItem(1, findMenuItem(menuItems, 18, defaultItem), 2));
        items1010.add(new OrderItem(2, findMenuItem(menuItems, 5, defaultItem), 4));
        items1010.add(new OrderItem(3, findMenuItem(menuItems, 32, defaultItem), 2));
        items1010.add(new OrderItem(4, findMenuItem(menuItems, 13, defaultItem), 6));
        items1010.add(new OrderItem(5, findMenuItem(menuItems, 20, defaultItem), 2));
        list.add(new Order(
                "#ORD-1010",
                "Bàn 10 - Tầng 2",
                7,
                items1010,
                1350000L,
                OrderStatus.COMPLETED,
                "Thẻ tín dụng Visa/MasterCard POS",
                null,
                1700036000000L
        ));

        List<OrderItem> items1011 = new ArrayList<>();
        items1011.add(new OrderItem(1, findMenuItem(menuItems, 1, defaultItem), 2));
        items1011.add(new OrderItem(2, findMenuItem(menuItems, 30, defaultItem), 2));
        items1011.add(new OrderItem(3, findMenuItem(menuItems, 35, defaultItem), 2));
        items1011.add(new OrderItem(4, findMenuItem(menuItems, 28, defaultItem), 2));
        list.add(new Order(
                "#ORD-1011",
                "Bàn 08 - Tầng 1",
                10,
                items1011,
                490000L,
                OrderStatus.COMPLETED,
                "Tiền mặt tại quầy",
                null,
                1700039600000L
        ));

        List<OrderItem> items1012 = new ArrayList<>();
        items1012.add(new OrderItem(1, findMenuItem(menuItems, 20, defaultItem), 2));
        items1012.add(new OrderItem(2, findMenuItem(menuItems, 27, defaultItem), 2));
        items1012.add(new OrderItem(3, findMenuItem(menuItems, 33, defaultItem), 4));
        items1012.add(new OrderItem(4, findMenuItem(menuItems, 7, defaultItem), 2));
        list.add(new Order(
                "#ORD-1012",
                "Bàn 14 - Tầng 2",
                11,
                items1012,
                720000L,
                OrderStatus.COMPLETED,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=720000&addInfo=#ORD-1012",
                1700043200000L
        ));

        List<OrderItem> items1013 = new ArrayList<>();
        items1013.add(new OrderItem(1, findMenuItem(menuItems, 17, defaultItem), 2));
        items1013.add(new OrderItem(2, findMenuItem(menuItems, 21, defaultItem), 4));
        items1013.add(new OrderItem(3, findMenuItem(menuItems, 6, defaultItem), 4));
        items1013.add(new OrderItem(4, findMenuItem(menuItems, 34, defaultItem), 8));
        items1013.add(new OrderItem(5, findMenuItem(menuItems, 26, defaultItem), 2));
        list.add(new Order(
                "#ORD-1013",
                "VIP 03 - Tầng 2",
                12,
                items1013,
                2094000L,
                OrderStatus.COMPLETED,
                "Chuyển khoản VietQR Napas247 (Mã VIP50K)",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=2094000&addInfo=#ORD-1013",
                1700046800000L
        ));

        List<OrderItem> items1014 = new ArrayList<>();
        items1014.add(new OrderItem(1, findMenuItem(menuItems, 3, defaultItem), 2));
        items1014.add(new OrderItem(2, findMenuItem(menuItems, 31, defaultItem), 2));
        items1014.add(new OrderItem(3, findMenuItem(menuItems, 36, defaultItem), 2));
        items1014.add(new OrderItem(4, findMenuItem(menuItems, 25, defaultItem), 1));
        list.add(new Order(
                "#ORD-1014",
                "Bàn 06 - Tầng 1",
                13,
                items1014,
                600000L,
                OrderStatus.COMPLETED,
                "Tiền mặt tại quầy",
                null,
                1700050400000L
        ));

        List<OrderItem> items1015 = new ArrayList<>();
        items1015.add(new OrderItem(1, findMenuItem(menuItems, 24, defaultItem), 2));
        items1015.add(new OrderItem(2, findMenuItem(menuItems, 26, defaultItem), 2));
        items1015.add(new OrderItem(3, findMenuItem(menuItems, 11, defaultItem), 2));
        items1015.add(new OrderItem(4, findMenuItem(menuItems, 33, defaultItem), 6));
        items1015.add(new OrderItem(5, findMenuItem(menuItems, 18, defaultItem), 1));
        list.add(new Order(
                "#ORD-1015",
                "Bàn 15 - Tầng 2",
                14,
                items1015,
                1340000L,
                OrderStatus.COMPLETED,
                "Thẻ tín dụng Visa/MasterCard POS",
                null,
                1700054000000L
        ));

        List<OrderItem> items1016 = new ArrayList<>();
        items1016.add(new OrderItem(1, findMenuItem(menuItems, 5, defaultItem), 3));
        items1016.add(new OrderItem(2, findMenuItem(menuItems, 8, defaultItem), 2));
        items1016.add(new OrderItem(3, findMenuItem(menuItems, 12, defaultItem), 4));
        items1016.add(new OrderItem(4, findMenuItem(menuItems, 27, defaultItem), 2));
        list.add(new Order(
                "#ORD-1016",
                "Bàn 01 - Tầng 1",
                1,
                items1016,
                645000L,
                OrderStatus.COMPLETED,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=645000&addInfo=#ORD-1016",
                1700057600000L
        ));

        List<OrderItem> items1017 = new ArrayList<>();
        items1017.add(new OrderItem(1, findMenuItem(menuItems, 16, defaultItem), 2));
        items1017.add(new OrderItem(2, findMenuItem(menuItems, 28, defaultItem), 3));
        items1017.add(new OrderItem(3, findMenuItem(menuItems, 15, defaultItem), 4));
        items1017.add(new OrderItem(4, findMenuItem(menuItems, 22, defaultItem), 1));
        list.add(new Order(
                "#ORD-1017",
                "Bàn 05 - Tầng 1",
                3,
                items1017,
                695000L,
                OrderStatus.COMPLETED,
                "Tiền mặt tại quầy",
                null,
                1700061200000L
        ));

        List<OrderItem> items1018 = new ArrayList<>();
        items1018.add(new OrderItem(1, findMenuItem(menuItems, 19, defaultItem), 2));
        items1018.add(new OrderItem(2, findMenuItem(menuItems, 22, defaultItem), 2));
        items1018.add(new OrderItem(3, findMenuItem(menuItems, 34, defaultItem), 4));
        items1018.add(new OrderItem(4, findMenuItem(menuItems, 10, defaultItem), 2));
        list.add(new Order(
                "#ORD-1018",
                "Bàn 12 - Tầng 2",
                2,
                items1018,
                812000L,
                OrderStatus.COMPLETED,
                "Chuyển khoản VietQR Napas247 (Mã CHIEUTHU3)",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=812000&addInfo=#ORD-1018",
                1700064800000L
        ));

        List<OrderItem> items1019 = new ArrayList<>();
        items1019.add(new OrderItem(1, findMenuItem(menuItems, 4, defaultItem), 1));
        items1019.add(new OrderItem(2, findMenuItem(menuItems, 29, defaultItem), 2));
        items1019.add(new OrderItem(3, findMenuItem(menuItems, 13, defaultItem), 3));
        items1019.add(new OrderItem(4, findMenuItem(menuItems, 28, defaultItem), 1));
        list.add(new Order(
                "#ORD-1019",
                "Bàn 02 - Tầng 1",
                4,
                items1019,
                335000L,
                OrderStatus.PREPARING,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=335000&addInfo=#ORD-1019",
                1700068400000L
        ));

        List<OrderItem> items1020 = new ArrayList<>();
        items1020.add(new OrderItem(1, findMenuItem(menuItems, 18, defaultItem), 1));
        items1020.add(new OrderItem(2, findMenuItem(menuItems, 6, defaultItem), 2));
        items1020.add(new OrderItem(3, findMenuItem(menuItems, 10, defaultItem), 1));
        items1020.add(new OrderItem(4, findMenuItem(menuItems, 33, defaultItem), 3));
        list.add(new Order(
                "#ORD-1020",
                "VIP 01 - Tầng 2",
                1,
                items1020,
                665000L,
                OrderStatus.PREPARING,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=665000&addInfo=#ORD-1020",
                1700072000000L
        ));

        List<OrderItem> items1021 = new ArrayList<>();
        items1021.add(new OrderItem(1, findMenuItem(menuItems, 21, defaultItem), 2));
        items1021.add(new OrderItem(2, findMenuItem(menuItems, 27, defaultItem), 1));
        items1021.add(new OrderItem(3, findMenuItem(menuItems, 35, defaultItem), 2));
        list.add(new Order(
                "#ORD-1021",
                "Bàn 07 - Tầng 1",
                8,
                items1021,
                405000L,
                OrderStatus.PREPARING,
                "Tiền mặt tại quầy",
                null,
                1700075600000L
        ));

        List<OrderItem> items1022 = new ArrayList<>();
        items1022.add(new OrderItem(1, findMenuItem(menuItems, 20, defaultItem), 1));
        items1022.add(new OrderItem(2, findMenuItem(menuItems, 23, defaultItem), 2));
        items1022.add(new OrderItem(3, findMenuItem(menuItems, 14, defaultItem), 3));
        list.add(new Order(
                "#ORD-1022",
                "Bàn 04 - Tầng 1",
                6,
                items1022,
                410000L,
                OrderStatus.PREPARING,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=410000&addInfo=#ORD-1022",
                1700079200000L
        ));

        List<OrderItem> items1023 = new ArrayList<>();
        items1023.add(new OrderItem(1, findMenuItem(menuItems, 1, defaultItem), 1));
        items1023.add(new OrderItem(2, findMenuItem(menuItems, 7, defaultItem), 2));
        items1023.add(new OrderItem(3, findMenuItem(menuItems, 12, defaultItem), 3));
        list.add(new Order(
                "#ORD-1023",
                "Bàn 09 - Tầng 1",
                10,
                items1023,
                285000L,
                OrderStatus.PREPARING,
                "Tiền mặt tại quầy",
                null,
                1700082800000L
        ));

        List<OrderItem> items1024 = new ArrayList<>();
        items1024.add(new OrderItem(1, findMenuItem(menuItems, 25, defaultItem), 1));
        items1024.add(new OrderItem(2, findMenuItem(menuItems, 32, defaultItem), 2));
        items1024.add(new OrderItem(3, findMenuItem(menuItems, 36, defaultItem), 3));
        list.add(new Order(
                "#ORD-1024",
                "Bàn 10 - Tầng 2",
                7,
                items1024,
                285000L,
                OrderStatus.PREPARING,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=285000&addInfo=#ORD-1024",
                1700086400000L
        ));

        List<OrderItem> items1025 = new ArrayList<>();
        items1025.add(new OrderItem(1, findMenuItem(menuItems, 2, defaultItem), 1));
        items1025.add(new OrderItem(2, findMenuItem(menuItems, 28, defaultItem), 2));
        items1025.add(new OrderItem(3, findMenuItem(menuItems, 15, defaultItem), 2));
        list.add(new Order(
                "#ORD-1025",
                "Bàn 03 - Tầng 1",
                9,
                items1025,
                215000L,
                OrderStatus.PENDING,
                "Tiền mặt tại quầy",
                null,
                1700090000000L
        ));

        List<OrderItem> items1026 = new ArrayList<>();
        items1026.add(new OrderItem(1, findMenuItem(menuItems, 16, defaultItem), 1));
        items1026.add(new OrderItem(2, findMenuItem(menuItems, 9, defaultItem), 2));
        items1026.add(new OrderItem(3, findMenuItem(menuItems, 33, defaultItem), 3));
        list.add(new Order(
                "#ORD-1026",
                "Bàn 14 - Tầng 2",
                11,
                items1026,
                395000L,
                OrderStatus.PENDING,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=395000&addInfo=#ORD-1026",
                1700093600000L
        ));

        List<OrderItem> items1027 = new ArrayList<>();
        items1027.add(new OrderItem(1, findMenuItem(menuItems, 17, defaultItem), 2));
        items1027.add(new OrderItem(2, findMenuItem(menuItems, 6, defaultItem), 3));
        items1027.add(new OrderItem(3, findMenuItem(menuItems, 24, defaultItem), 1));
        items1027.add(new OrderItem(4, findMenuItem(menuItems, 34, defaultItem), 5));
        list.add(new Order(
                "#ORD-1027",
                "VIP 02 - Tầng 2",
                5,
                items1027,
                1205000L,
                OrderStatus.PENDING,
                "Chuyển khoản VietQR Napas247",
                "https://img.vietqr.io/image/970422-0901234567-compact.png?amount=1205000&addInfo=#ORD-1027",
                1700097200000L
        ));

        List<OrderItem> items1028 = new ArrayList<>();
        items1028.add(new OrderItem(1, findMenuItem(menuItems, 3, defaultItem), 1));
        items1028.add(new OrderItem(2, findMenuItem(menuItems, 13, defaultItem), 2));
        list.add(new Order(
                "#ORD-1028",
                "Bàn 06 - Tầng 1",
                13,
                items1028,
                180000L,
                OrderStatus.CANCELLED,
                "Tiền mặt tại quầy",
                null,
                1700100800000L
        ));

        return list;
    }

    private static MenuItem findMenuItem(List<MenuItem> list, int id, MenuItem fallback) {
        if (list != null) {
            for (MenuItem item : list) {
                if (item.getId() == id) {
                    return item;
                }
            }
        }
        return fallback;
    }
}
