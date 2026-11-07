package com.example.chieuthu3_ketthucmonhoc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chieuthu3_ketthucmonhoc.R;
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemAdminFoodBinding;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodImageResolver;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminFoodAdapter extends RecyclerView.Adapter<AdminFoodAdapter.AdminFoodViewHolder> {

    public interface OnFoodActionListener {
        void onEdit(MenuItem item);
        void onDelete(MenuItem item);
        default void onToggleAvailability(MenuItem item) {}
    }

    private List<MenuItem> foodList;
    private final OnFoodActionListener actionListener;

    public AdminFoodAdapter(List<MenuItem> foodList, OnFoodActionListener actionListener) {
        this.foodList = foodList != null ? foodList : new ArrayList<>();
        this.actionListener = actionListener;
    }

    public static class AdminFoodViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminFoodBinding binding;

        public AdminFoodViewHolder(ItemAdminFoodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public AdminFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminFoodBinding binding = ItemAdminFoodBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new AdminFoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminFoodViewHolder holder, int position) {
        MenuItem food = foodList.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.imgAdminFood.setImageResource(FoodImageResolver.resolveImage(food));
        holder.binding.tvAdminFoodName.setText(food.getName());
        holder.binding.tvAdminFoodCategory.setText(food.getCategory().getDisplayName());
        holder.binding.tvAdminFoodPrice.setText(food.getFormattedPrice());

        String statusPrefix = food.isAvailable() ? "🟢 Còn phục vụ" : "🔴 Tạm hết hàng";
        holder.binding.tvAdminFoodStatus.setText(
                String.format(Locale.US, "%s • ⭐ %.1f • Đã bán %d", statusPrefix, food.getRating(), food.getOrderCount())
        );

        switch (food.getCategory()) {
            case MAIN_DISH:
                holder.binding.tvAdminFoodCategory.setBackgroundResource(R.drawable.bg_badge_mon_chinh);
                holder.binding.tvAdminFoodCategory.setTextColor(
                        ContextCompat.getColor(context, R.color.badge_mon_chinh_text)
                );
                break;
            case SIDE_DISH:
                holder.binding.tvAdminFoodCategory.setBackgroundResource(R.drawable.bg_badge_mon_phu);
                holder.binding.tvAdminFoodCategory.setTextColor(
                        ContextCompat.getColor(context, R.color.badge_mon_phu_text)
                );
                break;
            case BEVERAGE:
                holder.binding.tvAdminFoodCategory.setBackgroundResource(R.drawable.bg_badge_do_uong);
                holder.binding.tvAdminFoodCategory.setTextColor(
                        ContextCompat.getColor(context, R.color.badge_do_uong_text)
                );
                break;
        }

        holder.binding.btnToggleFoodAvailability.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onToggleAvailability(food);
            }
        });

        holder.binding.btnEditFood.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(food);
            }
        });

        holder.binding.btnDeleteFood.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDelete(food);
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
