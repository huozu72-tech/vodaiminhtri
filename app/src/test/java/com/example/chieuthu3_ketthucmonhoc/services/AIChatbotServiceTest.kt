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
}
