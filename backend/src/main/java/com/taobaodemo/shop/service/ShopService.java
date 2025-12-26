package com.taobaodemo.shop.service;

import com.taobaodemo.shop.entity.Shop;
import com.taobaodemo.shop.repository.ShopRepository;
import com.taobaodemo.product.entity.Product;
import com.taobaodemo.product.repository.ProductRepository;
import com.taobaodemo.auth.entity.User;
import com.taobaodemo.auth.repository.UserRepository;
import com.taobaodemo.order.repository.OrderMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 店铺服务类
 * 处理店铺相关的业务逻辑：查询、更新、删除
 */
@Service
public class ShopService {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderMasterRepository orderMasterRepository;

    /**
     * 根据ID获取店铺（只返回未删除的）
     * @param shopId 店铺ID
     * @return 店铺信息
     */
    public Optional<Shop> getShopById(Integer shopId) {
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop != null && !shop.getIsDeleted()) {
            return Optional.of(shop);
        }
        return Optional.empty();
    }

    /**
     * 根据用户ID获取店铺（只返回未删除的）
     * @param userId 用户ID
     * @return 店铺信息
     */
    public Optional<Shop> getShopByUserId(Integer userId) {
        return shopRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    /**
     * 获取所有店铺（只返回未删除的）
     * @return 店铺列表
     */
    public List<Shop> getAllShops() {
        return shopRepository.findByIsDeletedFalse();
    }

    /**
     * 更新店铺信息
     * @param shop 店铺信息
     * @return 更新后的店铺
     */
    public Shop updateShop(Shop shop) {
        return shopRepository.save(shop);
    }

    /**
     * 检查店铺名称是否存在（未删除的店铺）
     * @param shopName 店铺名称
     * @return 存在返回true，不存在返回false
     */
    public boolean shopNameExists(String shopName) {
        return shopRepository.existsByShopNameAndIsDeletedFalse(shopName);
    }

    /**
     * 软删除店铺（标记为已删除，并修改店铺名以释放店铺名）
     * 删除流程：1.商品标记为下架 2.订单shopId设为null 3.标记店铺为已删除 4.修改店铺名 5.软删除关联的用户
     * 注意：保留商品和评价等历史数据，不删除它们。删除店铺时会同时软删除关联的店铺用户。
     * @param shopId 店铺ID
     * @return 删除结果信息
     */
    @Transactional
    public String deleteShop(Integer shopId) {
        try {
            // 1. 检查店铺是否存在且未删除
            Shop shop = shopRepository.findById(shopId).orElse(null);
            if (shop == null) {
                return "店铺不存在";
            }
            if (shop.getIsDeleted()) {
                return "店铺已被删除";
            }

            // 2. 将商品标记为下架（保留商品数据，用户仍可查看历史订单和评价）
            List<Product> products = productRepository.findByShopIdOrderByProductIdDesc(shopId);
            for (Product product : products) {
                product.setIsOnSale(false);
                productRepository.save(product);
            }

            // 3. 将订单的shopId设为null（使用原生SQL，保留订单历史数据）
            try {
                orderMasterRepository.setShopIdToNullByShopId(shopId);
            } catch (Exception e) {
                // 如果SQL执行失败，记录日志但不影响删除流程
                e.printStackTrace();
            }

            // 4. 软删除：标记店铺为已删除
            shop.setIsDeleted(true);
            
            // 5. 修改店铺名，释放店铺名（格式：deleted_原店铺名_时间戳）
            String originalShopName = shop.getShopName();
            String newShopName = "deleted_" + originalShopName + "_" + System.currentTimeMillis();
            shop.setShopName(newShopName);
            shopRepository.save(shop);
            
            // 6. 软删除关联的用户（店铺用户应该和店铺一起被删除）
            User user = userRepository.findById(shop.getUserId()).orElse(null);
            if (user != null) {
                // 软删除用户：标记为已删除
                user.setIsDeleted(true);
                // 修改用户名，释放用户名（格式：deleted_原用户名_时间戳）
                String originalUsername = user.getUsername();
                String newUsername = "deleted_" + originalUsername + "_" + System.currentTimeMillis();
                user.setUsername(newUsername);
                userRepository.save(user);
            }

            return "删除成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "删除失败: " + e.getMessage();
        }
    }
}

