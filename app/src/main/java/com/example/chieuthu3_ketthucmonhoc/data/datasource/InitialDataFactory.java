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

public final class InitialDataFactory {

    private InitialDataFactory() {
        // Utility factory
    }

    public static List<AdminAccount> createInitialAdmins() {
        List<AdminAccount> list = new ArrayList<>();
        list.add(new AdminAccount(
                1,
                "admin",
                SecurityHelper.hashPassword("admin123"),
                "Quản Trị Viên",
                UserRole.SUPER_ADMIN
        ));
        list.add(new AdminAccount(
                2,
                "0900000000",
                SecurityHelper.hashPassword("admin123"),
                "Quản Trị Viên (SĐT)",
                UserRole.SUPER_ADMIN
        ));
        list.add(new AdminAccount(
                3,
                "manager",
                SecurityHelper.hashPassword("manager123"),
                "Quản Lý Nhà Hàng",
                UserRole.MANAGER
        ));
        return list;
    }

    public static List<Customer> createInitialCustomers() {
        List<Customer> list = new ArrayList<>();
        list.add(new Customer(
                1,
                "Khách Hàng Thân Thiết",
                "khachhang@restaurant.com",
                "0901234567",
                SecurityHelper.hashPassword("123456"),
                "Bàn 12 - Khu Vực A",
                System.currentTimeMillis()
        ));
        return list;
    }

    public static List<MenuItem> createInitialMenuItems() {
        List<MenuItem> list = new ArrayList<>();

        list.add(new MenuItem(
                1,
                "Chân Giò Muối Chiên Giòn",
                "Chân Giò Muối Chiên Giòn da giòn rụm, thịt mềm thơm đậm đà hương vị",
                120000,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.CRISPY, TasteAttribute.SAVORY, TasteAttribute.FRIED),
                true,
                4.9,
                45,
                R.drawable.food_chan_gio_muoi_chien_gion
        ));

        list.add(new MenuItem(
                2,
                "Ếch Nướng Muối Ớt Lá Chanh",
                "Ếch đồng tươi nướng muối ớt cay nồng thơm lừng hương lá chanh",
                85000,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SPICY, TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                true,
                4.7,
                38,
                R.drawable.food_ech_nuong_muoi_ot_la_chanh
        ));

        list.add(new MenuItem(
                3,
                "Gà Nướng Mật Ong",
                "Gà ta ướp mật ong hoa rừng nướng vàng óng, thịt ngọt thơm đậm vị",
                150000,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                true,
                4.9,
                62,
                R.drawable.food_ga_nuong_mat_ong
        ));

