package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.category.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取全部分区接口
     * @return 响应对象
     */
    @GetMapping("/category/getall")
    public ResponseResult getAll() {
        return categoryService.getAll();
    }

    /**
     *  获取单个分区接口
     * @return单个响应对象
     *
     * **/

    @GetMapping("/category/getone")
    public ResponseResult getOne(Integer mcId,Integer scId)
    {
        return CategoryService.getOne();
    }

}
