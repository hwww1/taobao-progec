package com.taobaodemo.product.repository;

import com.taobaodemo.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByShopIdOrderByProductIdDesc(Integer shopId);
    List<Product> findByIsOnSaleTrueOrderByProductIdDesc();
    List<Product> findByIsOnSaleTrueAndCategoryIdOrderByProductIdDesc(Integer categoryId);

    @Query("SELECT p FROM Product p WHERE p.isOnSale = true AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%) ORDER BY p.productId DESC")
    List<Product> searchProducts(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.isOnSale = true AND p.categoryId = :categoryId AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%) ORDER BY p.productId DESC")
    List<Product> searchProductsByCategory(@Param("categoryId") Integer categoryId, @Param("keyword") String keyword);
}

