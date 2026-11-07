package com.example.chieuthu3_ketthucmonhoc.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chieuthu3_ketthucmonhoc.databinding.ItemAiRecommendationCardBinding;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AIRecommendation;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodImageResolver;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class AIRecommendationAdapter extends RecyclerView.Adapter<AIRecommendationAdapter.RecViewHolder> {

    public interface OnAddToCartListener {
        void onAddToCart(MenuItem item);
    }

    private List<AIRecommendation> recommendationList;
    private final OnAddToCartListener onAddToCartListener;

    public AIRecommendationAdapter(List<AIRecommendation> recommendationList, OnAddToCartListener onAddToCartListener) {
        this.recommendationList = recommendationList != null ? recommendationList : new ArrayList<>();
        this.onAddToCartListener = onAddToCartListener;
    }

    public static class RecViewHolder extends RecyclerView.ViewHolder {
        final ItemAiRecommendationCardBinding binding;

        public RecViewHolder(ItemAiRecommendationCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public RecViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAiRecommendationCardBinding binding = ItemAiRecommendationCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new RecViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecViewHolder holder, int position) {
        AIRecommendation item = recommendationList.get(position);

        holder.binding.imgAiDish.setImageResource(FoodImageResolver.resolveImage(item.getMenuItem()));
        holder.binding.tvAiReasonBadge.setText(item.getReason());
        holder.binding.tvAiDishName.setText(item.getMenuItem().getName());
        holder.binding.tvAiDishPrice.setText(item.getMenuItem().getFormattedPrice());

        holder.binding.btnAiAddToCart.setOnClickListener(v -> {
            if (onAddToCartListener != null) {
                onAddToCartListener.onAddToCart(item.getMenuItem());
            }
        });
    }

    @Override
    public int getItemCount() {
        return recommendationList.size();
    }

    public void updateList(List<AIRecommendation> newList) {
        this.recommendationList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }
}
