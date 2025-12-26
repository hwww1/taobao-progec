package com.taobaodemo.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品实体类
 * 对应数据库product表，存储商品基本信息
 */
@Entity
@Table(name = "product")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "shop_id", nullable = false)
    private Integer shopId; // 所属店铺ID

    @Column(name = "name", nullable = false, length = 200)
    private String name; // 商品名称

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 商品描述

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // 商品价格

    @Column(name = "stock", nullable = false)
    private Integer stock = 0; // 库存数量

    @Column(name = "image_url", length = 255)
    private String imageUrl; // 商品图片URL

    @Column(name = "is_on_sale", nullable = false)
    private Boolean isOnSale = true; // 是否在售

    @Column(name = "category_id", nullable = false)
    private Integer categoryId; // 分类ID
}

