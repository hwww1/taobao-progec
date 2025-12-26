package com.taobaodemo.order.service;

import com.taobaodemo.order.entity.OrderMaster;
import com.taobaodemo.product.entity.Product;
import com.taobaodemo.order.repository.OrderMasterRepository;
import com.taobaodemo.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 订单服务类
 * 处理订单相关的业务逻辑：创建订单、订单状态更新、订单查询
 */
@Service
public class OrderService {
    @Autowired
    private OrderMasterRepository orderMasterRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 从购物车创建订单（每个商品生成一条订单记录）
     *
     * @param customerId      顾客ID
     * @param cartItems       购物车商品（商品ID -> 数量）
     *                        可以简单理解为：
     *                        例如 {1:2, 5:3} 表示
     *                        商品ID=1 买2件，商品ID=5 买3件
     * @param receiverName    收货人姓名
     * @param receiverPhone   收货人电话
     * @param receiverAddress 收货地址
     * @return 第一个创建成功的订单ID
     */
    @Transactional
    public Integer createOrderFromCart(Integer customerId, Map<Integer, Integer> cartItems, 
                                       String receiverName, String receiverPhone, String receiverAddress) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("购物车为空");
        }

        // 创建订单
        Integer shopId = null;          // 当前这批订单所属的店铺ID
        Integer firstOrderId = null;    // 记录第一条订单的ID，方便返回
        Timestamp orderDate = new Timestamp(System.currentTimeMillis()); // 同一批订单使用相同时间
        
        // 遍历购物车里的每一件商品
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            Integer productId = entry.getKey();    // 商品ID
            Integer quantity = entry.getValue();   // 购买数量

            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                // 商品不存在就跳过这条（也可以根据需要抛异常）
                continue;
            }
            
            if (shopId == null) {
                shopId = product.getShopId();
            }

            // 更新库存
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);

            // 创建订单（每个商品一个订单）
            OrderMaster order = new OrderMaster();
            order.setCustomerId(customerId);
            order.setShopId(shopId);
            order.setTotalAmount(product.getPrice().multiply(new BigDecimal(quantity)));
            order.setStatus(OrderMaster.STATUS_PENDING_PAYMENT);
            order.setOrderDate(orderDate); // 同一批订单使用相同时间
            order.setReceiverName(receiverName);
            order.setReceiverPhone(receiverPhone);
            order.setReceiverAddress(receiverAddress);
            order.setProductId(productId);
            order.setQuantity(quantity);
            order.setPriceAtPurchase(product.getPrice());
            
            order = orderMasterRepository.save(order);
            if (firstOrderId == null) {
                firstOrderId = order.getOrderId();
            }
        }
        
        return firstOrderId; // 返回第一个订单ID
    }

    /**
     * 处理订单支付（将状态从"待支付"改为"待发货"）
     * @param orderId 订单ID
     * @return 支付成功返回true，失败返回false
     */
    public boolean processPayment(Integer orderId) {
        OrderMaster order = orderMasterRepository.findById(orderId).orElse(null);
        if (order != null && OrderMaster.STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            order.setStatus(OrderMaster.STATUS_PENDING_SHIPMENT);
            orderMasterRepository.save(order);
            return true;
        }
        return false;
    }

    /**
     * 确认收货（将状态从"待收货"改为"已完成"）
     * @param orderId 订单ID
     * @param customerId 顾客ID（验证订单归属）
     * @return 确认成功返回true，失败返回false
     */
    public boolean confirmReceipt(Integer orderId, Integer customerId) {
        OrderMaster order = orderMasterRepository.findById(orderId).orElse(null);
        if (order != null && order.getCustomerId().equals(customerId) &&
            OrderMaster.STATUS_PENDING_RECEIPT.equals(order.getStatus())) {
            order.setStatus(OrderMaster.STATUS_COMPLETED);
            orderMasterRepository.save(order);
            return true;
        }
        return false;
    }

    /**
     * 获取顾客的所有订单
     * @param customerId 顾客ID
     * @return 订单列表
     */
    public List<OrderMaster> getOrdersByCustomerId(Integer customerId) {
        return orderMasterRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    /**
     * 获取店铺的所有订单
     * @param shopId 店铺ID
     * @return 订单列表
     */
    public List<OrderMaster> getOrdersByShopId(Integer shopId) {
        return orderMasterRepository.findByShopIdOrderByOrderDateDesc(shopId);
    }


    /**
     * 处理订单发货（将状态从"待发货"改为"待收货"）
     * @param orderId 订单ID
     * @param shopId 店铺ID（验证订单归属）
     * @return 发货成功返回true，失败返回false
     */
    public boolean processShipment(Integer orderId, Integer shopId) {
        OrderMaster order = orderMasterRepository.findById(orderId).orElse(null);
        if (order != null && order.getShopId() != null && order.getShopId().equals(shopId) &&
            OrderMaster.STATUS_PENDING_SHIPMENT.equals(order.getStatus())) {
            order.setStatus(OrderMaster.STATUS_PENDING_RECEIPT);
            orderMasterRepository.save(order);
            return true;
        }
        return false;
    }

    /**
     * 根据ID获取订单
     * @param orderId 订单ID
     * @return 订单信息
     */
    public Optional<OrderMaster> getOrderById(Integer orderId) {
        return orderMasterRepository.findById(orderId);
    }

    /**
     * 取消订单（仅限待支付和待发货状态）
     * @param orderId 订单ID
     * @param customerId 顾客ID（验证订单归属）
     * @return 取消成功返回true，失败返回false
     */
    @Transactional
    public boolean cancelOrder(Integer orderId, Integer customerId) {
        OrderMaster order = orderMasterRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        
        // 验证订单归属
        if (!order.getCustomerId().equals(customerId)) {
            return false;
        }
        
        // 只能取消待支付或待发货的订单
        if (!OrderMaster.STATUS_PENDING_PAYMENT.equals(order.getStatus()) && 
            !OrderMaster.STATUS_PENDING_SHIPMENT.equals(order.getStatus())) {
            return false;
        }
        
        // 如果订单是待发货状态（已付款），需要恢复库存
        if (OrderMaster.STATUS_PENDING_SHIPMENT.equals(order.getStatus())) {
            Product product = productRepository.findById(order.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + order.getQuantity());
                productRepository.save(product);
            }
        }
        
        // 更新订单状态为已取消
        order.setStatus(OrderMaster.STATUS_CANCELLED);
        orderMasterRepository.save(order);
        return true;
    }
}

