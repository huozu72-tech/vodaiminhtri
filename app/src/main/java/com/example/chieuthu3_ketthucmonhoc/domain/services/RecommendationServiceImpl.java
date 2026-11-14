package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository;
import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Recommendation;
import com.example.chieuthu3_ketthucmonhoc.domain.models.SmartComboProposal;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecommendationServiceImpl implements IRecommendationService {

    private static final double MAX_SCORE = 1.0;

    private final IMenuRepository menuRepository;
    private final IOrderRepository orderRepository;

    public RecommendationServiceImpl(IMenuRepository menuRepository, IOrderRepository orderRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Recommendation> getPersonalizedRecommendations(Integer customerId, int limit) {
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

        List<Recommendation> scored = new ArrayList<>();
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

            scored.add(new Recommendation(item, finalScore, finalReason));
        }

        scored.sort(Comparator.comparingDouble(Recommendation::getScore).reversed());
        return takeLimit(scored, limit);
    }

    @Override
    public List<Recommendation> getPersonalizedRecommendations(Integer customerId) {
        return getPersonalizedRecommendations(customerId, 4);
    }

    @Override
    public List<Recommendation> getSimilarDishes(int menuItemId, int limit) {
        MenuItem target = menuRepository.getMenuItemById(menuItemId);
        if (target == null) {
            return Collections.emptyList();
        }

        List<Recommendation> scored = new ArrayList<>();
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

            scored.add(new Recommendation(item, finalScore, finalReason));
        }

        scored.sort(Comparator.comparingDouble(Recommendation::getScore).reversed());
        return takeLimit(scored, limit);
    }

    @Override
    public List<Recommendation> getSimilarDishes(int menuItemId) {
        return getSimilarDishes(menuItemId, 3);
    }

    @Override
    public List<Recommendation> getTrendingDishes(int limit) {
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

        List<Recommendation> result = new ArrayList<>();
        for (MenuItem it : takeLimit(allAvailable, limit)) {
            double score = Math.round((it.getRating() / 5.0) * 100.0) / 100.0;
            result.add(new Recommendation(
                    it,
                    score,
                    "Món ăn bán chạy & được yêu thích nhất (⭐ " + it.getRating() + ")"
            ));
        }
        return result;
    }

    @Override
    public List<Recommendation> getTrendingDishes() {
        return getTrendingDishes(4);
    }

    @Override
    public List<Recommendation> getCartComplementaryRecommendations(List<MenuItem> currentCartDishes, int limit) {
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

        List<Recommendation> scored = new ArrayList<>();
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
            scored.add(new Recommendation(candidate, finalScore, joinStrings(reasons, " • ")));
        }

        scored.sort(Comparator.comparingDouble(Recommendation::getScore).reversed());
        return takeLimit(scored, limit);
    }

    @Override
    public int estimateDishCalories(MenuItem item) {
        if (item == null) return 0;
        int kcal;
        if (item.getCategory() == FoodCategory.MAIN_DISH) {
            kcal = 520;
        } else if (item.getCategory() == FoodCategory.SIDE_DISH) {
            kcal = 280;
        } else {
            kcal = 135;
        }
        for (TasteAttribute ta : item.getTasteAttributes()) {
            if (ta == TasteAttribute.FRIED || ta == TasteAttribute.CRISPY) {
                kcal += 85;
            } else if (ta == TasteAttribute.GRILLED) {
                kcal += 55;
            } else if (ta == TasteAttribute.SWEET) {
                kcal += 30;
            } else if (ta == TasteAttribute.SAVORY) {
                kcal += 35;
            } else if (ta == TasteAttribute.REFRESHING) {
                kcal -= 15;
            }
        }
        return Math.max(60, kcal);
    }

    @Override
    public List<SmartComboProposal> generateSmartCombos(int guestCount, long budgetVnd, String preferenceStyle) {
        int guests = Math.max(1, Math.min(20, guestCount));
        long targetBudget = Math.max(80000L, budgetVnd);
        String style = preferenceStyle != null ? preferenceStyle.trim().toUpperCase() : "ALL";

        List<MenuItem> mains = new ArrayList<>();
        List<MenuItem> sides = new ArrayList<>();
        List<MenuItem> drinks = new ArrayList<>();

        for (MenuItem item : menuRepository.getAllMenuItems()) {
            if (!item.isAvailable()) continue;
            if (item.getCategory() == FoodCategory.MAIN_DISH) mains.add(item);
            else if (item.getCategory() == FoodCategory.SIDE_DISH) sides.add(item);
            else if (item.getCategory() == FoodCategory.BEVERAGE) drinks.add(item);
        }

        if (mains.isEmpty() && sides.isEmpty()) {
            return Collections.emptyList();
        }

        mains.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
        sides.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
        drinks.sort((a, b) -> Integer.compare(b.getOrderCount(), a.getOrderCount()));

        List<SmartComboProposal> proposals = new ArrayList<>();

        // 1. Combo Cân Bằng Đủ Vị (Main + Side + Drinks)
        List<CartItem> combo1Items = new ArrayList<>();
        int mainQty = Math.max(1, (guests + 1) / 2);
        int sideQty = Math.max(1, guests / 2);
        int drinkQty = guests;

        if (!mains.isEmpty()) {
            MenuItem m1 = pickDishForBudget(mains, targetBudget / Math.max(1, mainQty + 1));
            combo1Items.add(new CartItem(m1, mainQty, "Món chính trung tâm"));
        }
        if (!sides.isEmpty()) {
            MenuItem s1 = sides.get(0);
            combo1Items.add(new CartItem(s1, sideQty, "Khai vị / ăn kèm chống ngấy"));
        }
        if (!drinks.isEmpty()) {
            MenuItem d1 = drinks.get(0);
            combo1Items.add(new CartItem(d1, drinkQty, "Giải khát cho " + guests + " người"));
        }
        proposals.add(buildProposal(
                "🍱 Mâm Cơm Cân Bằng Đủ Vị (" + guests + " người)",
                "Kết hợp hài hòa Món chính + Món phụ gỏi/khai vị + Nước uống giải khát, tối ưu theo ngân sách " + String.format("%,d", targetBudget) + " đ",
                combo1Items,
                10
        ));

        // 2. Combo Đặc Sản Bán Chạy Nhất (Signature Best-Sellers)
        List<MenuItem> byPopularity = new ArrayList<>(mains);
        byPopularity.sort((a, b) -> Integer.compare(b.getOrderCount(), a.getOrderCount()));
        List<CartItem> combo2Items = new ArrayList<>();
        if (!byPopularity.isEmpty()) {
            combo2Items.add(new CartItem(byPopularity.get(0), Math.max(1, guests / 2), "Best-seller số 1 nhà hàng"));
            if (byPopularity.size() > 1 && guests >= 2) {
                combo2Items.add(new CartItem(byPopularity.get(1), 1, "Đặc sản được yêu thích"));
            }
        }
        if (!sides.isEmpty() && sides.size() > 1) {
            combo2Items.add(new CartItem(sides.get(1), 1, "Món nhâm nhi cực cuốn"));
        } else if (!sides.isEmpty()) {
            combo2Items.add(new CartItem(sides.get(0), 1, "Món nhâm nhi cực cuốn"));
        }
        if (!drinks.isEmpty()) {
            MenuItem d2 = drinks.size() > 1 ? drinks.get(1) : drinks.get(0);
            combo2Items.add(new CartItem(d2, guests, "Nước ngọt mát lạnh"));
        }
        proposals.add(buildProposal(
                "🔥 Combo Đặc Sản Bán Chạy (" + guests + " người)",
                "Tuyển tập những món có lượt gọi cao nhất và đánh giá 4.8★–4.9★ tại nhà hàng",
                combo2Items,
                12
        ));

        // 3. Combo Theo Gu Khẩu Vị (Cay Nồng / Giòn Rụm / Thanh Đạm)
        List<CartItem> combo3Items = new ArrayList<>();
        MenuItem styleMain = pickDishByStyle(mains, style);
        MenuItem styleSide = pickDishByStyle(sides, style);
        if (styleMain != null) {
            combo3Items.add(new CartItem(styleMain, mainQty, "Chuẩn gu " + describeStyle(style)));
        }
        if (styleSide != null) {
            combo3Items.add(new CartItem(styleSide, sideQty, "Hương vị " + describeStyle(style)));
        }
        if (!drinks.isEmpty()) {
            combo3Items.add(new CartItem(drinks.get(0), drinkQty, "Cân bằng vị giác"));
        }
        proposals.add(buildProposal(
                "✨ Combo Gu " + describeStyle(style) + " (" + guests + " người)",
                "Được cá nhân hóa riêng theo sở thích hương vị " + describeStyle(style).toLowerCase() + " của bàn bạn",
                combo3Items,
                15
        ));

        return proposals;
    }

    private MenuItem pickDishForBudget(List<MenuItem> list, long targetUnitBudget) {
        MenuItem best = list.get(0);
        long minDiff = Math.abs(best.getPrice() - targetUnitBudget);
        for (MenuItem item : list) {
            long diff = Math.abs(item.getPrice() - targetUnitBudget);
            if (diff < minDiff) {
                minDiff = diff;
                best = item;
            }
        }
        return best;
    }

    private MenuItem pickDishByStyle(List<MenuItem> list, String style) {
        if (list.isEmpty()) return null;
        for (MenuItem item : list) {
            for (TasteAttribute ta : item.getTasteAttributes()) {
                if ("SPICY".equals(style) && (ta == TasteAttribute.SPICY || ta == TasteAttribute.SOUR)) {
                    return item;
                }
                if ("CRISPY".equals(style) && (ta == TasteAttribute.CRISPY || ta == TasteAttribute.FRIED || ta == TasteAttribute.GRILLED)) {
                    return item;
                }
                if ("LIGHT".equals(style) && (ta == TasteAttribute.REFRESHING || ta == TasteAttribute.SOUR || ta == TasteAttribute.SWEET)) {
                    return item;
                }
            }
        }
        return list.get(list.size() - 1);
    }

    private String describeStyle(String style) {
        if ("SPICY".equals(style)) return "Cay Nồng Đậm Đà";
        if ("CRISPY".equals(style)) return "Chiên Giòn & Nướng Thơm";
        if ("LIGHT".equals(style)) return "Thanh Mát Chống Ngấy";
        return "Tổng Hợp Đặc Sắc";
    }

    private SmartComboProposal buildProposal(String name, String rationale, List<CartItem> items, int discountPct) {
        long orig = 0L;
        int totalKcal = 0;
        for (CartItem ci : items) {
            orig += ci.getSubtotal();
            totalKcal += estimateDishCalories(ci.getMenuItem()) * ci.getQuantity();
        }
        long discount = (orig * discountPct) / 100L;
        long finalPrice = Math.max(0L, orig - discount);
        return new SmartComboProposal(name, rationale, items, orig, discount, finalPrice, totalKcal);
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
