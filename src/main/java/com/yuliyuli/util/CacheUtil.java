package com.yuliyuli.util;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 缓存工具类
 * 用于设置缓存过期时间，预防缓存雪崩和缓存击穿
 * 缓存穿透需要在业务层处理
 */
@Component
public class CacheUtil {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 基础过期时间(秒)
    private static final int BASE_EXPIRE_TIME = 3600;
    //随机过期时间范围(秒)
    private static final int RANDOM_EXPIRE_TIME_RANGE = 3600;

    /**
     * 用随机时间设置缓存，预防缓存雪崩
     */
    public void preventAvalanche(String key, Object value){
        int expireTime = BASE_EXPIRE_TIME + new Random().nextInt(RANDOM_EXPIRE_TIME_RANGE);
        redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
    }

    /**
     *设置永不过期时间，预防缓存击穿
     *也可在业务中加synchronized锁，防止高并发下的缓存击穿
     */
    public void preventBreakThroughWithoutExpireTime(String key, Object value){
        redisTemplate.opsForValue().set(key, value);
    }
}
