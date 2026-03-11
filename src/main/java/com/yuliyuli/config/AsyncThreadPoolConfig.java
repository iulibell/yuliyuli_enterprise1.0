package com.yuliyuli.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** 异步线程池配置 */
@Configuration
@EnableAsync
public class AsyncThreadPoolConfig {
  // 异步线程池配置
  @Bean(name = "asyncThreadPoolExecutor")
  public ExecutorService asyncThreadPoolExecutor() {
    return new ThreadPoolExecutor(
        Runtime.getRuntime().availableProcessors(),
        Runtime.getRuntime().availableProcessors() * 2,
        60L,
        TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(1000),
        new ThreadFactoryBuilder().setNameFormat("async-thread-pool-%d").build(),
        new ThreadPoolExecutor.CallerRunsPolicy());
  }
}
