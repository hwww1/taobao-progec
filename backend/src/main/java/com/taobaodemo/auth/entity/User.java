package com.taobaodemo.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

/**
 * 用户实体类
 * 对应数据库user表，存储用户基本信息
 */
@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @JsonIgnore // 密码不序列化到JSON响应中
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "user_type", nullable = false)
    private Integer userType; // 1:运营商, 2:店铺, 3:顾客, 4:浏览者

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false; // 是否已删除（软删除标记）

    // 用户类型常量
    public static final int TYPE_OPERATOR = 1;
    public static final int TYPE_SHOP = 2;
    public static final int TYPE_CUSTOMER = 3;
    public static final int TYPE_BROWSER = 4;

    /**
     * 判断是否为店铺用户
     * @return 是店铺用户返回true
     */
    public boolean isShopUser() {
        return this.userType != null && this.userType == TYPE_SHOP;
    }
}

