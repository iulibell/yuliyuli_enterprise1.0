package com.yuliyuli.consumer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.entity.Video;
import com.yuliyuli.entity.VideoDelivery;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.util.TransferUtil;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VideoDeliverConsumer {

    @Resource
    private TransferUtil transferUtil;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /** 
     * 视频队列消费者
     * @param videoDelivery 视频消息
     */
    @RabbitListener(queues = RabbitMqConfig.VIDEO_QUEUE_NAME)
    public void videoConsumer(VideoDelivery videoDelivery, Channel channel, Message mqMessage) throws Exception {
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        if(videoDelivery == null){
            basicReject(channel, deliveryTag);
            return;
        }

        String userId = videoDelivery.getVideo().getUserId().toString();
        String videoId = String.valueOf(snowflakeIdGenerator.nextId());

        // 视频锁，确保每个用户每个视频只插入一次
        String lockKey = "video:lock:" + videoId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
        int retryCount = headers.containsKey("x-retry-count") ? (int) headers.get("x-retry-count") : 0;
        
        boolean lockSuccess = lock.tryLock(3, 10, TimeUnit.SECONDS);
        if(!lockSuccess){
            basicNack(deliveryTag, channel, retryCount, headers);
            log.info("用户{}视频{}锁被其他线程占用,已重新放入队列", userId, videoId);
            return;
        }
        try{
            videoMapper.insert(videoDelivery.getVideo());
            // 视频文件存储路径
            String videoPath = transferUtil.saveVideoToDirectory(videoDelivery);   
            if(videoPath == null){
                channel.basicNack(deliveryTag, false, false);
                return;
            }
            basicAck(channel, deliveryTag);
        }catch(Exception e){
            //异常，拒绝消息
            try{
                channel.basicNack(deliveryTag, false, false);
            }catch(Exception e2){
                Long userID = videoDelivery.getVideo().getUserId() != null ? videoDelivery.getVideo().getUserId() : 0L;
                String videoID = videoDelivery.getVideo().getId() != null ? videoDelivery.getVideo().getId().toString() : "";
                log.error("用户{}视频{}分发失败", userID, videoID);
                throw new GlobalExceptionHandler.BusinessException("视频分发失败");
            }
        }finally{
                if(lock.isHeldByCurrentThread() && lock!=null){
                    lock.unlock();
                    log.info("用户{}视频{}锁已释放", 
                    userId == null ? null : userId, 
                    videoId == null ? null : videoId);
                }
            }
    }
    /**
     * 视频死信队列消费者
     * @param video
     */
    @RabbitListener(queues = RabbitMqConfig.VIDEO_DEAD_QUEUE_NAME)
    public void videoDeadConsumer(Video videoFromQueue, Channel channel, Message mqMessage) {
        log.info("视频死信队列消费者");
        Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        String userId = videoFromQueue.getUserId().toString();
        String videoId = videoFromQueue.getId().toString();
        try{
            log.error("视频分发最终失败（进入死信队列）,userId={}, videoId={}", userId, videoId);
        }catch(Exception e){
            try{
                channel.basicNack(deliveryTag, false, true);
            }catch(Exception e2){
                log.error("确认视频分发失败", e2);
                throw new GlobalExceptionHandler.BusinessException("确认视频分发失败");
            }
        }
    }

    /**
     * channel方法的封装
     */
    public void basicAck(Channel channel, long deliveryTag){
        try{
            channel.basicAck(deliveryTag, false);
        }catch(Exception e){
            log.error("确认消息失败", e);
            throw new GlobalExceptionHandler.BusinessException("ACK消息失败");
        }
    }

    public void basicNack(long deliveryTag, Channel channel, int retryCount, Map<String, Object> headers){
        try{
            if(retryCount >= 3){
                channel.basicNack(deliveryTag, false, false);
                headers.put("x-retry-count", retryCount + 1);
            }else{
                channel.basicNack(deliveryTag, false, true);
            }
        }catch(Exception e){
            log.error("拒绝消息失败", e);
            throw new GlobalExceptionHandler.BusinessException("NACK消息失败");
        }
    }

    public void basicReject(Channel channel, long deliveryTag){
        try{
            channel.basicReject(deliveryTag, false);
        }catch(Exception e){
            log.error("拒绝消息失败", e);
            throw new GlobalExceptionHandler.BusinessException("REJECT消息失败");
        }
    }
}
