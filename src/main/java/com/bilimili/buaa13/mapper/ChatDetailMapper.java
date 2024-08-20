package com.bilimili.buaa13.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.buaa13.entity.ChatDetailed;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatDetailMapper extends BaseMapper<ChatDetailed> {

    // 插入新聊天记录
    @Insert("INSERT INTO chat_detailed (post_id, accept_id, content, time, status) " +
            "VALUES (#{senderId}, #{receiverId}, #{content}, #{time}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertChatDetail(ChatDetailed chatDetail);

    // 根据聊天ID获取聊天详情
    @Select("SELECT * FROM chat_detailed WHERE id = #{id}")
    ChatDetailed getChatDetailById(@Param("id") Integer id);

    // 获取某个用户与另一个用户的所有聊天记录
    @Select("SELECT * FROM chat_detailed WHERE (post_id = #{userId1} AND accept_id = #{userId2}) " +
            "OR (post_id = #{userId2} AND accept_id = #{userId1}) ORDER BY time")
    List<ChatDetailed> getChatDetailsBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 获取某个用户的所有聊天记录
    @Select("SELECT * FROM chat_detailed WHERE post_id = #{userId} OR accept_id = #{userId} ORDER BY time")
    List<ChatDetailed> getAllChatDetailsForUser(@Param("userId") Integer userId);

    // 更新消息状态为已读
    @Update("UPDATE chat_detailed SET status = 'read' WHERE id = #{id}")
    int markcontentAsRead(@Param("id") Integer id);

    // 获取某个用户的未读消息数
    @Select("SELECT COUNT(*) FROM chat_detailed WHERE accept_id = #{userId} AND status = 'unread'")
    int getUnreadcontentCount(@Param("userId") Integer userId);

    // 获取未读消息的详细信息
    @Select("SELECT * FROM chat_detailed WHERE accept_id = #{userId} AND status = 'unread' ORDER BY time")
    List<ChatDetailed> getUnreadcontents(@Param("userId") Integer userId);

    // 删除某个聊天记录
    @Delete("DELETE FROM chat_detailed WHERE id = #{id}")
    int deleteChatDetailById(@Param("id") Integer id);

    // 删除某个用户与另一个用户之间的所有聊天记录
    @Delete("DELETE FROM chat_detailed WHERE (post_id = #{userId1} AND accept_id = #{userId2}) " +
            "OR (post_id = #{userId2} AND accept_id = #{userId1})")
    int deleteChatDetailsBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 获取某个用户发送的所有消息
    @Select("SELECT * FROM chat_detailed WHERE post_id = #{senderId} ORDER BY time")
    List<ChatDetailed> getcontentsSentByUser(@Param("senderId") Integer senderId);

    // 获取某个用户接收的所有消息
    @Select("SELECT * FROM chat_detailed WHERE accept_id = #{receiverId} ORDER BY time")
    List<ChatDetailed> getcontentsReceivedByUser(@Param("receiverId") Integer receiverId);

    // 获取某个时间范围内的聊天记录
    @Select("SELECT * FROM chat_detailed WHERE time BETWEEN #{startTime} AND #{endTime} ORDER BY time")
    List<ChatDetailed> getChatDetailsByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 获取最近的一条聊天记录
    @Select("SELECT * FROM chat_detailed WHERE (post_id = #{userId1} AND accept_id = #{userId2}) " +
            "OR (post_id = #{userId2} AND accept_id = #{userId1}) ORDER BY time DESC LIMIT 1")
    ChatDetailed getLatestChatDetailBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 获取未读消息的发送者ID列表
    @Select("SELECT DISTINCT post_id FROM chat_detailed WHERE accept_id = #{userId} AND status = 'unread'")
    List<Integer> getUnreadcontentSenderIds(@Param("userId") Integer userId);

    // 标记某个用户的所有未读消息为已读
    @Update("UPDATE chat_detailed SET status = 'read' WHERE accept_id = #{userId} AND status = 'unread'")
    int markAllcontentsAsReadForUser(@Param("userId") Integer userId);

    // 获取聊天记录的统计信息，例如总消息数、未读消息数等
    @Select("SELECT COUNT(*) as totalcontents, SUM(CASE WHEN status = 'unread' THEN 1 ELSE 0 END) as unreadcontents " +
            "FROM chat_detailed WHERE (post_id = #{userId1} AND accept_id = #{userId2}) " +
            "OR (post_id = #{userId2} AND accept_id = #{userId1})")
    Map<String, Integer> getChatStatisticsBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 获取聊天记录中使用最多的单词（简单文本分析）
    @Select("SELECT word, COUNT(*) as frequency FROM " +
            "(SELECT (content, '\\s+') as word FROM chat_detailed WHERE " +
            "(post_id = #{userId1} AND accept_id = #{userId2}) " +
            "OR (post_id = #{userId2} AND accept_id = #{userId1})) as words " +
            "GROUP BY word ORDER BY frequency DESC LIMIT 10")
    List<Map<String, Object>> getMostFrequentWordsInChat(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 获取某个用户的聊天伙伴列表（去重）
    @Select("SELECT DISTINCT CASE WHEN post_id = #{userId} THEN accept_id ELSE post_id END as chatPartnerId " +
            "FROM chat_detailed WHERE post_id = #{userId} OR accept_id = #{userId}")
    List<Integer> getChatPartnerIds(@Param("userId") Integer userId);

    // 获取某个用户与所有聊天伙伴的最新消息
    @Select("SELECT DISTINCT * FROM (" +
            "SELECT CASE WHEN post_id = #{userId} THEN accept_id ELSE post_id END as chatPartnerId, * " +
            "FROM chat_detailed WHERE post_id = #{userId} OR accept_id = #{userId} " +
            "ORDER BY chatPartnerId, time DESC) as subquery ORDER BY chatPartnerId")
    List<ChatDetailed> getLatestcontentsWithAllPartners(@Param("userId") Integer userId);

    // 获取某个用户与所有聊天伙伴的未读消息统计
    @Select("SELECT chatPartnerId, COUNT(*) as unreadcontents FROM (" +
            "SELECT CASE WHEN post_id = #{userId} THEN accept_id ELSE post_id END as chatPartnerId " +
            "FROM chat_detailed WHERE (post_id = #{userId} OR accept_id = #{userId}) AND status = 'unread') as subquery " +
            "GROUP BY chatPartnerId")
    List<Map<String, Object>> getUnreadcontentStatsForUser(@Param("userId") Integer userId);
}
