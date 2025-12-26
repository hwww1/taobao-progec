package com.taobaodemo.operator.service;

import com.taobaodemo.auth.entity.User;
import com.taobaodemo.auth.repository.UserRepository;
import com.taobaodemo.shop.entity.Shop;
import com.taobaodemo.shop.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 运营商服务类
 * 处理运营商管理相关的业务逻辑：用户查询、店铺查询
 */
@Service
public class OperatorService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    /**
     * 根据用户类型获取用户列表（只返回未删除的）
     * @param userType 用户类型（1:运营商, 2:店铺, 3:顾客, 4:浏览者）
     * @return 用户列表
     */
    public List<User> getUsersByType(Integer userType) {
        return userRepository.findByUserTypeAndIsDeletedFalse(userType);
    }

    /**
     * 获取所有店铺（只返回未删除的）
     * @return 店铺列表
     */
    public List<Shop> getAllShops() {
        return shopRepository.findByIsDeletedFalse();
    }
}

