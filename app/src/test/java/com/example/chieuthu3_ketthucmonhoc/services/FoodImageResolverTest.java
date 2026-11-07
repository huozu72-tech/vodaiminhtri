package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.R;
import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodImageResolver;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FoodImageResolverTest {

    @Test
    public void everyInitialMenuItemHasValidNonZeroImageResId() {
        List<MenuItem> items = InitialDataFactory.createInitialMenuItems();
        assertTrue("Thực đơn ban đầu phải có món", !items.isEmpty());

        for (MenuItem item : items) {
            assertTrue("Món '" + item.getName() + "' phải có imageResId > 0", item.getImageResId() > 0);
            int resolved = FoodImageResolver.resolveImage(item);
            assertEquals("Ảnh giải quyết phải khớp imageResId của món", item.getImageResId(), resolved);
        }
    }

    @Test
    public void resolveByNameCorrectlyMapsDishKeywordsWhenImageResIdIsZero() {
        MenuItem dummyItem = new MenuItem(
                999,
                "Chân Giò Hầm Muối Ớt",
                "Mô tả",
                100000L,
                FoodCategory.MAIN_DISH
        );
        int resolved = FoodImageResolver.resolveImage(dummyItem);
        assertEquals(R.drawable.food_chan_gio_muoi_chien_gion, resolved);

        assertEquals(R.drawable.food_ech_nuong_muoi_ot_la_chanh, FoodImageResolver.resolveByName("Ếch Chiên Bơ"));
        assertEquals(R.drawable.food_ga_nuong_mat_ong, FoodImageResolver.resolveByName("Gà Nướng Mật Ong Rừng"));
        assertEquals(R.drawable.food_ga_quay_banh_bao, FoodImageResolver.resolveByName("Gà Quay Ăn Kèm Bánh Bao"));
        assertEquals(R.drawable.food_lau_ga_la_giang, FoodImageResolver.resolveByName("Lẩu Gà Nấu Lá Giang"));
        assertEquals(R.drawable.food_tom_chien_xu, FoodImageResolver.resolveByName("Tôm Chiên Xù Giòn Rụm"));
        assertEquals(R.drawable.food_tom_nuong_muoi_ot, FoodImageResolver.resolveByName("Tôm Sú Nướng Muối Ớt"));
        assertEquals(R.drawable.food_chan_ga_sa_tac, FoodImageResolver.resolveByName("Chân Gà Ngâm Sả Tắc"));
        assertEquals(R.drawable.food_chan_ga_sot_thai, FoodImageResolver.resolveByName("Chân Gà Rút Xương Sốt Thái"));
        assertEquals(R.drawable.food_goi_ngo_sen, FoodImageResolver.resolveByName("Gỏi Ngó Sen Tôm Thịt"));
        assertEquals(R.drawable.food_goi_xoai_tom_kho, FoodImageResolver.resolveByName("Gỏi Xoài Xanh Tôm Khô"));
        assertEquals(R.drawable.food_7up, FoodImageResolver.resolveByName("Lon 7Up 330ml"));
        assertEquals(R.drawable.food_cocacola, FoodImageResolver.resolveByName("Coca Cola Tươi"));
        assertEquals(R.drawable.food_pepsi, FoodImageResolver.resolveByName("Pepsi Không Calo"));
        assertEquals(R.drawable.food_sting, FoodImageResolver.resolveByName("Sting Dâu Đỏ"));
    }

    @Test
    public void resolveByNameFallsBackCleanlyForUnknownDishes() {
        int fallbackMain = FoodImageResolver.resolveByName("Món Lạ Chưa Từng Thấy", FoodCategory.MAIN_DISH);
        assertEquals(R.drawable.food_ga_nuong_mat_ong, fallbackMain);

        int fallbackSide = FoodImageResolver.resolveByName("Món Phụ Mới", FoodCategory.SIDE_DISH);
        assertEquals(R.drawable.food_chan_ga_sa_tac, fallbackSide);

        int fallbackDrink = FoodImageResolver.resolveByName("Nước Uống Mới", FoodCategory.BEVERAGE);
        assertEquals(R.drawable.food_cocacola, fallbackDrink);
    }
}
