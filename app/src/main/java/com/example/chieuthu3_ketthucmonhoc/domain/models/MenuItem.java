package com.example.chieuthu3_ketthucmonhoc.domain.models;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MenuItem {
    private final int id;
    private final String name;
    private final String description;
    private final long price;
    private final FoodCategory category;
    private final List<TasteAttribute> tasteAttributes;
    private final boolean isAvailable;
    private final double rating;
    private final int orderCount;
    private final int imageResId;
    private final String imagePath;

    public MenuItem(int id, String name, String description, long price, FoodCategory category) {
        this(id, name, description, price, category, Collections.emptyList(), true, 4.8, 0, 0, "");
    }

    public MenuItem(int id, String name, String description, long price, FoodCategory category,
                    List<TasteAttribute> tasteAttributes, boolean isAvailable, double rating, int orderCount, int imageResId) {
        this(id, name, description, price, category, tasteAttributes, isAvailable, rating, orderCount, imageResId, "");
    }

    public MenuItem(int id, String name, String description, long price, FoodCategory category,
                    List<TasteAttribute> tasteAttributes, boolean isAvailable, double rating, int orderCount,
                    int imageResId, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.tasteAttributes = tasteAttributes != null ? Collections.unmodifiableList(new ArrayList<>(tasteAttributes)) : Collections.emptyList();
        this.isAvailable = isAvailable;
        this.rating = rating;
        this.orderCount = orderCount;
        this.imageResId = imageResId;
        this.imagePath = imagePath != null ? imagePath.trim() : "";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getPrice() {
        return price;
    }

    public FoodCategory getCategory() {
        return category;
    }

    public List<TasteAttribute> getTasteAttributes() {
        return tasteAttributes;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public double getRating() {
        return rating;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean hasCustomImage() {
        return imagePath != null && !imagePath.isEmpty();
    }

    public String getFormattedPrice() {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " đ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return id == menuItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                '}';
    }
}
