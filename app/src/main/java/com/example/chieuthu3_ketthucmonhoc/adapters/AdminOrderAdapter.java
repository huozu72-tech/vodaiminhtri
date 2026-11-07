package com.example.chieuthu3_ketthucmonhoc.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chieuthu3_ketthucmonhoc.R;
import com.example.chieuthu3_ketthucmonhoc.databinding.ItemAdminOrderBinding;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder> {

    public interface OnChangeStatusClickListener {
        void onChangeStatusClick(Order order);
    }

    private List<Order> orderList;
    private final OnChangeStatusClickListener onChangeStatusClickListener;

    public AdminOrderAdapter(List<Order> orderList, OnChangeStatusClickListener onChangeStatusClickListener) {
        this.orderList = orderList != null ? orderList : new ArrayList<>();
        this.onChangeStatusClickListener = onChangeStatusClickListener;
    }

    public static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminOrderBinding binding;

        public AdminOrderViewHolder(ItemAdminOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminOrderBinding binding = ItemAdminOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new AdminOrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvOrderId.setText(order.getOrderId() + " • " + order.getTableNumber());
        holder.binding.tvOrderTime.setText("Thời gian: " + order.getFormattedTime());
        holder.binding.tvOrderItemsSummary.setText("Món: " + order.getItemsSummary());
        holder.binding.tvOrderTotalPrice.setText(order.getFormattedTotal());
        holder.binding.tvOrderStatusBadge.setText(order.getStatus().getDisplayName());

        try {
            holder.binding.tvOrderStatusBadge.setTextColor(Color.parseColor(order.getStatus().getColorHex()));
        } catch (Exception e) {
            holder.binding.tvOrderStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.admin_status_pending));
        }

        holder.binding.btnChangeStatus.setOnClickListener(v -> {
            if (onChangeStatusClickListener != null) {
                onChangeStatusClickListener.onChangeStatusClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateList(List<Order> newList) {
        this.orderList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }
}
