package com.taobaodemo.review.service;

import com.taobaodemo.review.entity.Review;
import com.taobaodemo.review.repository.ReviewRepository;
import com.taobaodemo.auth.entity.User;
import com.taobaodemo.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * 商品评价服务类
 * 处理商品评价相关的业务逻辑：添加、查询、更新、删除评价
 */
@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 添加商品评价
     * @param review 评价信息
     * @return 保存后的评价
     * @throws IllegalArgumentException 如果评分不在1-5之间
     */
    public Review addReview(Review review) {
        // 验证评分范围
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }

        review.setReviewDate(new Timestamp(System.currentTimeMillis()));
        return reviewRepository.save(review);
    }

    /**
     * 获取指定商品的所有评价
     * @param productId 商品ID
     * @return 评价列表（按时间倒序）
     */
    public List<Review> getReviewsByProductId(Integer productId) {
        List<Review> list = reviewRepository.findByProductIdOrderByReviewDateDesc(productId);
        for (Review r : list) {
            User u = userRepository.findById(r.getCustomerId()).orElse(null);
            if (u != null) {
                r.setCustomerName(u.getUsername());
            }
        }
        return list;
    }

    /**
     * 检查用户是否已评价该商品
     * @param productId 商品ID
     * @param customerId 顾客ID
     * @return 已评价返回true，未评价返回false
     */
    public boolean hasUserReviewed(Integer productId, Integer customerId) {
        return reviewRepository.countByProductIdAndCustomerId(productId, customerId) > 0;
    }

    /**
     * 检查用户是否已购买该商品（已完成订单）
     * @param productId 商品ID
     * @param customerId 顾客ID
     * @return 已购买返回true，未购买返回false
     */
    public boolean hasUserPurchased(Integer productId, Integer customerId) {
        return reviewRepository.countPurchasesByProductIdAndCustomerId(productId, customerId) > 0;
    }

    /**
     * 获取商品的平均评分
     * @param productId 商品ID
     * @return 平均评分（无评价返回0.0）
     */
    public Double getAverageRating(Integer productId) {
        Double avg = reviewRepository.getAverageRatingByProductId(productId);
        return avg != null ? avg : 0.0;
    }

    /**
     * 获取商品的评价数量
     * @param productId 商品ID
     * @return 评价数量
     */
    public long getReviewCount(Integer productId) {
        return reviewRepository.countByProductId(productId);
    }

    /**
     * 根据ID获取评价
     * @param reviewId 评价ID
     * @return 评价信息
     */
    public Optional<Review> getReviewById(Integer reviewId) {
        return reviewRepository.findById(reviewId);
    }

    /**
     * 更新评价（仅评价作者本人）
     * @param reviewId 评价ID
     * @param userId 用户ID（验证权限）
     * @param rating 新评分（可选）
     * @param content 新内容（可选）
     * @return 更新后的评价
     * @throws IllegalArgumentException 如果评价不存在、无权限或评分不在1-5之间
     */
    public Review updateReview(Integer reviewId, Integer userId, Integer rating, String content) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }
        // 验证权限：只能修改自己的评价
        if (!review.getCustomerId().equals(userId)) {
            throw new IllegalArgumentException("无权限修改该评价");
        }
        // 验证评分范围
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }
        // 更新评分和内容
        if (rating != null) {
            review.setRating(rating);
        }
        if (content != null) {
            review.setContent(content);
        }
        review.setReviewDate(new Timestamp(System.currentTimeMillis()));
        return reviewRepository.save(review);
    }

    /**
     * 删除评价（仅评价作者本人）
     * @param reviewId 评价ID
     * @param userId 用户ID（验证权限）
     * @throws IllegalArgumentException 如果评价不存在或无权限
     */
    public void deleteReview(Integer reviewId, Integer userId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }
        // 验证权限：只能删除自己的评价
        if (!review.getCustomerId().equals(userId)) {
            throw new IllegalArgumentException("无权限删除该评价");
        }
        reviewRepository.delete(review);
    }
}

