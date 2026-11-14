package com.example.chieuthu3_ketthucmonhoc.data.repository;

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class MenuRepositoryImpl implements IMenuRepository {

    private final CopyOnWriteArrayList<MenuItem> menuList;
    private int nextId;

    public MenuRepositoryImpl() {
        this(InitialDataFactory.createInitialMenuItems());
    }

    public MenuRepositoryImpl(List<MenuItem> initialItems) {
        this.menuList = new CopyOnWriteArrayList<>(initialItems != null ? initialItems : new ArrayList<>());
        int max = 0;
        for (MenuItem item : this.menuList) {
            if (item.getId() > max) {
                max = item.getId();
            }
        }
        this.nextId = max + 1;
    }

    @Override
    public List<MenuItem> getAllMenuItems() {
        return new ArrayList<>(menuList);
    }

    @Override
    public MenuItem getMenuItemById(int id) {
        for (MenuItem item : menuList) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    @Override
    public List<MenuItem> getMenuItemsByCategory(FoodCategory category) {
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : menuList) {
            if (item.getCategory() == category) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public synchronized MenuItem saveMenuItem(MenuItem item) {
        MenuItem target;
        if (item.getId() <= 0) {
            target = new MenuItem(
                    nextId++,
                    item.getName(),
                    item.getDescription(),
                    item.getPrice(),
                    item.getCategory(),
                    item.getTasteAttributes(),
                    item.isAvailable(),
                    item.getRating(),
                    item.getOrderCount(),
                    item.getImageResId(),
                    item.getImagePath()
            );
        } else {
            target = item;
        }

        int index = -1;
        for (int i = 0; i < menuList.size(); i++) {
            if (menuList.get(i).getId() == target.getId()) {
                index = i;
                break;
            }
        }

        if (index >= 0) {
            menuList.set(index, target);
        } else {
            menuList.add(target);
        }
        return target;
    }

    @Override
    public boolean deleteMenuItem(int id) {
        return menuList.removeIf(item -> item.getId() == id);
    }

    @Override
    public List<MenuItem> searchMenuItems(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMenuItems();
        }
        String normalizedQuery = removeDiacritics(query.trim().toLowerCase());
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : menuList) {
            String normalizedName = removeDiacritics(item.getName().toLowerCase());
            String normalizedDesc = removeDiacritics(item.getDescription().toLowerCase());
            String normalizedCat = removeDiacritics(item.getCategory().getDisplayName().toLowerCase());
            if (normalizedName.contains(normalizedQuery) ||
                normalizedDesc.contains(normalizedQuery) ||
                normalizedCat.contains(normalizedQuery)) {
                result.add(item);
            }
        }
        return result;
    }

    private static String removeDiacritics(String str) {
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("")
                .replace("đ", "d")
                .replace("Đ", "D");
    }
}
