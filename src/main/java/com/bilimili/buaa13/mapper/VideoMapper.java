package com.bilimili.buaa13.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.Video;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
    //-------------------------------------------------------------------------------------------
    //修改于2024.08.19
    /**
// 查询所有对应状态的视频，并且除去已删除的
    @Select("SELECT * FROM video WHERE status = #{status} AND is_deleted = 0")
    List<Video> selectAllVideoByStatus(@Param("status") int status);

    // 随机返回count个对应状态的视频，除去已删除的
    @Select("SELECT * FROM video WHERE status = #{status} AND is_deleted = 0 ORDER BY RAND() LIMIT #{count}")
    List<Video> selectCountVideoByRandom(@Param("status") int status, @Param("count") int count);**/

    // 根据视频ID获取视频详细信息
    @Select("SELECT * FROM video WHERE id = #{id} AND is_deleted = 0")
    Video selectVideoById(@Param("id") Integer id);

    // 批量插入视频记录
    @Insert({
            "<script>",
            "INSERT INTO video (title, description, status, create_time, update_time, is_deleted) VALUES ",
            "<foreach collection='videos' item='video' separator=','>",
            "(#{video.title}, #{video.description}, #{video.status}, #{video.createTime}, #{video.updateTime}, #{video.isDeleted})",
            "</foreach>",
            "</script>"
    })
    int bulkInsertVideos(@Param("videos") List<Video> videos);

    // 批量删除视频记录（逻辑删除）
    @Update({
            "<script>",
            "UPDATE video SET is_deleted = 1 WHERE id IN ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int bulkLogicalDeleteVideos(@Param("ids") List<Integer> ids);

    // 批量恢复已删除的视频记录
    @Update({
            "<script>",
            "UPDATE video SET is_deleted = 0 WHERE id IN ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int bulkRestoreDeletedVideos(@Param("ids") List<Integer> ids);

    // 根据分类ID查询视频
    @Select("SELECT * FROM video WHERE category_id = #{categoryId} AND is_deleted = 0")
    List<Video> selectVideosByCategoryId(@Param("categoryId") Integer categoryId);

    // 获取视频的播放次数
    @Select("SELECT play_count FROM video WHERE id = #{id} AND is_deleted = 0")
    int getVideoPlayCount(@Param("id") Integer id);

    // 更新视频的播放次数
    @Update("UPDATE video SET play_count = play_count + 1 WHERE id = #{id} AND is_deleted = 0")
    int incrementVideoPlayCount(@Param("id") Integer id);

    // 获取视频的点赞次数
    @Select("SELECT like_count FROM video WHERE id = #{id} AND is_deleted = 0")
    int getVideoLikeCount(@Param("id") Integer id);

    // 更新视频的点赞次数
    @Update("UPDATE video SET like_count = like_count + 1 WHERE id = #{id} AND is_deleted = 0")
    int incrementVideoLikeCount(@Param("id") Integer id);

    // 获取视频的评论数量
    @Select("SELECT comment_count FROM video WHERE id = #{id} AND is_deleted = 0")
    int getVideoCommentCount(@Param("id") Integer id);

    // 更新视频的评论数量
    @Update("UPDATE video SET comment_count = comment_count + 1 WHERE id = #{id} AND is_deleted = 0")
    int incrementVideoCommentCount(@Param("id") Integer id);

    // 获取视频的收藏数量
    @Select("SELECT favorite_count FROM video WHERE id = #{id} AND is_deleted = 0")
    int getVideoFavoriteCount(@Param("id") Integer id);

    // 更新视频的收藏数量
    @Update("UPDATE video SET favorite_count = favorite_count + 1 WHERE id = #{id} AND is_deleted = 0")
    int incrementVideoFavoriteCount(@Param("id") Integer id);

    // 根据时间段查询视频
    @Select("SELECT * FROM video WHERE create_time BETWEEN #{startTime} AND #{endTime} AND is_deleted = 0 ORDER BY create_time DESC")
    List<Video> selectVideosByTimeRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    // 获取按播放次数排名的前N个视频
    @Select("SELECT * FROM video WHERE is_deleted = 0 ORDER BY play_count DESC LIMIT #{limit}")
    List<Video> getTopNVideosByPlayCount(@Param("limit") Integer limit);

    // 获取按点赞次数排名的前N个视频
    @Select("SELECT * FROM video WHERE is_deleted = 0 ORDER BY like_count DESC LIMIT #{limit}")
    List<Video> getTopNVideosByLikeCount(@Param("limit") Integer limit);

    // 获取按评论次数排名的前N个视频
    @Select("SELECT * FROM video WHERE is_deleted = 0 ORDER BY comment_count DESC LIMIT #{limit}")
    List<Video> getTopNVideosByCommentCount(@Param("limit") Integer limit);

    // 获取按收藏次数排名的前N个视频
    @Select("SELECT * FROM video WHERE is_deleted = 0 ORDER BY favorite_count DESC LIMIT #{limit}")
    List<Video> getTopNVideosByFavoriteCount(@Param("limit") Integer limit);

    // 获取指定用户上传的所有视频
    @Select("SELECT * FROM video WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY create_time DESC")
    List<Video> selectVideosByUserId(@Param("userId") Integer userId);

    // 获取指定用户上传的视频数量
    @Select("SELECT COUNT(*) FROM video WHERE user_id = #{userId} AND is_deleted = 0")
    int countVideosByUserId(@Param("userId") Integer userId);

    // 批量更新视频的状态
    @Update({
            "<script>",
            "<foreach collection='videos' item='video'>",
            "UPDATE video SET status = #{video.status} WHERE id = #{video.id} AND is_deleted = 0;",
            "</foreach>",
            "</script>"
    })
    int bulkUpdateVideoStatus(@Param("videos") List<Map<String, Object>> videos);

    // 批量更新视频的分类
    @Update({
            "<script>",
            "<foreach collection='videos' item='video'>",
            "UPDATE video SET category_id = #{video.categoryId} WHERE id = #{video.id} AND is_deleted = 0;",
            "</foreach>",
            "</script>"
    })
    int bulkUpdateVideoCategory(@Param("videos") List<Map<String, Object>> videos);

    // 获取指定分类下的视频数量
    @Select("SELECT COUNT(*) FROM video WHERE category_id = #{categoryId} AND is_deleted = 0")
    int countVideosByCategoryId(@Param("categoryId") Integer categoryId);

    // 获取指定分类下，按播放次数排名的前N个视频
    @Select("SELECT * FROM video WHERE category_id = #{categoryId} AND is_deleted = 0 ORDER BY play_count DESC LIMIT #{limit}")
    List<Video> getTopNVideosByCategoryAndPlayCount(@Param("categoryId") Integer categoryId, @Param("limit") Integer limit);

    // 获取指定分类下，按点赞次数排名的前N个视频
    @Select("SELECT * FROM video WHERE category_id = #{categoryId} AND is_deleted = 0 ORDER BY like_count DESC LIMIT #{limit}")
    List<Video> getTopNVideosByCategoryAndLikeCount(@Param("categoryId") Integer categoryId, @Param("limit") Integer limit);

    // 获取指定分类下，按评论次数排名的前N个视频
    @Select("SELECT * FROM video WHERE category_id = #{categoryId} AND is_deleted = 0 ORDER BY comment_count DESC LIMIT #{limit}")
    List<Video> getTopNVideosByCategoryAndCommentCount(@Param("categoryId") Integer categoryId, @Param("limit") Integer limit);

    // 获取指定分类下，按收藏次数排名的前N个视频
    @Select("SELECT * FROM video WHERE category_id = #{categoryId} AND is_deleted = 0 ORDER BY favorite_count DESC LIMIT #{limit}")
    List<Video> getTopNVideosByCategoryAndFavoriteCount(@Param("categoryId") Integer categoryId, @Param("limit") Integer limit);

    // 获取按时间倒序的前N个视频
    @Select("SELECT * FROM video WHERE is_deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<Video> getLatestNVideos(@Param("limit") Integer limit);

    // 获取按时间正序的前N个视频
    @Select("SELECT * FROM video WHERE is_deleted = 0 ORDER BY create_time ASC LIMIT #{limit}")
    List<Video> getOldestNVideos(@Param("limit") Integer limit);



    //-------------------------------------------------------------------------------------------


        //查询所有对应状态的视频，并且除去已删除的
    @Select("select * from video where status = #{status};")
    List<Video> selectAllVideoByStatus(@Param("status") int status);

    //随机返回count个对应状态的视频，除去已删除的
    @Select("select * from video where status = #{status} order by RAND() LIMIT #{count};")
    List<Video> selectCountVideoByRandom(@Param("status") int status, @Param("count") int count);

}
