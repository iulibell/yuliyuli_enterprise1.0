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
public class HotVideoPlayConsumer {

  private final String RETRY_HEADER = "hot-play-retry-count";
  private final int MAX_RETRY_COUNT = 3;
  private final String LOCK_KEY_PREFIX = "hot:video:play:lock:";
  private final int LOCK_WAIT = 3; // 3秒
  private final int LOCK_RELEASE = 10; // 10秒
  private final String DELAY_KEY = "hot:video:play:delay";
  private final Long DELAY_TIME = 1000 * 60L; // 1分钟

  @Resource private RedissonClient redissonClient;

  @RabbitListener(queues = RabbitMqConfig.HOT_PLAY_QUEUE_NAME)
  public void videoPlay(String videoUrl, Channel channel, Message mqMessage) 
    throws Exception {

    Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    Integer retryCount = (Integer) headers.getOrDefault(RETRY_HEADER, 0);
    //参数校验
    if (videoUrl == null) {
      log.error("视频URL为空");
      channel.basicReject(deliveryTag, false);
      return;
    }
      final String LOCK_KEY = LOCK_KEY_PREFIX + videoUrl;
      RLock lock = redissonClient.getLock(LOCK_KEY);
    try{
      //获取视频播放锁
      boolean isLock = lock.tryLock(LOCK_WAIT, LOCK_RELEASE, TimeUnit.SECONDS);
      if (!isLock) {
        log.error("热门视频播放锁获取失败");
        channel.basicNack(deliveryTag, false, true);
        return;
      }
        //放入延时有序集合
        redissonClient.getScoredSortedSet(DELAY_KEY)
            .add(System.currentTimeMillis() + DELAY_TIME, videoUrl);
        // 播放完成后，手动确认消息
        channel.basicAck(deliveryTag, false);
        log.info("热门视频播放成功,视频URL:{}", videoUrl);
         
    }catch (Exception e){
      log.error("热门视频播放消费异常,retry={}", retryCount, e);
      handleRetry(deliveryTag, channel, retryCount, headers);
    }finally {
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
      channel.basicAck(diliverTag, false);
    } catch (Exception e) {
      log.error("死信队列丢弃热门播放失败,视频URL:{}", videoUrl, e);
      throw new GlobalExceptionHandler.BusinessException("死信队列丢弃热门播放失败");
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
        log.error("重试热门播放消息失败,重试次数:{}", retryCount + 1, e);
      }
    } else {
      try {
        channel.basicReject(deliveryTag, false);
      } catch (Exception e) {
        log.error("热门播放消息重试次数超过最大重试次数,已丢弃,重试次数:{}", retryCount, e);
      }
    }
  }
}
