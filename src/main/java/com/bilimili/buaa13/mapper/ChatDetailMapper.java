package com.bilimili.buaa13.mapper;

//import com.bilimili.buaa13.entity.ChatDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatDetailMapper {

    // 插入新聊天记录
    @Insert("INSERT INTO chat_detail (sender_id, receiver_id, message, timestamp, status) " +
            "VALUES (#{senderId}, #{receiverId}, #{message}, #{timestamp}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
   // int insertChatDetail(ChatDetail chatDetail);

    // 根据聊天ID获取聊天详情
    @Select("SELECT * FROM chat_detail WHERE id = #{id}")
   // ChatDetail getChatDetailById(@Param("id") Integer id);

    // 获取某个用户与另一个用户的所有聊天记录
    @Select("SELECT * FROM chat_detail WHERE (sender_id = #{userId1} AND receiver_id = #{userId2}) " +
            "OR (sender_id = #{userId2} AND receiver_id = #{userId1}) ORDER BY timestamp")
   // List<ChatDetail> getChatDetailsBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 获取某个用户的所有聊天记录
    @Select("SELECT * FROM chat_detail WHERE sender_id = #{userId} OR receiver_id = #{userId} ORDER BY timestamp")
  //  List<ChatDetail> getAllChatDetailsForUser(@Param("userId") Integer userId);

    // 更新消息状态为已读
    @Update("UPDATE chat_detail SET status = 'read' WHERE id = #{id}")
    int markMessageAsRead(@Param("id") Integer id);

    // 获取某个用户的未读消息数
    @Select("SELECT COUNT(*) FROM chat_detail WHERE receiver_id = #{userId} AND status = 'unread'")
    int getUnreadMessageCount(@Param("userId") Integer userId);

    // 获取未读消息的详细信息
    @Select("SELECT * FROM chat_detail WHERE receiver_id = #{userId} AND status = 'unread' ORDER BY timestamp")
    //List<ChatDetail> getUnreadMessages(@Param("userId") Integer userId);

    // 删除某个聊天记录
    @Delete("DELETE FROM chat_detail WHERE id = #{id}")
    int deleteChatDetailById(@Param("id") Integer id);

    // 删除某个用户与另一个用户之间的所有聊天记录
    @Delete("DELETE FROM chat_detail WHERE (sender_id = #{userId1} AND receiver_id = #{userId2}) " +
            "OR (sender_id = #{userId2} AND receiver_id = #{userId1})")
    int deleteChatDetailsBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 获取某个用户发送的所有消息
    @Select("SELECT * FROM chat_detail WHERE sender_id = #{senderId} ORDER BY timestamp")
    //List<ChatDetail> getMessagesSentByUser(@Param("senderId") Integer senderId);

    // 获取某个用户接收的所有消息
    @Select("SELECT * FROM chat_detail WHERE receiver_id = #{receiverId} ORDER BY timestamp")
    //List<ChatDetail> getMessagesReceivedByUser(@Param("receiverId") Integer receiverId);

    // 获取某个时间范围内的聊天记录
    @Select("SELECT * FROM chat_detail WHERE timestamp BETWEEN #{startTime} AND #{endTime} ORDER BY timestamp")
    //List<ChatDetail> getChatDetailsByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    // 获取最近的一条聊天记录
    @Select("SELECT * FROM chat_detail WHERE (sender_id = #{userId1} AND receiver_id = #{userId2}) " +
            "OR (sender_id = #{userId2} AND receiver_id = #{userId1}) ORDER BY timestamp DESC LIMIT 1")
   // ChatDetail getLatestChatDetailBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 获取未读消息的发送者ID列表
    @Select("SELECT DISTINCT sender_id FROM chat_detail WHERE receiver_id = #{userId} AND status = 'unread'")
    List<Integer> getUnreadMessageSenderIds(@Param("userId") Integer userId);

    // 标记某个用户的所有未读消息为已读
    @Update("UPDATE chat_detail SET status = 'read' WHERE receiver_id = #{userId} AND status = 'unread'")
    int markAllMessagesAsReadForUser(@Param("userId") Integer userId);

    // 获取聊天记录的统计信息，例如总消息数、未读消息数等
    @Select("SELECT COUNT(*) as totalMessages, SUM(CASE WHEN status = 'unread' THEN 1 ELSE 0 END) as unreadMessages " +
            "FROM chat_detail WHERE (sender_id = #{userId1} AND receiver_id = #{userId2}) " +
            "OR (sender_id = #{userId2} AND receiver_id = #{userId1})")
    Map<String, Integer> getChatStatisticsBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 获取聊天记录中使用最多的单词（简单文本分析）
    @Select("SELECT word, COUNT(*) as frequency FROM " +
            "(SELECT REGEXP_SPLIT_TO_TABLE(message, '\\s+') as word FROM chat_detail WHERE " +
            "(sender_id = #{userId1} AND receiver_id = #{userId2}) " +
            "OR (sender_id = #{userId2} AND receiver_id = #{userId1})) as words " +
            "GROUP BY word ORDER BY frequency DESC LIMIT 10")
    List<Map<String, Object>> getMostFrequentWordsInChat(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 获取某个用户的聊天伙伴列表（去重）
    @Select("SELECT DISTINCT CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END as chatPartnerId " +
            "FROM chat_detail WHERE sender_id = #{userId} OR receiver_id = #{userId}")
    List<Integer> getChatPartnerIds(@Param("userId") Integer userId);

    // 获取某个用户与所有聊天伙伴的最新消息
    @Select("SELECT DISTINCT ON (chatPartnerId) * FROM (" +
            "SELECT CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END as chatPartnerId, * " +
            "FROM chat_detail WHERE sender_id = #{userId} OR receiver_id = #{userId} " +
            "ORDER BY chatPartnerId, timestamp DESC) as subquery ORDER BY chatPartnerId")
    //List<ChatDetail> getLatestMessagesWithAllPartners(@Param("userId") Integer userId);

    // 获取某个用户与所有聊天伙伴的未读消息统计
    @Select("SELECT chatPartnerId, COUNT(*) as unreadMessages FROM (" +
            "SELECT CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END as chatPartnerId " +
            "FROM chat_detail WHERE (sender_id = #{userId} OR receiver_id = #{userId}) AND status = 'unread') as subquery " +
            "GROUP BY chatPartnerId")
    List<Map<String, Object>> getUnreadMessageStatsForUser(@Param("userId") Integer userId);

    // 批量插入聊天记录
    @Insert({
            "<script>",
            "INSERT INTO chat_detail (sender_id, receiver_id, message, timestamp, status) VALUES ",
            "<foreach collection='chatDetails' item='chatDetail' separator=','>",
            "(#{chatDetail.senderId}, #{chatDetail.receiverId}, #{chatDetail.message}, #{chatDetail.timestamp}, #{chatDetail.status})",
            "</foreach>",
            "</script>"
    })
    //int bulkInsertChatDetails(@Param("chatDetails") List<ChatDetail> chatDetails);

    // 批量删除聊天记录
    @Delete({
            "<script>",
            "DELETE FROM chat_detail WHERE id IN ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int bulkDeleteChatDetails(@Param("ids") List<Integer> ids);
}
