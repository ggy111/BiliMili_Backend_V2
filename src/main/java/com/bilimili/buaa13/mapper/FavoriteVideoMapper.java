package com.bilimili.buaa13.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.FavoriteVideo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.FavoriteVideo;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Date;

@Mapper
public interface FavoriteVideoMapper extends BaseMapper<FavoriteVideo> {

    //----------------------------------------------------------------------
    //修改于2024.08.19
    // 根据收藏夹ID获取所有视频ID，按时间倒序排序

    // 根据收藏夹ID获取收藏夹中所有视频的详细信息
    @Select("SELECT * FROM favorite_video WHERE fid = #{fid} AND is_remove = 0 ORDER BY time DESC")
    List<FavoriteVideo> getFavoriteVideosByFid(@Param("fid") Integer fid);

    // 批量插入收藏视频记录
    @Insert({
            "<script>",
            "INSERT INTO favorite_video (fid, vid, time, is_remove) VALUES ",
            "<foreach collection='favoriteVideos' item='favoriteVideo' separator=','>",
            "(#{favoriteVideo.fid}, #{favoriteVideo.vid}, #{favoriteVideo.time}, #{favoriteVideo.isRemove})",
            "</foreach>",
            "</script>"
    })
    int bulkInsertFavoriteVideos(@Param("favoriteVideos") List<FavoriteVideo> favoriteVideos);

    // 批量删除收藏视频记录
    @Delete({
            "<script>",
            "DELETE FROM favorite_video WHERE fid = #{fid} AND vid IN ",
            "<foreach collection='vids' item='vid' open='(' separator=',' close=')'>",
            "#{vid}",
            "</foreach>",
            "</script>"
    })
    int bulkDeleteFavoriteVideos(@Param("fid") Integer fid, @Param("vids") List<Integer> vids);

    // 根据收藏夹ID和视频ID删除收藏记录
    @Delete("DELETE FROM favorite_video WHERE fid = #{fid} AND vid = #{vid}")
    int deleteFavoriteVideoByFidAndVid(@Param("fid") Integer fid, @Param("vid") Integer vid);

    // 更新收藏视频的时间（重新排序）
    @Update("UPDATE favorite_video SET time = #{time} WHERE fid = #{fid} AND vid = #{vid}")
    int updateFavoriteVideoTime(@Param("fid") Integer fid, @Param("vid") Integer vid, @Param("time") Date time);

    // 逻辑删除收藏视频记录（软删除）
    @Update("UPDATE favorite_video SET is_remove = 1 WHERE fid = #{fid} AND vid = #{vid}")
    int logicalDeleteFavoriteVideo(@Param("fid") Integer fid, @Param("vid") Integer vid);

    // 恢复逻辑删除的收藏视频记录
    @Update("UPDATE favorite_video SET is_remove = 0 WHERE fid = #{fid} AND vid = #{vid}")
    int restoreDeletedFavoriteVideo(@Param("fid") Integer fid, @Param("vid") Integer vid);

    // 获取指定用户的所有收藏视频ID
    @Select("SELECT vid FROM favorite_video WHERE fid IN (SELECT id FROM favorite WHERE user_id = #{userId}) AND is_remove = 0 ORDER BY time DESC")
    List<Integer> getVidsByUserId(@Param("userId") Integer userId);

    // 获取收藏夹中的视频数量
    @Select("SELECT COUNT(*) FROM favorite_video WHERE fid = #{fid} AND is_remove = 0")
    int countVideosByFid(@Param("fid") Integer fid);

    // 获取指定视频在多少个收藏夹中存在
    @Select("SELECT COUNT(*) FROM favorite_video WHERE vid = #{vid} AND is_remove = 0")
    int countFavoritesByVid(@Param("vid") Integer vid);

