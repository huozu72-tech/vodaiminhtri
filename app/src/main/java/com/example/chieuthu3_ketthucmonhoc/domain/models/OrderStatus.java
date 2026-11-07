package com.example.chieuthu3_ketthucmonhoc.domain.models;

public enum OrderStatus {
    PENDING("Đang Chờ", "#F59E0B"),       // Amber
    PREPARING("Đang Chế Biến", "#3B82F6"), // Blue
    COMPLETED("Hoàn Thành", "#10B981"),   // Emerald
    CANCELLED("Đã Hủy", "#EF4444");       // Red

    private final String displayName;
    private final String colorHex;

    OrderStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static OrderStatus fromDisplayName(String name) {
        if (name == null) return PENDING;
        for (OrderStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(name.trim())) {
                return status;
            }
        }
        return PENDING;
    }
}
