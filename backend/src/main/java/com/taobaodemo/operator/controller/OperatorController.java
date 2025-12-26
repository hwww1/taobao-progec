package com.taobaodemo.operator.controller;

import com.taobaodemo.shop.entity.Shop;
import com.taobaodemo.auth.entity.User;
import com.taobaodemo.operator.service.OperatorService;
import com.taobaodemo.operator.service.ReportService;
import com.taobaodemo.auth.service.UserService;
import com.taobaodemo.shop.service.ShopService;
import com.taobaodemo.common.util.ResponseUtil;
import com.taobaodemo.common.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 运营商控制器
 * 处理运营商管理相关的API请求：仪表板数据、用户管理、店铺管理
 */
@RestController
@RequestMapping("/api/operator")
public class OperatorController {
    @Autowired
    private OperatorService operatorService;
    
    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    /**
     * 获取运营商仪表板数据
     * @param session HTTP会话
     * @return 统计数据（总用户数、总店铺数、热销商品、热门店铺）
     */
    @GetMapping("/dashboard")
    public Object getDashboard(HttpSession session) {
        if (!SessionUtil.isOperator(session)) {
            return ResponseUtil.noPermission();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", reportService.getTotalUsers());
        data.put("totalShops", reportService.getTotalShops());
        data.put("topProducts", reportService.getTopSellingProducts(10));
        data.put("topShops", reportService.getTopShopsByOrderCount(10));
        return ResponseUtil.success(data);
    }

    /**
     * 获取所有店铺列表
     * @param session HTTP会话
     * @return 店铺列表
     */
    @GetMapping("/shops")
    public Object getAllShops(HttpSession session) {
        if (!SessionUtil.isOperator(session)) {
            return ResponseUtil.noPermission();
        }
        List<Shop> shops = operatorService.getAllShops();
        return ResponseUtil.success(shops);
    }

    /**
     * 获取所有用户列表（按类型分组）
     * @param session HTTP会话
     * @return 用户列表（店铺用户和顾客用户）
     */
    @GetMapping("/users")
    public Object getAllUsers(HttpSession session) {
        if (!SessionUtil.isOperator(session)) {
            return ResponseUtil.noPermission();
        }
        Map<String, List<User>> data = new HashMap<>();
        data.put("shops", operatorService.getUsersByType(User.TYPE_SHOP));
        data.put("customers", operatorService.getUsersByType(User.TYPE_CUSTOMER));
        return ResponseUtil.success(data);
    }

    /**
     * 删除用户
     * @param userId 用户ID
     * @param session HTTP会话
     * @return 删除结果
     */
    @DeleteMapping("/users/{userId}")
    public Object deleteUser(@PathVariable Integer userId, HttpSession session) {
        if (!SessionUtil.isOperator(session)) {
            return ResponseUtil.noPermission();
        }
        if (userService.deleteUser(userId)) {
            return ResponseUtil.success(null, "删除成功");
        } else {
            return ResponseUtil.error("用户不存在");
        }
    }

    /**
     * 删除店铺（需要二次确认和管理员密码）
     * @param shopId 店铺ID
     * @param requestBody 请求体（包含管理员密码）
     * @param session HTTP会话
     * @return 删除结果
     */
    @DeleteMapping("/shops/{shopId}")
    public Object deleteShop(@PathVariable Integer shopId, @RequestBody(required = false) Map<String, String> requestBody, HttpSession session) {
        if (!SessionUtil.isOperator(session)) {
            return ResponseUtil.noPermission();
        }
        
        // 验证管理员密码
        String password = requestBody != null ? requestBody.get("password") : null;
        if (password == null || password.isEmpty()) {
            return ResponseUtil.error("请输入管理员密码");
        }
        
        // 验证密码是否正确
        User currentUser = SessionUtil.getCurrentUser(session);
        Optional<User> currentUserOpt = userService.findById(currentUser.getUserId());
        if (currentUserOpt.isEmpty() || !currentUserOpt.get().getPassword().equals(password)) {
            return ResponseUtil.error("密码错误");
        }
        
        // 执行删除
        String resultMessage = shopService.deleteShop(shopId);
        if ("删除成功".equals(resultMessage)) {
            return ResponseUtil.success(null, resultMessage);
        } else {
            return ResponseUtil.error(resultMessage);
        }
    }
}

