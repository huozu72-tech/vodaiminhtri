package com.example.chieuthu3_ketthucmonhoc.data.database;

import com.example.chieuthu3_ketthucmonhoc.data.database.entity.AdminEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.CustomerEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.MenuItemEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderItemEntity;
import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public final class DatabaseSeeder {

    private DatabaseSeeder() {
        // Utility seeder
    }

    public static void seedIfEmpty(AppDatabase db) {
        boolean alreadySeeded = db.menuItemDao().count() > 0 ||
                db.userDao().countCustomers() > 0 ||
                db.orderDao().count() > 0;
        if (alreadySeeded) {
            return;
        }

        db.runInTransaction(() -> {
            List<MenuItem> menuItems = InitialDataFactory.createInitialMenuItems();
            List<MenuItemEntity> menuItemEntities = new ArrayList<>();
            for (MenuItem mi : menuItems) {
                menuItemEntities.add(MenuItemEntity.fromDomain(mi));
            }
            db.menuItemDao().insertAll(menuItemEntities);

            List<Customer> customers = InitialDataFactory.createInitialCustomers();
            List<CustomerEntity> customerEntities = new ArrayList<>();
            for (Customer c : customers) {
                customerEntities.add(CustomerEntity.fromDomain(c));
            }
            db.userDao().insertCustomers(customerEntities);

            List<AdminAccount> admins = InitialDataFactory.createInitialAdmins();
            List<AdminEntity> adminEntities = new ArrayList<>();
            for (AdminAccount a : admins) {
                adminEntities.add(AdminEntity.fromDomain(a));
            }
            db.userDao().insertAdmins(adminEntities);

            List<Order> orders = InitialDataFactory.createInitialOrders(menuItems);
            for (Order order : orders) {
                OrderEntity entity = OrderEntity.fromDomain(order);
                db.orderDao().insert(entity);

                List<OrderItemEntity> itemEntities = new ArrayList<>();
                for (OrderItem oi : order.getItems()) {
                    itemEntities.add(OrderItemEntity.fromDomain(oi, entity.getOrderId()));
                }
                db.orderDao().insertItems(itemEntities);
            }
        });
    }
}
