package com.bilimili.buaa13.service.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bilimili.buaa13.entity.ChatDetailed;
import com.bilimili.buaa13.entity.Video;
import com.bilimili.buaa13.mapper.ChatDetailedMapper;
import com.bilimili.buaa13.mapper.VideoMapper;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
public class EventListenerService {

    @Value("${directory.chunk}")
    private String Fragment_DIRECTORY;   // 分片存储目录

    @Autowired
    private RedisTool redisTool;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private ChatDetailedMapper chatDetailedMapper;

    public static List<RedisTool.ZSetScore> hotSearchWords = new ArrayList<>();     // 上次更新的热搜词条

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 每一小时更新一次热搜词条热度
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void updateHotSearch() {
        CompletableFuture<?> updateFuture = CompletableFuture.runAsync(()->{
            List<RedisTool.ZSetScore> list = redisTool.reverseRangeWithScores("search_word", 0, -1);
            if (list == null || list.isEmpty()) return;
            int count = list.size();
            double total = 0;
            // 计算总分数
            for (RedisTool.ZSetScore o : list) {
                total += o.getScore();
            }
            BigDecimal bt = new BigDecimal(total);
            total = bt.setScale(2, RoundingMode.HALF_UP).doubleValue();
            double totalScore = calculateTotalScore(list);
            // 更新每个词条的分数    新分数 = (旧分数 / 总分数) * 词条数
            List<RedisTool.ZSetScore> updatedList = list.stream()
                    .peek(o -> o.setScore(calculateNewScore(o.getScore(), totalScore, count)))
                    .toList();
            // 批量更新到redis上，根据分数
            CompletableFuture<?> redisFuture = CompletableFuture.runAsync(()->
                            redisTemplate.opsForZSet().add("search_word", convertToTupleSetByScore(list))
                    ,taskExecutor);
            CompletableFuture.allOf(redisFuture).join();
            // 保存新热搜
            if (list.size() < 10) {
                hotSearchWords = list.subList(0, list.size());
            } else {
                hotSearchWords = list.subList(0, 10);
            }
        },taskExecutor);
        CompletableFuture.allOf(updateFuture).join();
    }

