package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.util.Objects;

/**
 * Mô hình Mã Giảm Giá / Voucher khuyến mãi của nhà hàng.
 */
public final class PromoVoucher {
    private final String code;
    private final String title;
    private final String description;
    private final int discountPercent;
    private final long maxDiscountAmount;
    private final long minOrderAmount;
    private final int minItemsCount;
    private final boolean active;

    public PromoVoucher(
            String code,
            String title,
            String description,
            int discountPercent,
            long maxDiscountAmount,
            long minOrderAmount,
            int minItemsCount,
            boolean active
    ) {
        this.code = Objects.requireNonNull(code, "code cannot be null").trim().toUpperCase();
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.discountPercent = Math.max(0, Math.min(100, discountPercent));
        this.maxDiscountAmount = Math.max(0L, maxDiscountAmount);
        this.minOrderAmount = Math.max(0L, minOrderAmount);
        this.minItemsCount = Math.max(0, minItemsCount);
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public long getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public long getMinOrderAmount() {
        return minOrderAmount;
    }

    public int getMinItemsCount() {
        return minItemsCount;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEligible(long orderSubtotal, int totalItemCount) {
        return active && orderSubtotal >= minOrderAmount && totalItemCount >= minItemsCount;
    }

    public long calculateDiscount(long orderSubtotal, int totalItemCount) {
        if (!isEligible(orderSubtotal, totalItemCount) || orderSubtotal <= 0L) {
            return 0L;
        }
        long rawDiscount;
        if (discountPercent > 0) {
            rawDiscount = (orderSubtotal * discountPercent) / 100L;
            if (maxDiscountAmount > 0L) {
                rawDiscount = Math.min(rawDiscount, maxDiscountAmount);
            }
        } else {
            rawDiscount = maxDiscountAmount;
        }
        return Math.min(rawDiscount, orderSubtotal);
    }

    public PromoVoucher withActive(boolean newActive) {
        return new PromoVoucher(
                this.code,
                this.title,
                this.description,
                this.discountPercent,
                this.maxDiscountAmount,
                this.minOrderAmount,
                this.minItemsCount,
                newActive
        );
    }
}
