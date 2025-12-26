package com.taobaodemo.order.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CreateOrderRequest {
    // 购物车商品列表
    // key: 商品ID   value: 购买数量
    // 可以简单理解为：{ 商品1: 买几件, 商品2: 买几件 }
    private Map<Integer, Integer> cartItems; // 商品ID -> 数量
    private Integer addressId; // 收货地址ID（从个人信息页面选择的地址）
}

