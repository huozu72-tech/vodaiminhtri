package com.example.chieuthu3_ketthucmonhoc.di;

import android.content.Context;

import com.example.chieuthu3_ketthucmonhoc.data.database.AppDatabase;
import com.example.chieuthu3_ketthucmonhoc.data.database.RoomMenuRepository;
import com.example.chieuthu3_ketthucmonhoc.data.database.RoomOrderRepository;
import com.example.chieuthu3_ketthucmonhoc.data.database.RoomUserRepository;
import com.example.chieuthu3_ketthucmonhoc.data.repository.CartRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.data.repository.ICartRepository;
import com.example.chieuthu3_ketthucmonhoc.data.repository.IMenuRepository;
import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository;
import com.example.chieuthu3_ketthucmonhoc.data.repository.IUserRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AIChatbotServiceImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AIRecommendationServiceImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthenticationServiceImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.services.CartServiceImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.services.DashboardServiceImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAIChatbotService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAIRecommendationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAuthenticationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.ICartService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IDashboardService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IMenuService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IOrderService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.MenuServiceImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.services.OrderServiceImpl;

public final class AppServiceLocator {

    private static volatile Context sAppContext;

    private static volatile IUserRepository sUserRepository;
    private static volatile IMenuRepository sMenuRepository;
    private static volatile IOrderRepository sOrderRepository;
    private static volatile ICartRepository sCartRepository;

    private static volatile IAuthenticationService sAuthService;
    private static volatile IMenuService sMenuService;
    private static volatile ICartService sCartService;
    private static volatile IOrderService sOrderService;
    private static volatile IAIRecommendationService sAiRecommendationService;
    private static volatile IAIChatbotService sAiChatbotService;
    private static volatile IDashboardService sDashboardService;

    private AppServiceLocator() {
        // Utility singleton locator
    }

    public static void init(Context context) {
        if (sAppContext == null) {
            synchronized (AppServiceLocator.class) {
                if (sAppContext == null) {
                    sAppContext = context.getApplicationContext();
                }
            }
        }
    }

    private static AppDatabase getDatabase() {
        if (sAppContext == null) {
            throw new IllegalStateException("AppServiceLocator.init(context) chưa được gọi (xem App.onCreate)");
        }
        return AppDatabase.getInstance(sAppContext);
    }

    public static IUserRepository getUserRepository() {
        if (sUserRepository == null) {
            synchronized (AppServiceLocator.class) {
                if (sUserRepository == null) {
                    sUserRepository = new RoomUserRepository(getDatabase());
                }
            }
        }
        return sUserRepository;
    }

    public static IMenuRepository getMenuRepository() {
        if (sMenuRepository == null) {
            synchronized (AppServiceLocator.class) {
                if (sMenuRepository == null) {
                    sMenuRepository = new RoomMenuRepository(getDatabase());
                }
            }
        }
        return sMenuRepository;
    }

    public static IOrderRepository getOrderRepository() {
        if (sOrderRepository == null) {
            synchronized (AppServiceLocator.class) {
                if (sOrderRepository == null) {
                    sOrderRepository = new RoomOrderRepository(getDatabase());
                }
            }
        }
        return sOrderRepository;
    }

    public static ICartRepository getCartRepository() {
        if (sCartRepository == null) {
            synchronized (AppServiceLocator.class) {
                if (sCartRepository == null) {
                    sCartRepository = new CartRepositoryImpl();
                }
            }
        }
        return sCartRepository;
    }

    public static IAuthenticationService getAuthService() {
        if (sAuthService == null) {
            synchronized (AppServiceLocator.class) {
                if (sAuthService == null) {
                    sAuthService = new AuthenticationServiceImpl(getUserRepository());
                }
            }
        }
        return sAuthService;
    }

    public static IMenuService getMenuService() {
        if (sMenuService == null) {
            synchronized (AppServiceLocator.class) {
                if (sMenuService == null) {
                    sMenuService = new MenuServiceImpl(getMenuRepository());
                }
            }
        }
        return sMenuService;
    }

    public static ICartService getCartService() {
        if (sCartService == null) {
            synchronized (AppServiceLocator.class) {
                if (sCartService == null) {
                    sCartService = new CartServiceImpl(getCartRepository());
                }
            }
        }
        return sCartService;
    }

    public static IOrderService getOrderService() {
        if (sOrderService == null) {
            synchronized (AppServiceLocator.class) {
                if (sOrderService == null) {
                    sOrderService = new OrderServiceImpl(getOrderRepository());
                }
            }
        }
        return sOrderService;
    }

    public static IAIRecommendationService getAiRecommendationService() {
        if (sAiRecommendationService == null) {
            synchronized (AppServiceLocator.class) {
                if (sAiRecommendationService == null) {
                    sAiRecommendationService = new AIRecommendationServiceImpl(getMenuRepository(), getOrderRepository());
                }
            }
        }
        return sAiRecommendationService;
    }

    public static IAIChatbotService getAiChatbotService() {
        if (sAiChatbotService == null) {
            synchronized (AppServiceLocator.class) {
                if (sAiChatbotService == null) {
                    sAiChatbotService = new AIChatbotServiceImpl(getMenuRepository());
                }
            }
        }
        return sAiChatbotService;
    }

    public static IDashboardService getDashboardService() {
        if (sDashboardService == null) {
            synchronized (AppServiceLocator.class) {
                if (sDashboardService == null) {
                    sDashboardService = new DashboardServiceImpl(getMenuRepository(), getOrderRepository());
                }
            }
        }
        return sDashboardService;
    }
}
