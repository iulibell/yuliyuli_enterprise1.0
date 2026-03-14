package com.yuliyuli.util;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/** 限流工具类 */
@Component
public class RateLimiterUtil {

  @Autowired private RedisTemplate<String, Object> redisTemplate;

  /**
   * 限流
   *
   * @param key 限流键
   * @param limit 限制次数
   * @param window 时间窗口(秒)
   * @return 是否允许
   */
  public boolean rateLimit(String key, int limit, int window) {
    String luaScript =
        "local current = redis.call('get', KEYS[1]); "
            + "if current and tonumber(current) >= tonumber(ARGV[1]) then "
            + "return 0; "
            + "else "
            + "redis.call('incr', KEYS[1]); "
            + "if not current then "
            + "redis.call('expire', KEYS[1], tonumber(ARGV[2])); "
            + "end; "
            + "return 1; "
            + "end";

    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
    Long result =
        redisTemplate.execute(
            redisScript,
            Collections.singletonList(key),
            limit,
            window);

    return result != null && result == 1;
  }
}
