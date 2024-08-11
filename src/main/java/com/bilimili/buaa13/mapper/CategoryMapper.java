package com.bilimili.buaa13.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {


    // 自定义查询方法
    @Select("SELECT * FROM category WHERE main_class_id = #{mcId} AND sub_class_id = #{scId}")
    Category findByMainAndSubClassId(@Param("mcId") String mcId, @Param("scId") String scId);

}
