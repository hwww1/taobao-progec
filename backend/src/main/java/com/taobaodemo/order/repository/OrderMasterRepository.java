package com.taobaodemo.order.repository;

import com.taobaodemo.order.entity.OrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderMasterRepository extends JpaRepository<OrderMaster, Integer> {
    List<OrderMaster> findByCustomerIdOrderByOrderDateDesc(Integer customerId);
    List<OrderMaster> findByShopIdOrderByOrderDateDesc(Integer shopId);
    List<OrderMaster> findByStatusInOrderByOrderDateDesc(List<String> statuses);
    
    @Modifying
    @Query(value = "UPDATE ordermaster SET shop_id = NULL WHERE shop_id = :shopId", nativeQuery = true)
    void setShopIdToNullByShopId(@Param("shopId") Integer shopId);
}

