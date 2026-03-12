package com.yuliyuli.consumer;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.entity.Comment;
import com.yuliyuli.entity.Video;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.CommentMapper;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.wrapper.VideoWrapper;
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
public class CommentConsumer {

  private final String RETRY_HEADER = "comment-retry-count";
  private static final String LOCK_KEY_PREFIX = "comment:lock:";
  private static final int MAX_RETRY_COUNT = 3;
  private static final int LOCK_WAIT = 3; // 3秒
  private static final int LOCK_RELEASE = 10; // 10秒

  @Resource private RedissonClient redissonClient;

  @Resource private CommentMapper commentMapper;

  @Resource private VideoMapper videoMapper;

  @Resource private VideoWrapper videoWrapper;

  @Resource private SnowflakeIdGenerator snowflakeIdGenerator;

  @RabbitListener(queues = RabbitMqConfig.COMMENT_QUEUE_NAME)
  public void commentConsumer(Comment comment, Channel channel, Message mqMessage)
      throws Exception {
    Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 参数校验
    if(comment == null || comment.getVideoId() == null || comment.getUserId() == null) {
      channel.basicReject(deliveryTag, false);
      log.warn("评论消息为空,在评论消费者丢弃");
      return;
    }
    //重试次数,从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    Integer retryCount = (Integer) headers.getOrDefault(RETRY_HEADER, 0);
    // 锁: 每个用户评论时,加锁,防止并发评论
    String lockKey = LOCK_KEY_PREFIX + comment.getUserId();
    RLock lock = redissonClient.getLock(lockKey);
    // 使用雪花算法生成评论id
    Long commentId = snowflakeIdGenerator.nextId();

    try {
      // 尝试加锁,如果3秒内没有加锁成功,则重新放入队列
      boolean isLock = lock.tryLock(LOCK_WAIT, LOCK_RELEASE, TimeUnit.SECONDS);
      if (!isLock) {
        channel.basicNack(deliveryTag, false, true);
        log.info("用户{}评论锁被其他线程占用,已重新放入队列", comment.getUserId());
        return;
      }
      LambdaQueryWrapper<Video> getCommentCountWrapper =
          videoWrapper.getCommentCount(comment.getVideoId().toString());
      comment.setCommentId(commentId);
      commentMapper.insert(comment);
      int commentCount =
          Integer.valueOf(videoMapper.selectById(getCommentCountWrapper).getCommentCount());
      videoMapper.updateVideoCommentCount(commentCount, comment.getVideoId().toString());
      // 评论成功后,手动确认消息
      channel.basicAck(deliveryTag, false);
      log.info("评论成功,评论ID:{}", commentId);
    } catch (Exception e) {
      try {
        channel.basicAck(deliveryTag, false);
      } catch (Exception e2) {
        log.error("评论消费异常,retry={}", retryCount, e2);
        handleRetry(deliveryTag, channel, retryCount, headers);
      }
    } finally {
      if (lock != null && lock.isHeldByCurrentThread()) {
        lock.unlock();
        log.info("用户{}评论锁已释放", comment.getUserId());
      }
    }
  }

  /**
   * 死信队列（记录+警告）
   * @param comment
   * @param channel
   * @param mqMessage
   * @throws Exception
   */
  @RabbitListener(queues = RabbitMqConfig.COMMENT_DEAD_QUEUE_NAME)
  public void commentDeadConsumer(Comment comment, Channel channel, Message mqMessage)
      throws Exception {
    Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    log.info("死信队列收到失败评论,评论ID:{}", comment.getCommentId());
    try {
      channel.basicAck(deliveryTag, false);
    } catch (Exception e) {
      log.error("死信队列丢弃失败评论失败,评论ID:{}", comment.getCommentId(), e);
      throw new GlobalExceptionHandler.BusinessException("死信队列丢弃失败评论失败");
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
        log.error("重试评论消息失败,重试次数:{}", retryCount + 1, e);
      }
    } else {
      try {
        channel.basicReject(deliveryTag, false);
      } catch (Exception e) {
        log.error("评论消息重试次数超过最大重试次数,已丢弃,重试次数:{}", retryCount, e);
      }
    }
  }
}
