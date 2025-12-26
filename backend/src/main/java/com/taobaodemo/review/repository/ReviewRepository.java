package com.taobaodemo.review.repository;

import com.taobaodemo.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductIdOrderByReviewDateDesc(Integer productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.customerId = :customerId")
    long countByProductIdAndCustomerId(@Param("productId") Integer productId, @Param("customerId") Integer customerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double getAverageRatingByProductId(@Param("productId") Integer productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId")
    long countByProductId(@Param("productId") Integer productId);

    @Query("SELECT COUNT(om) FROM OrderMaster om " +
           "WHERE om.productId = :productId AND om.customerId = :customerId " +
           "AND om.status = '已完成'")
    long countPurchasesByProductIdAndCustomerId(@Param("productId") Integer productId, @Param("customerId") Integer customerId);
}

