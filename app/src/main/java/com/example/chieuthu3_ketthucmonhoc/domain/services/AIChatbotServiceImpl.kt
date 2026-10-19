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
        val matched = allItems.filter { item ->
            lowerHint.contains("tôm") && item.name.lowercase().contains("tôm") ||
                    lowerHint.contains("gà") && item.name.lowercase().contains("gà") ||
                    lowerHint.contains("ếch") && item.name.lowercase().contains("ếch") ||
                    lowerHint.contains("gỏi") && item.name.lowercase().contains("gỏi") ||
                    lowerHint.contains("chân giò") && item.name.lowercase().contains("chân giò") ||
                    lowerHint.contains("nước") && item.category == FoodCategory.BEVERAGE
        }.ifEmpty {
            // Default top 2 recommendations if no specific keyword matched
            allItems.take(2)
        }

        val aiMsg = ChatMessage(
            text = "🤖 AI đã phân tích ảnh! Dựa trên món ăn trong ảnh, hệ thống nhận diện thấy các món rất khớp với thực đơn nhà hàng dưới đây:",
            isUser = false,
            suggestedItems = matched
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

        // 1. Budget queries (dưới 100k, rẻ, dưới 50k, ...)
        val budgetMatch = Regex("(\\d+)\\s*(k|nghìn|nghin|000)").find(lower)
        if (budgetMatch != null) {
            val num = budgetMatch.groupValues[1].toLongOrNull() ?: 100
            val budget = if (num < 1000) num * 1000 else num
            val affordable = allItems.filter { it.price <= budget }.sortedByDescending { it.rating }
            if (affordable.isNotEmpty()) {
                val topChoices = affordable.take(3)
                return ChatMessage(
                    text = "Dạ với mức giá dưới ${budget / 1000}k, nhà hàng có các món ngon tuyệt vời này dành cho bạn:",
                    isUser = false,
                    suggestedItems = topChoices
                )
            }
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
}
