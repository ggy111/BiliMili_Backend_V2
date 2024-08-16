package com.bilimili.buaa13.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RedisUtil {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    private static final long REDIS_DEFAULT_EXPIRE_TIME = 60 * 60;
    private static final TimeUnit REDIS_DEFAULT_EXPIRE_TIMEUNIT = TimeUnit.SECONDS;

    // 定义ZSetObject类，表示需要写入到ZSet中的数据
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ZObjTime {
        private Object member;
        private Date time;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ZObjScore {
        private Object member;
        private Double score;
    }

    // ZSET 相关操作 begin ----------------------------------------------------------------------------------------------

    /**
     * 分数从大到小取排行榜
     * @param key
     * @param start
     * @param stop
     * @return
     */
    public Set<Object> zReverange(String key, long start, long stop) {
        return redisTemplate.opsForZSet().reverseRange(key, start, stop);
    }

    /**
     * 按时间从大到小取数据携带分数
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<ZObjScore> zReverangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<Object>> result = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        if (result == null) return null;
        List<ZObjScore> list = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : result) {
            list.add(new ZObjScore(tuple.getValue(), tuple.getScore()));
        }
        return list;
    }

    /**
     * 存入一条数据到sorted set    默认按时间排序
     * @param key
     * @param object
     */
    public boolean zset(String key, Object object){
        long now = System.currentTimeMillis();
        return this.zsetWithScore(key, object, now);
    }

    /**
     * 存入一条数据到sorted set    按分数排序
     * @param key
     * @param object
     */
    public boolean zsetWithScore(String key, Object object, double score){
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, object, score));
    }

    /**
     * 批量存入数据到sorted set
     *
     * @param key
     * @param zObjTimes 自定义的类 RedisUtil.ZObjTime 的集合或列表
     */
    public void zsetOfCollectionByTime(String key, Collection<ZObjTime> zObjTimes) {
        redisTemplate.opsForZSet().add(key, convertToTupleSetByTime(zObjTimes));
    }

    // 将ZSetObject集合转换为Tuple集合
    private Set<ZSetOperations.TypedTuple<Object>> convertToTupleSetByTime(Collection<ZObjTime> zObjTimes) {
        return zObjTimes.stream()
                .map(zObjTime -> new DefaultTypedTuple<>(zObjTime.getMember(), (double) zObjTime.getTime().getTime()))
                .collect(Collectors.toSet());
    }

    /**
     * 批量存入数据到sorted set
     *
     * @param key
     * @param zObjScores 自定义的类 RedisUtil.ZObjScores 的集合或列表
     */
    public void zsetOfCollectionByScore(String key, Collection<ZObjScore> zObjScores) {
        redisTemplate.opsForZSet().add(key, convertToTupleSetByScore(zObjScores));
    }

    private Set<ZSetOperations.TypedTuple<Object>> convertToTupleSetByScore(Collection<ZObjScore> zObjScores) {
        return zObjScores.stream()
                .map(zObjScore -> new DefaultTypedTuple<>(zObjScore.getMember(), zObjScore.getScore()))
                .collect(Collectors.toSet());
    }

    /**
     * 获取整个集合元素个数
     * @param key
     * @return
     */
    public Long zCard(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 删除Set指定key下的对象
     * @param key
     * @param value
     */
    public void zsetDelMember(String key, Object value) {
        redisTemplate.opsForZSet().remove(key, value);
    }


    // ZSET 相关操作 end ------------------------------------------------------------------------------------------------


    // SET 相关操作 begin -----------------------------------------------------------------------------------------------

    /**
     * set集合获取
     * @param key
     * @return
     */
    public Set<Object> getMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 集合set中是否存在目标对象
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean isMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 向SET中添加无过期时间的对象
     * @param key
     * @param value
     */
    public void addMember(String key, Object value) {
        redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 删除SET中的数据
     * @param key
     * @param value
     */
    public void delMember(String key, Object value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    // SET 相关操作 end -------------------------------------------------------------------------------------------------


    // String 相关操作 begin --------------------------------------------------------------------------------------------

    /**
     * 使用默认有效期存储实体类
     * @param key
     * @param value
     */
    public void setExObjectValue(String key, Object value) {
        String jsonString = JSON.toJSONString(value);
        redisTemplate.opsForValue().set(key, jsonString, REDIS_DEFAULT_EXPIRE_TIME, REDIS_DEFAULT_EXPIRE_TIMEUNIT);
    }

    /**
     * 根据传入的类型获取实体类
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String objectString = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(objectString)) {
            return JSONObject.parseObject(objectString, clazz);
        }
        return null;
    }

    /**
     * 删除简单数据类型或实体类
     * @param key
     */
    public void delValue(String key) {
        redisTemplate.opsForValue().getOperations().delete(key);
    }


    // String 相关操作 end ----------------------------------------------------------------------------------------------

}