package com.yuliyuli.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

  /**
   * Redis主机地址
   */
  @Value("${spring.data.redis.host:localhost}")
  private String host;

  /**
   * Redis端口号
   */
  @Value("${spring.data.redis.port:6379}")
  private int port;

  /**
   * Redis数据库索引
   */
  @Value("${spring.data.redis.database:0}")
  private int database;

  /**
   * Redisson客户端
   */
  @Bean(destroyMethod = "shutdown")
  public RedissonClient redissonClient() {
    Config config = new Config();
    String redisAddress = "redis://" + host + ":" + port;

    config.useSingleServer()
      .setAddress(redisAddress)
      .setDatabase(database);
    return Redisson.create(config);
  }
}
