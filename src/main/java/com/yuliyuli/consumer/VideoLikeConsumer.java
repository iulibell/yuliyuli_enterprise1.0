package com.yuliyuli.consumer;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.entity.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.VideoMapper;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VideoLikeConsumer {

  private static final String RETRY_HEADER = "video-like-retry-count";
  private final String DELAY_KEY = "video:like:delay";
  private final Long DELAY_TIME = System.currentTimeMillis() + 5000;
  private final String LOCK_KEY_PREFIX = "video:like:lock:";
  private final String USER_KEY_PREFIX = "user:like:";
  private final int LOCK_WAIT = 3; // 3秒
  private final int LOCK_RELEASE = 10; // 10秒
  private final int MAX_RETRY_COUNT = 3; // 最大重试次数

  @Resource private RedissonClient redissonClient;

  @Resource private VideoMapper videoMapper;

  @RabbitListener(queues = RabbitMqConfig.LIKE_QUEUE_NAME)
  public void videoLike(VideoLike videoLike, Channel channel, Message mqMessage) throws Exception {
    long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    Integer retryCount = (Integer) headers.getOrDefault(RETRY_HEADER, 0);
    //参数校验
    if (videoLike == null || videoLike.getVideoId() == null || videoLike.getUserId() == null) {
      log.error("点赞失败");
      channel.basicReject(deliveryTag, false);
      return;
    }
    // 构建分布式锁Key：视频ID + 用户ID
    String videoId = videoLike.getVideoId();
    String userId = videoLike.getUserId().toString();
    String lockKey = LOCK_KEY_PREFIX + videoId + ":" + userId;
    RLock lock = redissonClient.getLock(lockKey);
    try{
      boolean isLocked = lock.tryLock(LOCK_WAIT, LOCK_RELEASE, TimeUnit.SECONDS);
      if (!isLocked) {
        log.info("用户{}点赞视频{}失败，获取分布式锁失败,已重新放入队列", userId, videoId);
        handleRetry(deliveryTag, channel, retryCount, headers);
        return;
      }
      String userKey = USER_KEY_PREFIX + videoLike.getUserId();
      RSet<String> userSet = redissonClient.getSet(userKey);
      userSet.add(videoLike.getVideoId());
      redissonClient.getScoredSortedSet(DELAY_KEY).add(DELAY_TIME, videoLike);
      // 6. 手动ACK：确认消息消费成功（关键：防止重复消费）
      channel.basicAck(deliveryTag, false);
      log.info("用户{}点赞视频{}成功", userId, videoId);
    } catch (Exception e) {
      log.error("点赞消费异常,重试次数:{}", retryCount, e);
      handleRetry(deliveryTag, channel, retryCount, headers);
    } finally {
      if (lock != null && lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
  }
  }

  @RabbitListener(queues = RabbitMqConfig.LIKE_DEAD_QUEUE_NAME)
  public void videoLikeDeadConsumer(VideoLike videoLike, Channel channel, Message mqMessage) {
    log.info("点赞视频死信消费者,视频ID:{}", videoLike.getVideoId());
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    try {
      channel.basicAck(diliverTag, false);
    } catch (Exception e) {
      log.error("死信队列点赞视频失败,视频ID:{}", videoLike.getVideoId(), e);
      throw new GlobalExceptionHandler.BusinessException("死信队列点赞视频失败");
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
        log.error("重试点赞消息失败,重试次数:{}", retryCount + 1, e);
      }
    } else {
      try {
        channel.basicReject(deliveryTag, false);
      } catch (Exception e) {
        log.error("点赞消息重试次数超过最大重试次数,已丢弃,重试次数:{}", retryCount, e);
      }
    }
  }
}
