package com.yuliyuli.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 线程池配置 */
@Configuration
public class ThreadPoolConfig {
  
  /** 核心线程数 */
  private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
  /** 最大线程数 */
  private static final int MAXIMUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
  /** 线程空闲时间 */
  private static final long KEEP_ALIVE_TIME = 60L;
  /** 线程空闲时间单位 */
  private static final TimeUnit UNIT = TimeUnit.SECONDS;
  /** 任务队列容量 */
  private static final int QUEUE_CAPACITY = 1000;
  
  @Bean(destroyMethod = "shutdown")
  public ExecutorService threadPoolExecutor() {
    return new ThreadPoolExecutor(
        CORE_POOL_SIZE,
        MAXIMUM_POOL_SIZE,
        KEEP_ALIVE_TIME,
        UNIT,
        new ArrayBlockingQueue<>(QUEUE_CAPACITY),
        new ThreadFactoryBuilder().setNameFormat("thread-pool-%d").build(),
        new ThreadPoolExecutor.CallerRunsPolicy());
  }
}
