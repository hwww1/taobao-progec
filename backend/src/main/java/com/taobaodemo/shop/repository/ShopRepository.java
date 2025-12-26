package com.taobaodemo.shop.repository;

import com.taobaodemo.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {
    Optional<Shop> findByUserIdAndIsDeletedFalse(Integer userId); // 查找未删除的店铺
    boolean existsByShopNameAndIsDeletedFalse(String shopName); // 检查未删除的店铺名是否存在
    List<Shop> findByIsDeletedFalse(); // 查找未删除的店铺
    long countByIsDeletedFalse(); // 统计未删除的店铺
}

