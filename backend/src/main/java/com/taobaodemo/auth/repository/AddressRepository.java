package com.taobaodemo.auth.repository;

import com.taobaodemo.auth.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    // 返回指定用户的全部地址（不强制排序，按数据库默认顺序）
    List<Address> findByUserId(Integer userId);
}

