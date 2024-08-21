package com.bilimili.buaa13.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {


    //------------------------------------------------------------------------------
    //修改于2024.08.19


    // 获取指定用户的所有评论
    @Select("SELECT * FROM comment WHERE user_id = #{userId}")
    List<Comment> getCommentsByUserId(@Param("userId") Integer userId);

    // 获取指定视频下所有评论数量
    @Select("SELECT COUNT(*) FROM comment WHERE vid = #{vid}")
    int countCommentsByVid(@Param("vid") Integer vid);

    // 获取某个评论的子评论
    @Select("SELECT * FROM comment WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY create_time")
    List<Comment> getChildComments(@Param("parentId") Integer parentId);

    // 插入新评论
    @Insert("INSERT INTO comment (vid, user_id, content, create_time, up_vote, down_vote, root_id, parent_id, is_deleted) " +
            "VALUES (#{vid}, #{userId}, #{content}, #{createTime}, #{upVote}, #{downVote}, #{rootId}, #{parentId}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(Comment comment);

    // 根据评论ID删除评论（逻辑删除）
    @Update("UPDATE comment SET is_deleted = 1 WHERE id = #{id}")
    int deleteCommentById(@Param("id") Integer id);

    // 批量删除评论（逻辑删除）
    @Update({
            "<script>",
            "UPDATE comment SET is_deleted = 1 WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int bulkDeleteComments(@Param("ids") List<Integer> ids);

    // 恢复被删除的评论
    @Update("UPDATE comment SET is_deleted = 0 WHERE id = #{id}")
    int restoreDeletedCommentById(@Param("id") Integer id);

    // 获取热门评论（点赞数减去踩数排序）
    @Select("SELECT * FROM comment WHERE vid = #{vid} AND is_deleted = 0 ORDER BY (up_vote - down_vote) DESC LIMIT #{limit} OFFSET #{offset}")
    List<Comment> getHotCommentsByVid(@Param("vid") Integer vid, @Param("limit") Integer limit, @Param("offset") Integer offset);

    // 获取最新评论（按创建时间排序）
    @Select("SELECT * FROM comment WHERE vid = #{vid} AND is_deleted = 0 ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Comment> getLatestCommentsByVid(@Param("vid") Integer vid, @Param("limit") Integer limit, @Param("offset") Integer offset);

    // 获取评论数量，按根评论分组
    @Select("SELECT root_id, COUNT(*) AS count FROM comment WHERE vid = #{vid} AND is_deleted = 0 GROUP BY root_id")
    List<Map<String, Object>> countCommentsGroupedByRootId(@Param("vid") Integer vid);

    // 更新评论的点赞数
    @Update("UPDATE comment SET up_vote = up_vote + #{increment} WHERE id = #{id}")
    int incrementUpVote(@Param("id") Integer id, @Param("increment") Integer increment);

    // 更新评论的踩数
    @Update("UPDATE comment SET down_vote = down_vote + #{increment} WHERE id = #{id}")
    int incrementDownVote(@Param("id") Integer id, @Param("increment") Integer increment);

    // 获取指定用户在某个视频下的所有评论
    @Select("SELECT * FROM comment WHERE user_id = #{userId} AND vid = #{vid} AND is_deleted = 0 ORDER BY create_time DESC")
    List<Comment> getUserCommentsByVid(@Param("userId") Integer userId, @Param("vid") Integer vid);

    // 获取某个视频下的所有子评论数量
    @Select("SELECT COUNT(*) FROM comment WHERE root_id = #{rootId} AND is_deleted = 0")
    int countChildComments(@Param("rootId") Integer rootId);

    // 获取评论及其所有子评论（递归查询）
    @Select("WITH RECURSIVE comment_tree AS (" +
            "  SELECT * FROM comment WHERE id = #{id} " +
            "  UNION ALL " +
            "  SELECT c.* FROM comment c INNER JOIN comment_tree ct ON c.parent_id = ct.id " +
            ") SELECT * FROM comment_tree WHERE is_deleted = 0")
    List<Comment> getCommentTree(@Param("id") Integer id);

    // 批量插入评论
    @Insert({
            "<script>",
            "INSERT INTO comment (vid, user_id, content, create_time, up_vote, down_vote, root_id, parent_id, is_deleted) VALUES ",
            "<foreach collection='comments' item='comment' separator=','>",
            "(#{comment.vid}, #{comment.userId}, #{comment.content}, #{comment.createTime}, #{comment.upVote}, #{comment.downVote}, #{comment.rootId}, #{comment.parentId}, #{comment.isDeleted})",
            "</foreach>",
            "</script>"
    })
    int bulkInsertComments(@Param("comments") List<Comment> comments);

    // 批量更新评论的点赞数
    @Update({
            "<script>",
            "<foreach collection='votes' item='vote'>",
            "UPDATE comment SET up_vote = up_vote + #{vote.increment} WHERE id = #{vote.id};",
            "</foreach>",
            "</script>"
    })
    int bulkUpdateUpVotes(@Param("votes") List<Map<String, Object>> votes);

    // 获取指定用户在所有视频下的所有根评论
    @Select("SELECT * FROM comment WHERE user_id = #{userId} AND root_id = 0 AND is_deleted = 0 ORDER BY create_time DESC")
    List<Comment> getUserRootComments(@Param("userId") Integer userId);

    // 获取视频下的顶级热门评论，限制最多多少条
    @Select("SELECT * FROM comment WHERE vid = #{vid} AND root_id = 0 AND is_deleted = 0 ORDER BY (up_vote - down_vote) DESC LIMIT #{limit}")
    List<Comment> getTopHotRootCommentsByVid(@Param("vid") Integer vid, @Param("limit") Integer limit);

    // 获取某个用户的顶级热门评论，限制最多多少条
    @Select("SELECT * FROM comment WHERE user_id = #{userId} AND root_id = 0 AND is_deleted = 0 ORDER BY (up_vote - down_vote) DESC LIMIT #{limit}")
    List<Comment> getTopHotRootCommentsByUser(@Param("userId") Integer userId, @Param("limit") Integer limit);

    // 获取所有删除的评论（逻辑删除）
    @Select("SELECT * FROM comment WHERE is_deleted = 1")
    List<Comment> getAllDeletedComments();

    // 彻底删除评论（物理删除）
    @Delete("DELETE FROM comment WHERE id = #{id}")
    int permanentlyDeleteCommentById(@Param("id") Integer id);

    // 批量彻底删除评论（物理删除）
    @Delete({
            "<script>",
            "DELETE FROM comment WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int bulkPermanentlyDeleteComments(@Param("ids") List<Integer> ids);

    // 获取某个视频下的评论数量，按照时间分组（例如按天、按月）
    @Select("SELECT DATE_FORMAT(create_time, #{format}) as date, COUNT(*) as count " +
            "FROM comment WHERE vid = #{vid} AND is_deleted = 0 GROUP BY DATE_FORMAT(create_time, #{format})")
    List<Map<String, Object>> countCommentsByVidGroupedByTime(@Param("vid") Integer vid, @Param("format") String format);

    // 获取某个时间段内的所有评论
    @Select("SELECT * FROM comment WHERE create_time BETWEEN #{startTime} AND #{endTime} AND is_deleted = 0 ORDER BY create_time")
    List<Comment> getCommentsByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);



    //------------------------------------------------------------------------------
    /**
     * 获取vid视频对应的评论，要求是根评论
     * @param vid 视频id
     * @return 根评论数组
     */
    @Select("SELECT * FROM comment WHERE vid = #{vid} AND root_id = 0")
    List<Comment> getRootCommentsByVid(@Param("vid") Integer vid);

    /**
     * 根据开始的位置和偏移量获取子评论
     * @param rootId 根级节点的cid
     * @param start 开始位置
     * @param limit 限制的个数
     * @return
     */
    @Select("select * from comment where parent_id = #{root_id} and comment.is_deleted = 0 limit #{limit} offset #{start}")
    List<Comment> getRootCommentsByStartAndLimit(@Param("root_id") Integer rootId,
                                                 @Param("start") Long start,
                                                 @Param("limit") Long limit
    );
    @Select("select * from comment where parent_id = #{root_id} and comment.is_deleted = 0 LIMIT 18446744073709551615 offset #{start}")
    List<Comment> getRootCommentByStartNoLimit(@Param("root_id") Integer rootId,
                                               @Param("limit") Long limit
    );

    /**
     * 根据vid和开始位置，限制个数查询，按照热度排序
     */
    @Select("select * from comment where vid = #{vid} order by " +
            "comment.up_vote - comment.down_vote " +
            "limit #{limit} offset #{start}")
    List<Comment> getVidRootCommentsByHeat(@Param("vid") Integer vid,
                                           @Param("start") Long start,
                                           @Param("limit") Long limit);

    /**
     * 根据vid和时间排序查询
     */
    @Select("select * from comment where vid = #{vid} order by " +
            "(select comment.create_time from comment where comment.vid = #{vid})" +
            "limit #{limit} offset #{start}")
    List<Comment> getVidRootCommentsByTime(@Param("vid") Integer vid,
                                                @Param("start") Long start,
                                                @Param("limit") Long limit);




}
