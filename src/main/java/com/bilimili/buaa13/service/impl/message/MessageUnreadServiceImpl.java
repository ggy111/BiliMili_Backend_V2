package com.bilimili.buaa13.service.impl.message;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bilimili.buaa13.entity.Chat;
import com.bilimili.buaa13.entity.IMResponse;
import com.bilimili.buaa13.entity.MessageUnread;
import com.bilimili.buaa13.im.IMServer;
import com.bilimili.buaa13.mapper.ChatMapper;
import com.bilimili.buaa13.mapper.MessageUnreadMapper;
import com.bilimili.buaa13.service.message.MessageUnreadService;
import com.bilimili.buaa13.tools.RedisTool;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Service
public class MessageUnreadServiceImpl implements MessageUnreadService {

    private static final Logger log = LoggerFactory.getLogger(MessageUnreadServiceImpl.class);
    @Autowired
    private MessageUnreadMapper messageUnreadMapper;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private RedisTool redisTool;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ExecutorService executorService;

    /**
     * 关闭线程
     */
    private static void shutdownTermination(ExecutorService pool) {
        pool.shutdown(); // 禁止提交新任务
        if(!pool.isShutdown()){
            pool.shutdownNow();
        }
        try {
            // 等待现有任务完成
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // 取消正在执行的任务
                // 等待任务响应中断
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("线程池未能关闭");
            }
        } catch (InterruptedException ie) {
            // 重新中断当前线程
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 给指定用户的某一列未读消息加一
     * @param uid   用户ID
     * @param column    msg_unread表列名 "reply"/"at_num"/"love"/"system_message"/"message"/"dynamic"
     */
    @Override
    public void addOneUnread(Integer uid, String column) {
        executorService = Executors.newFixedThreadPool(10);
        try{
            Future<?> addFuture = executorService.submit(()->{
                UpdateWrapper<MessageUnread> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("uid", uid).setSql(column + " = " + column + " + 1");
                messageUnreadMapper.update(null, updateWrapper);
                redisTool.deleteValue("message_unread:" + uid);
            },taskExecutor);
            addFuture.get();
        }catch (Exception e){
            log.error("线程出现错误{}",e.getMessage());
        }finally {
            shutdownTermination(executorService);
        }
    }

    /**
     * 清除指定用户的某一列未读消息
     * @param uid   用户ID
     * @param column    msg_unread表列名 "reply"/"at_num"/"love"/"system_message"/"message"/"dynamic"
     */
    @Override
    public void clearOneUnread(Integer uid, String column) {
        QueryWrapper<MessageUnread> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).ne(column, 0);
        MessageUnread messageUnread = messageUnreadMapper.selectOne(queryWrapper);
        // 如果本身就是0条未读,不执行下面的操作了
        if (messageUnread == null) return;
        if (column.equals("message")) {
            // 如果是清除私聊消息还需要去把chat表的全部未读清掉
            UpdateWrapper<Chat> chatUpdateWrapper = new UpdateWrapper<>();
            chatUpdateWrapper.eq("accept_id", uid).set("unread_num", 0);
            chatMapper.update(null, chatUpdateWrapper);
        }
        UpdateWrapper<MessageUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).set(column, 0);
        messageUnreadMapper.update(null, updateWrapper);
        redisTool.deleteValue("message_unread:" + uid);

        // 通知用户的全部channel 更新该消息类型未读数为0
        Map<String, Object> map = new HashMap<>();
        map.put("type", "全部已读");
        Set<Channel> userChannels = IMServer.userChannel.get(uid);
        if (userChannels != null) {
            userChannels.stream().forEach(userChannel->userChannel.writeAndFlush(IMResponse.message(column, map)));
        }

    }

    /**
     * 私聊消息,减除一定数量的未读
     * @param uid   用户ID
     * @param count 要减多少
     */
    @Override
    public void subUnreadWhisper(Integer uid, Integer count) {
        redisTool.deleteValue("message_unread:" + uid);
        String sql = "UPDATE message_unread " +
                "SET message = IF(message - ? < 0, 0, message - ?) " +
                "WHERE uid = ?";
        // 执行 SQL 语句
        jdbcTemplate.update(sql, count, count, uid);
    }

    /**
     * 获取某人的全部消息未读数
     * @param uid   用户ID
     * @return  MsgUnread对象
     */
    @Override
    public MessageUnread getUnreadByUid(Integer uid) {
        executorService = Executors.newFixedThreadPool(10);
        MessageUnread messageUnread = redisTool.getObject("message_unread:" + uid, MessageUnread.class);
        if (messageUnread == null) {
            messageUnread = messageUnreadMapper.selectById(uid);
            if (messageUnread != null) {
                MessageUnread finalMessageUnread = messageUnread;
                try{
                    Future<?> updateMsgRedis = executorService.submit(()->{
                        redisTool.setExObjectValue("message_unread:" + uid, finalMessageUnread);    // 异步更新到redis
                    },taskExecutor);
                    updateMsgRedis.get();
                }catch (InterruptedException ie){
                    Thread.currentThread().interrupt();
                }catch(ExecutionException ee){
                    log.error(ee.getMessage());
                }finally {
                    shutdownTermination(executorService);
                }
            } else {
                return new MessageUnread(uid,0,0,0,0,0,0);
            }
        }
        return messageUnread;
    }

    public void updateCount(Integer uid, Integer count) {
        // 查询当前的 message 值
        MessageUnread currentMessage = messageUnreadMapper.selectById(uid);
        // 计算新的 message 值
        int newMessage = Math.max(currentMessage.getMessage() - count, 0);
        currentMessage.setMessage(newMessage);
        // 更新数据库
        messageUnreadMapper.updateById(currentMessage);

        // 假设MsgUnread类中有一个version字段用于乐观锁
        MessageUnread messageUnread = messageUnreadMapper.selectById(uid);
        if (messageUnread != null) {
            newMessage = Math.max(messageUnread.getMessage() - count, 0);
            messageUnread.setMessage(newMessage);

            // 尝试更新，如果更新失败则重试
            int updateCount = messageUnreadMapper.updateByIdWithVersion(messageUnread);
            if (updateCount == 0) {
                updateCount(uid,count);
            }
            else return;
        }
        return;
    }
}
