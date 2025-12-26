package com.taobaodemo.review.controller;

import com.taobaodemo.auth.entity.User;
import com.taobaodemo.review.entity.Review;
import com.taobaodemo.review.service.ReviewService;
import com.taobaodemo.common.util.ResponseUtil;
import com.taobaodemo.common.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品评价控制器
 * 处理商品评价相关的API请求：查询评价、添加评价、更新评价、删除评价
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    /**
     * 获取指定商品的所有评价
     * @param productId 商品ID
     * @return 评价列表
     */
    @GetMapping("/product/{productId}")
    public Object getReviewsByProductId(@PathVariable Integer productId) {
        return ResponseUtil.success(reviewService.getReviewsByProductId(productId));
    }

    /**
     * 获取商品的评价统计信息
     * @param productId 商品ID
     * @return 平均评分和评价数量
     */
    @GetMapping("/product/{productId}/stats")
    public Object getProductReviewStats(@PathVariable Integer productId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", reviewService.getAverageRating(productId));
        stats.put("reviewCount", reviewService.getReviewCount(productId));
        return ResponseUtil.success(stats);
    }

    /**
     * 添加商品评价（仅已购买且未评价的用户）
     * @param review 评价信息
     * @param session HTTP会话
     * @return 添加结果
     */
    @PostMapping
    public Object addReview(@RequestBody Review review, HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            return ResponseUtil.notLoggedIn();
        }
        review.setCustomerId(user.getUserId());
        try {
            Review savedReview = reviewService.addReview(review);
            // 返回时附带用户名，前端直接展示
            savedReview.setCustomerName(user.getUsername());
            return ResponseUtil.success(savedReview);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 更新评价（仅评价作者本人）
     * @param reviewId 评价ID
     * @param review 更新的评价信息
     * @param session HTTP会话
     * @return 更新结果
     */
    @PutMapping("/{reviewId}")
    public Object updateReview(@PathVariable Integer reviewId, @RequestBody Review review, HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            return ResponseUtil.notLoggedIn();
        }
        try {
            Review updated = reviewService.updateReview(reviewId, user.getUserId(), review.getRating(), review.getContent());
            return ResponseUtil.success(updated);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 删除评价（仅评价作者本人）
     * @param reviewId 评价ID
     * @param session HTTP会话
     * @return 删除结果
     */
    @DeleteMapping("/{reviewId}")
    public Object deleteReview(@PathVariable Integer reviewId, HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            return ResponseUtil.notLoggedIn();
        }
        try {
            reviewService.deleteReview(reviewId, user.getUserId());
            return ResponseUtil.success(null);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 检查用户是否可以评价该商品
     * 简化逻辑：只要登录就允许评价，hasReviewed/hasPurchased 供前端展示用
     */
    @GetMapping("/product/{productId}/can-review")
    public Object canReview(@PathVariable Integer productId, HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            return ResponseUtil.error("请先登录");
        }
        Map<String, Boolean> data = new HashMap<>();
        data.put("hasReviewed", reviewService.hasUserReviewed(productId, user.getUserId()));
        data.put("hasPurchased", true);
        return ResponseUtil.success(data);
    }
}

