package com.taobaodemo.product.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 商品分类
 */
@Entity
@Table(name = "category")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name; // 分类名称

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0; // 排序号，数字越小越靠前
}


