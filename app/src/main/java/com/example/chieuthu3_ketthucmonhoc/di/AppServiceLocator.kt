package com.example.chieuthu3_ketthucmonhoc.di

import android.content.Context
import com.example.chieuthu3_ketthucmonhoc.data.database.AppDatabase
import com.example.chieuthu3_ketthucmonhoc.data.database.RoomMenuRepository
import com.example.chieuthu3_ketthucmonhoc.data.database.RoomOrderRepository
import com.example.chieuthu3_ketthucmonhoc.data.database.RoomUserRepository
import com.example.chieuthu3_ketthucmonhoc.data.repository.CartRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.data.repository.ICartRepository
import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository
import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository
import com.example.chieuthu3_ketthucmonhoc.data.repository.IUserRepository
import com.example.chieuthu3_ketthucmonhoc.domain.services.AIChatbotServiceImpl
import com.example.chieuthu3_ketthucmonhoc.domain.services.AIRecommendationServiceImpl
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthenticationServiceImpl
import com.example.chieuthu3_ketthucmonhoc.domain.services.CartServiceImpl
import com.example.chieuthu3_ketthucmonhoc.domain.services.DashboardServiceImpl
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAIChatbotService
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAIRecommendationService
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAuthenticationService
import com.example.chieuthu3_ketthucmonhoc.domain.services.ICartService
import com.example.chieuthu3_ketthucmonhoc.domain.services.IDashboardService
import com.example.chieuthu3_ketthucmonhoc.domain.services.IMenuService
import com.example.chieuthu3_ketthucmonhoc.domain.services.IOrderService
import com.example.chieuthu3_ketthucmonhoc.domain.services.MenuServiceImpl
import com.example.chieuthu3_ketthucmonhoc.domain.services.OrderServiceImpl

object AppServiceLocator {

    @Volatile
    private var appContext: Context? = null

    /** Gọi một lần trong App.onCreate để tầng dữ liệu có Context mở SQLite. */
    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    private fun database(): AppDatabase {
        val context = appContext
            ?: error("AppServiceLocator.init(context) chưa được gọi (xem App.onCreate)")
        return AppDatabase.getInstance(context)
    }

    // Tầng dữ liệu chính: SQLite (Room) — dữ liệu sống qua các lần khởi động lại app,
    // ràng buộc FK/UNIQUE do chính database ép buộc; seed lần đầu từ InitialDataFactory.
    val userRepository: IUserRepository by lazy { RoomUserRepository(database()) }
    val menuRepository: IMenuRepository by lazy { RoomMenuRepository(database()) }
    val orderRepository: IOrderRepository by lazy { RoomOrderRepository(database()) }

    // Giỏ hàng là trạng thái theo phiên, cố ý giữ in-memory: tắt app là làm mới giỏ.
    val cartRepository: ICartRepository by lazy { CartRepositoryImpl() }

    val authService: IAuthenticationService by lazy {
        AuthenticationServiceImpl(userRepository)
    }

    val menuService: IMenuService by lazy {
        MenuServiceImpl(menuRepository)
    }

    val cartService: ICartService by lazy {
        CartServiceImpl(cartRepository)
    }

    val orderService: IOrderService by lazy {
        OrderServiceImpl(orderRepository)
    }

    val aiRecommendationService: IAIRecommendationService by lazy {
        AIRecommendationServiceImpl(menuRepository, orderRepository)
    }

    val aiChatbotService: IAIChatbotService by lazy {
        AIChatbotServiceImpl(menuRepository)
    }

    val dashboardService: IDashboardService by lazy {
        DashboardServiceImpl(menuRepository, orderRepository)
    }
}
