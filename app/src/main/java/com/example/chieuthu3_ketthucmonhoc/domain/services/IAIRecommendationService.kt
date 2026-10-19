package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.domain.models.AIRecommendation
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem

interface IAIRecommendationService {
    fun getPersonalizedRecommendations(customerId: Int?, limit: Int = 4): List<AIRecommendation>
    fun getSimilarDishes(menuItemId: Int, limit: Int = 3): List<AIRecommendation>
    fun getTrendingDishes(limit: Int = 4): List<AIRecommendation>
}