    // 获取所有收藏夹中收藏的视频数量排名
    @Select("SELECT vid, COUNT(*) as count FROM favorite_video WHERE is_remove = 0 GROUP BY vid ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopNFavoritedVideos(@Param("limit") Integer limit);

    // 获取用户收藏的最新N个视频
    @Select("SELECT vid FROM favorite_video WHERE fid IN (SELECT id FROM favorite WHERE user_id = #{userId}) AND is_remove = 0 ORDER BY time DESC LIMIT #{limit}")
    List<Integer> getLatestNVidsByUserId(@Param("userId") Integer userId, @Param("limit") Integer limit);

    // 获取用户收藏的最早N个视频
    @Select("SELECT vid FROM favorite_video WHERE fid IN (SELECT id FROM favorite WHERE user_id = #{userId}) AND is_remove = 0 ORDER BY time ASC LIMIT #{limit}")
    List<Integer> getOldestNVidsByUserId(@Param("userId") Integer userId, @Param("limit") Integer limit);

    // 根据视频ID获取被收藏的次数排名
    @Select("SELECT fid, COUNT(*) as count FROM favorite_video WHERE vid = #{vid} AND is_remove = 0 GROUP BY fid ORDER BY count DESC")
    List<Map<String, Object>> getTopNFavoritesByVid(@Param("vid") Integer vid, @Param("limit") Integer limit);

    // 批量更新收藏视频记录的时间
    @Update({
            "<script>",
            "<foreach collection='favoriteVideos' item='favoriteVideo'>",
            "UPDATE favorite_video SET time = #{favoriteVideo.time} WHERE fid = #{favoriteVideo.fid} AND vid = #{favoriteVideo.vid};",
            "</foreach>",
            "</script>"
    })
    int bulkUpdateFavoriteVideoTimes(@Param("favoriteVideos") List<Map<String, Object>> favoriteVideos);

    // 根据收藏夹ID获取最新收藏的视频信息
    @Select("SELECT * FROM favorite_video WHERE fid = #{fid} AND is_remove = 0 ORDER BY time DESC LIMIT #{limit}")
    List<FavoriteVideo> getLatestFavoriteVideosByFid(@Param("fid") Integer fid, @Param("limit") Integer limit);

    // 获取某段时间内的收藏视频记录
    @Select("SELECT * FROM favorite_video WHERE time BETWEEN #{startTime} AND #{endTime} AND is_remove = 0 ORDER BY time ASC")
    List<FavoriteVideo> getFavoriteVideosByTimeRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    // 获取某段时间内收藏视频次数最多的视频ID
    @Select("SELECT vid, COUNT(*) as count FROM favorite_video WHERE time BETWEEN #{startTime} AND #{endTime} AND is_remove = 0 GROUP BY vid ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopNFavoritedVideosInTimeRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("limit") Integer limit);

    // 批量逻辑删除收藏视频记录
    @Update({
            "<script>",
            "UPDATE favorite_video SET is_remove = 1 WHERE fid = #{fid} AND vid IN",
            "<foreach collection='vids' item='vid' open='(' separator=',' close=')'>",
            "#{vid}",
            "</foreach>",
            "</script>"
    })
    int bulkLogicalDeleteFavoriteVideos(@Param("fid") Integer fid, @Param("vids") List<Integer> vids);

    // 批量恢复逻辑删除的收藏视频记录
    @Update({
            "<script>",
            "UPDATE favorite_video SET is_remove = 0 WHERE fid = #{fid} AND vid IN",
            "<foreach collection='vids' item='vid' open='(' separator=',' close=')'>",
            "#{vid}",
            "</foreach>",
            "</script>"
    })
    int bulkRestoreDeletedFavoriteVideos(@Param("fid") Integer fid, @Param("vids") List<Integer> vids);

    // 根据用户ID获取某段时间内的收藏视频记录
    @Select("SELECT * FROM favorite_video WHERE fid IN (SELECT id FROM favorite WHERE user_id = #{userId}) AND time BETWEEN #{startTime} AND #{endTime} AND is_remove = 0 ORDER BY time ASC")
    List<FavoriteVideo> getFavoriteVideosByUserIdAndTimeRange(@Param("userId") Integer userId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);


    //----------------------------------------------------------------------





    @Select("select vid from favorite_video where fid = #{fid} and is_remove = 0 order by time desc")
    List<Integer> getVidByFid(Integer fid);
    @Select("select time from favorite_video where fid = #{fid} order by time desc")
    List<Date> getTimeByFid(Integer fid);

}
