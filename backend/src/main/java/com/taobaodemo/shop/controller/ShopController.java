package com.taobaodemo.shop.controller;

import com.taobaodemo.shop.entity.Shop;
import com.taobaodemo.auth.entity.User;
import com.taobaodemo.shop.service.ShopService;
import com.taobaodemo.common.util.ResponseUtil;
import com.taobaodemo.common.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺控制器
 * 处理店铺相关的API请求：查询店铺信息、更新店铺信息
 */
@RestController
@RequestMapping("/api/shop")
public class ShopController {
    @Autowired
    private ShopService shopService;

    /**
     * 获取当前登录店铺的信息（仅店铺用户）
     * @param session HTTP会话
     * @return 店铺信息或错误提示
     */
    @GetMapping("/info")
    public Object getShopInfo(HttpSession session) {
        if (!SessionUtil.isShopUser(session)) {
            return ResponseUtil.noPermission();
        }
        User user = SessionUtil.getCurrentUser(session);
        Shop shop = shopService.getShopByUserId(user.getUserId()).orElse(null);
        if (shop != null) {
            return ResponseUtil.success(shop);
        } else {
            return ResponseUtil.error("店铺不存在");
        }
    }

    /**
     * 更新店铺信息（仅店铺用户）
     * @param shop 更新的店铺信息
     * @param session HTTP会话
     * @return 更新后的店铺信息
     */
    @PutMapping("/info")
    public Object updateShopInfo(@RequestBody Shop shop, HttpSession session) {
        if (!SessionUtil.isShopUser(session)) {
            return ResponseUtil.noPermission();
        }
        User user = SessionUtil.getCurrentUser(session);
        Shop existing = shopService.getShopByUserId(user.getUserId()).orElse(null);
        if (existing == null) {
            return ResponseUtil.error("店铺不存在");
        }
        
        String newShopName = shop.getShopName();
        if (newShopName == null || newShopName.trim().isEmpty()) {
            return ResponseUtil.error("店铺名称不能为空");
        }
        
        if (!newShopName.equals(existing.getShopName()) && shopService.shopNameExists(newShopName)) {
            return ResponseUtil.error("店铺名称已存在，请使用其他名称");
        }
        
        shop.setShopId(existing.getShopId());
        shop.setUserId(existing.getUserId());
        Shop updated = shopService.updateShop(shop);
        return ResponseUtil.success(updated);
    }
}

