package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository
import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository
import com.example.chieuthu3_ketthucmonhoc.domain.models.AIRecommendation
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute

class AIRecommendationServiceImpl(
    private val menuRepository: IMenuRepository,
    private val orderRepository: IOrderRepository
) : IAIRecommendationService {

    override fun getPersonalizedRecommendations(customerId: Int?, limit: Int): List<AIRecommendation> {
        val allAvailable = menuRepository.getAllMenuItems().filter { it.isAvailable }
        if (allAvailable.isEmpty()) return emptyList()

        if (customerId == null) {
            return getTrendingDishes(limit)
        }

        val pastOrders = orderRepository.getOrdersByCustomer(customerId)
        if (pastOrders.isEmpty()) {
            return getTrendingDishes(limit)
        }

        // Extract customer's preferred categories and taste attributes from previous orders
        val orderedItems = pastOrders.flatMap { it.items }.map { it.menuItem }
        val preferredCategories = orderedItems.map { it.category }.groupingBy { it }.eachCount()
        val preferredTastes = orderedItems.flatMap { it.tasteAttributes }.groupingBy { it }.eachCount()
        val orderedItemIds = orderedItems.map { it.id }.toSet()

        val scored = allAvailable.map { item ->
            var score = 0.0
            val reasons = mutableListOf<String>()

            // 1. Category preference match (up to 0.35)
            val catCount = preferredCategories[item.category] ?: 0
            if (catCount > 0) {
                score += 0.35 * (catCount.toDouble() / orderedItems.size.coerceAtLeast(1))
                reasons.add("Đúng sở thích nhóm ${item.category.displayName}")
            }

            // 2. Taste attributes match (Jaccard-like overlap, up to 0.45)
            val sharedTastes = item.tasteAttributes.filter { preferredTastes.containsKey(it) }
            if (sharedTastes.isNotEmpty()) {
                val tasteWeight = sharedTastes.size.toDouble() / item.tasteAttributes.size.coerceAtLeast(1)
                score += 0.45 * tasteWeight
                val tags = sharedTastes.joinToString(", ") { it.tag }
                reasons.add("Hợp khẩu vị ($tags)")
            }

            // 3. Rating & Popularity weight (up to 0.20)
            score += 0.20 * (item.rating / 5.0)

            // Favor items not yet ordered to encourage variety
            if (!orderedItemIds.contains(item.id)) {
                score += 0.1
                reasons.add("Món mới hấp dẫn dành cho bạn")
            }

            val finalReason = if (reasons.isNotEmpty()) reasons.joinToString(" • ") else "Món ăn nổi bật tại nhà hàng"
            AIRecommendation(item, (score * 100).toInt() / 100.0, finalReason)
        }

        return scored.sortedByDescending { it.score }.take(limit)
    }

    override fun getSimilarDishes(menuItemId: Int, limit: Int): List<AIRecommendation> {
        val target = menuRepository.getMenuItemById(menuItemId) ?: return emptyList()
        val others = menuRepository.getAllMenuItems().filter { it.isAvailable && it.id != menuItemId }

        val scored = others.map { item ->
            var score = 0.0
            val reasons = mutableListOf<String>()

            if (item.category == target.category) {
                score += 0.4
                reasons.add("Cùng phân loại ${target.category.displayName}")
            }

            val sharedTastes = item.tasteAttributes.intersect(target.tasteAttributes.toSet())
            if (sharedTastes.isNotEmpty()) {
                score += 0.4 * (sharedTastes.size.toDouble() / target.tasteAttributes.size.coerceAtLeast(1))
                reasons.add("Vị tương đồng (${sharedTastes.joinToString(", ") { it.tag }})")
            }

            score += 0.2 * (item.rating / 5.0)
            AIRecommendation(item, (score * 100).toInt() / 100.0, reasons.joinToString(" • "))
        }

        return scored.sortedByDescending { it.score }.take(limit)
    }

    override fun getTrendingDishes(limit: Int): List<AIRecommendation> {
        val allAvailable = menuRepository.getAllMenuItems().filter { it.isAvailable }
        return allAvailable
            .sortedWith(compareByDescending<MenuItem> { it.orderCount }.thenByDescending { it.rating })
            .take(limit)
            .map {
                AIRecommendation(
                    menuItem = it,
                    score = (it.rating / 5.0 * 100).toInt() / 100.0,
                    reason = "Món ăn bán chạy & được yêu thích nhất (⭐ ${it.rating})"
                )
            }
    }
}
