package com.example.chieuthu3_ketthucmonhoc.data.database;

import android.content.Context;
import android.util.Log;

import androidx.sqlite.db.SupportSQLiteDatabase;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";

    private DatabaseSeeder() {
        // Utility seeder
    }

    /**
     * Seeds initial restaurant data if the database is currently empty.
     * Uses type-safe entity insertion from {@link InitialDataFactory} inside a Room transaction.
     *
     * @param db Room AppDatabase instance
     */
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

    /**
     * Context-aware seeding: performs reliable entity insertion.
     *
     * @param context Application context
     * @param db      Room AppDatabase instance
     */
    public static void seedIfEmpty(Context context, AppDatabase db) {
        seedIfEmpty(db);
    }

    /**
     * Executes SQL DML statements from an assets SQL file (e.g., "restaurant.sql") directly
     * into the underlying SQLite database within a transaction.
     * Skips DDL statements (DROP/CREATE TABLE) to preserve Room table metadata and integrity.
     *
     * @param context       Application context to open assets
     * @param db            Room AppDatabase instance
     * @param assetFileName Name of the asset file (e.g. "restaurant.sql")
     * @return true if statements were successfully read and executed, false otherwise
     */
    public static boolean seedFromSqlAsset(Context context, AppDatabase db, String assetFileName) {
        if (context == null || db == null || assetFileName == null) {
            return false;
        }

        try (InputStream is = context.getAssets().open(assetFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            List<String> statements = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("/*")) {
                    continue;
                }
                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    String stmt = sb.toString().trim();
                    if (stmt.endsWith(";")) {
                        stmt = stmt.substring(0, stmt.length() - 1).trim();
                    }
                    if (!stmt.isEmpty()) {
                        statements.add(stmt);
                    }
                    sb.setLength(0);
                }
            }

            if (statements.isEmpty()) {
                return false;
            }

            SupportSQLiteDatabase sdb = db.getOpenHelper().getWritableDatabase();
            sdb.beginTransaction();
            try {
                for (String sql : statements) {
                    String upper = sql.toUpperCase(java.util.Locale.ROOT);
                    if (upper.startsWith("DROP TABLE") || upper.startsWith("CREATE TABLE")) {
                        continue;
                    }
                    sdb.execSQL(sql);
                }
                sdb.setTransactionSuccessful();
                Log.i(TAG, "Successfully executed " + statements.size() + " SQL statements from asset: " + assetFileName);
                return true;
            } finally {
                sdb.endTransaction();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to seed from SQL asset " + assetFileName + ": " + e.getMessage(), e);
            return false;
        }
    }
}
