package com.yuliyuli.consumer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.entity.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.VideoMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VideoLikeConsumer {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private VideoMapper videoMapper;

    @RabbitListener(queues = RabbitMqConfig.LIKE_QUEUE_NAME)
    public void videoLike(VideoLike videoLike, Channel channel, Message mqMessage) throws Exception{
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        RLock lock = null;
        
        final String DELAY_KEY = "video:like:delay";
        final Long DELAY_TIME = System.currentTimeMillis() + 5000;
        final String LOCK_KEY_PREFIX = "video:like:lock:";
        final String USER_KEY_PREFIX = "user:like:";

        if(videoLike == null || videoLike.getVideoId() == null || videoLike.getUserId() == null){
            log.error("点赞失败");
            throw new GlobalExceptionHandler.BusinessException("视频id或用户id不能为空");
        }

        // 从消息头中获取重试次数,如果没有则默认0
        Map<String,Object> headers = mqMessage.getMessageProperties().getHeaders();
        Integer retryCount = (Integer) headers.getOrDefault("x-retry-count",0);

        // 构建分布式锁Key：视频ID + 用户ID
        String lockKey = LOCK_KEY_PREFIX + videoLike.getVideoId() + ":" + videoLike.getUserId();
        lock = redissonClient.getLock(lockKey);
        boolean isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
        if(!isLocked){
            channel.basicNack(deliveryTag, false, true);
            log.info("用户{}点赞视频{}失败，获取分布式锁失败,已重新放入队列", videoLike.getUserId(), videoLike.getVideoId());
            return;
        }
        try{
            final String USER_KEY = USER_KEY_PREFIX + videoLike.getUserId();
            RSet<Long> userSet = redissonClient.getSet(USER_KEY);
            
            userSet.add(videoLike.getVideoId());
            redissonClient.getScoredSortedSet(DELAY_KEY)
                .add(DELAY_TIME, videoLike);

            // 6. 手动ACK：确认消息消费成功（关键：防止重复消费）
            basicNack(deliveryTag, channel, retryCount, headers);
        } catch (Exception e) {
            // 7. 手动NACK：拒绝消息重新入队（关键：防止消息丢失）
            try{
                channel.basicNack(deliveryTag, false, true);
            }catch(Exception e2){
                Long userId = videoLike.getUserId() != null ? videoLike.getUserId() : 0L;
                String videoId = videoLike.getVideoId() != null ? videoLike.getVideoId().toString() : "";
                log.error("用户{}点赞视频{}失败", userId, videoId, e);
                throw new GlobalExceptionHandler.BusinessException("点赞失败");
            }   
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

    public void basicAck(Long deliveryTag, Channel channel){
        try{
            channel.basicAck(deliveryTag, false);
        }catch(Exception e){
            log.error("ACK点赞失败", e);
            throw new GlobalExceptionHandler.BusinessException("ACK点赞失败");
        }
    }

    public void basicNack(Long deliveryTag, Channel channel, int retryCount, Map<String,Object> headers){
        try{
            if(retryCount < 3){
                headers.put("like-retry-count", retryCount + 1);
                channel.basicNack(deliveryTag, false, true);
            }
            if(retryCount >= 3){
                channel.basicNack(deliveryTag, false, false);
            }
        }catch(Exception e){
            log.error("NACK点赞失败", e);
            throw new GlobalExceptionHandler.BusinessException("NACK点赞失败");
        }
    }
}
