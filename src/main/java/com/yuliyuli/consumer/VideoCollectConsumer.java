package com.yuliyuli.consumer;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.dto.VideoCollection;
import com.yuliyuli.exception.GlobalExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VideoCollectConsumer {

    @Autowired
    private RedissonClient redissonClient;

    @RabbitListener(queues = RabbitMqConfig.COLLECT_QUEUE_NAME)
    public void videoCollect(VideoCollection videoCollection, Channel channel, Message mqMessage){
        Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        RLock lock = null;

        final String LOCK_KEY_PREFIX = "video:collect:lock:";
        final String COUNTER_KEY_PREFIX = "video:collect:";
        final String USER_KEY_PREFIX = "user:collect:";

        if(videoCollection == null || videoCollection.getUserId() == null || videoCollection.getVideoId() == null){
            log.error("视频收藏消息参数错误,用户ID:{} 视频ID:{}", videoCollection.getUserId(), videoCollection.getVideoId());
            throw new GlobalExceptionHandler.BusinessException("视频收藏消息参数错误");
        }

    try{
        String LOCK_KEY = LOCK_KEY_PREFIX + videoCollection.getVideoId() + ":" + videoCollection.getUserId();
        lock = redissonClient.getLock(LOCK_KEY);
        boolean isLocked = lock.tryLock();
        if(!isLocked){
            log.error("视频收藏消息处理失败,用户ID:{} 视频ID:{} 锁未获取", videoCollection.getUserId(), videoCollection.getVideoId());
            throw new GlobalExceptionHandler.BusinessException("视频收藏消息处理失败,锁未获取");
        }

        String COUNTER_KEY = COUNTER_KEY_PREFIX + videoCollection.getVideoId();
        String USER_KEY = USER_KEY_PREFIX + videoCollection.getVideoId();

        RAtomicLong counter = redissonClient.getAtomicLong(COUNTER_KEY);
        RSet<Long> userSet = redissonClient.getSet(USER_KEY);
        Long finallyCount = null;

        if(userSet.contains(videoCollection.getUserId())){
            log.info("用户{}取消收藏视频{}，最新收藏数：{}", videoCollection.getUserId(), videoCollection.getVideoId(), finallyCount);
            userSet.remove(videoCollection.getUserId());
            finallyCount = counter.decrementAndGet();
        }else{
            log.info("用户{}收藏视频{}，最新收藏数：{}", videoCollection.getUserId(), videoCollection.getVideoId(), counter.incrementAndGet());
            userSet.add(videoCollection.getUserId());
            finallyCount = counter.incrementAndGet();
        }
        channel.basicAck(deliveryTag, false);
    }catch(Exception e){
        Long userId = videoCollection.getUserId() != null ? videoCollection.getUserId() : 0L;
        String videoId = videoCollection.getVideoId() != null ? videoCollection.getVideoId().toString() : "";
        log.error("用户{}收藏视频{}失败", userId, videoId, e);
        throw new GlobalExceptionHandler.BusinessException("收藏失败");
    }finally{
        if(lock != null && lock.isHeldByCurrentThread()){
            lock.unlock();
        }
    }
}

    @RabbitListener(queues = RabbitMqConfig.COLLECT_DEAD_QUEUE_NAME)
    public void videoCollectDeadConsumer(VideoCollection videoCollection, Channel channel, Message mqMessage){
        log.info("收藏死信消费者");
        Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
        try{
            String userId = videoCollection.getUserId() != null ? videoCollection.getUserId().toString() : "";
            String videoId = videoCollection.getVideoId() != null ? videoCollection.getVideoId().toString() : "";
            log.error("收藏死信队列消费者,userId={}, videoId={}", userId, videoId);
        }catch(Exception e){
            try{
                channel.basicNack(diliverTag, false, true);
            }catch(Exception e2){
                log.error("确认收藏失败", e2);
                throw new GlobalExceptionHandler.BusinessException("确认收藏失败");
            }
        }
    }
}
