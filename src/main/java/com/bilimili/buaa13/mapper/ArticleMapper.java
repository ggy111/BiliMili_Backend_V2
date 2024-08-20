package com.bilimili.buaa13.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    //--------------------------------------------------------------------------
    //修改于2024.08.19

    // 获取指定分类下的所有文章ID
    //@Select("select aid from article where category = #{category}")
    List<Integer> getArticleIdsByCategory(String category);

    // 获取指定时间范围内发布的文章ID
    //@Select("select aid from article where publish_date between #{startDate} and #{endDate}")
    List<Integer> getArticleIdsByDateRange(String startDate, String endDate);

    // 获取某个用户的特定状态的文章ID
    @Select("select aid from article where uid = #{uid} and status = #{status}")
    List<Integer> getArticleIdsByUidAndStatus(Integer uid, Integer status);

    // 获取指定关键词的文章ID
    //@Select("select aid from article where title like CONCAT('%', #{keyword}, '%') or content like CONCAT('%', #{keyword}, '%')")
    List<Integer> getArticleIdsByKeyword(String keyword);

    // 获取指定标签的文章ID
    //@Select("select aid from article where tags like CONCAT('%', #{tag}, '%')")
    List<Integer> getArticleIdsByTag(String tag);

    // 获取指定文章ID的详细信息
    @Select("select * from article where aid = #{aid}")
    Article getArticleById(Integer aid);

    // 获取指定用户的文章数量
    @Select("select count(*) from article where uid = #{uid}")
    Integer getArticleCountByUid(Integer uid);

    // 获取指定分类的文章数量
    //@Select("select count(*) from article where category = #{category}")
    Integer getArticleCountByCategory(String category);

    // 获取文章的平均阅读数
    //@Select("select avg(views) from article")
    Double getAverageViews();

    // 获取最高阅读数的文章ID
    //@Select("select aid from article order by views desc limit 1")
    Integer getMostViewedArticleId();

    // 获取最早发布的文章ID
    //@Select("select aid from article order by publish_date asc limit 1")
    Integer getEarliestPublishedArticleId();

    // 获取最新发布的文章ID
    //@Select("select aid from article order by publish_date desc limit 1")
    Integer getLatestPublishedArticleId();


    // 获取按用户分组的文章数量统计
    @Select("select uid, count(*) as article_count from article group by uid")
    List<Map<String, Object>> getArticleCountGroupedByUser();

    // 获取按分类分组的文章数量统计
    //@Select("select category, count(*) as article_count from article group by category")
    List<Map<String, Object>> getArticleCountGroupedByCategory();

    // 获取按状态分组的文章数量统计
    @Select("select status, count(*) as article_count from article group by status")
    List<Map<String, Object>> getArticleCountGroupedByStatus();

    // 获取按年份分组的文章数量统计
    //@Select("select year(publish_date) as year, count(*) as article_count from article group by year(publish_date)")
    List<Map<String, Object>> getArticleCountGroupedByYear();

    // 获取按月份分组的文章数量统计（指定年份）
    //@Select("select month(publish_date) as month, count(*) as article_count from article where year(publish_date) = #{year} group by month(publish_date)")
    List<Map<String, Object>> getArticleCountGroupedByMonth(Integer year);


    // 获取某个用户在指定时间范围内发布的文章数量
    //@Select("select count(*) from article where uid = #{uid} and publish_date between #{startDate} and #{endDate}")
    Integer getArticleCountByUidAndDateRange(Integer uid, String startDate, String endDate);

    // 获取指定标签的文章数量
    //@Select("select count(*) from article where tags like CONCAT('%', #{tag}, '%')")
    Integer getArticleCountByTag(String tag);

    // 获取某个用户的最高阅读数文章ID
    //@Select("select aid from article where uid = #{uid} order by views desc limit 1")
    Integer getMostViewedArticleIdByUid(Integer uid);

    // 获取指定关键词的文章数量
    //@Select("select count(*) from article where title like CONCAT('%', #{keyword}, '%') or content like CONCAT('%', #{keyword}, '%')")
    Integer getArticleCountByKeyword(String keyword);

    // 获取最高点赞数的文章ID
    //@Select("select aid from article order by likes desc limit 1")
    Integer getMostLikedArticleId();

    // 获取某个用户最高点赞数的文章ID
    //@Select("select aid from article where uid = #{uid} order by likes desc limit 1")
    Integer getMostLikedArticleIdByUid(Integer uid);

    // 获取文章的最高评论数
    //@Select("select aid from article order by comments desc limit 1")
    Integer getMostCommentedArticleId();

    // 获取某个用户最高评论数的文章ID
    //@Select("select aid from article where uid = #{uid} order by comments desc limit 1")
    Integer getMostCommentedArticleIdByUid(Integer uid);

    // 获取某个分类最高阅读数的文章ID
    //@Select("select aid from article where category = #{category} order by views desc limit 1")
    Integer getMostViewedArticleIdByCategory(String category);

    @Select("select aid from article where status = #{status}")
    List<Integer> getArticleIdsByStatus(Integer status);
    @Select("select aid from article where uid = #{uids}")
    List<Integer> getArticleIdsByUids(Integer uids);

    //----------------------------------------------------------------------
}
