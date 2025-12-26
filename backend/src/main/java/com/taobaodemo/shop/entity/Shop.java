package com.taobaodemo.shop.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 店铺实体类
 * 对应数据库shop表，存储店铺基本信息
 */
@Entity
@Table(name = "shop")
@Data
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_id")
    private Integer shopId;

    @Column(name = "shop_name", unique = true, nullable = false, length = 100)
    private String shopName; // 店铺名称

    @Column(name = "user_id", unique = true, nullable = false)
    private Integer userId; // 店铺所属用户ID（关联到user表）

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false; // 是否已删除（软删除标记）
}

