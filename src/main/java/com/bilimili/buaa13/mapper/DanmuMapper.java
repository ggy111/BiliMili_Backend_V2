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
    @Insert("INSERT INTO barrage (bid,vid, uid, content, time_in_video, create_date, color, mode, word_size) " +
            "VALUES (#{bid},#{vid}, #{userId}, #{content}, #{sendTime}, #{createTime}, #{color}, #{mode}, #{fontSize})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertBarrage(Danmu danmu);

    // 根据视频ID获取弹幕列表
    @Select("SELECT * FROM barrage WHERE vid = #{vid} ORDER BY time_in_video ASC")
    List<Danmu> getBarragesByVid(@Param("vid") Integer vid);

    // 根据用户ID获取用户发送的所有弹幕
    @Select("SELECT * FROM barrage WHERE uid = #{userId} ORDER BY create_date DESC")
    List<Danmu> getBarragesByUserId(@Param("userId") Integer userId);

    // 获取指定视频指定时间范围内的弹幕
    @Select("SELECT * FROM barrage WHERE vid = #{vid} AND time_in_video BETWEEN #{startTime} AND #{endTime} ORDER BY time_in_video ASC")
    List<Danmu> getBarragesByVidAndTimeRange(@Param("vid") Integer vid, @Param("startTime") String startTime, @Param("endTime") String endTime);

    // 删除指定用户的所有弹幕
    @Delete("DELETE FROM barrage WHERE uid = #{userId}")
    int deleteBarragesByUserId(@Param("userId") Integer userId);

    // 根据弹幕ID删除弹幕
    @Delete("DELETE FROM barrage WHERE bid = #{id}")
    int deleteBarrageById(@Param("id") Integer id);

    // 获取某个视频下的弹幕总数
    @Select("SELECT COUNT(*) FROM barrage WHERE vid = #{vid}")
    int countBarragesByVid(@Param("vid") Integer vid);

    // 获取某个用户发送的弹幕总数
    @Select("SELECT COUNT(*) FROM barrage WHERE uid = #{userId}")
    int countBarragesByUserId(@Param("userId") Integer userId);

    // 获取某个视频下特定颜色的弹幕
    @Select("SELECT * FROM barrage WHERE vid = #{vid} AND color = #{color} ORDER BY time_in_video ASC")
    List<Danmu> getBarragesByVidAndColor(@Param("vid") Integer vid, @Param("color") String color);

    // 获取某个视频下特定类型的弹幕
    @Select("SELECT * FROM barrage WHERE vid = #{vid} AND mode = #{mode} ORDER BY time_in_video ASC")
    List<Danmu> getBarragesByVidAndmode(@Param("vid") Integer vid, @Param("mode") Integer mode);

    // 获取某个视频下特定字体大小的弹幕
    @Select("SELECT * FROM barrage WHERE vid = #{vid} AND word_size = #{fontSize} ORDER BY time_in_video ASC")
    List<Danmu> getBarragesByVidAndFontSize(@Param("vid") Integer vid, @Param("fontSize") Integer fontSize);

    // 获取某个视频下的热门弹幕（按出现频率排序）
    @Select("SELECT content, COUNT(*) as count FROM barrage WHERE vid = #{vid} GROUP BY content ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopHotBarragesByVid(@Param("vid") Integer vid, @Param("limit") Integer limit);

    // 获取某个用户的热门弹幕（按出现频率排序）
    @Select("SELECT content, COUNT(*) as count FROM barrage WHERE uid = #{userId} GROUP BY content ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopHotBarragesByUser(@Param("userId") Integer userId, @Param("limit") Integer limit);

    // 获取视频下的弹幕数量，按时间分组（例如按小时、天）
    @Select("SELECT DATE_FORMAT(time_in_video, #{format}) as timePeriod, COUNT(*) as count " +
            "FROM barrage WHERE vid = #{vid} GROUP BY DATE_FORMAT(time_in_video, #{format})")
    List<Map<String, Object>> countBarragesGroupedByTime(@Param("vid") Integer vid, @Param("format") String format);

    // 获取所有删除的弹幕（物理删除）
    @Select("SELECT * FROM barrage WHERE state = 3")
    List<Danmu> getAllDeletedBarrages();

    // 逻辑删除弹幕
    @Update("UPDATE barrage SET state = 3 WHERE bid = #{id}")
    int logicalDeleteBarrageById(@Param("id") Integer id);

    // 恢复逻辑删除的弹幕
    @Update("UPDATE barrage SET state = 1 WHERE bid = #{id}")
    int restoreDeletedBarrageById(@Param("id") Integer id);

    // 获取某段时间内的所有弹幕
    @Select("SELECT * FROM barrage WHERE create_date BETWEEN #{startTime} AND #{endTime} ORDER BY create_date ASC")
    List<Danmu> getBarragesByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 获取指定时间段内特定用户的弹幕
    @Select("SELECT * FROM barrage WHERE uid = #{userId} AND create_date BETWEEN #{startTime} AND #{endTime} ORDER BY create_date ASC")
    List<Danmu> getUserBarragesByTimeRange(@Param("userId") Integer userId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    // 获取指定时间段内特定视频的弹幕
    @Select("SELECT * FROM barrage WHERE vid = #{vid} AND create_date BETWEEN #{startTime} AND #{endTime} ORDER BY create_date ASC")
    List<Danmu> getVidBarragesByTimeRange(@Param("vid") Integer vid, @Param("startTime") String startTime, @Param("endTime") String endTime);


    //-------------------------------------------------------------------------------
}
