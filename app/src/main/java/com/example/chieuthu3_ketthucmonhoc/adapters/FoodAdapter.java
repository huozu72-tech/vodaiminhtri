package com.example.chieuthu3_ketthucmonhoc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chieuthu3_ketthucmonhoc.R;
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemFoodCardBinding;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodImageResolver;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface OnAddToCartListener {
        void onAddToCart(MenuItem item);
    }

    public interface OnFoodItemClickListener {
        void onFoodItemClick(MenuItem item);
    }

    private List<MenuItem> foodList;
    private final OnAddToCartListener onAddToCartListener;
    private final OnFoodItemClickListener onFoodItemClickListener;

    public FoodAdapter(List<MenuItem> foodList, OnAddToCartListener onAddToCartListener) {
        this(foodList, onAddToCartListener, null);
    }

    public FoodAdapter(
            List<MenuItem> foodList,
            OnAddToCartListener onAddToCartListener,
            OnFoodItemClickListener onFoodItemClickListener
    ) {
        this.foodList = foodList != null ? foodList : new ArrayList<>();
        this.onAddToCartListener = onAddToCartListener;
        this.onFoodItemClickListener = onFoodItemClickListener;
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        final ItemFoodCardBinding binding;

        public FoodViewHolder(ItemFoodCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodCardBinding binding = ItemFoodCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new FoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        MenuItem item = foodList.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.imgFood.setImageResource(FoodImageResolver.resolveImage(item));
        holder.binding.tvFoodName.setText(item.getName());
        holder.binding.tvFoodDesc.setText(item.getDescription());
        holder.binding.tvFoodRatingSold.setText(
                String.format(Locale.US, "⭐ %.1f • Đã bán %d", item.getRating(), item.getOrderCount())
        );
        holder.binding.tvFoodPrice.setText(item.getFormattedPrice());
        holder.binding.tvCategoryBadge.setText(item.getCategory().getDisplayName());

        switch (item.getCategory()) {
            case MAIN_DISH:
                holder.binding.tvCategoryBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_mon_chinh));
                holder.binding.tvCategoryBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_mon_chinh_text));
                break;
            case SIDE_DISH:
                holder.binding.tvCategoryBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_mon_phu));
                holder.binding.tvCategoryBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_mon_phu_text));
                break;
            case BEVERAGE:
                holder.binding.tvCategoryBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_do_uong));
                holder.binding.tvCategoryBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_do_uong_text));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (onFoodItemClickListener != null) {
                onFoodItemClickListener.onFoodItemClick(item);
            }
        });

        holder.binding.btnAddToCart.setOnClickListener(v -> {
            if (onAddToCartListener != null) {
                onAddToCartListener.onAddToCart(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public void updateList(List<MenuItem> newList) {
        this.foodList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }
}
