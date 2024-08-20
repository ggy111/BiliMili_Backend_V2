package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.Category;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.Video;
import com.bilimili.buaa13.mapper.CategoryMapper;
import com.bilimili.buaa13.service.category.CategoryService;
import com.bilimili.buaa13.service.user.UserService;
import com.bilimili.buaa13.service.video.VideoStatusService;
import com.bilimili.buaa13.tools.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTool redisTool;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private VideoStatusService videoStatusService;


    /**
     * 获取全部分区接口
     * @return 响应对象
     */
    @GetMapping("/category/getall")
    public ResponseResult getAll() {
        //--------------------------------------------------------------------------------------------------------
        //修改于2024.08.16
        boolean canBeReceived = false;
        if(canBeReceived){
            //当没有分区被创建时，用于初始化
            Video video = new Video();
            Map<String,Object> map = new HashMap<>();
            if (video.getStatus() != 3) {

                try{
                    map.put("video", video);
                    map.put("user", userService.getUserByUId(video.getUid()));
                    map.put("stats", videoStatusService.getStatusByVideoId(video.getVid()));
                    map.put("category", categoryService.getCategoryById(video.getMainClassId(), video.getSubClassId()));
                }
                catch (Exception e){
                    //log.error(e.getMessage(),e);
                }
            }
            else{
                //视频被删除
                Video video1 = new Video();
                video1.setVid(video.getVid());
                video1.setUid(video.getUid());
                video1.setStatus(video.getStatus());
                video1.setDeleteDate(video.getDeleteDate());
                map.put("video", video1);
            }
        }
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
        return categoryService.getAll();
    }




    @GetMapping("/category/getAPage")
    public ResponseResult getAPage(Integer mcId, Integer num){
        String mainCategoryId = mcId.toString();
        String subCategoryId = num.toString();
        categoryService.getCategoryById(mainCategoryId,subCategoryId);
        ResponseResult result = new ResponseResult();
        String redisKey = String.format("category:%s:%s", mainCategoryId, subCategoryId);

        // 使用Optional避免NullPointerException
        Optional<Category> cachedCategory = Optional.ofNullable(redisTool.getObject(redisKey, Category.class));

        // 如果在Redis中找到数据，直接返回
        if (cachedCategory.isPresent()) {
            //return cachedCategory.get();
        }

        // 如果Redis中没有数据，从数据库中查询
        Category category = categoryMapper.findByMainAndSubClassId(mainCategoryId, subCategoryId);

        // 如果数据库中没有数据，返回一个新的Category实例
        if (category == null) {
            //return new Category();
        }

        // 使用异步操作将数据存储到Redis中
        //taskExecutor.execute(() -> redisTool.setExObjectValue(redisKey, category));

        return  result;
    }

}
