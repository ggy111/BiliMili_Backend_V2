package com.bilimili.buaa13.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.Danmu;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface DanmuMapper extends BaseMapper<Danmu> {


    //----------------------------------------------------------------------------------
    //修改于2024.8.19

    // 插入新的弹幕
    @Insert("INSERT INTO danmu (vid, user_id, content, send_time, create_time, color, type, font_size) " +
            "VALUES (#{vid}, #{userId}, #{content}, #{sendTime}, #{createTime}, #{color}, #{type}, #{fontSize})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDanmu(Danmu danmu);

    // 批量插入弹幕
    @Insert({
            "<script>",
            "INSERT INTO danmu (vid, user_id, content, send_time, create_time, color, type, font_size) VALUES ",
            "<foreach collection='danmus' item='danmu' separator=','>",
            "(#{danmu.vid}, #{danmu.userId}, #{danmu.content}, #{danmu.sendTime}, #{danmu.createTime}, #{danmu.color}, #{danmu.type}, #{danmu.fontSize})",
            "</foreach>",
            "</script>"
    })
    int bulkInsertDanmus(@Param("danmus") List<Danmu> danmus);

    // 根据视频ID获取弹幕列表
    @Select("SELECT * FROM danmu WHERE vid = #{vid} ORDER BY send_time ASC")
    List<Danmu> getDanmusByVid(@Param("vid") Integer vid);

    // 根据用户ID获取用户发送的所有弹幕
    @Select("SELECT * FROM danmu WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Danmu> getDanmusByUserId(@Param("userId") Integer userId);

    // 获取指定视频指定时间范围内的弹幕
    @Select("SELECT * FROM danmu WHERE vid = #{vid} AND send_time BETWEEN #{startTime} AND #{endTime} ORDER BY send_time ASC")
    List<Danmu> getDanmusByVidAndTimeRange(@Param("vid") Integer vid, @Param("startTime") String startTime, @Param("endTime") String endTime);

    // 删除指定用户的所有弹幕
    @Delete("DELETE FROM danmu WHERE user_id = #{userId}")
    int deleteDanmusByUserId(@Param("userId") Integer userId);

    // 根据弹幕ID删除弹幕
    @Delete("DELETE FROM danmu WHERE id = #{id}")
    int deleteDanmuById(@Param("id") Integer id);

    // 批量删除弹幕
    @Delete({
            "<script>",
            "DELETE FROM danmu WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int bulkDeleteDanmus(@Param("ids") List<Integer> ids);

    // 获取某个视频下的弹幕总数
    @Select("SELECT COUNT(*) FROM danmu WHERE vid = #{vid}")
    int countDanmusByVid(@Param("vid") Integer vid);

    // 获取某个用户发送的弹幕总数
    @Select("SELECT COUNT(*) FROM danmu WHERE user_id = #{userId}")
    int countDanmusByUserId(@Param("userId") Integer userId);

    // 获取某个视频下特定颜色的弹幕
    @Select("SELECT * FROM danmu WHERE vid = #{vid} AND color = #{color} ORDER BY send_time ASC")
    List<Danmu> getDanmusByVidAndColor(@Param("vid") Integer vid, @Param("color") String color);

    // 获取某个视频下特定类型的弹幕
    @Select("SELECT * FROM danmu WHERE vid = #{vid} AND type = #{type} ORDER BY send_time ASC")
    List<Danmu> getDanmusByVidAndType(@Param("vid") Integer vid, @Param("type") Integer type);

    // 获取某个视频下特定字体大小的弹幕
    @Select("SELECT * FROM danmu WHERE vid = #{vid} AND font_size = #{fontSize} ORDER BY send_time ASC")
    List<Danmu> getDanmusByVidAndFontSize(@Param("vid") Integer vid, @Param("fontSize") Integer fontSize);

    // 获取某个视频下的热门弹幕（按出现频率排序）
    @Select("SELECT content, COUNT(*) as count FROM danmu WHERE vid = #{vid} GROUP BY content ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopHotDanmusByVid(@Param("vid") Integer vid, @Param("limit") Integer limit);

    // 获取某个用户的热门弹幕（按出现频率排序）
    @Select("SELECT content, COUNT(*) as count FROM danmu WHERE user_id = #{userId} GROUP BY content ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopHotDanmusByUser(@Param("userId") Integer userId, @Param("limit") Integer limit);

    // 批量更新弹幕的颜色
    @Update({
            "<script>",
            "<foreach collection='danmus' item='danmu'>",
            "UPDATE danmu SET color = #{danmu.color} WHERE id = #{danmu.id};",
            "</foreach>",
            "</script>"
    })
    int bulkUpdateDanmuColors(@Param("danmus") List<Map<String, Object>> danmus);

    // 获取视频下的弹幕数量，按时间分组（例如按小时、天）
    @Select("SELECT DATE_FORMAT(send_time, #{format}) as timePeriod, COUNT(*) as count " +
            "FROM danmu WHERE vid = #{vid} GROUP BY DATE_FORMAT(send_time, #{format})")
    List<Map<String, Object>> countDanmusGroupedByTime(@Param("vid") Integer vid, @Param("format") String format);

    // 获取所有删除的弹幕（物理删除）
    @Select("SELECT * FROM danmu WHERE is_deleted = 1")
    List<Danmu> getAllDeletedDanmus();

    // 逻辑删除弹幕
    @Update("UPDATE danmu SET is_deleted = 1 WHERE id = #{id}")
    int logicalDeleteDanmuById(@Param("id") Integer id);

    // 批量逻辑删除弹幕
    @Update({
            "<script>",
            "UPDATE danmu SET is_deleted = 1 WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int bulkLogicalDeleteDanmus(@Param("ids") List<Integer> ids);

    // 恢复逻辑删除的弹幕
    @Update("UPDATE danmu SET is_deleted = 0 WHERE id = #{id}")
    int restoreDeletedDanmuById(@Param("id") Integer id);

    // 批量恢复逻辑删除的弹幕
    @Update({
            "<script>",
            "UPDATE danmu SET is_deleted = 0 WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int bulkRestoreDeletedDanmus(@Param("ids") List<Integer> ids);

    // 获取某段时间内的所有弹幕
    @Select("SELECT * FROM danmu WHERE create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time ASC")
    List<Danmu> getDanmusByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 获取指定时间段内特定用户的弹幕
    @Select("SELECT * FROM danmu WHERE user_id = #{userId} AND create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time ASC")
    List<Danmu> getUserDanmusByTimeRange(@Param("userId") Integer userId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    // 获取指定时间段内特定视频的弹幕
    @Select("SELECT * FROM danmu WHERE vid = #{vid} AND create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time ASC")
    List<Danmu> getVidDanmusByTimeRange(@Param("vid") Integer vid, @Param("startTime") String startTime, @Param("endTime") String endTime);


    //-------------------------------------------------------------------------------
}
