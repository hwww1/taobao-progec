package com.taobaodemo.product.controller;

import com.taobaodemo.product.entity.Product;
import com.taobaodemo.product.service.ProductService;
import com.taobaodemo.shop.service.ShopService;
import com.taobaodemo.shop.entity.Shop;
import com.taobaodemo.common.util.ResponseUtil;
import com.taobaodemo.common.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 * 处理商品相关的API请求：查询、添加、更新
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private ShopService shopService;

    /**
     * 获取所有在售商品（支持关键词、分类筛选）
     * @param keyword 搜索关键词（可选）
     * @param categoryId 分类ID（可选）
     * @return 商品列表
     */
    @GetMapping
    public Object getAllProducts(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Integer categoryId) {
        List<Product> products;
        if (categoryId != null) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 分类 + 关键词
                products = productService.searchOnSaleProductsByCategory(categoryId, keyword);
            } else {
                // 分类筛选
                products = productService.getOnSaleProductsByCategory(categoryId);
            }
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 仅关键词
            products = productService.searchOnSaleProducts(keyword);
        } else {
            products = productService.getAllOnSaleProducts();
        }
        return ResponseUtil.success(products);
    }

    /**
     * 根据ID获取商品详情
     * @param id 商品ID
     * @return 商品信息或错误提示
     */
    @GetMapping("/{id}")
    public Object getProductById(@PathVariable Integer id) {
        Product product = productService.getProductById(id).orElse(null);
        if (product != null) {
            return ResponseUtil.success(product);
        } else {
            return ResponseUtil.error("商品不存在");
        }
    }

    /**
     * 获取指定店铺的所有商品
     * @param shopId 店铺ID
     * @return 商品列表
     */
    @GetMapping("/shop/{shopId}")
    public Object getProductsByShopId(@PathVariable Integer shopId) {
        return ResponseUtil.success(productService.getProductsByShopId(shopId));
    }

    /**
     * 添加商品（仅店铺用户）
     * @param product 商品信息
     * @param session HTTP会话
     * @return 保存后的商品信息或错误提示
     */
    /**
     * 获取当前登录用户的店铺ID
     * @param session HTTP会话
     * @return 店铺ID，如果不存在则返回null
     */
    private Integer getCurrentShopId(HttpSession session) {
        Shop shop = shopService.getShopByUserId(SessionUtil.getCurrentUser(session).getUserId()).orElse(null);
        return shop != null ? shop.getShopId() : null;
    }

    @PostMapping
    public Object addProduct(@RequestBody Product product, HttpSession session) {
        if (!SessionUtil.isShopUser(session)) {
            return ResponseUtil.noPermission();
        }
        Integer shopId = getCurrentShopId(session);
        if (shopId == null) {
            return ResponseUtil.error("店铺不存在");
        }
        product.setShopId(shopId);
        try {
            Product savedProduct = productService.addProduct(product);
            return ResponseUtil.success(savedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    /**
     * 更新商品信息（仅店铺用户，且只能更新自己店铺的商品）
     * @param id 商品ID
     * @param product 更新的商品信息
     * @param session HTTP会话
     * @return 更新后的商品信息或错误提示
     */
    @PutMapping("/{id}")
    public Object updateProduct(@PathVariable Integer id, @RequestBody Product product, HttpSession session) {
        if (!SessionUtil.isShopUser(session)) {
            return ResponseUtil.noPermission();
        }
        Product existing = productService.getProductById(id).orElse(null);
        if (existing == null) {
            return ResponseUtil.error("商品不存在");
        }
        Integer shopId = getCurrentShopId(session);
        if (shopId == null) {
            return ResponseUtil.error("店铺不存在");
        }
        if (!existing.getShopId().equals(shopId)) {
            return ResponseUtil.error("无权限修改此商品");
        }
        product.setProductId(id);
        product.setShopId(shopId);
        try {
            Product updatedProduct = productService.updateProduct(product);
            return ResponseUtil.success(updatedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(e.getMessage());
        }
    }
}

