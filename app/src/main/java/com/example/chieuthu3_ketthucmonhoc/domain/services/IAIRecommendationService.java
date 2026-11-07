package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.AIRecommendation;

import java.util.List;

public interface IAIRecommendationService {
    List<AIRecommendation> getPersonalizedRecommendations(Integer customerId, int limit);
    List<AIRecommendation> getPersonalizedRecommendations(Integer customerId);
    List<AIRecommendation> getSimilarDishes(int menuItemId, int limit);
    List<AIRecommendation> getSimilarDishes(int menuItemId);
    List<AIRecommendation> getTrendingDishes(int limit);
    List<AIRecommendation> getTrendingDishes();
}
