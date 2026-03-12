package com.yuliyuli.consumer;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.entity.VideoCollection;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.VideoMapper;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VideoCollectConsumer {

  private final String LOCK_KEY_PREFIX = "video:collect:lock:";
  private final String COUNTER_KEY_PREFIX = "video:collect:";
  private final String USER_KEY_PREFIX = "user:collect:";
  private final int MAX_RETRY_COUNT = 3;
  private final int LOCK_WAIT = 3; // 3秒
  private final int LOCK_RELEASE = 10; // 10秒
  private final String RETRY_HEADER = "video-collect-retry-count";

  @Resource private RedissonClient redissonClient;

  @Resource private VideoMapper videoMapper;

  @SuppressWarnings("null")
  @RabbitListener(queues = RabbitMqConfig.COLLECT_QUEUE_NAME)
  public void videoCollect(VideoCollection videoCollection, Channel channel, Message mqMessage)
      throws Exception {
    Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();

    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    Integer retryCount = (Integer) headers.getOrDefault(RETRY_HEADER, 0);
    // 检查视频收藏消息参数是否完整
    if (videoCollection == null || videoCollection.getUserId() == null || videoCollection.getVideoId() == null) {
      log.error("视频收藏消息参数错误,用户ID:{} 视频ID:{}", videoCollection.getUserId(), videoCollection.getVideoId());
      channel.basicReject(deliveryTag, false);
      return;
    }
    String videoId = videoCollection.getVideoId();
    String userId = videoCollection.getUserId().toString();
    String LOCK_KEY = LOCK_KEY_PREFIX + videoId + ":" + userId;
    RLock lock = redissonClient.getLock(LOCK_KEY);
    try{
      boolean isLocked = lock.tryLock(LOCK_WAIT, LOCK_RELEASE, TimeUnit.SECONDS);
      if (!isLocked) {
        handleRetry(deliveryTag, channel, retryCount, headers);
        log.info("视频收藏消息处理失败,用户ID:{} 视频ID:{} 锁未获取,已重新放入队列",
          videoCollection.getUserId(),
          videoCollection.getVideoId());
        return;
      }
        String counterKey = COUNTER_KEY_PREFIX + videoId;
        String userKey = USER_KEY_PREFIX + userId;
        RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
        RSet<String> userSet = redissonClient.getSet(userKey);
        Long finallyCount = null;
        //判断用户是否已收藏视频
        if (userSet.contains(userId)) {
          //用户已收藏视频，取消收藏
          log.info("用户{}取消收藏视频{}，最新收藏数：{}",
            userId,
            videoId,
            finallyCount);
          userSet.remove(userId);
          finallyCount = counter.decrementAndGet();
          videoMapper.updateVideoCollectCount(
            finallyCount.intValue(), videoId);
        } else {
          //用户未收藏视频，收藏视频
          log.info("用户{}收藏视频{}，最新收藏数：{}",
            userId,
            videoId,
            counter.incrementAndGet());
          userSet.add(userId);
          finallyCount = counter.incrementAndGet();
          videoMapper.updateVideoCollectCount(finallyCount.intValue(), videoId);
        }
      // 收藏完成后，手动确认消息
      channel.basicAck(deliveryTag, false);
      log.info("用户{}收藏视频{}成功", userId, videoId);
    } catch (Exception e) {
      log.error("视频收藏消费者异常，重试次数:{}", retryCount, e);
      handleRetry(deliveryTag, channel, retryCount, headers);
    }finally {
      if (lock != null && lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  @RabbitListener(queues = RabbitMqConfig.COLLECT_DEAD_QUEUE_NAME)
  public void videoCollectDeadConsumer(
      VideoCollection videoCollection, Channel channel, Message mqMessage) {
    log.info("收藏死信消费者,用户ID:{} 视频ID:{}", videoCollection.getUserId(), videoCollection.getVideoId());
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    try {
      channel.basicAck(diliverTag, false);
    } catch (Exception e) {
      log.error("死信队列丢弃收藏失败,用户ID:{} 视频ID:{}", videoCollection.getUserId(), videoCollection.getVideoId(), e);
      throw new GlobalExceptionHandler.BusinessException("死信队列丢弃收藏失败");
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