    private double calculateTotalScore(List<RedisTool.ZSetScore> list) {
        double total = list.stream()
                .mapToDouble(RedisTool.ZSetScore::getScore)
                .sum();
        return new BigDecimal(total).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double calculateNewScore(double oldScore, double totalScore, int count) {
        BigDecimal newScore = new BigDecimal((oldScore / totalScore) * count);
        return newScore.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 每天4点删除三天前未使用的分片文件
     */
    @Scheduled(cron = "0 0 4 * * ?")  // 每天4点0分0秒触发任务 // cron表达式格式：{秒数} {分钟} {小时} {日期} {月份} {星期} {年份(可为空)}
    public void deleteFragments() {
        CompletableFuture<?> deleteFuture = CompletableFuture.runAsync(()->{
            // 获取分片文件的存储目录
            File FragmentDir = new File(Fragment_DIRECTORY);
            // 获取所有分片文件
            File[] fragmentFiles = FragmentDir.listFiles();
            if (fragmentFiles != null) {
                Arrays.stream(fragmentFiles)
                        .filter(file -> {
                            try{
                                return isOldFile(file);
                            }catch (IOException ioe){
                                log.error("每天检查删除过期分片时出错了：{}", ioe.getMessage());
                                return false;
                            }
                        })
                        .forEach(file -> {
                            try{
                                deleteIfOld(file);
                            } catch (IOException e) {
                                log.error("每天检查删除过期分片时出错了:{}", e.getMessage());
                            }
                        });
            }
        },taskExecutor);
        deleteFuture.join();
    }

    private boolean isOldFile(File fragmentFile) throws IOException {
        Path filePath = fragmentFile.toPath();
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
        FileTime createTime = attr.creationTime();
        Instant instant = createTime.toInstant();
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        LocalDateTime createDateTime = zonedDateTime.toLocalDateTime();
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        return createDateTime.isBefore(threeDaysAgo);
    }

    private void deleteIfOld(File fragmentFile) throws IOException {
        Path filePath = fragmentFile.toPath();
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
        FileTime createTime = attr.creationTime();
        Instant instant = createTime.toInstant();
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        LocalDateTime createDateTime = zonedDateTime.toLocalDateTime();
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        if (createDateTime.isBefore(threeDaysAgo)) {
            boolean deleted = fragmentFile.delete();
            if (deleted) {
                log.info("已删除文件: {}", fragmentFile.getName());
            } else {
                log.error("无法删除文件: {}", fragmentFile.getName());
            }
        }
    }

    /**
     * 每24小时同步一下各状态的视频集合
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    public void updateVideoStatus() {
        for (int i = 0; i < 3; i++) {
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", i).isNull("delete_date").select("vid");
            List<Object> vidList = videoMapper.selectObjs(queryWrapper);
            try {
                redisTool.deleteValue("video_status:" + i);   // 先将原来的删掉
                if (vidList != null && !vidList.isEmpty()) {
                    //向SET中添加无过期时间的对象列表
                    redisTemplate.opsForSet().add("video_status:" + i, vidList);
                }
            } catch (Exception e) {
                log.error("redis更新审核视频集合失败");
            }
        }
    }

    public void updateVideoStats() {
        IntStream.range(0, 3).forEach(i -> {
            AtomicReference<List<Object>> vidList = new AtomicReference<>(new ArrayList<>());
            CompletableFuture<?> futureVideo = CompletableFuture.runAsync(()->{
                QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("status", i).isNull("delete_date").select("vid");
                vidList.set(videoMapper.selectObjs(queryWrapper));
            },taskExecutor);
            CompletableFuture<?> futureRedis = CompletableFuture.runAsync(()->{
                try {
                    redisTool.deleteValue("video_status:" + i);
                    if (vidList.get() != null && !vidList.get().isEmpty()) {
                        //向SET中添加无过期时间的对象列表
                        redisTemplate.opsForSet().add("video_status:" + i, vidList.get());
                    }
                } catch (Exception e) {
                    log.error("Redis 更新审核视频集合失败：{}", e.getMessage());
                }
            },taskExecutor);
            CompletableFuture.allOf(futureVideo,futureRedis).join();
        });
    }
    /**
     * 每天4点15分同步一下全部用户的聊天记录
     */
    @Scheduled(cron = "0 15 4 * * ?")   // 每天4点15分0秒触发任务
    public void updateChatDetailedZSet() {
        try {
            QueryWrapper<ChatDetailed> queryWrapper = new QueryWrapper<>();
            List<ChatDetailed> list = chatDetailedMapper.selectList(queryWrapper);
            // 按用户将对应的消息分类整理
            Set<Map<String, Integer>> chatSet = new HashSet<>();
            Map<Integer, Map<Integer, Set<RedisTool.ZSetTime>>> setMap = new HashMap<>();
            for (ChatDetailed chatDetailed : list) {
                Integer from = chatDetailed.getPostId();    // 发送者ID
                Integer to = chatDetailed.getAcceptId();   // 接收者ID

                // 发送者视角 chat_detailed_zset:to:from
                Map<String, Integer> fromMap = new HashMap<>();
                fromMap.put("user_id", to);
                fromMap.put("another_id", from);
                chatSet.add(fromMap);
                if (chatDetailed.getPostDel() == 0) {
                    // 发送者没删就加到对应聊天的有序集合
                    addOrderlySet(setMap, chatDetailed, from, to);
                }

                // 接收者视角 chat_detailed_zset:from:to
                Map<String, Integer> toMap = new HashMap<>();
                toMap.put("user_id", from);
                toMap.put("another_id", to);
                chatSet.add(toMap);
                if (chatDetailed.getAcceptDel() == 0) {
                    // 接收者没删就加到对应聊天的有序集合
                    addOrderlySet(setMap, chatDetailed, to, from);
                }
            }

            // 更新redis,根据时间
            for (Map<String, Integer> map : chatSet) {
                Integer uid = map.get("post_id");
                Integer aid = map.get("accept_id");
                String key = "chat_detailed_zset:" + uid + ":" + aid;
                redisTool.deleteValue(key);
                //批量存入数据到sorted set
                redisTemplate.opsForZSet().add(key, convertToTupleSetByTime(setMap.get(uid).get(aid)));
            }
        } catch (Exception e) {
            log.error("每天同步聊天记录时出错了：" + e);
        }

    }

    public void updateChatDetailedOrderlySet() {
        CompletableFuture.runAsync(() -> {
            try {
                QueryWrapper<ChatDetailed> queryWrapper = new QueryWrapper<>();
                List<ChatDetailed> list = chatDetailedMapper.selectList(queryWrapper);
                // 使用并行流来处理列表
                Map<Integer, Map<Integer, Set<RedisTool.ZSetTime>>> setMap = list.parallelStream()
                        .flatMap(chatDetailed -> {
                            Integer from = chatDetailed.getPostId();    // 发送者ID
                            Integer to = chatDetailed.getAcceptId();   // 接收者ID
                            // 发送者视角
                            Set<Map.Entry<String, Integer>> fromEntries = new HashSet<>();
                            if (chatDetailed.getPostDel() == 0) {
                                fromEntries.add(Map.entry("user_id", to));
                                fromEntries.add(Map.entry("another_id", from));
                            }
                            // 接收者视角
                            Set<Map.Entry<String, Integer>> toEntries = new HashSet<>();
                            if (chatDetailed.getAcceptDel() == 0) {
                                toEntries.add(Map.entry("user_id", from));
                                toEntries.add(Map.entry("another_id", to));
                            }
                            // 将两个视角的结果返回
                            return Stream.of(fromEntries, toEntries).flatMap(Set::stream);
                        })
                        .collect(Collectors.toMap(
                                Map.Entry::getValue,
                                e -> {
                                    Map<Integer, Set<RedisTool.ZSetTime>> innerMap = new HashMap<>();
                                    innerMap.put(e.getValue(), new HashSet<>());
                                    return innerMap;
                                },
                                (existing, replacement) -> existing
                        ));
                // 更新Redis，异步操作
                CompletableFuture.allOf(
                        setMap.entrySet().stream()
                                .flatMap(entry -> entry.getValue().entrySet().stream())
                                .map(innerEntry -> CompletableFuture.runAsync(() -> {
                                    String key = "chat_detailed_zset:" + innerEntry.getKey() + ":" + innerEntry.getValue();
                                    redisTool.deleteValue(key);
                                    //批量存入数据到sorted set
                                    redisTemplate.opsForZSet().add(key, convertToTupleSetByTime(innerEntry.getValue()));
                                }))
                                .toArray(CompletableFuture[]::new)
                ).join();  // 等待所有异步操作完成
            } catch (Exception e) {
                log.error("每天同步聊天记录时出错了:{}", e.getMessage());
            }
        });
    }

    private void addOrderlySet(Map<Integer, Map<Integer, Set<RedisTool.ZSetTime>>> setMap, ChatDetailed chatDetailed, Integer from, Integer to) {
        if (setMap.get(from) == null) {
            Map<Integer, Set<RedisTool.ZSetTime>> map = new HashMap<>();
            Set<RedisTool.ZSetTime> set = new HashSet<>();
            set.add(new RedisTool.ZSetTime(chatDetailed.getId(), chatDetailed.getTime()));
            map.put(to, set);
            setMap.put(from, map);
        } else {
            if (setMap.get(from).get(to) == null) {
                Set<RedisTool.ZSetTime> set = new HashSet<>();
                set.add(new RedisTool.ZSetTime(chatDetailed.getId(), chatDetailed.getTime()));
                setMap.get(from).put(to, set);
            } else {
                setMap.get(from).get(to)
                        .add(new RedisTool.ZSetTime(chatDetailed.getId(), chatDetailed.getTime()));
            }
        }
    }

    private void addZSet(Map<Integer, Map<Integer, Set<RedisTool.ZSetTime>>> setMap, ChatDetailed chatDetailed, Integer from, Integer to) {
        // 使用 computeIfAbsent 方法减少重复代码
        setMap.computeIfAbsent(from, k -> new HashMap<>())
                .computeIfAbsent(to, k -> new HashSet<>())
                .add(new RedisTool.ZSetTime(chatDetailed.getId(), chatDetailed.getTime()));
    }

    // 将ZSetObject集合转换为Tuple集合，根据时间
    private Set<ZSetOperations.TypedTuple<Object>> convertToTupleSetByTime(Collection<RedisTool.ZSetTime> zSetTimes) {
        return zSetTimes.stream()
                .map(zSetTime -> new DefaultTypedTuple<>(zSetTime.getMember(), (double) zSetTime.getTime().getTime()))
                .collect(Collectors.toSet());
    }

    //将ZSetObject集合转换为Tuple集合，根据分数
    private Set<ZSetOperations.TypedTuple<Object>> convertToTupleSetByScore(Collection<RedisTool.ZSetScore> zSetScores) {
        return zSetScores.stream()
                .map(zSetScore -> new DefaultTypedTuple<>(zSetScore.getMember(), zSetScore.getScore()))
                .collect(Collectors.toSet());
    }
}
