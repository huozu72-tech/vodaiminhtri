package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository;
import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AIRecommendation;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AIRecommendationServiceImpl implements IAIRecommendationService {

    private static final double MAX_SCORE = 1.0;

    private final IMenuRepository menuRepository;
    private final IOrderRepository orderRepository;

    public AIRecommendationServiceImpl(IMenuRepository menuRepository, IOrderRepository orderRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<AIRecommendation> getPersonalizedRecommendations(Integer customerId, int limit) {
        List<MenuItem> allAvailable = new ArrayList<>();
        for (MenuItem item : menuRepository.getAllMenuItems()) {
            if (item.isAvailable()) {
                allAvailable.add(item);
            }
        }
        if (allAvailable.isEmpty()) {
            return Collections.emptyList();
        }

        if (customerId == null) {
            return getTrendingDishes(limit);
        }

        List<Order> pastOrders = orderRepository.getOrdersByCustomer(customerId);
        if (pastOrders == null || pastOrders.isEmpty()) {
            return getTrendingDishes(limit);
        }

        List<MenuItem> orderedItems = new ArrayList<>();
        Map<FoodCategory, Integer> preferredCategories = new HashMap<>();
        Map<TasteAttribute, Integer> preferredTastes = new HashMap<>();
        Set<Integer> orderedItemIds = new HashSet<>();

        for (Order order : pastOrders) {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }
            for (OrderItem oi : order.getItems()) {
                MenuItem mi = oi.getMenuItem();
                if (mi != null) {
                    orderedItems.add(mi);
                    orderedItemIds.add(mi.getId());
                    preferredCategories.put(mi.getCategory(), preferredCategories.getOrDefault(mi.getCategory(), 0) + oi.getQuantity());
                    for (TasteAttribute ta : mi.getTasteAttributes()) {
                        preferredTastes.put(ta, preferredTastes.getOrDefault(ta, 0) + oi.getQuantity());
                    }
                }
            }
        }
        if (orderedItems.isEmpty()) {
            return getTrendingDishes(limit);
        }

        List<AIRecommendation> scored = new ArrayList<>();
        int totalOrderedSize = Math.max(orderedItems.size(), 1);

        for (MenuItem item : allAvailable) {
            double score = 0.0;
            List<String> reasons = new ArrayList<>();

            // 1. Category preference match (up to 0.35)
            int catCount = preferredCategories.getOrDefault(item.getCategory(), 0);
            if (catCount > 0) {
                score += 0.35 * ((double) catCount / totalOrderedSize);
                reasons.add("Đúng sở thích nhóm " + item.getCategory().getDisplayName());
            }

            // 2. Taste attributes match (up to 0.45)
            List<TasteAttribute> sharedTastes = new ArrayList<>();
            for (TasteAttribute ta : item.getTasteAttributes()) {
                if (preferredTastes.containsKey(ta)) {
                    sharedTastes.add(ta);
                }
            }
            if (!sharedTastes.isEmpty()) {
                int itemTasteSize = Math.max(item.getTasteAttributes().size(), 1);
                score += 0.45 * ((double) sharedTastes.size() / itemTasteSize);
                StringBuilder tags = new StringBuilder();
                for (int i = 0; i < sharedTastes.size(); i++) {
                    tags.append(sharedTastes.get(i).getTag());
                    if (i < sharedTastes.size() - 1) tags.append(", ");
                }
                reasons.add("Hợp khẩu vị (" + tags + ")");
            }

            // 3. Rating & Popularity weight (up to 0.20)
            score += 0.20 * (item.getRating() / 5.0);

            // Favor items not yet ordered
            if (!orderedItemIds.contains(item.getId())) {
                score += 0.1;
                reasons.add("Món mới hấp dẫn dành cho bạn");
            }

            String finalReason = reasons.isEmpty() ? "Món ăn nổi bật tại nhà hàng" : joinStrings(reasons, " • ");
            double finalScore = Math.min(score, MAX_SCORE);
            finalScore = Math.round(finalScore * 100.0) / 100.0;

            scored.add(new AIRecommendation(item, finalScore, finalReason));
        }

        scored.sort(Comparator.comparingDouble(AIRecommendation::getScore).reversed());
        return takeLimit(scored, limit);
    }

    @Override
    public List<AIRecommendation> getPersonalizedRecommendations(Integer customerId) {
        return getPersonalizedRecommendations(customerId, 4);
    }

    @Override
    public List<AIRecommendation> getSimilarDishes(int menuItemId, int limit) {
        MenuItem target = menuRepository.getMenuItemById(menuItemId);
        if (target == null) {
            return Collections.emptyList();
        }

        List<AIRecommendation> scored = new ArrayList<>();
        Set<TasteAttribute> targetTastes = new HashSet<>(target.getTasteAttributes());

        for (MenuItem item : menuRepository.getAllMenuItems()) {
            if (!item.isAvailable() || item.getId() == menuItemId) {
                continue;
            }

            double score = 0.0;
            List<String> reasons = new ArrayList<>();

            if (item.getCategory() == target.getCategory()) {
                score += 0.4;
                reasons.add("Cùng phân loại " + target.getCategory().getDisplayName());
            }

            List<TasteAttribute> shared = new ArrayList<>();
            for (TasteAttribute ta : item.getTasteAttributes()) {
                if (targetTastes.contains(ta)) {
                    shared.add(ta);
                }
            }
            if (!shared.isEmpty()) {
                int targetSize = Math.max(target.getTasteAttributes().size(), 1);
                score += 0.4 * ((double) shared.size() / targetSize);
                StringBuilder tags = new StringBuilder();
                for (int i = 0; i < shared.size(); i++) {
                    tags.append(shared.get(i).getTag());
                    if (i < shared.size() - 1) tags.append(", ");
                }
                reasons.add("Vị tương đồng (" + tags + ")");
            }

            score += 0.2 * (item.getRating() / 5.0);
            String finalReason = reasons.isEmpty() ? "Món được khách yêu thích tại nhà hàng" : joinStrings(reasons, " • ");
            double finalScore = Math.min(score, MAX_SCORE);
            finalScore = Math.round(finalScore * 100.0) / 100.0;

            scored.add(new AIRecommendation(item, finalScore, finalReason));
        }

        scored.sort(Comparator.comparingDouble(AIRecommendation::getScore).reversed());
        return takeLimit(scored, limit);
    }

    @Override
    public List<AIRecommendation> getSimilarDishes(int menuItemId) {
        return getSimilarDishes(menuItemId, 3);
    }

    @Override
    public List<AIRecommendation> getTrendingDishes(int limit) {
        List<MenuItem> allAvailable = new ArrayList<>();
        for (MenuItem item : menuRepository.getAllMenuItems()) {
            if (item.isAvailable()) {
                allAvailable.add(item);
            }
        }

        allAvailable.sort((a, b) -> {
            int cmp = Integer.compare(b.getOrderCount(), a.getOrderCount());
            if (cmp != 0) return cmp;
            return Double.compare(b.getRating(), a.getRating());
        });

        List<AIRecommendation> result = new ArrayList<>();
        for (MenuItem it : takeLimit(allAvailable, limit)) {
            double score = Math.round((it.getRating() / 5.0) * 100.0) / 100.0;
            result.add(new AIRecommendation(
                    it,
                    score,
                    "Món ăn bán chạy & được yêu thích nhất (⭐ " + it.getRating() + ")"
            ));
        }
        return result;
    }

    @Override
    public List<AIRecommendation> getTrendingDishes() {
        return getTrendingDishes(4);
    }

    @Override
    public List<AIRecommendation> getCartComplementaryRecommendations(List<MenuItem> currentCartDishes, int limit) {
        if (currentCartDishes == null || currentCartDishes.isEmpty()) {
            return getTrendingDishes(limit);
        }

        Set<Integer> inCartIds = new HashSet<>();
        boolean hasMain = false;
        boolean hasSide = false;
        boolean hasDrink = false;
        boolean hasRichOrSpicy = false;

        for (MenuItem dish : currentCartDishes) {
            if (dish == null) continue;
            inCartIds.add(dish.getId());
            if (dish.getCategory() == FoodCategory.MAIN_DISH) hasMain = true;
            else if (dish.getCategory() == FoodCategory.SIDE_DISH) hasSide = true;
            else if (dish.getCategory() == FoodCategory.BEVERAGE) hasDrink = true;

            for (TasteAttribute ta : dish.getTasteAttributes()) {
                if (ta == TasteAttribute.SPICY || ta == TasteAttribute.FRIED || ta == TasteAttribute.GRILLED || ta == TasteAttribute.CRISPY) {
                    hasRichOrSpicy = true;
                }
            }
        }

        List<AIRecommendation> scored = new ArrayList<>();
        for (MenuItem candidate : menuRepository.getAllMenuItems()) {
            if (!candidate.isAvailable() || inCartIds.contains(candidate.getId())) {
                continue;
            }

            double score = 0.2 * (candidate.getRating() / 5.0);
            List<String> reasons = new ArrayList<>();

            // Cross-sell: Nếu giỏ đã có món chính mà chưa có Nước uống hoặc Món phụ
            if (hasMain && !hasDrink && candidate.getCategory() == FoodCategory.BEVERAGE) {
                score += 0.55;
                reasons.add("Ghép đôi giải khát cho bữa ăn");
            } else if (hasMain && !hasSide && candidate.getCategory() == FoodCategory.SIDE_DISH) {
                score += 0.50;
                reasons.add("Món phụ ăn kèm chống ngấy tuyệt vời");
            } else if (!hasMain && candidate.getCategory() == FoodCategory.MAIN_DISH) {
                score += 0.50;
                reasons.add("Món chính đặc sắc hoàn thiện bàn tiệc");
            }

            // Cân bằng vị giác: Nếu giỏ có đồ chiên/nướng/cay, ưu tiên vị thanh mát hoặc chua dịu
            if (hasRichOrSpicy) {
                for (TasteAttribute ta : candidate.getTasteAttributes()) {
                    if (ta == TasteAttribute.REFRESHING || ta == TasteAttribute.SOUR) {
                        score += 0.25;
                        reasons.add("Cân bằng vị (" + ta.getDisplayName() + ")");
                        break;
                    }
                }
            }

            if (reasons.isEmpty()) {
                reasons.add("Gợi ý gọi thêm hợp khẩu vị bàn tiệc");
            }

            double finalScore = Math.min(MAX_SCORE, Math.round(score * 100.0) / 100.0);
            scored.add(new AIRecommendation(candidate, finalScore, joinStrings(reasons, " • ")));
        }

        scored.sort(Comparator.comparingDouble(AIRecommendation::getScore).reversed());
        return takeLimit(scored, limit);
    }

    private static String joinStrings(List<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) sb.append(delimiter);
        }
        return sb.toString();
    }

    private static <T> List<T> takeLimit(List<T> list, int limit) {
        if (list.size() <= limit) return list;
        return new ArrayList<>(list.subList(0, limit));
    }
}
