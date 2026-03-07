package com.yuliyuli.consumer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import com.yuliyuli.entity.VideoCollection;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.VideoMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VideoCollectConsumer {

    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired
    private VideoMapper videoMapper;

    @SuppressWarnings("null")
    @RabbitListener(queues = RabbitMqConfig.COLLECT_QUEUE_NAME)
    public void videoCollect(VideoCollection videoCollection, Channel channel, Message mqMessage) throws Exception{
        Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        RLock lock = null;

        final String LOCK_KEY_PREFIX = "video:collect:lock:";
        final String COUNTER_KEY_PREFIX = "video:collect:";
        final String USER_KEY_PREFIX = "user:collect:";

        if(videoCollection == null || videoCollection.getUserId() == null || videoCollection.getVideoId() == null){
            log.error("视频收藏消息参数错误,用户ID:{} 视频ID:{}", videoCollection.getUserId(), videoCollection.getVideoId());
            throw new GlobalExceptionHandler.BusinessException("视频收藏消息参数错误");
        }

        Map<String,Object> headers = mqMessage.getMessageProperties().getHeaders();
        Integer retryCount = (Integer) headers.getOrDefault("x-retry-count",0);
        String LOCK_KEY = LOCK_KEY_PREFIX + videoCollection.getVideoId() + ":" + videoCollection.getUserId();
        lock = redissonClient.getLock(LOCK_KEY);
        boolean isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
        if(!isLocked){
            channel.basicNack(deliveryTag, false, true);
            log.info("视频收藏消息处理失败,用户ID:{} 视频ID:{} 锁未获取,已重新放入队列", videoCollection.getUserId(), videoCollection.getVideoId());
            return;
        }
    try{
        String COUNTER_KEY = COUNTER_KEY_PREFIX + videoCollection.getVideoId();
        String USER_KEY = USER_KEY_PREFIX + videoCollection.getUserId();
        RAtomicLong counter = redissonClient.getAtomicLong(COUNTER_KEY);
        RSet<Long> userSet = redissonClient.getSet(USER_KEY);
        Long finallyCount = null;

        if(userSet.contains(videoCollection.getUserId())){
            log.info("用户{}取消收藏视频{}，最新收藏数：{}", videoCollection.getUserId(), videoCollection.getVideoId(), finallyCount);
            userSet.remove(videoCollection.getUserId());
            finallyCount = counter.decrementAndGet();
            videoMapper.updateVideoCollectCount(finallyCount.intValue(), videoCollection.getVideoId().toString());
        }else{
            log.info("用户{}收藏视频{}，最新收藏数：{}", videoCollection.getUserId(), videoCollection.getVideoId(), counter.incrementAndGet());
            userSet.add(videoCollection.getUserId());
            finallyCount = counter.incrementAndGet();
            videoMapper.updateVideoCollectCount(finallyCount.intValue(), videoCollection.getVideoId().toString());
        }
        basicAck(deliveryTag, channel);
    }catch(Exception e){
        try{
            basicNack(deliveryTag, channel, retryCount, headers);
        }catch(Exception e2){
            Long userId = videoCollection.getUserId() != null ? videoCollection.getUserId() : 0L;
            String videoId = videoCollection.getVideoId() != null ? videoCollection.getVideoId().toString() : "";
            log.error("用户{}收藏视频{}失败", userId, videoId, e);
            throw new GlobalExceptionHandler.BusinessException("收藏失败");
        }
    }finally{
        if(lock != null && lock.isHeldByCurrentThread()){
            lock.unlock();
            log.info("用户{}视频{}锁已释放", 
            videoCollection.getUserId() == null ? null : videoCollection.getUserId(), 
            videoCollection.getVideoId() == null ? null : videoCollection.getVideoId());
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

    public void basicAck(Long deliveryTag, Channel channel){
        try{
            channel.basicAck(deliveryTag, false);
        }catch(Exception e){
            log.error("ACK收藏失败", e);
            throw new GlobalExceptionHandler.BusinessException("ACK收藏失败");
        }
    }

    public void basicNack(Long deliveryTag, Channel channel, int retryCount, Map<String,Object> headers){
        try{
            if(retryCount < 3){
                headers.put("x-retry-count", retryCount + 1);
                channel.basicNack(deliveryTag, false, true);
            }
            if(retryCount >= 3){
                channel.basicNack(deliveryTag, false, false);
            }
        }catch(Exception e){
            log.error("NACK收藏失败", e);
            throw new GlobalExceptionHandler.BusinessException("NACK收藏失败");
        }
    }
}
