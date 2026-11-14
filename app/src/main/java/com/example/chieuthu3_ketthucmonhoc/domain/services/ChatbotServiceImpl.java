package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.ChatMessage;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatbotServiceImpl implements IChatbotService {

    private final IMenuRepository menuRepository;
    private final ChatbotMLModel mlModel;
    private final CopyOnWriteArrayList<ChatMessage> history = new CopyOnWriteArrayList<>();

    public ChatbotServiceImpl(IMenuRepository menuRepository) {
        this(menuRepository, new ChatbotMLModel());
    }

    public ChatbotServiceImpl(IMenuRepository menuRepository, ChatbotMLModel mlModel) {
        this.menuRepository = menuRepository;
        this.mlModel = mlModel != null ? mlModel : new ChatbotMLModel();
        history.add(new ChatMessage(
                "Xin chào! Tôi là Trợ Lý Ẩm Thực (tích hợp mô hình Học Máy NLP & Thị Giác). Tôi có thể tư vấn món ăn theo khẩu vị (cay, giòn, chua ngọt...), ngân sách, gợi ý combo nhóm hoặc nhận diện món từ hình ảnh giúp bạn!",
                false
        ));
    }

    public ChatbotMLModel getMlModel() {
        return mlModel;
    }

    @Override
    public ChatMessage sendMessage(String userText) {
        String trimmed = userText != null ? userText.trim() : "";
        if (trimmed.isEmpty()) {
            ChatMessage reminder = new ChatMessage(
                    "Bạn chưa nhập nội dung rồi! Hãy cho Trợ Lý biết bạn muốn ăn món gì (khẩu vị, ngân sách, combo nhóm, hình ảnh...) nhé!",
                    false
            );
            history.add(reminder);
            return reminder;
        }

        ChatMessage userMsg = new ChatMessage(trimmed, true);
        history.add(userMsg);

        ChatMessage botResponse = generateBotResponse(trimmed);
        history.add(botResponse);
        return botResponse;
    }

    @Override
    public ChatMessage analyzeFoodImage(String imageNameOrHint) {
        ChatMessage userMsg = new ChatMessage(
                null,
                "📷 [Đã tải lên hình ảnh món ăn: " + imageNameOrHint + "]",
                true,
                Collections.emptyList(),
                imageNameOrHint,
                System.currentTimeMillis()
        );
        history.add(userMsg);

        List<MenuItem> allItems = getAvailableItems();
        List<MenuItem> suggestedItems;
        String responseText;

        if (allItems.isEmpty()) {
            suggestedItems = Collections.emptyList();
            responseText = "🤖 Xin lỗi, hiện tại thực đơn nhà hàng chưa có món nào để gợi ý bạn nhé!";
        } else {
            ChatbotMLModel.ImageRecognitionResult recognition = mlModel.classifyFoodImage(imageNameOrHint, allItems);
            suggestedItems = recognition.getMatchedItems().size() > 4
                    ? new ArrayList<>(recognition.getMatchedItems().subList(0, 4))
                    : recognition.getMatchedItems();

            if (recognition.isRecognized()) {
                long confidencePct = Math.round(recognition.getConfidence() * 100);
                responseText = "🤖 Hệ thống đã phân tích ảnh (độ tin cậy " + confidencePct
                        + "%)! Dựa trên đặc trưng món ăn trong ảnh, quán gợi ý các món khớp với thực đơn nhà hàng dưới đây:";
            } else {
                responseText = "🤖 Hệ thống chưa nhận diện được món cụ thể trong ảnh. Mình gửi bạn 2 món được yêu thích nhiều nhất của nhà hàng thay nhé:";
            }
        }

        ChatMessage botMsg = new ChatMessage(
                null,
                responseText,
                false,
                suggestedItems,
                null,
                System.currentTimeMillis()
        );
        history.add(botMsg);
        return botMsg;
    }

    @Override
    public List<ChatMessage> getChatHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public void clearChat() {
        history.clear();
        history.add(new ChatMessage(
                "Cuộc trò chuyện đã được làm mới. Bạn cần tìm món gì tiếp theo?",
                false
        ));
    }

    private List<MenuItem> getAvailableItems() {
        List<MenuItem> allItems = new ArrayList<>();
        for (MenuItem it : menuRepository.getAllMenuItems()) {
            if (it.isAvailable()) {
                allItems.add(it);
            }
        }
        return allItems;
    }

    private ChatMessage generateBotResponse(String query) {
        List<MenuItem> allItems = getAvailableItems();
        if (allItems.isEmpty()) {
            return new ChatMessage(
                    "Dạ xin lỗi, hiện tại thực đơn nhà hàng đang trống, bạn vui lòng quay lại sau nhé!",
                    false
            );
        }

        ChatbotMLModel.ExtractedSlots slots = mlModel.extractSlots(query);
        ChatbotMLModel.IntentPrediction prediction = mlModel.predictIntent(query);
        ChatbotMLModel.Intent intent = prediction.getIntent();

        // 1. Kiểm tra món ngoài phạm vi thực đơn nhà hàng (Out-of-Scope RAG Guardrail)
        if (slots.getOutOfScopeKeyword() != null || intent == ChatbotMLModel.Intent.OUT_OF_SCOPE) {
            String outFood = slots.getOutOfScopeKeyword() != null ? slots.getOutOfScopeKeyword() : "món này";
            List<MenuItem> topPicks = getTopSellingItems(allItems, 3);
            return buildMessage(
                    "Dạ hiện tại thực đơn nhà hàng chưa phục vụ \"" + outFood
                            + "\". Nhà hàng chuyên các món đặc sản gà, ếch, tôm, chân giò và lẩu lá giang. Bạn tham khảo các món bán chạy nhất của quán nhé:",
                    topPicks
            );
        }

        // 2. Truy vấn có ràng buộc Ngân Sách (Kết hợp đa điều kiện: Ngân sách + Khẩu vị / Danh mục / Tên món)
        Long budget = slots.getBudget();
        if (budget != null) {
            return handleBudgetQuery(query, allItems, budget, slots);
        }

        // 3. Truy vấn Món Rẻ Nhất / Đắt Nhất
        if (slots.isCheapestRequested()) {
            List<MenuItem> sortedCheapest = new ArrayList<>(allItems);
            sortedCheapest.sort(Comparator.comparingLong(MenuItem::getPrice)
                    .thenComparing(Comparator.comparingDouble(MenuItem::getRating).reversed()));
            List<MenuItem> topCheap = sortedCheapest.size() > 3
                    ? new ArrayList<>(sortedCheapest.subList(0, 3))
                    : sortedCheapest;
            return buildMessage(
                    "Dạ đây là các món có mức giá tiết kiệm, bình dân nhất trong thực đơn nhà hàng:",
                    topCheap
            );
        }

        if (slots.isMostExpensiveRequested()) {
            List<MenuItem> sortedPremium = new ArrayList<>(allItems);
            sortedPremium.sort(Comparator.comparingLong(MenuItem::getPrice).reversed()
                    .thenComparing(Comparator.comparingDouble(MenuItem::getRating).reversed()));
            List<MenuItem> topPremium = sortedPremium.size() > 3
                    ? new ArrayList<>(sortedPremium.subList(0, 3))
                    : sortedPremium;
            return buildMessage(
                    "Dạ đây là các món đặc sản cao cấp, thịnh soạn nhất tại nhà hàng dành cho bạn:",
                    topPremium
            );
        }

        // 4. Tư vấn Combo / Đặt bàn theo số người
        if (slots.getPartySize() != null || intent == ChatbotMLModel.Intent.COMBO_CONSULT) {
            int people = slots.getPartySize() != null ? slots.getPartySize() : 4;
            return handleComboConsultation(allItems, people);
        }

        // 5. Tìm kiếm Món Cụ Thể + Khẩu Vị kết hợp
        List<ChatbotMLModel.ScoredMenuItem> ranked = mlModel.rankMenuItems(query, allItems, slots);
        boolean hasStrongDishKeyword = !ranked.isEmpty() && ranked.get(0).getScore() >= 0.48;

        if (!slots.getTastes().isEmpty()) {
            List<MenuItem> tasteMatched = filterByTastes(allItems, slots.getTastes(), slots.getCategory());
            if (!tasteMatched.isEmpty()) {
                List<ChatbotMLModel.ScoredMenuItem> rankedTastes = mlModel.rankMenuItems(query, tasteMatched, slots);
                List<MenuItem> topTasteItems = new ArrayList<>();
                for (int i = 0; i < Math.min(3, rankedTastes.size()); i++) {
                    topTasteItems.add(rankedTastes.get(i).getMenuItem());
                }
                String tasteNames = formatTasteNames(slots.getTastes());
                return buildMessage(
                        "Dạ các món mang hương vị " + tasteNames + " hấp dẫn nhất tại quán là:",
                        topTasteItems
                );
            }
        }

        // 6. Hỏi trực tiếp tên món / nguyên liệu cụ thể (Gà, Tôm, Ếch, Lẩu, Chân giò...)
        if (hasStrongDishKeyword || intent == ChatbotMLModel.Intent.DISH_SEARCH) {
            List<MenuItem> matchedDishes = new ArrayList<>();
            double topScore = ranked.isEmpty() ? 0.0 : ranked.get(0).getScore();
            for (ChatbotMLModel.ScoredMenuItem smi : ranked) {
                if (smi.getScore() >= Math.max(0.32, topScore * 0.65)) {
                    matchedDishes.add(smi.getMenuItem());
                    if (matchedDishes.size() == 3) break;
                }
            }
            if (!matchedDishes.isEmpty()) {
                MenuItem best = matchedDishes.get(0);
                return buildMessage(
                        "Dạ nhà hàng có món " + best.getName() + " (" + best.getFormattedPrice()
                                + ") rất được thực khách yêu thích! Mình gửi bạn danh sách món phù hợp nhé:",
                        matchedDishes
                );
            }
        }

        // 7. Truy vấn theo Danh mục (Món chính / Món phụ / Nước uống)
        if (slots.getCategory() != null || intent == ChatbotMLModel.Intent.CATEGORY_QUERY) {
            FoodCategory cat = slots.getCategory() != null ? slots.getCategory() : FoodCategory.MAIN_DISH;
            return handleCategoryQuery(allItems, cat);
        }

        // 8. Chào hỏi / Hướng dẫn sử dụng
        if (intent == ChatbotMLModel.Intent.GREETING_HELP) {
            List<MenuItem> topPicks = getTopSellingItems(allItems, 3);
            return buildMessage(
                    "Dạ Nhà Hàng Ẩm Thực Việt xin chào quý khách! 👋 Mình có thể tư vấn món theo khẩu vị (cay, chua ngọt, giòn...), theo ngân sách (VD: dưới 100k), gợi ý combo nhóm hoặc nhận diện ảnh món ăn. Dưới đây là 3 món đặc sản nổi bật nhất của quán:",
                    topPicks
            );
        }

        // 9. Mặc định / Gợi ý món bán chạy nhất (Bestseller)
        List<MenuItem> topPicks = getTopSellingItems(allItems, 3);
        return buildMessage(
                "Dạ xin gửi bạn các món đặc sản bán chạy nhất của nhà hàng hôm nay! Bạn có thể nhấn trực tiếp vào món để thêm vào giỏ hàng nhé:",
                topPicks
        );
    }

    private ChatMessage handleBudgetQuery(
            String query,
            List<MenuItem> allItems,
             long budget,
            ChatbotMLModel.ExtractedSlots slots
    ) {
        List<MenuItem> affordable = new ArrayList<>();
        for (MenuItem it : allItems) {
            if (it.getPrice() <= budget) {
                affordable.add(it);
            }
        }

        long budgetK = budget / 1000L;

        if (affordable.isEmpty()) {
            MenuItem cheapest = Collections.min(allItems, Comparator.comparingLong(MenuItem::getPrice));
            return buildMessage(
                    "Dạ xin lỗi, hiện không có món nào dưới " + budgetK + "k. Món rẻ nhất hiện có là "
                            + cheapest.getName() + " (" + cheapest.getFormattedPrice() + "), bạn tham khảo nhé:",
                    Collections.singletonList(cheapest)
            );
        }

        // Kiểm tra nếu khách vừa yêu cầu ngân sách VỪA yêu cầu khẩu vị hoặc danh mục (VD: "Tìm món cay ngon dưới 100k")
        if (!slots.getTastes().isEmpty() || slots.getCategory() != null) {
            List<MenuItem> filteredAffordable = filterByTastes(affordable, slots.getTastes(), slots.getCategory());
            if (!filteredAffordable.isEmpty()) {
                List<ChatbotMLModel.ScoredMenuItem> ranked = mlModel.rankMenuItems(query, filteredAffordable, slots);
                List<MenuItem> topChoices = new ArrayList<>();
                for (int i = 0; i < Math.min(3, ranked.size()); i++) {
                    topChoices.add(ranked.get(i).getMenuItem());
                }
                String extraDesc = !slots.getTastes().isEmpty()
                        ? "vị " + formatTasteNames(slots.getTastes()) + " "
                        : (slots.getCategory() != null ? "thuộc nhóm " + slots.getCategory().getDisplayName() + " " : "");
                return buildMessage(
                        "Dạ với yêu cầu món " + extraDesc + "dưới " + budgetK
                                + "k, Trợ Lý gợi ý các món chuẩn vị và hợp túi tiền nhất cho bạn:",
                        topChoices
                );
            }
        }

        affordable.sort(Comparator.comparingDouble(MenuItem::getRating).reversed()
                .thenComparingInt(MenuItem::getOrderCount).reversed());
        List<MenuItem> topChoices = affordable.size() > 3
                ? new ArrayList<>(affordable.subList(0, 3))
                : affordable;

        return buildMessage(
                "Dạ với mức giá dưới " + budgetK + "k, nhà hàng có các món ngon tuyệt vời này dành cho bạn:",
                topChoices
        );
    }

    private ChatMessage handleComboConsultation(List<MenuItem> allItems, int peopleCount) {
        List<MenuItem> mains = new ArrayList<>();
        List<MenuItem> sides = new ArrayList<>();
        List<MenuItem> drinks = new ArrayList<>();

        for (MenuItem item : allItems) {
            if (item.getCategory() == FoodCategory.MAIN_DISH) mains.add(item);
            else if (item.getCategory() == FoodCategory.SIDE_DISH) sides.add(item);
            else if (item.getCategory() == FoodCategory.BEVERAGE) drinks.add(item);
        }

        mains.sort(Comparator.comparingInt(MenuItem::getOrderCount).reversed());
        sides.sort(Comparator.comparingInt(MenuItem::getOrderCount).reversed());
        drinks.sort(Comparator.comparingInt(MenuItem::getOrderCount).reversed());

        List<MenuItem> combo = new ArrayList<>();
        if (!mains.isEmpty()) combo.add(mains.get(0));
        if (peopleCount >= 3 && mains.size() > 1) combo.add(mains.get(1));
        if (!sides.isEmpty()) combo.add(sides.get(0));
        if (!drinks.isEmpty() && combo.size() < 4) combo.add(drinks.get(0));

        if (combo.isEmpty()) {
            combo = getTopSellingItems(allItems, 3);
        }

        long comboTotal = 0L;
        for (MenuItem item : combo) {
            comboTotal += item.getPrice();
        }
        String formattedTotal = String.format(new Locale("vi", "VN"), "%,d đ", comboTotal);

        return buildMessage(
                "Dạ với bàn " + peopleCount + " người, Trợ Lý đề xuất combo cân bằng gồm Món chính + Khai vị + Nước uống (tổng khoảng "
                        + formattedTotal + ") rất hợp lý dưới đây:",
                combo
        );
    }

    private ChatMessage handleCategoryQuery(List<MenuItem> allItems, FoodCategory category) {
        List<MenuItem> matched = new ArrayList<>();
        int limit = (category == FoodCategory.BEVERAGE) ? 4 : 3;
        for (MenuItem it : allItems) {
            if (it.getCategory() == category) {
                matched.add(it);
                if (matched.size() == limit) break;
            }
        }

        String text;
        if (category == FoodCategory.BEVERAGE) {
            text = "Dạ đây là danh mục nước uống mát lạnh của nhà hàng:";
        } else if (category == FoodCategory.SIDE_DISH) {
            text = "Dạ nhà hàng có các món phụ / khai vị thanh mát, đậm đà này ạ:";
        } else {
            text = "Dạ đây là các món chính đặc sắc, đậm đà hương vị của nhà hàng:";
        }

        return buildMessage(text, matched);
    }

    private List<MenuItem> filterByTastes(
            List<MenuItem> items,
            List<TasteAttribute> requiredTastes,
            FoodCategory optionalCategory
    ) {
        List<MenuItem> matched = new ArrayList<>();
        for (MenuItem it : items) {
            if (optionalCategory != null && it.getCategory() != optionalCategory) {
                continue;
            }
            if (requiredTastes == null || requiredTastes.isEmpty()) {
                matched.add(it);
                continue;
            }
            for (TasteAttribute ta : it.getTasteAttributes()) {
                if (requiredTastes.contains(ta)) {
                    matched.add(it);
                    break;
                }
            }
        }
        matched.sort(Comparator.comparingDouble(MenuItem::getRating).reversed());
        return matched;
    }

    private List<MenuItem> getTopSellingItems(List<MenuItem> allItems, int limit) {
        List<MenuItem> copy = new ArrayList<>(allItems);
        copy.sort(Comparator.comparingInt(MenuItem::getOrderCount).reversed()
                .thenComparing(Comparator.comparingDouble(MenuItem::getRating).reversed()));
        return copy.size() > limit ? new ArrayList<>(copy.subList(0, limit)) : copy;
    }

    private String formatTasteNames(List<TasteAttribute> tastes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tastes.size(); i++) {
            sb.append(tastes.get(i).getTag());
            if (i < tastes.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private ChatMessage buildMessage(String text, List<MenuItem> suggestedItems) {
        return new ChatMessage(
                null,
                text,
                false,
                suggestedItems != null ? suggestedItems : Collections.emptyList(),
                null,
                System.currentTimeMillis()
        );
    }
}
