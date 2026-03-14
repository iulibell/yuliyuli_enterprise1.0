package com.yuliyuli.consumer;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.document.VideoDocument;
import com.yuliyuli.entity.VideoDelivery;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.repository.VideoRepository;
import com.yuliyuli.util.TransferUtil;
import jakarta.annotation.Resource;
import java.util.Date;
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
public class VideoDeliverConsumer {

  private static final String RETRY_HEADER = "video-delivery-retry-count";
  private static final int MAX_RETRY_COUNT = 1;
  private static final String LOCK_KEY_PREFIX = "delivery:video:lock:";
  private static final int LOCK_WAIT = 3; // 3秒
  private static final int LOCK_RELEASE = 10; // 10秒

  @Resource private TransferUtil transferUtil;

  @Resource private VideoMapper videoMapper;

  @Resource private RedissonClient redissonClient;

  @Resource private VideoRepository videoRepository;

  /**
   * 视频队列消费者
   *
   * @param videoDelivery 视频消息
   */
  @RabbitListener(queues = RabbitMqConfig.VIDEO_QUEUE_NAME)
  public void videoDeliveryConsumer(VideoDelivery videoDelivery, Channel channel, Message mqMessage)
      throws Exception {
    Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    Integer retryCount = (Integer) headers.getOrDefault(RETRY_HEADER, 0);

    if (videoDelivery == null || videoDelivery.getVideo() == null) {
      log.error("视频分发消息为空");
      channel.basicReject(deliveryTag, false);
      return;
    }

    String userId = videoDelivery.getVideo().getUserId().toString();
    Long videoId = Long.parseLong(videoDelivery.getVideo().getUrl());

    // 视频锁，确保每个用户每个视频只插入一次
    String lockKey = LOCK_KEY_PREFIX + videoId + ":" + userId;
    // 尝试获取锁,如果锁被其他线程占用,则重新放入队列
    RLock lock = redissonClient.getLock(lockKey);
    try{
      boolean isLock = lock.tryLock(LOCK_WAIT, LOCK_RELEASE, TimeUnit.SECONDS);
      if (!isLock) {
        log.info("用户{}视频{}锁被其他线程占用,已重新放入队列", userId, videoId);
        handleRetry(deliveryTag, channel, retryCount, headers);
        return;
      }
      log.info("开始保存到ES索引");
      saveVideoToSearchIndex(videoDelivery, videoId);
      log.info("ES索引保存成功");
      log.info("开始插入数据库, videoId={}, userId={}", videoId, userId);
      int insertResult = videoMapper.insert(videoDelivery.getVideo());   
      if (insertResult != 1) {
        channel.basicNack(deliveryTag, false, false);
        log.error("用户{}视频{}插入失败", userId, videoId);
        throw new GlobalExceptionHandler.BusinessException("视频插入失败");
      }         
      log.info("数据库插入结果:{}", insertResult);
      // 视频分发成功后,手动确认消息
      channel.basicAck(deliveryTag, false);
      log.info("用户{}视频{}分发成功", userId, videoId);
    } catch (Exception e) {
      log.error("用户{}视频{}分发失败,异常:{}", userId, videoId, e.getMessage(), e);
      handleRetry(deliveryTag, channel, retryCount, headers);
    } finally {
      if (lock.isHeldByCurrentThread() && lock != null) {
        lock.unlock();
      }
    }
  }

  /**
   * 视频分发死信队列消费者
   * @param videoDelivery 视频分发消息
   */
  @RabbitListener(queues = RabbitMqConfig.VIDEO_DEAD_QUEUE_NAME)
  public void videoDeadConsumer(VideoDelivery videoDelivery, Channel channel, Message mqMessage) {
    log.info("分发视频死信消费者,视频URL:{}", videoDelivery.getVideo().getUrl());
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    try {
      channel.basicAck(diliverTag, false);
    } catch (Exception e) {
      log.error("死信队列丢弃分发视频失败,视频URL:{}", videoDelivery.getVideo().getUrl(), e);
      throw new GlobalExceptionHandler.BusinessException("死信队列丢弃分发视频失败");
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

  // 保存视频到搜索索引
  public void saveVideoToSearchIndex(VideoDelivery videoDelivery, Long videoId) {
    VideoDocument videoDocument = new VideoDocument();
    videoDocument.setId(videoId.toString());
    videoDocument.setTitle(videoDelivery.getVideo().getTitle());
    videoDocument.setTypeId(videoDelivery.getVideo().getTypeId());
    videoDocument.setUrl(videoDelivery.getVideo().getUrl());
    videoDocument.setPlayCount(0L);
    videoDocument.setCreateTime(new Date());
    videoDocument.setUserId(videoDelivery.getVideo().getUserId());
    videoDocument.setAuthorName(videoDelivery.getVideo().getAuthorName());
    videoRepository.save(videoDocument);
  }
}
