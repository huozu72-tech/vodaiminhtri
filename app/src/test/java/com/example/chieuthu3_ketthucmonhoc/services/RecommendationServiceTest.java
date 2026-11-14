package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.data.repository.OrderRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Recommendation;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.SmartComboProposal;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;
import com.example.chieuthu3_ketthucmonhoc.domain.services.RecommendationServiceImpl;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RecommendationServiceTest {

    private RecommendationServiceImpl recService;
    private MenuRepositoryImpl menuRepository;
    private OrderRepositoryImpl orderRepository;

    private final MenuItem dishCrispy1 = new MenuItem(
            1, "Chân Giò Muối Chiên Giòn", "", 120000L, FoodCategory.MAIN_DISH,
            Arrays.asList(TasteAttribute.CRISPY, TasteAttribute.SAVORY), true, 4.9, 100, 0
    );
    private final MenuItem dishCrispy2 = new MenuItem(
            2, "Tôm Chiên Xù", "", 95000L, FoodCategory.MAIN_DISH,
            Arrays.asList(TasteAttribute.CRISPY, TasteAttribute.SAVORY), true, 4.8, 80, 0
    );
    private final MenuItem dishSpicy = new MenuItem(
            3, "Chân Gà Sốt Thái", "", 65000L, FoodCategory.SIDE_DISH,
            Arrays.asList(TasteAttribute.SPICY, TasteAttribute.SOUR), true, 4.7, 50, 0
    );
    private final MenuItem dishDrink = new MenuItem(
            4, "7Up", "", 15000L, FoodCategory.BEVERAGE,
            Collections.singletonList(TasteAttribute.REFRESHING), true, 4.5, 30, 0
    );

    @Before
    public void setup() {
        List<MenuItem> menuList = Arrays.asList(dishCrispy1, dishCrispy2, dishSpicy, dishDrink);
        menuRepository = new MenuRepositoryImpl(menuList);
        orderRepository = new OrderRepositoryImpl(menuList, Collections.emptyList());
        recService = new RecommendationServiceImpl(menuRepository, orderRepository);
    }

    @Test
    public void coldStartRecommendationsReturnsTrendingDishesWhenNoCustomerOrNoOrders() {
        List<Recommendation> recsNoUser = recService.getPersonalizedRecommendations(null, 2);
        assertEquals(2, recsNoUser.size());
        assertEquals("Chân Giò Muối Chiên Giòn", recsNoUser.get(0).getMenuItem().getName());

        List<Recommendation> recsNewUser = recService.getPersonalizedRecommendations(999, 2);
        assertEquals(2, recsNewUser.size());
        assertTrue(recsNewUser.get(0).getReason().contains("bán chạy"));
    }

    @Test
    public void personalizedRecommendationMatchesCustomerTasteHistory() {
        orderRepository.saveOrder(
                new Order(
                        "#ORD-001",
                        "Bàn 1",
                        1,
                        Collections.singletonList(new OrderItem(1, dishCrispy1, 2))
                )
        );

        List<Recommendation> recs = recService.getPersonalizedRecommendations(1, 3);
        assertFalse(recs.isEmpty());
        MenuItem topItem = recs.get(0).getMenuItem();
        assertNotNull(topItem);
        assertTrue(recs.get(0).getScore() > 0.5);
        assertTrue(recs.get(0).getReason().contains("khẩu vị") || recs.get(0).getReason().contains("sở thích"));
    }

    @Test
    public void similarDishesMatchesSameCategoryAndTaste() {
        List<Recommendation> similar = recService.getSimilarDishes(dishCrispy1.getId(), 2);
        assertEquals(2, similar.size());
        assertEquals(dishCrispy2.getId(), similar.get(0).getMenuItem().getId());
        assertTrue(similar.get(0).getReason().contains("Vị tương đồng"));
    }

    @Test
    public void similarDishesAlwaysHaveNonBlankReasonForUnrelatedItems() {
        List<Recommendation> similar = recService.getSimilarDishes(dishDrink.getId(), 3);

        assertEquals(3, similar.size());
        boolean hasFavorite = false;
        for (Recommendation rec : similar) {
            assertTrue(rec.getReason() != null && !rec.getReason().trim().isEmpty());
            if (rec.getReason().contains("yêu thích")) {
                hasFavorite = true;
            }
        }
        assertTrue(hasFavorite);
    }

    @Test
    public void personalizedScoreIsCappedAt1Point0() {
        orderRepository.saveOrder(
                new Order(
                        "#ORD-010",
                        "Bàn 1",
                        7,
                        Collections.singletonList(new OrderItem(1, dishCrispy1, 2))
                )
        );

        List<Recommendation> recs = recService.getPersonalizedRecommendations(7, 4);
        assertFalse(recs.isEmpty());
        double maxScore = 0.0;
        for (Recommendation rec : recs) {
            assertTrue(rec.getScore() <= 1.0);
            if (rec.getScore() > maxScore) {
                maxScore = rec.getScore();
            }
        }
        assertEquals(1.0, maxScore, 0.001);
    }

    @Test
    public void cartComplementaryRecommendationsSuggestsBeverageOrSideWhenOnlyMainDishInCart() {
        List<Recommendation> crossSell = recService.getCartComplementaryRecommendations(
                Collections.singletonList(dishCrispy1),
                2
        );
        assertFalse(crossSell.isEmpty());
        for (Recommendation rec : crossSell) {
            assertTrue(rec.getMenuItem().getId() != dishCrispy1.getId());
        }
        FoodCategory topCategory = crossSell.get(0).getMenuItem().getCategory();
        assertTrue(topCategory == FoodCategory.BEVERAGE || topCategory == FoodCategory.SIDE_DISH);
    }

    @Test
    public void estimateDishCaloriesReturnsHigherCaloriesForMainCrispyDishThanBeverage() {
        int mainKcal = recService.estimateDishCalories(dishCrispy1);
        int drinkKcal = recService.estimateDishCalories(dishDrink);
        assertTrue(mainKcal > drinkKcal);
        assertTrue(mainKcal >= 450);
        assertTrue(drinkKcal >= 80);
    }

    @Test
    public void generateSmartCombosProducesThreeCompleteComboProposalsWithDiscountAndCalories() {
        List<SmartComboProposal> combos = recService.generateSmartCombos(4, 300000L, "BALANCED");
        assertEquals(3, combos.size());
        for (SmartComboProposal combo : combos) {
            assertNotNull(combo.getComboName());
            assertFalse(combo.getItems().isEmpty());
            assertTrue(combo.getOriginalTotal() > 0);
            assertTrue(combo.getComboDiscount() > 0);
            assertEquals(combo.getOriginalTotal() - combo.getComboDiscount(), combo.getFinalPrice());
            assertTrue(combo.getTotalCaloriesKcal() > 0);
        }
    }
}
