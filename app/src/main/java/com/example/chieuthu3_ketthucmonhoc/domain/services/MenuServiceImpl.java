package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuServiceImpl implements IMenuService {

    private final IMenuRepository menuRepository;

    public MenuServiceImpl(IMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public List<MenuItem> getAllMenuItems() {
        return menuRepository.getAllMenuItems();
    }

    @Override
    public List<MenuItem> getAvailableMenuItems() {
        List<MenuItem> all = menuRepository.getAllMenuItems();
        List<MenuItem> available = new ArrayList<>();
        for (MenuItem item : all) {
            if (item.isAvailable()) {
                available.add(item);
            }
        }
        return available;
    }

    @Override
    public List<MenuItem> getMenuItemsByCategory(FoodCategory category) {
        List<MenuItem> byCat = menuRepository.getMenuItemsByCategory(category);
        List<MenuItem> available = new ArrayList<>();
        for (MenuItem item : byCat) {
            if (item.isAvailable()) {
                available.add(item);
            }
        }
        return available;
    }

    @Override
    public List<MenuItem> searchMenuItems(String query) {
        return menuRepository.searchMenuItems(query);
    }

    @Override
    public MenuItem getMenuItemById(int id) {
        return menuRepository.getMenuItemById(id);
    }

    @Override
    public MenuItem createMenuItem(String name, String description, long price, FoodCategory category, List<TasteAttribute> tasteAttributes) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên món ăn không được để trống");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Giá món ăn phải lớn hơn 0");
        }

        MenuItem newItem = new MenuItem(
                0,
                name.trim(),
                description != null ? description.trim() : "",
                price,
                category,
                tasteAttributes != null ? tasteAttributes : Collections.emptyList(),
                true,
                4.8,
                0,
                0
        );
        return menuRepository.saveMenuItem(newItem);
    }

    @Override
    public MenuItem createMenuItem(String name, String description, long price, FoodCategory category) {
        return createMenuItem(name, description, price, category, Collections.emptyList());
    }

    @Override
    public MenuItem updateMenuItem(MenuItem item) {
        if (item == null || item.getName() == null || item.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên món ăn không được để trống");
        }
        if (item.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá món ăn phải lớn hơn 0");
        }
        return menuRepository.saveMenuItem(item);
    }

    @Override
    public boolean deleteMenuItem(int id) {
        return menuRepository.deleteMenuItem(id);
    }

    @Override
    public MenuItem toggleAvailability(int id) {
        MenuItem item = menuRepository.getMenuItemById(id);
        if (item == null) {
            return null;
        }
        MenuItem updated = new MenuItem(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getCategory(),
                item.getTasteAttributes(),
                !item.isAvailable(),
                item.getRating(),
                item.getOrderCount(),
                item.getImageResId()
        );
        return menuRepository.saveMenuItem(updated);
    }

    @Override
    public MenuItem rateMenuItem(int id, int stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Số sao đánh giá phải từ 1 đến 5");
        }
        MenuItem item = menuRepository.getMenuItemById(id);
        if (item == null) {
            return null;
        }
        int baseWeight = Math.max(5, Math.min(item.getOrderCount() / 10, 20));
        double blended = ((item.getRating() * baseWeight) + stars) / (double) (baseWeight + 1);
        double newRating = Math.round(blended * 10.0) / 10.0;
        newRating = Math.max(1.0, Math.min(5.0, newRating));

        MenuItem updated = new MenuItem(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getCategory(),
                item.getTasteAttributes(),
                item.isAvailable(),
                newRating,
                item.getOrderCount(),
                item.getImageResId()
        );
        return menuRepository.saveMenuItem(updated);
    }
}
