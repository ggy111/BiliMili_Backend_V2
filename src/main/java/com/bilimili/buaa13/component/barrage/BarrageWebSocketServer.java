package com.bilimili.buaa13.component.barrage;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bilimili.buaa13.mapper.BarrageMapper;
import com.bilimili.buaa13.entity.Barrage;
import com.bilimili.buaa13.entity.User;
import com.bilimili.buaa13.service.video.VideoStatusService;
import com.bilimili.buaa13.tools.JsonWebTokenTool;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/barrage/{vid}")
public class BarrageWebSocketServer {

    // 由于每个连接都不是共享一个WebSocketServer，所以要静态注入
    private static JsonWebTokenTool jsonWebTokenTool;
    private static RedisTool redisTool;
    private static BarrageMapper barrageMapper;
    private static VideoStatusService videoStatusService;

    @Autowired
    public void setDependencies(JsonWebTokenTool jsonWebTokenTool, RedisTool redisTool, BarrageMapper barrageMapper, VideoStatusService videoStatusService) {
        BarrageWebSocketServer.jsonWebTokenTool = jsonWebTokenTool;
        BarrageWebSocketServer.redisTool = redisTool;
        BarrageWebSocketServer.barrageMapper = barrageMapper;
        BarrageWebSocketServer.videoStatusService = videoStatusService;
    }
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    // 对每个视频存储该视频下的session集合
    private static final Map<String, List<Session>> videoConnectionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立时触发，记录session到map
     * @param session 会话
     * @param vid   视频的ID
     */
    @OnOpen
    public void connectRecordMap(Session session, @PathParam("vid") String vid) {
        //检测vid字段有没有session
        if (videoConnectionMap.get(vid) == null || videoConnectionMap.get(vid).isEmpty()) {
            List<Session> sessionList = new ArrayList<>();
            sessionList.add(session);
            videoConnectionMap.put(vid, sessionList);
        } else {
            videoConnectionMap.get(vid).add(session);
        }
        sendMessage(vid, "当前观看人数" + videoConnectionMap.get(vid).size());
    }

    /**
     * 收到消息时触发，记录到数据库并转发到对应的全部连接
     * @param session   当前会话
     * @param message   信息体（包含"token"、"vid"、"data"字段）
     * @param vid   视频ID
     */
    @OnMessage
    public void connectOnMessage(Session session, String message, @PathParam("vid") String vid) {
        try {
            JSONObject jsonMessage = JSON.parseObject(message);
            // token鉴权
            String token = jsonMessage.getString("token");
            boolean verifyTokenMessage = jsonWebTokenTool.verifyToken(token.substring(7));
            if (!StringUtils.hasText(token) || !token.startsWith("Bearer ") || !verifyTokenMessage) {
                session.getBasicRemote().sendText("登录已过期");
                return;
            }
            token = token.substring(7);
            String userId = JsonWebTokenTool.getSubjectFromToken(token);
            String role = JsonWebTokenTool.getClaimFromToken(token, "role");
            User user = redisTool.getObject("security:" + role + ":" + userId, User.class);
            if (user == null) {
                session.getBasicRemote().sendText("登录已过期");
                return;
            }

            JSONObject data = jsonMessage.getJSONObject("data");
            Barrage barrage = new Barrage(
                    null,
                    Integer.parseInt(vid),
                    user.getUid(),
                    data.getString("content"),
                    data.getInteger("word_size"),
                    data.getInteger("mode"),
                    data.getString("color"),
                    data.getDouble("timePoint"),
                    1,
                    new Date()
            );

            // 使用CompletableFuture处理异步操作
            CompletableFuture<Void> insertBarrageFuture = CompletableFuture.runAsync(() -> barrageMapper.insert(barrage),taskExecutor);

            CompletableFuture<Void> updateVideoStatsFuture = CompletableFuture.runAsync(() ->
                    videoStatusService.updateVideoStatus(Integer.parseInt(vid), "barrage", true, 1)
                    , taskExecutor
            );

            CompletableFuture<Void> addRedisMemberFuture = CompletableFuture.runAsync(() ->
                    redisTool.addSetMember("barrage_bidSet:" + vid, barrage.getBid()),taskExecutor
            );

            // 等待所有异步任务完成
            CompletableFuture.allOf(insertBarrageFuture, updateVideoStatsFuture, addRedisMemberFuture).join();

            // 广播弹幕
            String barrageJson = JSON.toJSONString(barrage);
            sendMessage(vid, barrageJson);

        } catch (Exception e) {
            log.error(e.getMessage(), e); // 记录异常
        }
    }

    /**
     * 连接关闭时执行
     * @param session   当前会话
     * @param vid   视频ID
     */
    @OnClose
    public void connectOnClose(Session session, @PathParam("vid") String vid) {

        CopyOnWriteArrayList<Session> sessions = (CopyOnWriteArrayList<Session>) videoConnectionMap.get(vid);
        // 从缓存中移除连接记录
        videoConnectionMap.get(vid).remove(session);
        if (videoConnectionMap.get(vid).isEmpty()) {
            // 如果没人了就直接移除这个视频
            videoConnectionMap.remove(vid);
        } else {
            // 否则更新在线人数
            sendMessage(vid, "当前观看人数" + videoConnectionMap.get(vid).size());
        }
    }

    @OnError
    public void onError(Throwable error) {
        log.error("websocket发生错误");
        error.printStackTrace();
    }

    /**
     * 往对应的全部连接发送消息
     * @param vid   视频ID
     * @param text  消息内容，对象需转成JSON字符串
     */
    private void sendMessage(String vid, String text) {
        List<Session> sessionList = videoConnectionMap.get(vid);
        // 使用并行流往各客户端发送数据
        sessionList.parallelStream().forEach(session -> {
            try {
                session.getBasicRemote().sendText(text);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        });
    }
}
