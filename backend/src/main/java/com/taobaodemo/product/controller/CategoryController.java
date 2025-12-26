package com.taobaodemo.product.controller;

import com.taobaodemo.product.service.CategoryService;
import com.taobaodemo.common.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取全部分类
     */
    @GetMapping
    public Object getAll() {
        return ResponseUtil.success(categoryService.getAllCategories());
    }
}


