package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

public class TopSellingItem {
    private final MenuItem menuItem;
    private final int quantitySold;

    public TopSellingItem(MenuItem menuItem, int quantitySold) {
        this.menuItem = menuItem;
        this.quantitySold = quantitySold;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantitySold() {
        return quantitySold;
    }
}
