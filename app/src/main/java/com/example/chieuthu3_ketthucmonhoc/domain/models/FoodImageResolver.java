package com.example.chieuthu3_ketthucmonhoc.domain.models;

import com.example.chieuthu3_ketthucmonhoc.R;

public final class FoodImageResolver {

    private FoodImageResolver() {
        // Utility class
    }

    public static int resolveImage(MenuItem item) {
        if (item == null) return R.drawable.ic_food_placeholder;
        if (item.getImageResId() != 0) return item.getImageResId();
        return resolveByName(item.getName(), item.getCategory());
    }

    public static int resolveByName(String name) {
        return resolveByName(name, null);
    }

    public static int resolveByName(String name, FoodCategory category) {
        if (name == null) {
            return fallbackCategory(category);
        }
        String lower = name.toLowerCase().trim();

        // Món chính
        if (lower.contains("chân giò") || lower.contains("giò heo")) {
            return R.drawable.food_chan_gio_muoi_chien_gion;
        } else if (lower.contains("ếch")) {
            return R.drawable.food_ech_nuong_muoi_ot_la_chanh;
        } else if (lower.contains("gà quay") || lower.contains("bánh bao")) {
            return R.drawable.food_ga_quay_banh_bao;
        } else if (lower.contains("gà nướng") || lower.contains("mật ong")) {
            return R.drawable.food_ga_nuong_mat_ong;
        } else if (lower.contains("lẩu") || lower.contains("lá giang")) {
            return R.drawable.food_lau_ga_la_giang;
        } else if (lower.contains("tôm chiên") || lower.contains("chiên xù")) {
            return R.drawable.food_tom_chien_xu;
        } else if (lower.contains("tôm nướng") || (lower.contains("tôm") && lower.contains("muối ớt"))) {
            return R.drawable.food_tom_nuong_muoi_ot;
        }

        // Món phụ
        else if (lower.contains("sả tắc")) {
            return R.drawable.food_chan_ga_sa_tac;
        } else if (lower.contains("sốt thái")) {
            return R.drawable.food_chan_ga_sot_thai;
        } else if (lower.contains("ngó sen")) {
            return R.drawable.food_goi_ngo_sen;
        } else if (lower.contains("tôm khô")) {
            return R.drawable.food_goi_xoai_tom_kho;
        } else if (lower.contains("tôm thịt") || lower.contains("gỏi xoài")) {
            return R.drawable.food_goi_xoai_tom_thit;
        }

        // Nước uống
        else if (lower.contains("7up") || lower.contains("bảy up")) {
            return R.drawable.food_7up;
        } else if (lower.contains("coca")) {
            return R.drawable.food_cocacola;
        } else if (lower.contains("pepsi")) {
            return R.drawable.food_pepsi;
        } else if (lower.contains("sting")) {
            return R.drawable.food_sting;
        }

        return fallbackCategory(category);
    }

    private static int fallbackCategory(FoodCategory category) {
        if (category == FoodCategory.MAIN_DISH) {
            return R.drawable.food_ga_nuong_mat_ong;
        } else if (category == FoodCategory.SIDE_DISH) {
            return R.drawable.food_chan_ga_sa_tac;
        } else if (category == FoodCategory.BEVERAGE) {
            return R.drawable.food_cocacola;
        }
        return R.drawable.ic_food_placeholder;
    }
}
