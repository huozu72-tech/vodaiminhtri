package com.example.chieuthu3_ketthucmonhoc.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chieuthu3_ketthucmonhoc.databinding.ItemAdminCustomerBinding;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminCustomerAdapter extends RecyclerView.Adapter<AdminCustomerAdapter.CustomerViewHolder> {

    public interface OnCustomerClickListener {
        void onCustomerClick(Customer customer);
    }

    public interface CustomerOrdersProvider {
        List<Order> getOrdersForCustomer(int customerId);
    }

    private List<Customer> customerList;
    private final CustomerOrdersProvider ordersProvider;
    private final OnCustomerClickListener onCustomerClickListener;

    public AdminCustomerAdapter(
            List<Customer> customerList,
            CustomerOrdersProvider ordersProvider,
            OnCustomerClickListener onCustomerClickListener
    ) {
        this.customerList = customerList != null ? customerList : new ArrayList<>();
        this.ordersProvider = ordersProvider;
        this.onCustomerClickListener = onCustomerClickListener;
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminCustomerBinding binding;

        public CustomerViewHolder(ItemAdminCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminCustomerBinding binding = ItemAdminCustomerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CustomerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);

        holder.binding.tvAdminCustomerName.setText(customer.getFullName());
        String emailPart = (customer.getEmail() != null && !customer.getEmail().trim().isEmpty())
                ? " • " + customer.getEmail()
                : "";
        String addressPart = (customer.getAddress() != null && !customer.getAddress().trim().isEmpty())
                ? " (" + customer.getAddress() + ")"
                : "";
        holder.binding.tvAdminCustomerPhoneEmail.setText("📞 " + customer.getPhoneNumber() + emailPart + addressPart);

        List<Order> orders = ordersProvider != null
                ? ordersProvider.getOrdersForCustomer(customer.getId())
                : new ArrayList<>();
        long totalSpent = 0;
        for (Order o : orders) {
            if (o.getStatus() != OrderStatus.CANCELLED) {
                totalSpent += o.getTotalAmount();
            }
        }
        int points = (int) (totalSpent / 10000L);
        String tierBadge;
        if (totalSpent >= 1_500_000L) {
            tierBadge = "💎 VIP Kim Cương";
        } else if (totalSpent >= 700_000L) {
            tierBadge = "🥇 VIP Vàng";
        } else if (totalSpent >= 300_000L) {
            tierBadge = "🥈 VIP Bạc";
        } else {
            tierBadge = "🥉 Hạng Đồng";
        }

        holder.binding.tvAdminCustomerStats.setText(
                String.format(
                        Locale.getDefault(),
                        "%s (⭐ %d điểm) • 🛒 %d đơn • Chi tiêu: %,d đ",
                        tierBadge,
                        points,
                        orders.size(),
                        totalSpent
                )
        );

        holder.itemView.setOnClickListener(v -> {
            if (onCustomerClickListener != null) {
                onCustomerClickListener.onCustomerClick(customer);
            }
        });
        holder.binding.btnViewCustomerOrders.setOnClickListener(v -> {
            if (onCustomerClickListener != null) {
                onCustomerClickListener.onCustomerClick(customer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public void updateList(List<Customer> newList) {
        this.customerList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }
}
