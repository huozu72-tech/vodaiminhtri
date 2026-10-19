package com.example.chieuthu3_ketthucmonhoc.di

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory
import com.example.chieuthu3_ketthucmonhoc.data.repository.CartRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.data.repository.ICartRepository
import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository
import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository
import com.example.chieuthu3_ketthucmonhoc.data.repository.IUserRepository
import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.data.repository.OrderRepositoryImpl
import com.example.chieuthu3_ketthucmonhoc.data.repository.UserRepositoryImpl
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

    val userRepository: IUserRepository by lazy {
        UserRepositoryImpl(
            initialCustomers = InitialDataFactory.createInitialCustomers(),
            initialAdmins = InitialDataFactory.createInitialAdmins()
        )
    }

    val menuRepository: IMenuRepository by lazy {
        MenuRepositoryImpl(
            initialItems = InitialDataFactory.createInitialMenuItems()
        )
    }

    val orderRepository: IOrderRepository by lazy {
        OrderRepositoryImpl(
            menuItems = menuRepository.getAllMenuItems(),
            initialOrders = InitialDataFactory.createInitialOrders(menuRepository.getAllMenuItems())
        )
    }

    val cartRepository: ICartRepository by lazy {
        CartRepositoryImpl()
    }

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
