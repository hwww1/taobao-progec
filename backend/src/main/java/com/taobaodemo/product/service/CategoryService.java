package com.taobaodemo.product.service;

import com.taobaodemo.product.entity.Category;
import com.taobaodemo.product.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 获取所有分类（按排序号升序）
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }
}


