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
import com.yuliyuli.dto.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VideoLikeConsumer {

    @Autowired
    private RedissonClient redissonClient;

    @RabbitListener(queues = RabbitMqConfig.LIKE_QUEUE_NAME)
    public void videoLike(VideoLike videoLike, Channel channel, Message mqMessage){
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        RLock lock = null;
        
        final String LOCK_KEY_PREFIX = "video:like:lock:";
        final String COUNTER_KEY_PREFIX = "video:like:";
        final String USER_KEY_PREFIX = "user:like:";

    try{
            if(videoLike == null || videoLike.getVideoId() == null || videoLike.getUserId() == null){
            log.error("点赞失败");
            throw new GlobalExceptionHandler.BusinessException("视频id或用户id不能为空");
        }

        // 构建分布式锁Key：视频ID + 用户ID
        String lockKey = LOCK_KEY_PREFIX + videoLike.getVideoId() + ":" + videoLike.getUserId();
        lock = redissonClient.getLock(lockKey);
        boolean isLocked = lock.tryLock();
        if(!isLocked){
            log.error("用户{}点赞视频{}失败，获取分布式锁失败", videoLike.getUserId(), videoLike.getVideoId());
            throw new GlobalExceptionHandler.BusinessException("点赞失败");
        }
        
        final String COUNTER_KEY = COUNTER_KEY_PREFIX + videoLike.getVideoId();
        final String USER_KEY = USER_KEY_PREFIX + videoLike.getUserId();

        // 获取点赞计数器和用户点赞集合
        RAtomicLong counter = redissonClient.getAtomicLong(COUNTER_KEY);
        RSet<Long> userSet = redissonClient.getSet(USER_KEY);
        Long finallyCount = null;
        
        if(userSet.contains(videoLike.getUserId())){
            log.info("用户{}已点赞视频{},取消点赞", videoLike.getUserId(), videoLike.getVideoId());
            // 取消点赞：移除用户ID + 计数-1
            userSet.remove(videoLike.getUserId());
            finallyCount = counter.decrementAndGet();
            log.info("用户{}取消点赞视频{}，最新点赞数：{}", videoLike.getUserId(), videoLike.getVideoId(), finallyCount);
        }else{
            // 点赞：添加用户ID + 计数+1
            userSet.add(videoLike.getUserId());
            finallyCount = counter.incrementAndGet();
            log.info("用户{}点赞视频{}，最新点赞数：{}", videoLike.getUserId(), videoLike.getVideoId(), finallyCount);
        }

        // 6. 手动ACK：确认消息消费成功（关键：防止重复消费）
        channel.basicAck(deliveryTag, false);
    } catch (Exception e) {
        Long userId = videoLike.getUserId() != null ? videoLike.getUserId() : 0L;
        String videoId = videoLike.getVideoId() != null ? videoLike.getVideoId().toString() : "";
        log.error("用户{}点赞视频{}失败", userId, videoId, e);
        throw new GlobalExceptionHandler.BusinessException("点赞失败");
    }finally{
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("释放用户{}点赞视频{}的分布式锁", 
                videoLike != null ? videoLike.getUserId() : null,
                videoLike != null ? videoLike.getVideoId() : null);
            }
        }
    }

    @RabbitListener(queues = RabbitMqConfig.LIKE_DEAD_QUEUE_NAME)
    public void videoLikeDeadConsumer(VideoLike videoLike, Channel channel, Message mqMessage){
        log.info("点赞死信消费者");
        Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        try{
            String userId = videoLike.getUserId() != null ? videoLike.getUserId().toString() : "";
            String videoId = videoLike.getVideoId() != null ? videoLike.getVideoId().toString() : "";
            log.info("死信队列用户{}点赞视频{}", userId, videoId);
            // 手动ACK：确认消息消费成功（关键：防止重复消费）
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try{
                channel.basicNack(deliveryTag, false, true);
            }catch(Exception e2){
                log.error("确认点赞失败",e2);
                throw new GlobalExceptionHandler.BusinessException("确认点赞失败");
            }
        }
    }
}
