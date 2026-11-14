package com.example.chieuthu3_ketthucmonhoc.data.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "menu_items")
public class MenuItemEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String description;
    private long price;
    private String category;
    private String tasteAttributes;
    private boolean isAvailable;
    private double rating;
    private int orderCount;

    @ColumnInfo(name = "image_res_id")
    private int imageResId;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    public MenuItemEntity(int id, String name, String description, long price, String category,
                          String tasteAttributes, boolean isAvailable, double rating, int orderCount,
                          int imageResId, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.tasteAttributes = tasteAttributes;
        this.isAvailable = isAvailable;
        this.rating = rating;
        this.orderCount = orderCount;
        this.imageResId = imageResId;
        this.imagePath = imagePath != null ? imagePath : "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTasteAttributes() {
        return tasteAttributes;
    }

    public void setTasteAttributes(String tasteAttributes) {
        this.tasteAttributes = tasteAttributes;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath != null ? imagePath : "";
    }

    public MenuItem toDomain() {
        List<TasteAttribute> tastes = new ArrayList<>();
        if (tasteAttributes != null && !tasteAttributes.trim().isEmpty()) {
            String[] parts = tasteAttributes.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        tastes.add(TasteAttribute.valueOf(trimmed));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }

        FoodCategory cat;
        try {
            cat = FoodCategory.valueOf(category);
        } catch (Exception e) {
            cat = FoodCategory.MAIN_DISH;
        }

        return new MenuItem(
                id,
                name,
                description,
                price,
                cat,
                tastes,
                isAvailable,
                rating,
                orderCount,
                imageResId,
                imagePath != null ? imagePath : ""
        );
    }

    public static MenuItemEntity fromDomain(MenuItem item) {
        StringBuilder sb = new StringBuilder();
        if (item.getTasteAttributes() != null) {
            for (int i = 0; i < item.getTasteAttributes().size(); i++) {
                sb.append(item.getTasteAttributes().get(i).name());
                if (i < item.getTasteAttributes().size() - 1) sb.append(",");
            }
        }
        return new MenuItemEntity(
                item.getId(),
                item.getName().trim(),
                item.getDescription(),
                item.getPrice(),
                item.getCategory().name(),
                sb.toString(),
                item.isAvailable(),
                item.getRating(),
                item.getOrderCount(),
                item.getImageResId(),
                item.getImagePath()
        );
    }
}
