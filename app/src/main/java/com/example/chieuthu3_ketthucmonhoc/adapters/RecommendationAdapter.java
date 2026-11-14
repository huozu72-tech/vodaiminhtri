package com.example.chieuthu3_ketthucmonhoc.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chieuthu3_ketthucmonhoc.databinding.ItemRecommendationCardBinding;
import com.example.chieuthu3_ketthucmonhoc.utils.FoodImageLoader;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Recommendation;

import java.util.ArrayList;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecViewHolder> {

    public interface OnAddToCartListener {
        void onAddToCart(MenuItem item);
    }

    public interface OnFoodItemClickListener {
        void onFoodItemClick(MenuItem item);
    }

    private List<Recommendation> recommendationList;
    private final OnAddToCartListener onAddToCartListener;
    private final OnFoodItemClickListener onFoodItemClickListener;

    public RecommendationAdapter(List<Recommendation> recommendationList, OnAddToCartListener onAddToCartListener) {
        this(recommendationList, onAddToCartListener, null);
    }

    public RecommendationAdapter(
            List<Recommendation> recommendationList,
            OnAddToCartListener onAddToCartListener,
            OnFoodItemClickListener onFoodItemClickListener
    ) {
        this.recommendationList = recommendationList != null ? recommendationList : new ArrayList<>();
        this.onAddToCartListener = onAddToCartListener;
        this.onFoodItemClickListener = onFoodItemClickListener;
    }

    public static class RecViewHolder extends RecyclerView.ViewHolder {
        final ItemRecommendationCardBinding binding;

        public RecViewHolder(ItemRecommendationCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public RecViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecommendationCardBinding binding = ItemRecommendationCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new RecViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecViewHolder holder, int position) {
        Recommendation item = recommendationList.get(position);

        FoodImageLoader.loadFoodImage(holder.binding.imgRecommendedDish, item.getMenuItem());
        holder.binding.tvReasonBadge.setText(item.getReason());
        holder.binding.tvRecommendedDishName.setText(item.getMenuItem().getName());
        holder.binding.tvRecommendedDishPrice.setText(item.getMenuItem().getFormattedPrice());

        holder.itemView.setOnClickListener(v -> {
            if (onFoodItemClickListener != null) {
                onFoodItemClickListener.onFoodItemClick(item.getMenuItem());
            }
        });

        holder.binding.btnRecAddToCart.setOnClickListener(v -> {
            if (onAddToCartListener != null) {
                onAddToCartListener.onAddToCart(item.getMenuItem());
            }
        });
    }

    @Override
    public int getItemCount() {
        return recommendationList.size();
    }

    public void updateList(List<Recommendation> newList) {
        this.recommendationList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }
}
