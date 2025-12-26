package com.taobaodemo.operator.service;

import com.taobaodemo.auth.repository.UserRepository;
import com.taobaodemo.order.entity.OrderMaster;
import com.taobaodemo.order.repository.OrderMasterRepository;
import com.taobaodemo.product.entity.Product;
import com.taobaodemo.product.repository.ProductRepository;
import com.taobaodemo.shop.entity.Shop;
import com.taobaodemo.shop.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 报表服务类
 * 处理运营商仪表板的数据统计：热销商品、热门店铺、用户数、店铺数
 */
@Service
public class ReportService {
    @Autowired
    private OrderMasterRepository orderMasterRepository;


    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取热销商品TOP N
     * @param limit 返回数量
     * @return 商品名称和销量的Map（按销量降序）
     */
    public Map<String, Integer> getTopSellingProducts(int limit) {
        // 1. 获取所有已完成的订单
        List<OrderMaster> completedOrders = orderMasterRepository.findByStatusInOrderByOrderDateDesc(
            Arrays.asList(OrderMaster.STATUS_COMPLETED)
        );

        // 2. 统计每个商品的销量（合并后，订单信息已包含商品信息）
        Map<Integer, Integer> productSales = new HashMap<>();
        for (OrderMaster order : completedOrders) {
            Integer productId = order.getProductId();
            Integer quantity = order.getQuantity();
            if (productId != null && quantity != null) {
                productSales.put(productId, productSales.getOrDefault(productId, 0) + quantity);
            }
        }

        // 3. 转换为列表并排序
        List<Map.Entry<Integer, Integer>> sortedList = new ArrayList<>(productSales.entrySet());
        sortedList.sort((a, b) -> b.getValue() - a.getValue());

        // 4. 取前N个，转换为商品名称
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < limit && i < sortedList.size(); i++) {
            Map.Entry<Integer, Integer> entry = sortedList.get(i);
            Integer productId = entry.getKey();
            Integer sales = entry.getValue();
            
            Product product = productRepository.findById(productId).orElse(null);
            String productName = product != null ? product.getName() : "未知商品";
            
            result.put(productName, sales);
        }

        return result;
    }

    /**
     * 获取热门店铺TOP N（按订单数）
     * @param limit 返回数量
     * @return 店铺名称和订单数的Map（按订单数降序）
     */
    public Map<String, Integer> getTopShopsByOrderCount(int limit) {
        // 1. 获取所有已完成的订单
        List<OrderMaster> completedOrders = orderMasterRepository.findByStatusInOrderByOrderDateDesc(
            Arrays.asList(OrderMaster.STATUS_COMPLETED)
        );

        // 2. 统计每个店铺的订单数
        Map<Integer, Integer> shopOrderCounts = new HashMap<>();
        for (OrderMaster order : completedOrders) {
            Integer shopId = order.getShopId();
            if (shopId != null) {
                shopOrderCounts.put(shopId, shopOrderCounts.getOrDefault(shopId, 0) + 1);
            }
        }

        // 3. 转换为列表并排序
        List<Map.Entry<Integer, Integer>> sortedList = new ArrayList<>(shopOrderCounts.entrySet());
        sortedList.sort((a, b) -> b.getValue() - a.getValue());

        // 4. 取前N个，转换为店铺名称
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < limit && i < sortedList.size(); i++) {
            Map.Entry<Integer, Integer> entry = sortedList.get(i);
            Integer shopId = entry.getKey();
            Integer orderCount = entry.getValue();
            
            Shop shop = shopRepository.findById(shopId).orElse(null);
            String shopName = shop != null ? shop.getShopName() : "未知店铺";
            
            result.put(shopName, orderCount);
        }

        return result;
    }

    /**
     * 获取总用户数（只统计未删除的）
     * @return 用户总数
     */
    public long getTotalUsers() {
        return userRepository.countByIsDeletedFalse();
    }

    /**
     * 获取总店铺数（只统计未删除的）
     * @return 店铺总数
     */
    public long getTotalShops() {
        return shopRepository.countByIsDeletedFalse();
    }
}

