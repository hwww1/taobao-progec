package com.taobaodemo.order.controller;

import com.taobaodemo.order.dto.CreateOrderRequest;
import com.taobaodemo.order.entity.OrderMaster;
import com.taobaodemo.order.service.OrderService;
import com.taobaodemo.shop.service.ShopService;
import com.taobaodemo.shop.entity.Shop;
import com.taobaodemo.auth.entity.Address;
import com.taobaodemo.auth.service.AddressService;
import com.taobaodemo.common.util.ResponseUtil;
import com.taobaodemo.common.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 * 处理订单相关的API请求：创建订单、查询订单、订单状态更新
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private AddressService addressService;

    /**
     * 从购物车创建订单
     * @param request 订单创建请求（包含购物车商品和地址信息）
     * @param session HTTP会话
     * @return 订单ID或错误信息
     */
    @PostMapping
    public Object createOrder(@RequestBody CreateOrderRequest request, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) {
            return ResponseUtil.notLoggedIn();
        }
        
        // 验证是否选择了地址
        if (request.getAddressId() == null) {
            return ResponseUtil.error("请选择收货地址");
        }
        
        // 查询地址信息
        Address address = addressService.getAddressById(request.getAddressId()).orElse(null);
        if (address == null) {
            return ResponseUtil.error("地址不存在");
        }
        
        // 验证地址是否属于当前用户
        Integer currentUserId = SessionUtil.getCurrentUser(session).getUserId();
        if (!address.getUserId().equals(currentUserId)) {
            return ResponseUtil.error("无权使用此地址");
        }
        
        // 创建订单（使用地址信息）
        Integer orderId = orderService.createOrderFromCart(
            currentUserId, 
            request.getCartItems(),
            address.getReceiverName(),
            address.getReceiverPhone(),
            address.getProvince() + " " + address.getCity() + " " + address.getDistrict() + " " + address.getDetailAddress()
        );
        return ResponseUtil.success(orderId);
    }

    /**
     * 获取顾客的所有订单
     * @param session HTTP会话
     * @return 订单列表
     */
    @GetMapping("/customer")
    public Object getCustomerOrders(HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) {
            return ResponseUtil.notLoggedIn();
        }
        List<OrderMaster> orders = orderService.getOrdersByCustomerId(SessionUtil.getCurrentUser(session).getUserId());
        return ResponseUtil.success(orders);
    }

    /**
     * 获取店铺的所有订单（仅店铺用户）
     * @param session HTTP会话
     * @return 订单列表
     */
    /**
     * 获取当前登录用户的店铺ID
     * @param session HTTP会话
     * @return 店铺ID，如果不存在则返回null
     */
    private Integer getCurrentShopId(HttpSession session) {
        Shop shop = shopService.getShopByUserId(SessionUtil.getCurrentUser(session).getUserId()).orElse(null);
        return shop != null ? shop.getShopId() : null;
    }

    @GetMapping("/shop")
    public Object getShopOrders(HttpSession session) {
        if (!SessionUtil.isShopUser(session)) {
            return ResponseUtil.noPermission();
        }
        Integer shopId = getCurrentShopId(session);
        if (shopId == null) {
            return ResponseUtil.error("店铺不存在");
        }
        List<OrderMaster> orders = orderService.getOrdersByShopId(shopId);
        return ResponseUtil.success(orders);
    }

    /**
     * 根据ID获取订单详情
     * @param id 订单ID
     * @return 订单信息或错误提示
     */
    @GetMapping("/{id}")
    public Object getOrderById(@PathVariable Integer id) {
        OrderMaster order = orderService.getOrderById(id).orElse(null);
        if (order != null) {
            return ResponseUtil.success(order);
        } else {
            return ResponseUtil.error("订单不存在");
        }
    }


    /**
     * 处理订单支付
     * @param id 订单ID
     * @return 支付结果
     */
    @PostMapping("/{id}/payment")
    public Object processPayment(@PathVariable Integer id) {
        if (orderService.processPayment(id)) {
            return ResponseUtil.success(null, "支付成功");
        } else {
            return ResponseUtil.error("支付失败");
        }
    }

    /**
     * 处理订单发货（仅店铺用户）
     * @param id 订单ID
     * @param session HTTP会话
     * @return 发货结果
     */
    @PostMapping("/{id}/shipment")
    public Object processShipment(@PathVariable Integer id, HttpSession session) {
        if (!SessionUtil.isShopUser(session)) {
            return ResponseUtil.noPermission();
        }
        Integer shopId = getCurrentShopId(session);
        if (shopId == null) {
            return ResponseUtil.error("店铺不存在");
        }
        if (orderService.processShipment(id, shopId)) {
            return ResponseUtil.success(null, "发货成功");
        } else {
            return ResponseUtil.error("发货失败");
        }
    }

    /**
     * 确认收货
     * @param id 订单ID
     * @param session HTTP会话
     * @return 确认收货结果
     */
    @PostMapping("/{id}/receipt")
    public Object confirmReceipt(@PathVariable Integer id, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) {
            return ResponseUtil.notLoggedIn();
        }
        if (orderService.confirmReceipt(id, SessionUtil.getCurrentUser(session).getUserId())) {
            return ResponseUtil.success(null, "确认收货成功");
        } else {
            return ResponseUtil.error("确认收货失败");
        }
    }

    /**
     * 取消订单（仅限待支付和待发货状态）
     * @param id 订单ID
     * @param session HTTP会话
     * @return 取消结果
     */
    @PostMapping("/{id}/cancel")
    public Object cancelOrder(@PathVariable Integer id, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) {
            return ResponseUtil.notLoggedIn();
        }
        if (orderService.cancelOrder(id, SessionUtil.getCurrentUser(session).getUserId())) {
            return ResponseUtil.success(null, "订单取消成功");
        } else {
            return ResponseUtil.error("订单取消失败，只能取消待支付或待发货的订单");
        }
    }
}

