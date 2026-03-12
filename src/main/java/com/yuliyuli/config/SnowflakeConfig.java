package com.yuliyuli.config;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {

  /**
   * 工作节点ID
   */
  @Value("${snowflake.worker-id:1}")
  private Long workId;
  /**
   * 数据中心ID
   */ 
  @Value("${snowflake.datacenter-id:1}")
    private Long datacenterId;
    
  @Bean
  public SnowflakeIdGenerator snowflakeIdGenerator() {
    return new SnowflakeIdGenerator(workId, datacenterId);
  }
}
