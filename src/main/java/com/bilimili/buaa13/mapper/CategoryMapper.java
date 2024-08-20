package com.bilimili.buaa13.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    //---------------------------------------------------------------------
    //修改于2024.08.19

    //@Select("SELECT * FROM category WHERE main_class_id = #{mcId} AND sub_class_id = #{scId}")
    //Category findByMainAndSubClassId(@Param("mcId") String mcId, @Param("scId") String scId);

    // 查找所有主分类
    @Select("SELECT DISTINCT main_class_id FROM category")
    List<String> findAllMainClassIds();

    // 查找某主分类下的所有子分类ID
    @Select("SELECT sub_class_id FROM category WHERE main_class_id = #{mcId}")
    List<String> findSubClassIdsByMainClassId(@Param("mcId") String mcId);

    // 获取指定分类名称的分类对象
    @Select("SELECT * FROM category WHERE main_class_name = #{name}")
    Category findByCategoryName(@Param("name") String name);

    // 获取所有分类及其对应的主分类ID
    @Select("SELECT main_class_name, main_class_id FROM category")
    List<Map<String, String>> findAllCategoryNamesAndMainClassIds();

    // 根据分类ID获取分类名称
    @Select("SELECT main_class_name FROM category WHERE category.main_class_id = #{id}")
    String findCategoryNameById(@Param("id") Integer id);

    // 获取指定主分类下的所有分类名称
    @Select("SELECT main_class_name FROM category WHERE main_class_id = #{mcId}")
    List<String> findCategoryNamesByMainClassId(@Param("mcId") String mcId);

    // 获取分类数量
    @Select("SELECT COUNT(*) FROM category")
    Integer getCategoryCount();

    // 获取某主分类下的分类数量
    @Select("SELECT COUNT(*) FROM category WHERE main_class_id = #{mcId}")
    Integer getCategoryCountByMainClassId(@Param("mcId") String mcId);

    // 获取某主分类下的子分类数量
    @Select("SELECT COUNT(DISTINCT sub_class_id) FROM category WHERE main_class_id = #{mcId}")
    Integer getSubCategoryCountByMainClassId(@Param("mcId") String mcId);

    // 查找没有子分类的主分类
    @Select("SELECT DISTINCT main_class_id FROM category WHERE sub_class_id IS NULL OR sub_class_id = ''")
    List<String> findMainClassIdsWithoutSubClass();

    // 查找所有子分类ID
    @Select("SELECT DISTINCT sub_class_id FROM category WHERE sub_class_id IS NOT NULL AND sub_class_id != ''")
    List<String> findAllSubClassIds();

    // 查找子分类的分类名称
    @Select("SELECT main_class_name FROM category WHERE sub_class_id = #{scId}")
    List<String> findCategoryNamesBySubClassId(@Param("scId") String scId);

    // 获取主分类及其子分类的统计信息
    @Select("SELECT main_class_id, COUNT(*) as total_categories, COUNT(DISTINCT sub_class_id) as total_sub_categories FROM category GROUP BY main_class_id")
    List<Map<String, Object>> getMainAndSubCategoryStats();

    // 获取具有多个子分类的主分类ID
    @Select("SELECT main_class_id FROM category GROUP BY main_class_id HAVING COUNT(DISTINCT sub_class_id) > 1")
    List<String> findMainClassIdsWithMultipleSubClasses();

    // 查找具有指定子分类ID的主分类ID
    @Select("SELECT DISTINCT main_class_id FROM category WHERE sub_class_id = #{scId}")
    List<String> findMainClassIdsBySubClassId(@Param("scId") String scId);

    // 获取所有分类的详细信息，包括主分类和子分类
    @Select("SELECT main_class_id, sub_class_id, main_class_name FROM category")
    List<Map<String, String>> findAllCategoryDetails();

    // 获取某个主分类下，所有分类及其子分类名称
    @Select("SELECT main_class_name FROM category WHERE main_class_id = #{mcId} ORDER BY sub_class_id")
    List<String> findCategoryNamesByMainClassIdOrderedBySubClass(@Param("mcId") String mcId);

    // 获取指定主分类和子分类ID的分类名称
    @Select("SELECT main_class_name FROM category WHERE main_class_id = #{mcId} AND sub_class_id = #{scId}")
    String findCategoryNameByMainAndSubClassId(@Param("mcId") String mcId, @Param("scId") String scId);

    // 获取某子分类下的所有主分类ID
    @Select("SELECT DISTINCT main_class_id FROM category WHERE sub_class_id = #{scId}")
    List<String> findMainClassIdsBySubClassIdDistinct(@Param("scId") String scId);

    // 根据分类ID获取主分类ID和子分类ID
    @Select("SELECT main_class_id, sub_class_id FROM category WHERE category.main_class_id = #{id}")
    Map<String, String> findMainAndSubClassIdById(@Param("id") Integer id);

    // 获取分类表中所有的主分类和子分类的组合
    @Select("SELECT DISTINCT main_class_id, sub_class_id FROM category")
    List<Map<String, String>> findAllMainAndSubClassCombinations();

    // 获取某个主分类下的子分类列表，并按子分类ID排序
    @Select("SELECT sub_class_id, main_class_name FROM category WHERE main_class_id = #{mcId} ORDER BY sub_class_id")
    List<Map<String, String>> findSubClassListByMainClassIdOrdered(@Param("mcId") String mcId);

    // 获取所有分类的层级结构
    @Select("SELECT main_class_id, sub_class_id, main_class_name FROM category ORDER BY main_class_id, sub_class_id")
    List<Map<String, String>> findAllCategoryHierarchy();

    // 根据分类名称模糊查询获取分类ID
    @Select("SELECT category.main_class_id FROM category WHERE main_class_name LIKE CONCAT('%', #{name}, '%')")
    List<Integer> findCategoryIdsByNameLike(@Param("name") String name);

    // 查找某个主分类下具有最多子分类的主分类ID
    @Select("SELECT main_class_id FROM category GROUP BY main_class_id ORDER BY COUNT(DISTINCT sub_class_id) DESC LIMIT 1")
    String findMainClassIdWithMaxSubClasses();

    // 获取分类表中重复的分类名称及其出现次数
    @Select("SELECT main_class_name, COUNT(*) as occurrence FROM category GROUP BY main_class_name HAVING COUNT(*) > 1")
    List<Map<String, Object>> findDuplicateCategoryNames();


    //-------------------------------------------------------------------------------

    // 自定义查询方法
    @Select("SELECT * FROM category WHERE main_class_id = #{mcId} AND sub_class_id = #{scId}")
    Category findByMainAndSubClassId(@Param("mcId") String mcId, @Param("scId") String scId);

}
