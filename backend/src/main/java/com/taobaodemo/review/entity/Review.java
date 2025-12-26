package com.taobaodemo.review.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Entity
@Table(name = "review")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "review_date", nullable = false)
    private Timestamp reviewDate;

    // 非持久化字段：用于前端展示用户名
    @Transient
    private String customerName;
}

