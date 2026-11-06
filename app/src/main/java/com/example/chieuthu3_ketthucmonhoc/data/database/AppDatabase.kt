package com.example.chieuthu3_ketthucmonhoc.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chieuthu3_ketthucmonhoc.data.database.dao.MenuItemDao
import com.example.chieuthu3_ketthucmonhoc.data.database.dao.OrderDao
import com.example.chieuthu3_ketthucmonhoc.data.database.dao.UserDao
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.AdminEntity
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.CustomerEntity
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.MenuItemEntity
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderEntity
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderItemEntity

/**
 * Cơ sở dữ liệu SQLite của ứng dụng (Room). Toàn bộ ràng buộc quan hệ
 * (khóa chính, khóa ngoại CASCADE/RESTRICT/SET NULL, UNIQUE) do chính SQLite ép buộc.
 *
 * allowMainThreadQueries: các interface I*Repository hiện tại là đồng bộ và được
 * service gọi trực tiếp; đồ án học phần giữ nguyên hợp đồng này. Khi nâng cấp lên
 * coroutine thì bỏ flag và chuyển DAO sang suspend.
 */
@Database(
    entities = [
        MenuItemEntity::class,
        CustomerEntity::class,
        AdminEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun menuItemDao(): MenuItemDao
    abstract fun userDao(): UserDao
    abstract fun orderDao(): OrderDao

    companion object {
        private const val DATABASE_NAME = "restaurant.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { built ->
                        instance = built
                        DatabaseSeeder.seedIfEmpty(built)
                    }
            }
        }
    }
}
