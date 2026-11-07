package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.util.ArrayList;
import java.util.List;

public enum TasteAttribute {
    SPICY("cay"),
    SWEET("ngọt"),
    CRISPY("giòn"),
    SAVORY("đậm đà"),
    SOUR("chua"),
    REFRESHING("thanh mát"),
    GRILLED("nướng"),
    FRIED("chiên");

    private final String tag;

    TasteAttribute(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static List<TasteAttribute> parseFromText(String text) {
        List<TasteAttribute> result = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }
        String lower = text.toLowerCase();
        for (TasteAttribute attr : values()) {
            if (lower.contains(attr.tag)) {
                result.add(attr);
            }
        }
        return result;
    }
}
