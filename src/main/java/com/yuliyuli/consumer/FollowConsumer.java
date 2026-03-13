package com.yuliyuli.consumer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.Channel;

import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.yuliyuli.config.RabbitMqConfig;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FollowConsumer {
    
    private static final int MAX_RETRY_COUNT = 3;
    private static final String RETRY_HEADER = "follow-retry-count";
    private static final String LOCK_KEY_PROFIX = "follow_lock:";
    private static final String DELAY_KEY = "follow:delay";
    private static final String FOLLOW_KEY_PROFIX = "follow:";
    private static final int LOCK_WAIT = 3;
    private static final int LOCK_LEASE = 3;
    private static final long DELAY_TIME = 1000 * 5;

    @Resource
    RedissonClient redissonClient;

    @RabbitListener(queues = RabbitMqConfig.FOLLOW_QUEUE_NAME)
    public void followConsumer(Map<String, Object> map, Channel channel, Message mqMessage) 
        throws Exception{
        Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        // 校验参数
        if(map.isEmpty() || !map.containsKey("userId") || !map.containsKey("targetUserId")){
            log.error("关注消费参数校验失败,userId:{}", map.get("fanUserId"));
            // 拒绝消息,不重新入队
            channel.basicReject(deliveryTag, false);
            return;
        }
        //重试次数,从消息头中获取重试次数,如果没有则默认0
        Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
        Integer retryCount = (Integer) headers.getOrDefault(RETRY_HEADER, 0); 
        //进行加锁操作
        RLock lock = redissonClient.getLock(LOCK_KEY_PROFIX + map.get("userId") + ":" + map.get("targetUserId"));
        try {
          boolean isLock = lock.tryLock(LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS);
          if (!isLock) {
            log.error("关注操作加锁失败,userId:{}", map.get("userId"));
            handleRetry(deliveryTag, channel, retryCount, headers);
            return;
          }
          //将主动关注的用户ID添加到关注集合中当主键，值为被关注的用户ID
          String followKey = FOLLOW_KEY_PROFIX + map.get("fanUserId");
          RSet<String> followSet = redissonClient.getSet(followKey);
          followSet.add(map.get("followUserId").toString());
          redissonClient.getScoredSortedSet(DELAY_KEY).add(DELAY_TIME, map);
          //手动确认消息
          channel.basicAck(deliveryTag, false);
          log.info("关注操作成功,userId:{}", map.get("fanUserId"));
        } catch (InterruptedException e) {
          log.error("关注操作加锁失败,重试次数:{}", retryCount, e);
          // 拒绝消息,不重新入队
          handleRetry(deliveryTag, channel, retryCount, headers);
          return;
        }finally{
          // 释放锁
          if(lock.isHeldByCurrentThread()){
            lock.unlock();
          }
        }
    }

    @RabbitListener(queues = RabbitMqConfig.FOLLOW_DEAD_QUEUE_NAME)
    public void followDeadConsumer(Map<String, Object> map, Channel channel, Message mqMessage){
        log.info("关注操作死信队列消费,userId:{}", map.get("fanUserId"));
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
