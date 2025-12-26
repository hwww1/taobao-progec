package com.taobaodemo.common.util;

import com.taobaodemo.auth.entity.User;
import jakarta.servlet.http.HttpSession;

/**
 * Session工具类
 * 用于统一处理用户登录状态和权限检查
 */
public class SessionUtil {
    
    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
    
    /**
     * 检查用户是否已登录
     */
    public static boolean isLoggedIn(HttpSession session) {
        return getCurrentUser(session) != null;
    }
    
    /**
     * 检查用户是否为店铺用户
     */
    public static boolean isShopUser(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null && user.getUserType() == User.TYPE_SHOP;
    }
    
    /**
     * 检查用户是否为运营商
     */
    public static boolean isOperator(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null && user.getUserType() == User.TYPE_OPERATOR;
    }
    
    /**
     * 检查用户是否为顾客
     */
    public static boolean isCustomer(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null && user.getUserType() == User.TYPE_CUSTOMER;
    }
}

