package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Recommendation;
import com.example.chieuthu3_ketthucmonhoc.domain.models.SmartComboProposal;

import java.util.List;

public interface IRecommendationService {
    List<Recommendation> getPersonalizedRecommendations(Integer customerId, int limit);
    List<Recommendation> getPersonalizedRecommendations(Integer customerId);
    List<Recommendation> getSimilarDishes(int menuItemId, int limit);
    List<Recommendation> getSimilarDishes(int menuItemId);
    List<Recommendation> getTrendingDishes(int limit);
    List<Recommendation> getTrendingDishes();
    List<Recommendation> getCartComplementaryRecommendations(List<MenuItem> currentCartDishes, int limit);
    int estimateDishCalories(MenuItem item);
    List<SmartComboProposal> generateSmartCombos(int guestCount, long budgetVnd, String preferenceStyle);
}
