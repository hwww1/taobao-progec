package com.taobaodemo.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应工具类
 * 用于减少Controller中的重复代码
 */
public class ResponseUtil {
    
    /**
     * 成功响应
     */
    public static Map<String, Object> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }
    
    /**
     * 成功响应（带消息）
     */
    public static Map<String, Object> success(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        if (data != null) {
            response.put("data", data);
        }
        if (message != null) {
            response.put("message", message);
        }
        return response;
    }
    
    /**
     * 失败响应
     */
    public static Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
    
    /**
     * 未登录响应
     */
    public static Map<String, Object> notLoggedIn() {
        return error("请先登录");
    }
    
    /**
     * 无权限响应
     */
    public static Map<String, Object> noPermission() {
        return error("无权限");
    }
}

