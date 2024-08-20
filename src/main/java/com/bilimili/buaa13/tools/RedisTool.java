package com.bilimili.buaa13.tools;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisTool {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    private static final long REDIS_DEFAULT_EXPIRE_TIME = 60 * 60;
    private static final TimeUnit REDIS_DEFAULT_EXPIRE_TIMEUNIT = TimeUnit.SECONDS;

    // 定义ZSetObject类，表示需要写入到ZSet中的数据
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ZSetTime {
        private Object member;
        private Date time;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ZSetScore {
        private Object member;
        private Double score;
    }

    //通用
    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0 代表为永久有效
     */
    public long getExpire(String key) {
        Long expireTime = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if(expireTime == null) return -1;
        else return expireTime;
    }

    /**
     * 设置过期日期
     *
     * @param key  key
     * @param date 过期日期
     *
     * @return 是否成功
     */
    public Boolean expireAt(String key, Date date) {
        return redisTemplate.expireAt(key, date);
    }

    /**
     * 移除 key 的过期时间，key 将持久保持
     *
     * @param key key
     *
     * @return 是否成功
     */
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 删除key
     *
     * @param key key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除key
     *
     * @param keys key集合
     */
    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 序列化key
     *
     * @param key key
     *
     * @return 字节数组
     */
    public byte[] dump(String key) {
        return redisTemplate.dump(key);
    }

    // ZSET 相关操作 begin ----------------------------------------------------------------------------------------------

    /**
     * 分数从大到小取排行榜
     * @param key
     * @param start
     * @param stop
     * @return
     */
    public Set<Object> reverseRange(String key, long start, long stop) {
        return redisTemplate.opsForZSet().reverseRange(key, start, stop);
    }

    /**
     * 按时间从大到小取数据携带分数
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<ZSetScore> reverseRangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<Object>> result = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        if (result == null) return null;
        List<ZSetScore> list = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : result) {
            list.add(new ZSetScore(tuple.getValue(), tuple.getScore()));
        }
        return list;
    }

    /**
     * 存入一条数据到sorted set    默认按时间排序
     * @param key
     * @param object
     */
    public boolean storeZSet(String key, Object object){
        long now = System.currentTimeMillis();
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, object, now));
    }

    /**
     * 获取整个集合元素个数
     * @param key
     * @return
     */
    public Long getZSetNumber(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 删除Set指定key下的对象
     * @param key
     * @param value
     */
    public void deleteZSetMember(String key, Object value) {
        redisTemplate.opsForZSet().remove(key, value);
    }


    // ZSET 相关操作 end ------------------------------------------------------------------------------------------------


    // SET 相关操作 begin -----------------------------------------------------------------------------------------------

    /**
     * 普通缓存放入
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * set集合获取
     * @param key
     * @return
     */
    public Set<Object> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 集合set中是否存在目标对象
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean isSetMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 向SET中添加无过期时间的对象
     * @param key
     * @param value
     */
    public void addSetMember(String key, Object value) {
        redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 删除SET中的数据
     * @param key
     * @param value
     */
    public void deleteSetMember(String key, Object value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    /**
     * 获取hashKey对应的所有键值
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hashGet(String key) {
        return redisTemplate.opsForHash().entries(key);
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
     * 使用 指定有效期 和 指定时间单位 存储简单数据类型
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     */
    void setExValue(String key, Object value, long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, time, timeUnit);
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

    /**
     * 删除简单数据类型或实体类
     * @param key
     */
    public void deleteValue(String key) {
        redisTemplate.opsForValue().getOperations().delete(key);
    }


    // String 相关操作 end ----------------------------------------------------------------------------------------------

    /**
     * 将当前数据库的 key 移动到给定的数据库 db 当中
     *
     * @param key     key
     * @param dbIndex 目标DB
     *
     * @return 是否成功
     */
    public Boolean move(String key, int dbIndex) {
        return redisTemplate.move(key, dbIndex);
    }

    /**
     * 从当前数据库中随机返回一个 key
     *
     * @return 随机的 key
     */
    public String randomKey() {
        return redisTemplate.randomKey();
    }

    /**
     * 批量获取
     *
     * @param keys key 集合
     *
     * @return 值列表
     */
    public List<Object> multiGet(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

}