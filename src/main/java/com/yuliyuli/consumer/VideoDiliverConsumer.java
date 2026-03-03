package com.yuliyuli.consumer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.dto.Video;
import com.yuliyuli.dto.VideoDelivery;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.util.TransferUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VideoDiliverConsumer {

    @Autowired
    private TransferUtil transferUtil;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private RedissonClient redissonClient;

    /** 
     * 视频队列消费者
     * @param videoFromQueue 视频消息
     */
    @RabbitListener(queues = RabbitMqConfig.VIDEO_QUEUE_NAME)
    public void videoConsumer(VideoDelivery videoDilivery, Channel channel, Message mqMessage) throws Exception {
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        if(videoDilivery == null){
            basicReject(channel, deliveryTag);
            return;
        }

        String userId = videoDilivery.getVideo().getUserId().toString();
        String videoId = UUID.randomUUID().toString();

        // 视频锁，确保每个用户每个视频只插入一次
        String lockKey = "video:lock:" + videoId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        
        boolean lockSuccess = lock.tryLock(3, 10, TimeUnit.SECONDS);
        if(!lockSuccess){
            basicAck(channel, deliveryTag);
            return;
        }
        try{
            videoMapper.insert(videoDilivery.getVideo());
            
            // 视频文件存储路径
            String videoPath = transferUtil.saveVideoToDirectory(videoDilivery);
            if(videoPath == null){
                basicNack(channel, deliveryTag);
                return;
            }
            basicAck(channel, deliveryTag);
        }catch(Exception e){
            //异常，拒绝消息
            try{
                basicNack(channel, deliveryTag);
            }catch(Exception e2){
                log.error("拒绝消息失败", e2);
                throw new GlobalExceptionHandler.BusinessException("拒绝消息失败");
            }
        }finally{
                if(lock.isHeldByCurrentThread() && lock!=null){
                    lock.unlock();
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

    public void basicNack(Channel channel, long deliveryTag){
        try{
            channel.basicNack(deliveryTag, false, true);
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
