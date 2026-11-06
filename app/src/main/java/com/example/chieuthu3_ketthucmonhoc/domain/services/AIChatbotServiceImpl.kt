package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository
import com.example.chieuthu3_ketthucmonhoc.domain.models.ChatMessage
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute
import java.util.concurrent.CopyOnWriteArrayList

class AIChatbotServiceImpl(
    private val menuRepository: IMenuRepository
) : IAIChatbotService {

    private val history = CopyOnWriteArrayList<ChatMessage>()

    init {
        // Initial welcome message from AI Assistant
        history.add(
            ChatMessage(
                text = "Xin chào! Tôi là Trợ Lý Ẩm Thực AI. Tôi có thể tư vấn món ăn theo khẩu vị (cay, giòn, chua ngọt...), ngân sách hoặc nhận diện món từ hình ảnh giúp bạn!",
                isUser = false
            )
        )
    }

    override fun sendMessage(userText: String): ChatMessage {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) {
            // Chuỗi rỗng/toàn khoảng trắng: không thêm bubble người dùng rỗng vào lịch sử,
            // chỉ trả về lời nhắc nhập nội dung
            val reminder = ChatMessage(
                text = "Bạn chưa nhập nội dung rồi! Hãy cho Trợ Lý AI biết bạn muốn ăn món gì (khẩu vị, ngân sách, hình ảnh...) nhé!",
                isUser = false
            )
            history.add(reminder)
            return reminder
        }

        val userMsg = ChatMessage(text = trimmed, isUser = true)
        history.add(userMsg)

        val aiResponse = generateAiResponse(trimmed)
        history.add(aiResponse)
        return aiResponse
    }

    override fun analyzeFoodImage(imageNameOrHint: String): ChatMessage {
        val userMsg = ChatMessage(
            text = "📷 [Đã tải lên hình ảnh món ăn: $imageNameOrHint]",
            isUser = true,
            imageUri = imageNameOrHint
        )
        history.add(userMsg)

        val lowerHint = imageNameOrHint.lowercase()
        val allItems = menuRepository.getAllMenuItems().filter { it.isAvailable }
        val matchedByKeyword = allItems.filter { item ->
            lowerHint.contains("tôm") && item.name.lowercase().contains("tôm") ||
                    lowerHint.contains("gà") && item.name.lowercase().contains("gà") ||
                    lowerHint.contains("ếch") && item.name.lowercase().contains("ếch") ||
                    lowerHint.contains("gỏi") && item.name.lowercase().contains("gỏi") ||
                    lowerHint.contains("chân giò") && item.name.lowercase().contains("chân giò") ||
                    lowerHint.contains("nước") && item.category == FoodCategory.BEVERAGE
        }

        val suggestedItems: List<MenuItem>
        val responseText: String
        if (allItems.isEmpty()) {
            suggestedItems = emptyList()
            responseText = "🤖 Xin lỗi, hiện tại thực đơn nhà hàng chưa có món nào để gợi ý bạn nhé!"
        } else if (matchedByKeyword.isNotEmpty()) {
            suggestedItems = matchedByKeyword
            responseText = "🤖 AI đã phân tích ảnh! Dựa trên món ăn trong ảnh, hệ thống nhận diện thấy các món rất khớp với thực đơn nhà hàng dưới đây:"
        } else {
            // Không khớp từ khóa nào: nói thật thay vì khẳng định đã nhận diện thành công
            suggestedItems = allItems.take(2)
            responseText = "🤖 AI chưa nhận diện được món cụ thể trong ảnh. Mình gửi bạn 2 món được yêu thích nhiều nhất của nhà hàng thay nhé:"
        }

        val aiMsg = ChatMessage(
            text = responseText,
            isUser = false,
            suggestedItems = suggestedItems
        )
        history.add(aiMsg)
        return aiMsg
    }

    override fun getChatHistory(): List<ChatMessage> = history.toList()

    override fun clearChat() {
        history.clear()
        history.add(
            ChatMessage(
                text = "Cuộc trò chuyện đã được làm mới. Bạn cần tìm món gì tiếp theo?",
                isUser = false
            )
        )
    }

    private fun generateAiResponse(query: String): ChatMessage {
        val lower = query.lowercase()
        val allItems = menuRepository.getAllMenuItems().filter { it.isAvailable }

        // 1. Budget queries (dưới 100k, rẻ, dưới 50k, 1000000đ, ...)
        val budget = parseBudget(lower)
        if (budget != null) {
            val affordable = allItems.filter { it.price <= budget }.sortedByDescending { it.rating }
            if (affordable.isNotEmpty()) {
                val topChoices = affordable.take(3)
                return ChatMessage(
                    text = "Dạ với mức giá dưới ${budget / 1000}k, nhà hàng có các món ngon tuyệt vời này dành cho bạn:",
                    isUser = false,
                    suggestedItems = topChoices
                )
            }
            // Không món nào trong ngân sách: trả lời thẳng kèm món rẻ nhất,
            // không rơi về gợi ý mặc định vốn đắt hơn ngân sách
            if (allItems.isEmpty()) {
                return ChatMessage(
                    text = "Dạ xin lỗi, hiện tại thực đơn nhà hàng đang trống, bạn vui lòng quay lại sau nhé!",
                    isUser = false
                )
            }
            val cheapest = allItems.minByOrNull { it.price }
                ?: return ChatMessage(text = "Dạ hiện tại không có món phù hợp với ngân sách của bạn!", isUser = false)
            return ChatMessage(
                text = "Dạ xin lỗi, hiện không có món nào dưới ${budget / 1000}k. Món rẻ nhất hiện có là ${cheapest.name} (${cheapest.formattedPrice}), bạn tham khảo nhé:",
                isUser = false,
                suggestedItems = listOf(cheapest)
            )
        }

        // 2. Taste attribute queries
        val detectedTastes = TasteAttribute.parseFromText(lower)
        if (detectedTastes.isNotEmpty()) {
            val matched = allItems.filter { item ->
                item.tasteAttributes.any { detectedTastes.contains(it) }
            }.sortedByDescending { it.rating }.take(3)

            if (matched.isNotEmpty()) {
                val tasteNames = detectedTastes.joinToString(", ") { it.tag }
                return ChatMessage(
                    text = "Dạ các món mang hương vị $tasteNames hấp dẫn nhất tại quán là:",
                    isUser = false,
                    suggestedItems = matched
                )
            }
        }

        // 3. Category queries (nước, đồ uống, món phụ, món chính)
        if (lower.contains("nước") || lower.contains("uống") || lower.contains("giải khát")) {
            val drinks = allItems.filter { it.category == FoodCategory.BEVERAGE }.take(4)
            return ChatMessage(
                text = "Dạ đây là danh mục nước uống mát lạnh của nhà hàng:",
                isUser = false,
                suggestedItems = drinks
            )
        }
        if (lower.contains("món phụ") || lower.contains("khai vị") || lower.contains("ăn chơi") || lower.contains("gỏi")) {
            val sides = allItems.filter { it.category == FoodCategory.SIDE_DISH }.take(3)
            return ChatMessage(
                text = "Dạ nhà hàng có các món phụ / khai vị thanh mát, đậm đà này ạ:",
                isUser = false,
                suggestedItems = sides
            )
        }

        // 4. Default helpful recommendation
        val topPicks = allItems.sortedByDescending { it.orderCount }.take(3)
        return ChatMessage(
            text = "Dạ xin gửi bạn các món đặc sản bán chạy nhất của nhà hàng hôm nay! Bạn có thể nhấn trực tiếp vào món để thêm vào giỏ hàng nhé:",
            isUser = false,
            suggestedItems = topPicks
        )
    }

    /**
     * Đọc ngân sách (VND) từ câu hỏi:
     * - Hậu tố "k" / "nghìn" / "nghin" (phải là từ độc lập, không phải chữ "k" đầu của "không"...):
     *   số × 1000 — ví dụ "50k", "100 nghìn", "1000k".
     * - Nếu không có hậu tố, chỉ nhận số tiền VND đầy đủ từ 4 chữ số trở lên
     *   ("100000", "1000000") và dùng nguyên giá trị — tránh lỗi cũ khi "1000000"
     *   bị regex cắt sai thành 1.000đ.
     */
    private fun parseBudget(lowerQuery: String): Long? {
        val thousandSuffix = Regex("(\\d+)\\s*(?:k|nghìn|nghin)(?!\\w)").find(lowerQuery)
        if (thousandSuffix != null) {
            val num = thousandSuffix.groupValues[1].toLongOrNull() ?: return null
            return (num * 1000).takeIf { it > 0 }
        }
        val plainAmount = Regex("\\d{4,}").find(lowerQuery)
        val amount = plainAmount?.value?.toLongOrNull() ?: return null
        return amount.takeIf { it > 0 }
    }
}
