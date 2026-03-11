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
public class VideoDeleteConsumer {

  @Resource private RedissonClient redissonClient;

  @RabbitListener(queues = RabbitMqConfig.DELETE_QUEUE_NAME)
  public void videoDelete(Map<String, Object> map, Channel channel, Message mqMessage) {

    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    RLock lock = null;

    if (map.get("videoId") == null || map.get("userId") == null) {
      log.error("视频删除消息videoId为空");
      throw new GlobalExceptionHandler.BusinessException("删除视频失败,视频id为空");
    }

    final String VIDEO_DELETE_LOCK_PREFIX = "video:delete:lock:";
    final int DELAY_TIME = 1000 * 5;
    final String DELAY_KEY = "video:delete:delay:";

    lock = redissonClient.getLock(VIDEO_DELETE_LOCK_PREFIX + map.get("videoUrl").toString());
    boolean isLock = lock.tryLock();
    if (isLock) {
      log.error("视频删除锁已被占用");
      throw new GlobalExceptionHandler.BusinessException("视频删除锁已被占用");
    }
    try {
      redissonClient
          .getScoredSortedSet(DELAY_KEY)
          .add(System.currentTimeMillis() + DELAY_TIME, map);
      // 播放完成后，手动确认消息
      basicAck(diliverTag, channel);
    } catch (Exception e) {
      log.error("视频删除失败", e);
      throw new GlobalExceptionHandler.BusinessException("视频删除失败");
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  @RabbitListener(queues = RabbitMqConfig.DELETE_QUEUE_NAME)
  public void videoDeleteDelay(Map<String, Object> map, Channel channel, Message mqMessage) {
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    Integer retryCount = (Integer) headers.getOrDefault("x-retry-count", 0);
    try {
      log.error("视频删除延时队列消费者,视频ID:{}", map.get("videoId"));
    } catch (Exception e) {
      try {
        basicNack(diliverTag, channel, retryCount, headers);
      } catch (Exception e2) {
        log.error("确认视频删除失败", e2);
        throw new GlobalExceptionHandler.BusinessException("确认视频删除失败");
      }
    }
  }

  public void basicAck(Long deliveryTag, Channel channel) {
    try {
      channel.basicAck(deliveryTag, false);
    } catch (Exception e) {
      log.error("ACK视频删除失败", e);
      throw new GlobalExceptionHandler.BusinessException("ACK视频删除失败");
    }
  }

  public void basicNack(
      Long deliveryTag, Channel channel, int retryCount, Map<String, Object> headers) {
    try {
      if (retryCount < 3) {
        headers.put("play-retry-count", retryCount + 1);
        channel.basicNack(deliveryTag, false, true);
      }
      if (retryCount >= 3) {
        channel.basicNack(deliveryTag, false, false);
      }
    } catch (Exception e) {
      log.error("NACK视频删除失败", e);
      throw new GlobalExceptionHandler.BusinessException("NACK视频删除失败");
    }
  }
}
