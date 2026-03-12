package com.yuliyuli.consumer;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.exception.GlobalExceptionHandler;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VideoDeleteConsumer {

  private final String VIDEO_DELETE_LOCK_PREFIX = "video:delete:lock:";
  private final int DELAY_TIME = 1000 * 5;
  private final String DELAY_KEY = "video:delete:delay:";
  private final String RETRY_HEADER = "video-collect-retry-count";
  private final int MAX_RETRY_COUNT = 3;
  private final int LOCK_WAIT = 3; // 3秒
  private final int LOCK_RELEASE = 10; // 10秒
    
  @Resource private RedissonClient redissonClient;

  @RabbitListener(queues = RabbitMqConfig.DELETE_QUEUE_NAME)
  public void videoDelete(Map<String, Object> map, Channel channel, Message mqMessage) 
      throws Exception {
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    Integer retryCount = (Integer) headers.getOrDefault(RETRY_HEADER, 0);

    if (map.get("videoId") == null || map.get("userId") == null) {
      log.error("视频删除消息videoId或userId为空");
      channel.basicReject(diliverTag, false);
      return;
    }
    String lockKey = VIDEO_DELETE_LOCK_PREFIX + map.get("videoUrl").toString();
    // 获取删除视频锁
    RLock lock = redissonClient.getLock(lockKey);
    try{
      boolean isLock = lock.tryLock(LOCK_WAIT, LOCK_RELEASE, TimeUnit.SECONDS);
      if (!isLock) {
        log.error("视频删除锁已被占用");
        handleRetry(diliverTag, channel, retryCount, headers);
        return;
      }
        redissonClient
            .getScoredSortedSet(DELAY_KEY)
            .add(System.currentTimeMillis() + DELAY_TIME, map);
        // 播放完成后，手动确认消息
        channel.basicAck(diliverTag, false);
        log.info("视频删除消息确认成功,视频ID:{}", map.get("videoId"));
    }catch (Exception e){
      log.error("视频删除消费者异常，重试次数:{}", retryCount, e);
      handleRetry(diliverTag, channel, retryCount, headers);
    } finally {
        if (lock.isHeldByCurrentThread()) {
          lock.unlock();
        }
      }
  }

  @RabbitListener(queues = RabbitMqConfig.DELETE_QUEUE_NAME)
  public void videoDeleteDeadConsumer(Map<String, Object> map, Channel channel, Message mqMessage) {
    log.info("删除视频死信消费者,视频URL:{}", map.get("videoUrl"));
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    try {
      channel.basicAck(diliverTag, false);
    } catch (Exception e) {
      log.error("死信队列丢弃删除视频失败,视频URL:{}", map.get("videoUrl"), e);
      throw new GlobalExceptionHandler.BusinessException("死信队列丢弃删除视频失败");
    }
  }

  /**
   * 处理重试
   * @param deliveryTag 消息标签
   * @param channel 通道
   * @param retryCount 重试次数
   * @param headers 消息头
   */
  private void handleRetry(Long deliveryTag, Channel channel, Integer retryCount, Map<String, Object> headers) {
    if (retryCount < MAX_RETRY_COUNT) {
      headers.put(RETRY_HEADER, retryCount + 1);
      try {
        channel.basicNack(deliveryTag, false, true);
      } catch (Exception e) {
        log.error("重试收藏消息失败,重试次数:{}", retryCount + 1, e);
      }
    } else {
      try {
        channel.basicReject(deliveryTag, false);
      } catch (Exception e) {
        log.error("收藏消息重试次数超过最大重试次数,已丢弃,重试次数:{}", retryCount, e);
      }
    }
  }
}
