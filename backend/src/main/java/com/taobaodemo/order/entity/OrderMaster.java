package com.taobaodemo.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 订单主表实体类
 * 对应数据库ordermaster表，存储订单基本信息
 */
@Entity
@Table(name = "ordermaster")
@Data
public class OrderMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId; // 顾客ID

    @Column(name = "shop_id")
    private Integer shopId; // 店铺ID（允许为null，店铺删除后设为null）

    @Column(name = "order_date", nullable = false)
    private Timestamp orderDate; // 订单创建时间

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // 订单总金额

    @Column(name = "status", nullable = false, length = 50)
    private String status; // 订单状态

    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName; // 收货人姓名

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone; // 收货人电话

    @Column(name = "receiver_address", nullable = false, length = 500)
    private String receiverAddress; // 收货地址（完整地址字符串）

    @Column(name = "product_id", nullable = false)
    private Integer productId; // 商品ID（合并订单项）

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 购买数量（合并订单项）

    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase; // 购买时的商品价格（快照，合并订单项）

    // 订单状态常量
    public static final String STATUS_PENDING_PAYMENT = "待支付";
    public static final String STATUS_PENDING_SHIPMENT = "待发货";
    public static final String STATUS_PENDING_RECEIPT = "待收货";
    public static final String STATUS_COMPLETED = "已完成";
    public static final String STATUS_CANCELLED = "已取消";
}

