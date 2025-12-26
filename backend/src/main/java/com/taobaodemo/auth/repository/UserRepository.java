package com.taobaodemo.auth.repository;

import com.taobaodemo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsernameAndIsDeletedFalse(String username); // 查找未删除的用户
    List<User> findByUserTypeAndIsDeletedFalse(Integer userType); // 查找未删除的用户
    boolean existsByUsernameAndIsDeletedFalse(String username); // 检查未删除的用户名是否存在
    long countByIsDeletedFalse(); // 统计未删除的用户
}

