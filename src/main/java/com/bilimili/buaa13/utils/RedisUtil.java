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
    private RedisTemplate redisTemplate;

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

    // 通用 相关操作 begin -----------------------------------------------------------------------------------------------

    /**
     * 删除指定前缀的所有key
     * @param prefix
     */
    public void deleteKeysWithPrefix(String prefix) {
        // 获取以指定前缀开头的所有键
        Set<String> keys = redisTemplate.keys(prefix + "*");
        // 删除匹配的键
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 查询key是否存在
     *
     * @param redisKey
     * @return
     */
    public boolean isExist(String redisKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }

    // 通用 相关操作 end -------------------------------------------------------------------------------------------------

    // ZSET 相关操作 begin ----------------------------------------------------------------------------------------------

    /**
     * 分数从小到大取排行榜
     * @param key
     * @param start
     * @param stop
     * @return
     */
    public Set<Object> zRange(String key, long start, long stop) {
        return redisTemplate.opsForZSet().range(key, start, stop);
    }

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
        return redisTemplate.opsForZSet().add(key, object, score);
    }

    /**
     * 批量存入数据到sorted set
     * @param key
     * @param zObjTimes   自定义的类 RedisUtil.ZObjTime 的集合或列表
     */
    public Long zsetOfCollectionByTime(String key, Collection<ZObjTime> zObjTimes) {
        return redisTemplate.opsForZSet().add(key, convertToTupleSetByTime(zObjTimes));
    }

    // 将ZSetObject集合转换为Tuple集合
    private Set<ZSetOperations.TypedTuple<Object>> convertToTupleSetByTime(Collection<ZObjTime> zObjTimes) {
        return zObjTimes.stream()
                .map(zObjTime -> new DefaultTypedTuple<>(zObjTime.getMember(), (double) zObjTime.getTime().getTime()))
                .collect(Collectors.toSet());
    }

    /**
     * 批量存入数据到sorted set
     * @param key
     * @param zObjScores   自定义的类 RedisUtil.ZObjScores 的集合或列表
     */
    public Long zsetOfCollectionByScore(String key, Collection<ZObjScore> zObjScores) {
        return redisTemplate.opsForZSet().add(key, convertToTupleSetByScore(zObjScores));
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

    /**
     * 查询某个元素的分数
     * @param key
     * @param value
     * @return
     */
    public Double zscore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 对某个元素增加分数
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public Double zincrby(String key, Object value, double score) {
        return redisTemplate.opsForZSet().incrementScore(key, value, score);
    }

    /**
     * 集合zset中是否存在目标对象
     * @param key
     * @param value
     * @return
     */
    public Boolean zsetExist(String key, Object value) {
        Double d = zscore(key, value);
        return null != d;
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
     * 向SET中添加无过期时间的对象列表
     * @param key
     * @param list
     */
    public void addMembers(String key, List<Object> list) {
        redisTemplate.opsForSet().add(key, list.toArray());
    }

    /**
     * 删除SET中的数据
     * @param key
     * @param value
     */
    public void delMember(String key, Object value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    /**
     * 查询SET大小
     * @param key
     * @return
     */
    public Long scard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 随机返回集合中count个元素的集合
     * @param key
     * @param count
     * @return
     */
    public Set<Object> srandmember(String key, Integer count) {
        return redisTemplate.opsForSet().distinctRandomMembers(key, count);
    }

    // SET 相关操作 end -------------------------------------------------------------------------------------------------


    // String 相关操作 begin --------------------------------------------------------------------------------------------
    /**
     * 存储简单数据类型
     * 不用更新的缓存信息
     * @param key
     * @param value
     */
    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 使用 默认有效期 和 默认时间单位 存储简单数据类型
     * @param key
     * @param value
     */
    public void setExValue(String key, Object value) {
        setExValue(key, value, REDIS_DEFAULT_EXPIRE_TIME, REDIS_DEFAULT_EXPIRE_TIMEUNIT);
    }

    /**
     * 使用 指定有效期 和 指定时间单位 存储简单数据类型
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     */
    public void setExValue(String key, Object value, long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, time, timeUnit);
    }

    /**
     * 使用默认有效期存储实体类
     * @param key
     * @param value
     */
    public void setExObjectValue(String key, Object value) {
        String jsonString = JSON.toJSONString(value);
        setExValue(key, jsonString);
    }

    /**
     * 使用指定有效期存储实体类
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     */
    public void setExObjectValue(String key, Object value, long time, TimeUnit timeUnit) {
        String jsonString = JSON.toJSONString(value);
        setExValue(key, jsonString, time, timeUnit);
    }

    /**
     * 获取简单数据类型
     * @param key
     * @return
     */
    public Object getValue(Object key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取实体类的JSONString
     * @param key
     * @return
     */
    public String getObjectString(String key) {
        return (String) redisTemplate.opsForValue().get(key);
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

    /**
     * 删除多个key
     * @param keys
     */
    public void delValues(Collection<String> keys) {
        redisTemplate.opsForValue().getOperations().delete(keys);
    }


    // String 相关操作 end ----------------------------------------------------------------------------------------------

    // List 相关操作 start ----------------------------------------------------------------------------------------------
    /**
     * 把list存入redis
     * @param key
     * @return
     */
    public Long setAllList(String key, List list) {
        List<String> dataList = new ArrayList<>();
        for (Object temp : list) {
            dataList.add(JSON.toJSONString(temp));
        }
        return this.redisTemplate.opsForList().rightPushAll(key, dataList);
    }

    /**
     * 获取list中全部数据
     * @param key
     * @param clazz
     * @return
     */
    public <T> List<T> getAllList(String key, Class<T> clazz) {
        List list = this.redisTemplate.opsForList().range(key, 0, -1);
        List<T> resultList = new ArrayList<>();
        for (Object temp : list) {
            resultList.add(JSON.parseObject((String) temp, clazz));
        }
        return resultList;
    }

    // List 相关操作 end ------------------------------------------------------------------------------------------------
}