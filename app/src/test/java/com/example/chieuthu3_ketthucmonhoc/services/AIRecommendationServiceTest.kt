package com.example.chieuthu3_ketthucmonhoc.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.data.repository.OrderRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute
import com.example.chieuthu3_ketthucmonhoc.domain.services.AIRecommendationServiceImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AIRecommendationServiceTest {

    private lateinit var aiService: AIRecommendationServiceImpl
    private lateinit var menuRepository: MenuRepositoryImpl
    private lateinit var orderRepository: OrderRepositoryImpl

    private val dishCrispy1 = MenuItem(1, "Chân Giò Muối Chiên Giòn", "", 120000, FoodCategory.MAIN_DISH, listOf(TasteAttribute.CRISPY, TasteAttribute.SAVORY), rating = 4.9, orderCount = 100)
    private val dishCrispy2 = MenuItem(2, "Tôm Chiên Xù", "", 95000, FoodCategory.MAIN_DISH, listOf(TasteAttribute.CRISPY, TasteAttribute.SAVORY), rating = 4.8, orderCount = 80)
    private val dishSpicy = MenuItem(3, "Chân Gà Sốt Thái", "", 65000, FoodCategory.SIDE_DISH, listOf(TasteAttribute.SPICY, TasteAttribute.SOUR), rating = 4.7, orderCount = 50)
    private val dishDrink = MenuItem(4, "7Up", "", 15000, FoodCategory.BEVERAGE, listOf(TasteAttribute.REFRESHING), rating = 4.5, orderCount = 30)

    @Before
    fun setup() {
        val menuList = listOf(dishCrispy1, dishCrispy2, dishSpicy, dishDrink)
        menuRepository = MenuRepositoryImpl(menuList)
        orderRepository = OrderRepositoryImpl(menuList, emptyList())
        aiService = AIRecommendationServiceImpl(menuRepository, orderRepository)
    }

    @Test
    fun `cold start recommendations returns trending dishes when no customer or no orders`() {
        val recsNoUser = aiService.getPersonalizedRecommendations(null, 2)
        assertEquals(2, recsNoUser.size)
        assertEquals("Chân Giò Muối Chiên Giòn", recsNoUser[0].menuItem.name)

        val recsNewUser = aiService.getPersonalizedRecommendations(999, 2)
        assertEquals(2, recsNewUser.size)
        assertTrue(recsNewUser[0].reason.contains("bán chạy"))
    }

    @Test
    fun `personalized recommendation matches customer taste history`() {
        // Customer 1 previously ordered dishCrispy1
        orderRepository.saveOrder(
            Order(
                orderId = "#ORD-001",
                tableNumber = "Bàn 1",
                customerId = 1,
                items = listOf(OrderItem(1, dishCrispy1, 2))
            )
        )

        val recs = aiService.getPersonalizedRecommendations(1, 3)
        assertFalse(recs.isEmpty())
        // The top recommendation should favor crispy / savory main dishes like dishCrispy2
        val topItem = recs[0].menuItem
        assertNotNull(topItem)
        assertTrue(recs[0].score > 0.5)
        assertTrue(recs[0].reason.contains("khẩu vị") || recs[0].reason.contains("sở thích"))
    }

    @Test
    fun `similar dishes matches same category and taste`() {
        val similar = aiService.getSimilarDishes(dishCrispy1.id, 2)
        assertEquals(2, similar.size)
        assertEquals(dishCrispy2.id, similar[0].menuItem.id)
        assertTrue(similar[0].reason.contains("Vị tương đồng"))
    }
}
