package com.example.chieuthu3_ketthucmonhoc.domain.models;

/**
 * Trạng thái bàn ăn trong nhà hàng.
 */
public enum TableStatus {
    AVAILABLE("Trống • Sẵn sàng", "#2E7D32"),
    OCCUPIED("Đang có khách", "#C62828"),
    RESERVED("Đã đặt trước", "#EF6C00");

    private final String displayName;
    private final String colorHex;

    TableStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }
}
