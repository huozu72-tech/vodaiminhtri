package com.example.chieuthu3_ketthucmonhoc.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.chieuthu3_ketthucmonhoc.data.database.dao.MenuItemDao;
import com.example.chieuthu3_ketthucmonhoc.data.database.dao.OrderDao;
import com.example.chieuthu3_ketthucmonhoc.data.database.dao.UserDao;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.AdminEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.CustomerEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.MenuItemEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderItemEntity;

@Database(
    entities = {
        MenuItemEntity.class,
        CustomerEntity.class,
        AdminEntity.class,
        OrderEntity.class,
        OrderItemEntity.class
    },
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "restaurant.db";
    private static volatile AppDatabase sInstance;

    public abstract MenuItemDao menuItemDao();
    public abstract UserDao userDao();
    public abstract OrderDao orderDao();

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    AppDatabase built = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration(true)
                            .build();
                    sInstance = built;
                    DatabaseSeeder.seedIfEmpty(built);
                }
            }
        }
        return sInstance;
    }
}
