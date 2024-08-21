package com.bilimili.buaa13.service.impl.category;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bilimili.buaa13.mapper.CategoryMapper;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.dto.CategoryDTO;
import com.bilimili.buaa13.entity.Category;
import com.bilimili.buaa13.service.category.CategoryService;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisTool redisTool;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取全部分区数据
     * @return 响应对象
     */
    @Override
    public ResponseResult getAll() {
        ResponseResult responseResult = new ResponseResult();
        List<CategoryDTO> sortedCategories = new ArrayList<>();

        // 尝试从redis中获取数据
        try {
            sortedCategories = redisTool.getAllList("categoryList", CategoryDTO.class);
            if (!sortedCategories.isEmpty()) {
                responseResult.setData(sortedCategories);
                return responseResult;
            }
            log.warn("redis中获取不到分区数据");
        } catch (Exception e) {
            log.error("获取redis分区数据失败");
            e.printStackTrace();
        }

        // 将分区表一次全部查询出来，再在内存执行处理逻辑，可以减少数据库的IO
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        List<Category> list = categoryMapper.selectList(queryWrapper);

        // 开一个临时整合map
        Map<String, CategoryDTO> categoryDTOMap = new HashMap<>();

        for (Category category : list) {
            String mcId = category.getMainClassId();
            String scId = category.getSubClassId();
            String mcName = category.getMainClassName();
            String scName = category.getSubClassName();
            String description = category.getDescription();
            List<String> rcmTag = new ArrayList<>();
            if (category.getRcmTag() != null) {
                String[] strings = category.getRcmTag().split("\n");    // 将每个标签切出来组成列表封装
                rcmTag = Arrays.asList(strings);
            }

            // 先将主分类和空的子分类列表整合到map中
            if (!categoryDTOMap.containsKey(mcId)) {
                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setMcId(mcId);
                categoryDTO.setMcName(mcName);
                categoryDTO.setScList(new ArrayList<>());
                categoryDTOMap.put(mcId, categoryDTO);
            }

            // 把子分类整合到map的子分类列表里
            Map<String, Object> scMap = new HashMap<>();
            scMap.put("mcId", mcId);
            scMap.put("scId", scId);
            scMap.put("scName", scName);
            scMap.put("descr", description);
            scMap.put("rcmTag", rcmTag);
            categoryDTOMap.get(mcId).getScList().add(scMap);

        }

        // 按指定序列排序
        List<String> sortOrder = Arrays.asList("anime", "guochuang", "douga", "game", "kichiku",
                "music", "dance", "cinephile", "ent", "knowledge",
                "tech", "information", "food", "life", "car",
                "fashion", "sports", "animal", "virtual");

        for (String mcId : sortOrder) {
            if (categoryDTOMap.containsKey(mcId)) {
                sortedCategories.add(categoryDTOMap.get(mcId));
            }
        }
        // 将分类添加到redis缓存中
        try {
            redisTool.deleteValue("categoryList");
            List<String> dataList = new ArrayList<>();
            for (Object sor : sortedCategories) {
                dataList.add(JSON.toJSONString(sor));
                redisTemplate.opsForList().rightPush("categoryList", JSON.toJSONString(sor));
            }
        } catch (Exception e) {
            log.error("存储redis分类列表失败");
        }
        responseResult.setData(sortedCategories);
        return responseResult;
    }

    /**
     * 获取某个分区数据
     *
     * @param mainCategoryId 主分区ID
     * @param subCategoryId 子分区ID
     * @return 响应对象
     */
    @Override
    public ResponseResult getOne(Integer mainCategoryId, Integer subCategoryId) {
        String redisKey = String.format("category:%s:%s", mainCategoryId.toString(), subCategoryId.toString());
        Optional<Category> cachedCategory = Optional.ofNullable(redisTool.getObject(redisKey, Category.class));
        ResponseResult responseResult = new ResponseResult();
        // 如果在Redis中找到数据，直接返回
        if (cachedCategory.isPresent()) {
            responseResult.setData(cachedCategory.get());
            return responseResult;
        }
        Category category = categoryMapper.findByMainAndSubClassId(mainCategoryId.toString(), subCategoryId.toString());
        if (category == null) {
            responseResult.setData(new Category());
            return responseResult;
        }
        // 使用异步操作将数据存储到Redis中
        taskExecutor.execute(() -> redisTool.setExObjectValue(redisKey, category));
        return responseResult;
    }

    //---------------------------------------------------------------------------------------------
    //更新于 2024.08.10

    /**
     * 根据id查询对应分区信息
     * @param mainCategoryId 主分区ID
     * @param subCategoryId 子分区ID
     * @return Category类信息
     */

    @Override
    public Category getOne(String mainCategoryId, String subCategoryId) {
        String redisKey = String.format("category:%s:%s", mainCategoryId, subCategoryId);

        // 使用Optional避免NullPointerException
        Optional<Category> cachedCategory = Optional.ofNullable(redisTool.getObject(redisKey, Category.class));

        // 如果在Redis中找到数据，直接返回
        if (cachedCategory.isPresent()) {
            return cachedCategory.get();
        }

        // 如果Redis中没有数据，从数据库中查询
        Category category = categoryMapper.findByMainAndSubClassId(mainCategoryId, subCategoryId);

        // 如果数据库中没有数据，返回一个新的Category实例
        if (category == null) {
            return new Category();
        }

        // 使用异步操作将数据存储到Redis中
        taskExecutor.execute(() -> redisTool.setExObjectValue(redisKey, category));

        return category;
    }

    /**
     * 根据id查询对应分区信息
     * @param mcId 主分区ID
     * @param scId 子分区ID
     * @return Category类信息
     */
    @Override
    public Category getCategoryById(String mcId, String scId) {
        // 从redis中获取最新数据
        Category category = redisTool.getObject("category:" + mcId + ":" + scId, Category.class);
        // 如果redis中没有数据，就从mysql中获取并更新到redis
        if (category == null) {
            QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("main_class_id", mcId).eq("sub_class_id", scId);
            category = categoryMapper.selectOne(queryWrapper);
            if (category == null) {
                return new Category();    // 如果不存在则返回空
            }

            Category finalCategory = category;
            CompletableFuture.runAsync(() -> {
                redisTool.setExObjectValue("category:" + mcId + ":" + scId, finalCategory);  // 默认存活1小时
            }, taskExecutor);
        }
        return category;
    }
}