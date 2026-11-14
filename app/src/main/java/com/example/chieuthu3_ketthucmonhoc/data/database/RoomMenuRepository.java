package com.example.chieuthu3_ketthucmonhoc.data.database;

import android.database.sqlite.SQLiteConstraintException;

import com.example.chieuthu3_ketthucmonhoc.data.database.dao.MenuItemDao;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.MenuItemEntity;
import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RoomMenuRepository implements IMenuRepository {

    private final MenuItemDao dao;

    public RoomMenuRepository(AppDatabase database) {
        this.dao = database.menuItemDao();
    }

    @Override
    public List<MenuItem> getAllMenuItems() {
        List<MenuItemEntity> entities = dao.getAll();
        List<MenuItem> result = new ArrayList<>();
        for (MenuItemEntity entity : entities) {
            result.add(entity.toDomain());
        }
        return result;
    }

    @Override
    public MenuItem getMenuItemById(int id) {
        MenuItemEntity entity = dao.getById(id);
        return entity != null ? entity.toDomain() : null;
    }

    @Override
    public List<MenuItem> getMenuItemsByCategory(FoodCategory category) {
        List<MenuItemEntity> entities = dao.getByCategory(category.name());
        List<MenuItem> result = new ArrayList<>();
        for (MenuItemEntity entity : entities) {
            result.add(entity.toDomain());
        }
        return result;
    }

    @Override
    public MenuItem saveMenuItem(MenuItem item) {
        MenuItemEntity entity = MenuItemEntity.fromDomain(item);
        if (entity.getId() <= 0) {
            entity.setId(0);
            long newId = dao.insert(entity);
            return new MenuItem(
                    (int) newId,
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
        }

        MenuItemEntity existing = dao.getById(entity.getId());
        if (existing == null) {
            dao.insert(entity);
        } else {
            dao.update(entity);
        }
        return item;
    }

    @Override
    public boolean deleteMenuItem(int id) {
        MenuItemEntity existing = dao.getById(id);
        if (existing == null) {
            return false;
        }
        try {
            return dao.deleteById(id) > 0;
        } catch (SQLiteConstraintException e) {
            // Món đang được đơn hàng tham chiếu (FK RESTRICT): ẩn món thay vì xóa cứng
            existing.setAvailable(false);
            dao.update(existing);
            return true;
        }
    }

    @Override
    public List<MenuItem> searchMenuItems(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMenuItems();
        }
        String normalizedQuery = removeDiacritics(query.trim().toLowerCase());
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : getAllMenuItems()) {
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
