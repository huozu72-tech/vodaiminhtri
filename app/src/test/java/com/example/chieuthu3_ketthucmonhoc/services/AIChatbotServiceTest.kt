package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute
import com.example.chieuthu3_ketthucmonhoc.domain.services.AIChatbotServiceImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AIChatbotServiceTest {

    private lateinit var chatbotService: AIChatbotServiceImpl
    private lateinit var menuRepository: MenuRepositoryImpl

    private val dishSpicy = MenuItem(1, "Chân Gà Sốt Thái", "Chua cay", 65000, FoodCategory.SIDE_DISH, listOf(TasteAttribute.SPICY, TasteAttribute.SOUR))
    private val dishCrispy = MenuItem(2, "Chân Giò Muối Chiên Giòn", "Giòn rụm", 120000, FoodCategory.MAIN_DISH, listOf(TasteAttribute.CRISPY))
    private val dishCheap = MenuItem(3, "7Up", "Nước ngọt", 15000, FoodCategory.BEVERAGE, listOf(TasteAttribute.REFRESHING))

    @Before
    fun setup() {
        menuRepository = MenuRepositoryImpl(listOf(dishSpicy, dishCrispy, dishCheap))
        chatbotService = AIChatbotServiceImpl(menuRepository)
    }

    @Test
    fun `initial chat contains welcome message`() {
        val history = chatbotService.getChatHistory()
        assertEquals(1, history.size)
        assertFalse(history[0].isUser)
    }

    @Test
    fun `budget query recommends items within limit`() {
        val response = chatbotService.sendMessage("Có món nào dưới 50k không bạn?")
        assertFalse(response.isUser)
        assertTrue(response.suggestedItems.isNotEmpty())
        assertTrue(response.suggestedItems.all { it.price <= 50000 })
        assertEquals(dishCheap.id, response.suggestedItems[0].id)
    }

    @Test
    fun `taste query matches spicy taste attribute`() {
        val response = chatbotService.sendMessage("Tôi thích ăn món cay")
        assertFalse(response.isUser)
        assertTrue(response.text.contains("cay"))
        assertEquals(dishSpicy.id, response.suggestedItems[0].id)
    }

    @Test
    fun `image analysis matches food by hint`() {
        val response = chatbotService.analyzeFoodImage("anh_mon_an_chan_ga.jpg")
        assertFalse(response.isUser)
        assertTrue(response.suggestedItems.isNotEmpty())
        assertEquals(dishSpicy.id, response.suggestedItems[0].id)
    }

    @Test
    fun `image analysis with unmatched hint admits no match instead of claiming success`() {
        val response = chatbotService.analyzeFoodImage("anh_mon_an_chan_ga.jpg")

        assertFalse(response.isUser)
        // Hint không chứa từ khóa có dấu -> phải nói thật là chưa nhận diện được
        assertTrue(response.text.contains("chưa nhận diện"))
        assertTrue(response.suggestedItems.isNotEmpty())
    }

    @Test
    fun `blank message is rejected without adding empty user bubble`() {
        val response = chatbotService.sendMessage("   ")

        assertFalse(response.isUser)
        assertTrue(response.text.isNotBlank())
        // Chỉ còn 1 tin chào + 1 tin nhắc nhở của AI, không có bubble người dùng rỗng
        val history = chatbotService.getChatHistory()
        assertEquals(2, history.size)
        assertFalse(history.any { it.isUser })
    }

    @Test
    fun `budget below every item price returns honest no match answer with cheapest item`() {
        val response = chatbotService.sendMessage("Có món nào dưới 1k không?")

        assertFalse(response.isUser)
        assertTrue(response.text.contains("không có món"))
        // Đề xuất đúng món rẻ nhất thay vì rơi về gợi ý mặc định đắt hơn ngân sách
        assertEquals(listOf(dishCheap.id), response.suggestedItems.map { it.id })
    }

    @Test
    fun `full seven digit vnd amount is parsed as one million not one thousand`() {
        val response = chatbotService.sendMessage("Có món nào dưới 1000000 không?")

        assertFalse(response.isUser)
        assertTrue(response.text.contains("1000k"))
        // Ngân sách 1.000.000đ phải phủ được món đắt nhất 120k (trước đây bị cắt sai thành 1.000đ)
        assertTrue(response.suggestedItems.any { it.id == dishCrispy.id })
        assertTrue(response.suggestedItems.all { it.price <= 1000000L })
    }
}
