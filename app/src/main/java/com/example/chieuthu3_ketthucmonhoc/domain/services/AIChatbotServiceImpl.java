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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIChatbotServiceImpl implements IAIChatbotService {

    private final IMenuRepository menuRepository;
    private final CopyOnWriteArrayList<ChatMessage> history = new CopyOnWriteArrayList<>();

    public AIChatbotServiceImpl(IMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
        history.add(new ChatMessage(
                "Xin chào! Tôi là Trợ Lý Ẩm Thực AI. Tôi có thể tư vấn món ăn theo khẩu vị (cay, giòn, chua ngọt...), ngân sách hoặc nhận diện món từ hình ảnh giúp bạn!",
                false
        ));
    }

    @Override
    public ChatMessage sendMessage(String userText) {
        String trimmed = userText != null ? userText.trim() : "";
        if (trimmed.isEmpty()) {
            ChatMessage reminder = new ChatMessage(
                    "Bạn chưa nhập nội dung rồi! Hãy cho Trợ Lý AI biết bạn muốn ăn món gì (khẩu vị, ngân sách, hình ảnh...) nhé!",
                    false
            );
            history.add(reminder);
            return reminder;
        }

        ChatMessage userMsg = new ChatMessage(trimmed, true);
        history.add(userMsg);

        ChatMessage aiResponse = generateAiResponse(trimmed);
        history.add(aiResponse);
        return aiResponse;
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

        String lowerHint = imageNameOrHint != null ? imageNameOrHint.toLowerCase() : "";
        List<MenuItem> allItems = new ArrayList<>();
        for (MenuItem it : menuRepository.getAllMenuItems()) {
            if (it.isAvailable()) {
                allItems.add(it);
            }
        }

        List<MenuItem> matchedByKeyword = new ArrayList<>();
        for (MenuItem item : allItems) {
            String itemNameLower = item.getName().toLowerCase();
            if ((lowerHint.contains("tôm") && itemNameLower.contains("tôm")) ||
                (lowerHint.contains("gà") && itemNameLower.contains("gà")) ||
                (lowerHint.contains("ếch") && itemNameLower.contains("ếch")) ||
                (lowerHint.contains("gỏi") && itemNameLower.contains("gỏi")) ||
                (lowerHint.contains("chân giò") && itemNameLower.contains("chân giò")) ||
                (lowerHint.contains("nước") && item.getCategory() == FoodCategory.BEVERAGE)) {
                matchedByKeyword.add(item);
            }
        }

        List<MenuItem> suggestedItems;
        String responseText;
        if (allItems.isEmpty()) {
            suggestedItems = Collections.emptyList();
            responseText = "🤖 Xin lỗi, hiện tại thực đơn nhà hàng chưa có món nào để gợi ý bạn nhé!";
        } else if (!matchedByKeyword.isEmpty()) {
            suggestedItems = matchedByKeyword;
            responseText = "🤖 AI đã phân tích ảnh! Dựa trên món ăn trong ảnh, hệ thống nhận diện thấy các món rất khớp với thực đơn nhà hàng dưới đây:";
        } else {
            suggestedItems = allItems.size() > 2 ? new ArrayList<>(allItems.subList(0, 2)) : allItems;
            responseText = "🤖 AI chưa nhận diện được món cụ thể trong ảnh. Mình gửi bạn 2 món được yêu thích nhiều nhất của nhà hàng thay nhé:";
        }

        ChatMessage aiMsg = new ChatMessage(
                null,
                responseText,
                false,
                suggestedItems,
                null,
                System.currentTimeMillis()
        );
        history.add(aiMsg);
        return aiMsg;
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

    private ChatMessage generateAiResponse(String query) {
        String lower = query.toLowerCase();
        List<MenuItem> allItems = new ArrayList<>();
        for (MenuItem it : menuRepository.getAllMenuItems()) {
            if (it.isAvailable()) {
                allItems.add(it);
            }
        }

        // 1. Budget queries
        Long budget = parseBudget(lower);
        if (budget != null) {
            List<MenuItem> affordable = new ArrayList<>();
            for (MenuItem it : allItems) {
                if (it.getPrice() <= budget) {
                    affordable.add(it);
                }
            }
            affordable.sort(Comparator.comparingDouble(MenuItem::getRating).reversed());

            if (!affordable.isEmpty()) {
                List<MenuItem> topChoices = affordable.size() > 3 ? new ArrayList<>(affordable.subList(0, 3)) : affordable;
                return new ChatMessage(
                        null,
                        "Dạ với mức giá dưới " + (budget / 1000) + "k, nhà hàng có các món ngon tuyệt vời này dành cho bạn:",
                        false,
                        topChoices,
                        null,
                        System.currentTimeMillis()
                );
            }

            if (allItems.isEmpty()) {
                return new ChatMessage(
                        "Dạ xin lỗi, hiện tại thực đơn nhà hàng đang trống, bạn vui lòng quay lại sau nhé!",
                        false
                );
            }

            MenuItem cheapest = Collections.min(allItems, Comparator.comparingLong(MenuItem::getPrice));
            return new ChatMessage(
                    null,
                    "Dạ xin lỗi, hiện không có món nào dưới " + (budget / 1000) + "k. Món rẻ nhất hiện có là " + cheapest.getName() + " (" + cheapest.getFormattedPrice() + "), bạn tham khảo nhé:",
                    false,
                    Collections.singletonList(cheapest),
                    null,
                    System.currentTimeMillis()
            );
        }

        // 2. Taste attribute queries
        List<TasteAttribute> detectedTastes = TasteAttribute.parseFromText(lower);
        if (!detectedTastes.isEmpty()) {
            List<MenuItem> matched = new ArrayList<>();
            for (MenuItem it : allItems) {
                for (TasteAttribute ta : it.getTasteAttributes()) {
                    if (detectedTastes.contains(ta)) {
                        matched.add(it);
                        break;
                    }
                }
            }
            matched.sort(Comparator.comparingDouble(MenuItem::getRating).reversed());

            if (!matched.isEmpty()) {
                List<MenuItem> top = matched.size() > 3 ? new ArrayList<>(matched.subList(0, 3)) : matched;
                StringBuilder tasteNames = new StringBuilder();
                for (int i = 0; i < detectedTastes.size(); i++) {
                    tasteNames.append(detectedTastes.get(i).getTag());
                    if (i < detectedTastes.size() - 1) tasteNames.append(", ");
                }
                return new ChatMessage(
                        null,
                        "Dạ các món mang hương vị " + tasteNames + " hấp dẫn nhất tại quán là:",
                        false,
                        top,
                        null,
                        System.currentTimeMillis()
                );
            }
        }

        // 3. Category queries
        if (lower.contains("nước") || lower.contains("uống") || lower.contains("giải khát")) {
            List<MenuItem> drinks = new ArrayList<>();
            for (MenuItem it : allItems) {
                if (it.getCategory() == FoodCategory.BEVERAGE) {
                    drinks.add(it);
                    if (drinks.size() == 4) break;
                }
            }
            return new ChatMessage(
                    null,
                    "Dạ đây là danh mục nước uống mát lạnh của nhà hàng:",
                    false,
                    drinks,
                    null,
                    System.currentTimeMillis()
            );
        }

        if (lower.contains("món phụ") || lower.contains("khai vị") || lower.contains("ăn chơi") || lower.contains("gỏi")) {
            List<MenuItem> sides = new ArrayList<>();
            for (MenuItem it : allItems) {
                if (it.getCategory() == FoodCategory.SIDE_DISH) {
                    sides.add(it);
                    if (sides.size() == 3) break;
                }
            }
            return new ChatMessage(
                    null,
                    "Dạ nhà hàng có các món phụ / khai vị thanh mát, đậm đà này ạ:",
                    false,
                    sides,
                    null,
                    System.currentTimeMillis()
            );
        }

        // 4. Default recommendation
        List<MenuItem> topPicks = new ArrayList<>(allItems);
        topPicks.sort(Comparator.comparingInt(MenuItem::getOrderCount).reversed());
        if (topPicks.size() > 3) {
            topPicks = new ArrayList<>(topPicks.subList(0, 3));
        }
        return new ChatMessage(
                null,
                "Dạ xin gửi bạn các món đặc sản bán chạy nhất của nhà hàng hôm nay! Bạn có thể nhấn trực tiếp vào món để thêm vào giỏ hàng nhé:",
                false,
                topPicks,
                null,
                System.currentTimeMillis()
        );
    }

    private Long parseBudget(String lowerQuery) {
        Pattern thousandSuffixPattern = Pattern.compile("(\\d+)\\s*(?:k|nghìn|nghin)(?!\\w)");
        Matcher matcher = thousandSuffixPattern.matcher(lowerQuery);
        if (matcher.find()) {
            try {
                long num = Long.parseLong(matcher.group(1));
                if (num > 0) {
                    return num * 1000;
                }
            } catch (NumberFormatException ignored) {
            }
            return null;
        }

        Pattern plainAmountPattern = Pattern.compile("\\d{4,}");
        Matcher plainMatcher = plainAmountPattern.matcher(lowerQuery);
        if (plainMatcher.find()) {
            try {
                long amount = Long.parseLong(plainMatcher.group());
                if (amount > 0) {
                    return amount;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
