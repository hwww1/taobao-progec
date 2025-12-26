package com.taobaodemo.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 用户地址实体类
 * 对应数据库address表，存储用户的收货地址信息
 */
@Entity
@Table(name = "address")
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;

    @Column(name = "user_id", nullable = false)
    private Integer userId; // 用户ID

    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName; // 收货人姓名

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone; // 收货人电话

    @Column(name = "province", nullable = false, length = 50)
    private String province; // 省份

    @Column(name = "city", nullable = false, length = 50)
    private String city; // 城市

    @Column(name = "district", nullable = false, length = 50)
    private String district; // 区/县

    @Column(name = "detail_address", nullable = false, length = 200)
    private String detailAddress; // 详细地址
}

