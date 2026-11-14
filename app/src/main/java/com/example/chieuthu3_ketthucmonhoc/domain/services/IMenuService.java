package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;

import java.util.List;

public interface IMenuService {
    List<MenuItem> getAllMenuItems();
    List<MenuItem> getAvailableMenuItems();
    List<MenuItem> getMenuItemsByCategory(FoodCategory category);
    List<MenuItem> searchMenuItems(String query);
    MenuItem getMenuItemById(int id);
    MenuItem createMenuItem(String name, String description, long price, FoodCategory category, List<TasteAttribute> tasteAttributes);
    MenuItem createMenuItem(String name, String description, long price, FoodCategory category, List<TasteAttribute> tasteAttributes, String imagePath);
    MenuItem createMenuItem(String name, String description, long price, FoodCategory category);
    MenuItem updateMenuItem(MenuItem item);
    boolean deleteMenuItem(int id);
    MenuItem toggleAvailability(int id);
    MenuItem rateMenuItem(int id, int stars);
}
