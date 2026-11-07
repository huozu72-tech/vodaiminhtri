package com.example.chieuthu3_ketthucmonhoc.domain.models;

public enum FoodCategory {
    MAIN_DISH("Món chính"),
    SIDE_DISH("Món phụ"),
    BEVERAGE("Nước uống");

    private final String displayName;

    FoodCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FoodCategory fromDisplayName(String name) {
        if (name == null) return MAIN_DISH;
        for (FoodCategory category : values()) {
            if (category.displayName.equalsIgnoreCase(name.trim())) {
                return category;
            }
        }
        return MAIN_DISH;
    }
}
