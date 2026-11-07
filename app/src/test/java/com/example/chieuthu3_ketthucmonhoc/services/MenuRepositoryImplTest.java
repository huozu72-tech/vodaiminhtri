package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MenuRepositoryImplTest {

    private MenuRepositoryImpl repository;

    private final MenuItem gaNuong = new MenuItem(
            1,
            "Gà Nướng Mật Ong",
            "Gà ta ướp mật ong nướng vàng óng",
            150000L,
            FoodCategory.MAIN_DISH
    );
    private final MenuItem tomChien = new MenuItem(
            2,
            "Tôm Chiên Xù",
            "Tôm sú tẩm bột chiên xù",
            95000L,
            FoodCategory.MAIN_DISH
    );
    private final MenuItem banhDuc = new MenuItem(
            3,
            "Bánh Đúc Nhân Thịt",
            "Bánh đúc mặn kèm nước mắm",
            40000L,
            FoodCategory.SIDE_DISH
    );
    private final MenuItem coca = new MenuItem(
            4,
            "Coca Cola",
            "Nước ngọt có ga mát lạnh",
            15000L,
            FoodCategory.BEVERAGE
    );

    @Before
    public void setup() {
        repository = new MenuRepositoryImpl(Arrays.asList(gaNuong, tomChien, banhDuc, coca));
    }

    @Test
    public void searchMatchesUnaccentedQueryAgainstAccentedName() {
        List<MenuItem> results = repository.searchMenuItems("ga nuong");
        assertEquals(Arrays.asList(gaNuong), results);
    }

    @Test
    public void searchNormalizesDToD() {
        List<MenuItem> results = repository.searchMenuItems("banh duc");
        assertEquals(Arrays.asList(banhDuc), results);
    }

    @Test
    public void searchWithAccentedQueryStillWorks() {
        List<MenuItem> results = repository.searchMenuItems("Gà Nướng");
        assertEquals(Arrays.asList(gaNuong), results);
    }

    @Test
    public void searchMatchesByDescriptionAndByCategoryDisplayName() {
        assertEquals(Arrays.asList(coca), repository.searchMenuItems("mat lanh"));
        assertEquals(Arrays.asList(coca), repository.searchMenuItems("nuoc uong"));
    }

    @Test
    public void blankQueryReturnsAllItemsAndUnmatchedQueryReturnsEmpty() {
        assertEquals(4, repository.searchMenuItems("   ").size());
        assertTrue(repository.searchMenuItems("pizza").isEmpty());
    }

    @Test
    public void getMenuItemsByCategoryFiltersByCategory() {
        List<MenuItem> mainDishes = repository.getMenuItemsByCategory(FoodCategory.MAIN_DISH);
        Set<Integer> ids = new HashSet<>();
        for (MenuItem item : mainDishes) {
            ids.add(item.getId());
        }
        Set<Integer> expected = new HashSet<>(Arrays.asList(1, 2));
        assertEquals(expected, ids);
    }

    @Test
    public void getMenuItemByIdReturnsNullForUnknownId() {
        assertNull(repository.getMenuItemById(99));
    }

    @Test
    public void saveMenuItemAssignsIncrementalIdWhenIdIsNotPositive() {
        MenuItem saved = repository.saveMenuItem(
                new MenuItem(0, "Trà Tắc", "Trà tắc giải khát", 20000L, FoodCategory.BEVERAGE)
        );
        assertEquals(5, saved.getId());
        assertEquals(5, repository.getAllMenuItems().size());
        assertEquals(saved, repository.getMenuItemById(5));

        MenuItem savedAgain = repository.saveMenuItem(
                new MenuItem(0, "Khác", "Mô tả", 10000L, FoodCategory.SIDE_DISH)
        );
        assertEquals(6, savedAgain.getId());
    }

    @Test
    public void saveMenuItemWithExistingIdUpdatesInPlace() {
        MenuItem updatedInput = new MenuItem(
                gaNuong.getId(),
                gaNuong.getName(),
                gaNuong.getDescription(),
                999000L,
                gaNuong.getCategory(),
                gaNuong.getTasteAttributes(),
                gaNuong.isAvailable(),
                gaNuong.getRating(),
                gaNuong.getOrderCount(),
                gaNuong.getImageResId()
        );
        MenuItem updated = repository.saveMenuItem(updatedInput);
        assertEquals(1, updated.getId());
        assertEquals("Không thêm phần tử mới khi cập nhật", 4, repository.getAllMenuItems().size());
        assertNotNull(repository.getMenuItemById(1));
        assertEquals(999000L, repository.getMenuItemById(1).getPrice());
    }

    @Test
    public void deleteMenuItemReturnsTrueOnlyForAnExistingId() {
        assertTrue(repository.deleteMenuItem(2));
        assertFalse(repository.deleteMenuItem(2));
        assertNull(repository.getMenuItemById(2));
        assertEquals(3, repository.getAllMenuItems().size());
    }
}
