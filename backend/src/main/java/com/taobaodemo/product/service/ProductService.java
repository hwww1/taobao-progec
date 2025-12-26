package com.taobaodemo.product.service;

import com.taobaodemo.product.entity.Product;
import com.taobaodemo.product.repository.ProductRepository;
import com.taobaodemo.shop.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 商品服务类
 * 处理商品相关的业务逻辑：添加、更新、查询、库存管理
 */
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShopRepository shopRepository;

    /**
     * 验证商品信息（只保留必要的验证）
     * @param product 商品信息
     * @throws IllegalArgumentException 如果验证失败
     */
    private void validateProduct(Product product) {
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("价格必须大于0");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new IllegalArgumentException("库存不能为负数");
        }
    }

    /**
     * 添加商品
     * @param product 商品信息
     * @return 保存后的商品
     */
    public Product addProduct(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    /**
     * 更新商品信息
     * @param product 商品信息
     * @return 更新后的商品
     */
    public Product updateProduct(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    /**
     * 根据ID获取商品
     * @param productId 商品ID
     * @return 商品信息
     */
    public Optional<Product> getProductById(Integer productId) {
        return productRepository.findById(productId);
    }

    /**
     * 获取指定店铺的所有商品
     * @param shopId 店铺ID
     * @return 商品列表
     */
    public List<Product> getProductsByShopId(Integer shopId) {
        return productRepository.findByShopIdOrderByProductIdDesc(shopId);
    }

    /**
     * 过滤掉店铺已删除的商品
     * @param products 商品列表
     * @return 过滤后的商品列表
     */
    private List<Product> filterProductsByShopExists(List<Product> products) {
        List<Product> result = new ArrayList<>();
        for (Product product : products) {
            if (shopRepository.existsById(product.getShopId())) {
                result.add(product);
            }
        }
        return result;
    }

    /**
     * 获取所有在售商品（过滤掉店铺已删除的商品）
     * @return 在售商品列表
     */
    public List<Product> getAllOnSaleProducts() {
        return filterProductsByShopExists(
                productRepository.findByIsOnSaleTrueOrderByProductIdDesc()
        );
    }

    /**
     * 获取指定分类的在售商品
     * @param categoryId 分类ID
     * @return 商品列表
     */
    public List<Product> getOnSaleProductsByCategory(Integer categoryId) {
        return filterProductsByShopExists(
                productRepository.findByIsOnSaleTrueAndCategoryIdOrderByProductIdDesc(categoryId)
        );
    }

    /**
     * 搜索在售商品（过滤掉店铺已删除的商品）
     * @param keyword 搜索关键词
     * @return 匹配的商品列表
     */
    public List<Product> searchOnSaleProducts(String keyword) {
        List<Product> products;
        if (keyword == null || keyword.trim().isEmpty()) {
            products = productRepository.findByIsOnSaleTrueOrderByProductIdDesc();
        } else {
            products = productRepository.searchProducts(keyword);
        }
        return filterProductsByShopExists(products);
    }

    /**
     * 在指定分类下搜索在售商品
     * @param categoryId 分类ID
     * @param keyword 关键词
     * @return 商品列表
     */
    public List<Product> searchOnSaleProductsByCategory(Integer categoryId, String keyword) {
        List<Product> products;
        if (keyword == null || keyword.trim().isEmpty()) {
            products = productRepository.findByIsOnSaleTrueAndCategoryIdOrderByProductIdDesc(categoryId);
        } else {
            products = productRepository.searchProductsByCategory(categoryId, keyword);
        }
        return filterProductsByShopExists(products);
    }

    /**
     * 扣减商品库存
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 扣减成功返回true，库存不足或商品不存在返回false
     */
    @Transactional
    public boolean deductStock(Integer productId, Integer quantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null && product.getStock() >= quantity) {
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);
            return true;
        }
        return false;
    }

    /**
     * 检查店铺是否存在
     * @param shopId 店铺ID
     * @return 存在返回true，不存在返回false
     */
    public boolean isShopExists(Integer shopId) {
        return shopRepository.existsById(shopId);
    }
}

