package com.example.chieuthu3_ketthucmonhoc.domain.models

import com.example.chieuthu3_ketthucmonhoc.R

/**
 * Bộ ánh xạ và giải quyết hình ảnh món ăn.
 * Ưu tiên:
 * 1. imageResId hợp lệ đã lưu trên MenuItem.
 * 2. Tìm kiếm theo từ khóa tên món tương ứng với kho hình ảnh trong res/drawable.
 * 3. Fallback theo danh mục món ăn (FoodCategory).
 * 4. Placeholder mặc định nếu không khớp.
 */
object FoodImageResolver {

    fun resolveImage(item: MenuItem): Int {
        if (item.imageResId != 0) return item.imageResId
        return resolveByName(item.name, item.category)
    }

    fun resolveByName(name: String, category: FoodCategory? = null): Int {
        val lower = name.lowercase().trim()
        return when {
            // Món chính
            lower.contains("chân giò") || lower.contains("giò heo") -> R.drawable.food_chan_gio_muoi_chien_gion
            lower.contains("ếch") -> R.drawable.food_ech_nuong_muoi_ot_la_chanh
            lower.contains("gà quay") || lower.contains("bánh bao") -> R.drawable.food_ga_quay_banh_bao
            lower.contains("gà nướng") || lower.contains("mật ong") -> R.drawable.food_ga_nuong_mat_ong
            lower.contains("lẩu") || lower.contains("lá giang") -> R.drawable.food_lau_ga_la_giang
            lower.contains("tôm chiên") || lower.contains("chiên xù") -> R.drawable.food_tom_chien_xu
            lower.contains("tôm nướng") || (lower.contains("tôm") && lower.contains("muối ớt")) -> R.drawable.food_tom_nuong_muoi_ot

            // Món phụ
            lower.contains("sả tắc") -> R.drawable.food_chan_ga_sa_tac
            lower.contains("sốt thái") -> R.drawable.food_chan_ga_sot_thai
            lower.contains("ngó sen") -> R.drawable.food_goi_ngo_sen
            lower.contains("tôm khô") -> R.drawable.food_goi_xoai_tom_kho
            lower.contains("tôm thịt") || lower.contains("gỏi xoài") -> R.drawable.food_goi_xoai_tom_thit

            // Nước uống
            lower.contains("7up") || lower.contains("bảy up") -> R.drawable.food_7up
            lower.contains("coca") -> R.drawable.food_cocacola
            lower.contains("pepsi") -> R.drawable.food_pepsi
            lower.contains("sting") -> R.drawable.food_sting

            // Fallback theo nhóm món
            category == FoodCategory.MAIN_DISH -> R.drawable.food_ga_nuong_mat_ong
            category == FoodCategory.SIDE_DISH -> R.drawable.food_chan_ga_sa_tac
            category == FoodCategory.BEVERAGE -> R.drawable.food_cocacola
            else -> R.drawable.ic_food_placeholder
        }
    }
}
