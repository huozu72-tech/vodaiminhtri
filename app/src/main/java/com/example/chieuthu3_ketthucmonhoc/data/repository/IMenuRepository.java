package com.example.chieuthu3_ketthucmonhoc.data.repository;

import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.util.List;

public interface IMenuRepository {
    List<MenuItem> getAllMenuItems();
    MenuItem getMenuItemById(int id);
    List<MenuItem> getMenuItemsByCategory(FoodCategory category);
    MenuItem saveMenuItem(MenuItem item);
    boolean deleteMenuItem(int id);
    List<MenuItem> searchMenuItems(String query);
}
