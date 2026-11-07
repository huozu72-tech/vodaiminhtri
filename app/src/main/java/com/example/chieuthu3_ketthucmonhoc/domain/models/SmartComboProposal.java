package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Đề xuất Mâm cơm / Combo thông minh do AI tự động thiết kế theo số người và ngân sách.
 */
public final class SmartComboProposal {
    private final String comboName;
    private final String rationale;
    private final List<CartItem> items;
    private final long originalTotal;
    private final long comboDiscount;
    private final long finalPrice;
    private final int totalCaloriesKcal;

    public SmartComboProposal(
            String comboName,
            String rationale,
            List<CartItem> items,
            long originalTotal,
            long comboDiscount,
            long finalPrice,
            int totalCaloriesKcal
    ) {
        this.comboName = Objects.requireNonNull(comboName, "comboName cannot be null");
        this.rationale = Objects.requireNonNull(rationale, "rationale cannot be null");
        this.items = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(items)));
        this.originalTotal = Math.max(0L, originalTotal);
        this.comboDiscount = Math.max(0L, comboDiscount);
        this.finalPrice = Math.max(0L, finalPrice);
        this.totalCaloriesKcal = Math.max(0, totalCaloriesKcal);
    }

    public String getComboName() {
        return comboName;
    }

    public String getRationale() {
        return rationale;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public long getOriginalTotal() {
        return originalTotal;
    }

    public long getComboDiscount() {
        return comboDiscount;
    }

    public long getFinalPrice() {
        return finalPrice;
    }

    public int getTotalCaloriesKcal() {
        return totalCaloriesKcal;
    }
}