        list.add(new MenuItem(
                4,
                "Gà Quay Bánh Bao",
                "Gà quay thơm lừng ăn kèm bánh bao chiên vàng giòn rụm",
                160000,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SAVORY, TasteAttribute.CRISPY),
                true,
                4.8,
                50,
                R.drawable.food_ga_quay_banh_bao
        ));

        list.add(new MenuItem(
                5,
                "Tôm Chiên Xù",
                "Tôm sú tươi tẩm bột chiên xù vàng ruộm, chấm kèm sốt tương ớt mayonnaise",
                95000,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.CRISPY, TasteAttribute.SAVORY, TasteAttribute.FRIED),
                true,
                4.7,
                41,
                R.drawable.food_tom_chien_xu
        ));

        list.add(new MenuItem(
                6,
                "Tôm Nướng Muối Ớt",
                "Tôm sú nướng muối ớt cay xè, giữ trọn vị ngọt tự nhiên của tôm biển",
                105000,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SPICY, TasteAttribute.SAVORY, TasteAttribute.GRILLED),
                true,
                4.8,
                53,
                R.drawable.food_tom_nuong_muoi_ot
        ));

        list.add(new MenuItem(
                7,
                "Chân Gà Sả Tắc",
                "Chân gà rút xương ngâm sả tắc chua cay thanh mát giòn sần sật",
                60000,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SPICY, TasteAttribute.REFRESHING, TasteAttribute.CRISPY),
                true,
                4.8,
                70,
                R.drawable.food_chan_ga_sa_tac
        ));

        list.add(new MenuItem(
                8,
                "Chân Gà Sốt Thái",
                "Chân gà ngập sốt Thái chua cay đậm vị thơm mùi xoài non và cóc non",
                65000,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.SPICY, TasteAttribute.SOUR, TasteAttribute.SWEET),
                true,
                4.9,
                82,
                R.drawable.food_chan_ga_sot_thai
        ));

        list.add(new MenuItem(
                9,
                "Gỏi Ngó Sen",
                "Gỏi ngó sen tôm thịt thanh đạm giải ngấy, chua ngọt vừa miệng",
                55000,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SWEET, TasteAttribute.REFRESHING, TasteAttribute.CRISPY),
                true,
                4.6,
                33,
                R.drawable.food_goi_ngo_sen
        ));

        list.add(new MenuItem(
                10,
                "Gỏi Xoài Tôm Khô",
                "Xoài xanh băm sợi trộn tôm khô thơm bùi nước mắm chua cay hấp dẫn",
                70000,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SPICY, TasteAttribute.CRISPY),
                true,
                4.7,
                29,
                R.drawable.food_goi_xoai_tom_kho
        ));

        list.add(new MenuItem(
                11,
                "Gỏi Xoài Tôm Thịt",
                "Gỏi xoài tôm thịt đầy đặn, rắc đậu phộng rang giòn thơm nức mũi",
                80000,
                FoodCategory.SIDE_DISH,
                Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SPICY, TasteAttribute.SAVORY),
                true,
                4.8,
                44,
                R.drawable.food_goi_xoai_tom_thit
        ));

        list.add(new MenuItem(
                12,
                "7Up",
                "Nước giải khát 7Up có ga hương chanh thanh mát sảng khoái",
                15000,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                true,
                4.9,
                110,
                R.drawable.food_7up
        ));

        list.add(new MenuItem(
                13,
                "Coca Cola",
                "Nước ngọt có ga Coca Cola mát lạnh giải nhiệt tức thì",
                15000,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                true,
                4.9,
                135,
                R.drawable.food_cocacola
        ));

        list.add(new MenuItem(
                14,
                "Pepsi",
                "Nước giải khát Pepsi mát lạnh sảng khoái kích thích vị giác",
                15000,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                true,
                4.8,
                98,
                R.drawable.food_pepsi
        ));

        list.add(new MenuItem(
                15,
                "Sting",
                "Nước tăng lực Sting dâu thơm ngon tiếp thêm năng lượng",
                15000,
                FoodCategory.BEVERAGE,
                Arrays.asList(TasteAttribute.SWEET, TasteAttribute.REFRESHING),
                true,
                4.8,
                88,
                R.drawable.food_sting
        ));

        list.add(new MenuItem(
                16,
                "Lẩu Gà Lá Giang",
                "Lẩu gà lá giang chua cay đậm đà, thịt gà ta dai ngon nước lẩu thanh tao",
                180000,
                FoodCategory.MAIN_DISH,
                Arrays.asList(TasteAttribute.SOUR, TasteAttribute.SAVORY, TasteAttribute.SPICY),
                true,
                4.9,
                65,
                R.drawable.food_lau_ga_la_giang
        ));

        return list;
    }

    public static List<Order> createInitialOrders(List<MenuItem> menuItems) {
        MenuItem defaultItem = (menuItems != null && !menuItems.isEmpty()) ? menuItems.get(0) : null;

        List<Order> list = new ArrayList<>();

        List<OrderItem> items1 = new ArrayList<>();
        items1.add(new OrderItem(1, findMenuItem(menuItems, 1, defaultItem), 1));
        items1.add(new OrderItem(2, findMenuItem(menuItems, 12, defaultItem), 2));
        list.add(new Order(
                "#ORD-1024",
                "Bàn 12",
                1,
                items1,
                150000,
                OrderStatus.PENDING,
                "Chuyển khoản QR",
                null,
                System.currentTimeMillis() - 3600000
        ));

        List<OrderItem> items2 = new ArrayList<>();
        items2.add(new OrderItem(3, findMenuItem(menuItems, 3, defaultItem), 1));
        items2.add(new OrderItem(4, findMenuItem(menuItems, 13, defaultItem), 1));
        list.add(new Order(
                "#ORD-1025",
                "Bàn 05",
                1,
                items2,
                165000,
                OrderStatus.PREPARING,
                "Chuyển khoản QR",
                null,
                System.currentTimeMillis() - 1800000
        ));

        List<OrderItem> items3 = new ArrayList<>();
        items3.add(new OrderItem(5, findMenuItem(menuItems, 5, defaultItem), 1));
        items3.add(new OrderItem(6, findMenuItem(menuItems, 15, defaultItem), 2));
        list.add(new Order(
                "#ORD-1023",
                "Bàn 02",
                1,
                items3,
                125000,
                OrderStatus.COMPLETED,
                "Chuyển khoản QR",
                null,
                System.currentTimeMillis() - 7200000
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
