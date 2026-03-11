package com.yuliyuli.consumer;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.exception.GlobalExceptionHandler;
import jakarta.annotation.Resource;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HotVideoPlayConsumer {

  @Resource private RedissonClient redissonClient;

  @RabbitListener(queues = RabbitMqConfig.HOT_PLAY_QUEUE_NAME)
  public void videoPlay(String videoUrl, Channel channel, Message mqMessage) {

    Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    RLock lock = null;

    // 从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    Integer retryCount = (Integer) headers.getOrDefault("x-retry-count", 0);

    if (videoUrl == null) {
      log.error("视频URL为空");
      basicNack(deliveryTag, channel, retryCount, headers);
    }

    final String DELAY_KEY = "hot:video:play:delay";
    final String LOCK_KEY = "hot:video:play:lock:" + videoUrl;
    final int DELAY_TIME = 1000 * 60; // 1分钟

    lock = redissonClient.getLock(LOCK_KEY);
    boolean isLock = lock.tryLock();
    if (!isLock) {
      log.error("视频播放锁获取失败");
      throw new GlobalExceptionHandler.BusinessException("视频播放锁获取失败");
    }
    try {
      redissonClient
          .getScoredSortedSet(DELAY_KEY)
          .add(System.currentTimeMillis() + DELAY_TIME, videoUrl);
      // 播放完成后，手动确认消息
      basicAck(deliveryTag, channel);
    } catch (Exception e) {
      log.error("视频播放锁获取失败", e);
      throw new GlobalExceptionHandler.BusinessException("视频播放锁获取失败");
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  @RabbitListener(queues = RabbitMqConfig.HOT_PLAY_DEAD_QUEUE_NAME)
  public void videoPlayDeadConsumer(String videoUrl, Channel channel, Message mqMessage) {
    log.info("播放死信消费者,视频URL:{}", videoUrl);
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    try {
      log.error("播放死信队列消费者,视频URL:{}", videoUrl);
    } catch (Exception e) {
      try {
        channel.basicNack(diliverTag, false, true);
      } catch (Exception e2) {
        log.error("确认播放失败", e2);
        throw new GlobalExceptionHandler.BusinessException("确认播放失败");
      }
    }
  }

  public void basicAck(Long deliveryTag, Channel channel) {
    try {
      channel.basicAck(deliveryTag, false);
    } catch (Exception e) {
      log.error("ACK热门播放失败", e);
      throw new GlobalExceptionHandler.BusinessException("ACK热门播放失败");
    }
  }

  public void basicNack(
      Long deliveryTag, Channel channel, int retryCount, Map<String, Object> headers) {
    try {
      if (retryCount < 3) {
        headers.put("hot-play-retry-count", retryCount + 1);
        channel.basicNack(deliveryTag, false, true);
      }
      if (retryCount >= 3) {
        channel.basicNack(deliveryTag, false, false);
      }
    } catch (Exception e) {
      log.error("NACK热门播放失败", e);
      throw new GlobalExceptionHandler.BusinessException("NACK热门播放失败");
    }
  }
}
