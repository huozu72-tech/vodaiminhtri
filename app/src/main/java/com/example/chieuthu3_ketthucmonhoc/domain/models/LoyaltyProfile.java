package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.util.Objects;

/**
 * Thông tin Hạng Thành Viên VIP & Điểm thưởng tích lũy của khách hàng.
 */
public final class LoyaltyProfile {
    private final String tierName;
    private final int points;
    private final long totalSpent;
    private final int completedOrdersCount;
    private final int discountPercentBonus;
    private final String nextTierName;
    private final long amountToNextTier;
    private final int progressPercent;

    public LoyaltyProfile(
            String tierName,
            int points,
            long totalSpent,
            int completedOrdersCount,
            int discountPercentBonus,
            String nextTierName,
            long amountToNextTier,
            int progressPercent
    ) {
        this.tierName = Objects.requireNonNull(tierName, "tierName cannot be null");
        this.points = Math.max(0, points);
        this.totalSpent = Math.max(0L, totalSpent);
        this.completedOrdersCount = Math.max(0, completedOrdersCount);
        this.discountPercentBonus = Math.max(0, discountPercentBonus);
        this.nextTierName = Objects.requireNonNull(nextTierName, "nextTierName cannot be null");
        this.amountToNextTier = Math.max(0L, amountToNextTier);
        this.progressPercent = Math.max(0, Math.min(100, progressPercent));
    }

    public String getTierName() {
        return tierName;
    }

    public int getPoints() {
        return points;
    }

    public long getTotalSpent() {
        return totalSpent;
    }

    public int getCompletedOrdersCount() {
        return completedOrdersCount;
    }

    public int getDiscountPercentBonus() {
        return discountPercentBonus;
    }

    public String getNextTierName() {
        return nextTierName;
    }

    public long getAmountToNextTier() {
        return amountToNextTier;
    }

    public int getProgressPercent() {
        return progressPercent;
    }
}
