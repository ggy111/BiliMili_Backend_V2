package com.bilimili.buaa13.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    //--------------------------------------------------------------------------
    //修改于20244.08.19

    // 插入新的收藏记录
    @Insert("INSERT INTO favorite (user_id, vid, create_time) VALUES (#{userId}, #{vid}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFavorite(Favorite favorite);

    // 批量插入收藏记录
    @Insert({
            "<script>",
            "INSERT INTO favorite (user_id, vid, create_time) VALUES ",
            "<foreach collection='favorites' item='favorite' separator=','>",
            "(#{favorite.userId}, #{favorite.vid}, #{favorite.createTime})",
            "</foreach>",
            "</script>"
    })
    int bulkInsertFavorites(@Param("favorites") List<Favorite> favorites);

    // 根据用户ID获取所有收藏
    @Select("SELECT * FROM favorite WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Favorite> getFavoritesByUserId(@Param("userId") Integer userId);

    // 根据视频ID获取收藏的用户列表
    @Select("SELECT * FROM favorite WHERE vid = #{vid} ORDER BY create_time DESC")
    List<Favorite> getFavoritesByVid(@Param("vid") Integer vid);

    // 删除指定用户的所有收藏记录
    @Delete("DELETE FROM favorite WHERE user_id = #{userId}")
    int deleteFavoritesByUserId(@Param("userId") Integer userId);

    // 根据收藏ID删除收藏记录
    @Delete("DELETE FROM favorite WHERE id = #{id}")
    int deleteFavoriteById(@Param("id") Integer id);

    // 批量删除收藏记录
    @Delete({
            "<script>",
            "DELETE FROM favorite WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int bulkDeleteFavorites(@Param("ids") List<Integer> ids);

    // 获取用户收藏的视频数量
    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId}")
    int countFavoritesByUserId(@Param("userId") Integer userId);

    // 获取视频的收藏数量
    @Select("SELECT COUNT(*) FROM favorite WHERE vid = #{vid}")
    int countFavoritesByVid(@Param("vid") Integer vid);

    // 判断用户是否已经收藏了某个视频
    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId} AND vid = #{vid}")
    int isVideoFavoritedByUser(@Param("userId") Integer userId, @Param("vid") Integer vid);

    // 获取某个视频的前N个收藏用户
    @Select("SELECT user_id FROM favorite WHERE vid = #{vid} ORDER BY create_time DESC LIMIT #{limit}")
    List<Integer> getTopNUsersByVid(@Param("vid") Integer vid, @Param("limit") Integer limit);

    // 获取用户收藏的视频列表，支持分页
    @Select("SELECT * FROM favorite WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Favorite> getFavoritesByUserIdWithPagination(@Param("userId") Integer userId, @Param("limit") Integer limit, @Param("offset") Integer offset);

    // 获取用户收藏的所有视频ID
    @Select("SELECT vid FROM favorite WHERE user_id = #{userId}")
    List<Integer> getVidListByUserId(@Param("userId") Integer userId);

    // 获取用户收藏的最新N个视频
    @Select("SELECT vid FROM favorite WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<Integer> getLatestNVidByUserId(@Param("userId") Integer userId, @Param("limit") Integer limit);

    // 获取用户收藏的最早N个视频
    @Select("SELECT vid FROM favorite WHERE user_id = #{userId} ORDER BY create_time ASC LIMIT #{limit}")
    List<Integer> getOldestNVidByUserId(@Param("userId") Integer userId, @Param("limit") Integer limit);

    // 更新收藏记录的创建时间（用于重排序）
    @Update("UPDATE favorite SET create_time = #{createTime} WHERE id = #{id}")
    int updateFavoriteTime(@Param("id") Integer id, @Param("createTime") String createTime);

    // 批量更新收藏记录的创建时间
    @Update({
            "<script>",
            "<foreach collection='favorites' item='favorite'>",
            "UPDATE favorite SET create_time = #{favorite.createTime} WHERE id = #{favorite.id};",
            "</foreach>",
            "</script>"
    })
    int bulkUpdateFavoriteTimes(@Param("favorites") List<Map<String, Object>> favorites);

    // 根据用户ID和视频ID删除收藏记录
    @Delete("DELETE FROM favorite WHERE user_id = #{userId} AND vid = #{vid}")
    int deleteFavoriteByUserIdAndVid(@Param("userId") Integer userId, @Param("vid") Integer vid);

    // 获取指定时间范围内的收藏记录
    @Select("SELECT * FROM favorite WHERE create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time ASC")
    List<Favorite> getFavoritesByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 获取某段时间内收藏次数最多的视频ID
    @Select("SELECT vid, COUNT(*) as count FROM favorite WHERE create_time BETWEEN #{startTime} AND #{endTime} GROUP BY vid ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopNFavoritedVideosInTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("limit") Integer limit);

    // 获取某段时间内收藏次数最多的用户ID
    @Select("SELECT user_id, COUNT(*) as count FROM favorite WHERE create_time BETWEEN #{startTime} AND #{endTime} GROUP BY user_id ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopNUsersInTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("limit") Integer limit);

    // 根据用户ID获取某段时间内的收藏记录
    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time ASC")
    List<Favorite> getFavoritesByUserIdAndTimeRange(@Param("userId") Integer userId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    // 获取某段时间内所有用户收藏的总数
    @Select("SELECT COUNT(*) FROM favorite WHERE create_time BETWEEN #{startTime} AND #{endTime}")
    int countTotalFavoritesInTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 获取某段时间内所有用户收藏的不同视频数量
    @Select("SELECT COUNT(DISTINCT vid) FROM favorite WHERE create_time BETWEEN #{startTime} AND #{endTime}")
    int countDistinctVidsInTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 获取某段时间内所有用户收藏的不同用户数量
    @Select("SELECT COUNT(DISTINCT user_id) FROM favorite WHERE create_time BETWEEN #{startTime} AND #{endTime}")
    int countDistinctUsersInTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 逻辑删除收藏记录（软删除）
    @Update("UPDATE favorite SET is_deleted = 1 WHERE id = #{id}")
    int logicalDeleteFavoriteById(@Param("id") Integer id);

    // 批量逻辑删除收藏记录
    @Update({
            "<script>",
            "UPDATE favorite SET is_deleted = 1 WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int bulkLogicalDeleteFavorites(@Param("ids") List<Integer> ids);

    // 恢复逻辑删除的收藏记录
    @Update("UPDATE favorite SET is_deleted = 0 WHERE id = #{id}")
    int restoreDeletedFavoriteById(@Param("id") Integer id);

    // 批量恢复逻辑删除的收藏记录
    @Update({
            "<script>",
            "UPDATE favorite SET is_deleted = 0 WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int bulkRestoreDeletedFavorites(@Param("ids") List<Integer> ids);

    //-------------------------------------------------------------------
}
