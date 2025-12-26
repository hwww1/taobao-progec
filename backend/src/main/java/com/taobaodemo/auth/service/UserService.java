package com.taobaodemo.auth.service;

import com.taobaodemo.auth.entity.User;
import com.taobaodemo.auth.repository.UserRepository;
import com.taobaodemo.shop.entity.Shop;
import com.taobaodemo.shop.repository.ShopRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户服务类
 * 处理用户相关的业务逻辑：登录、注册、查询、删除
 */
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    /**
     * 用户登录验证（只验证未删除的用户）
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户信息，失败返回空
     */
    public Optional<User> login(String username, String password) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(username).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * 检查用户名是否存在（未删除的用户）
     * @param username 用户名
     * @return 存在返回true，不存在返回false
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsernameAndIsDeletedFalse(username);
    }

    /**
     * 用户注册（只检查未删除的用户名）
     * @param user 用户信息
     * @return 注册成功返回true，失败返回false
     */
    @Transactional
    public boolean register(User user) {
        // 检查用户名是否已被未删除的用户使用
        if (userRepository.existsByUsernameAndIsDeletedFalse(user.getUsername())) {
            return false;
        }

        // 如果是店铺用户，需要创建店铺
        if (user.getUserType() == User.TYPE_SHOP) {
            return registerShopUser(user);
        } else {
            // 普通用户直接保存
            userRepository.save(user);
            return true;
        }
    }

    /**
     * 注册店铺用户（同时创建店铺）
     * @param user 店铺用户信息
     * @return 注册成功返回true，失败返回false
     */
    @Transactional
    private boolean registerShopUser(User user) {
        try {
            // 先保存用户
            user = userRepository.save(user);

            // 创建店铺（关联到用户）
            Shop shop = new Shop();
            shop.setShopName(user.getUsername() + "的店铺");
            shop.setUserId(user.getUserId());
            shopRepository.save(shop);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据用户ID查找用户
     * @param userId 用户ID
     * @return 用户信息
     */
    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId);
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 修改成功返回true，用户不存在返回false
     */
    @Transactional
    public boolean changePassword(Integer userId, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setPassword(newPassword);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * 更新用户名
     * @param userId 用户ID
     * @param newUsername 新用户名
     * @return 更新成功返回true，用户不存在返回false
     */
    @Transactional
    public boolean updateUsername(Integer userId, String newUsername) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setUsername(newUsername);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * 软删除用户（标记为已删除，并修改用户名以释放用户名）
     * @param userId 用户ID
     * @return 删除成功返回true，用户不存在返回false
     */
    @Transactional
    public boolean deleteUser(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getIsDeleted()) {
            return false;
        }
        
        // 软删除：标记为已删除
        user.setIsDeleted(true);
        
        // 修改用户名，释放用户名（格式：deleted_原用户名_时间戳）
        String originalUsername = user.getUsername();
        String newUsername = "deleted_" + originalUsername + "_" + System.currentTimeMillis();
        user.setUsername(newUsername);
        
        userRepository.save(user);
        return true;
    }
}

