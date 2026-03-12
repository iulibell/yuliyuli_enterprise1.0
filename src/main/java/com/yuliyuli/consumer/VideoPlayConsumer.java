package com.yuliyuli.consumer;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.exception.GlobalExceptionHandler;
import jakarta.annotation.Resource;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VideoPlayConsumer {

  private final int MAX_RETRY_COUNT = 3;
  private final String RETRY_HEADER = "play-retry-count";
  private final String DELAY_KEY = "video:play:delay";
  private final int DELAY_TIME = 1000 * 5; // 5秒

  @Resource private RedissonClient redissonClient;

  @RabbitListener(queues = RabbitMqConfig.PLAY_QUEUE_NAME)
  public void videoPlay(String videoUrl, Channel channel, Message mqMessage) throws Exception {

    Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    Integer retryCount = (Integer) headers.getOrDefault(RETRY_HEADER, 0);

    if (videoUrl == null) {
      log.error("视频URL为空");
      channel.basicReject(deliveryTag, false);
      return;
    }

    try{
      redissonClient.getScoredSortedSet(DELAY_KEY)
          .add(System.currentTimeMillis() + DELAY_TIME, videoUrl);
      // 播放完成后，手动确认消息
      channel.basicAck(deliveryTag, false);
      log.info("视频播放成功,视频URL:{}", videoUrl);
    } catch (Exception e) {
      log.error("视频播放消费异常,重试次数:{}", retryCount, e);
      handleRetry(deliveryTag, channel, retryCount, headers);
    }
  }

  @RabbitListener(queues = RabbitMqConfig.PLAY_DEAD_QUEUE_NAME)
  public void videoPlayDeadConsumer(String videoUrl, Channel channel, Message mqMessage) {
    log.info("播放视频死信消费者,视频URL:{}", videoUrl);
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    try {
      channel.basicAck(diliverTag, false);
    } catch (Exception e) {
      log.error("死信队列播放视频失败,视频URL:{}", videoUrl, e);
      throw new GlobalExceptionHandler.BusinessException("死信队列播放视频失败");
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
