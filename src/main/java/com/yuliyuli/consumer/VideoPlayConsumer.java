package com.yuliyuli.consumer;

import java.util.Map;

import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.exception.GlobalExceptionHandler;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VideoPlayConsumer {

    @Resource
    private RedissonClient redissonClient;

    @RabbitListener(queues = RabbitMqConfig.PLAY_QUEUE_NAME)
    public void videoPlay(String videoUrl, Channel channel, Message mqMessage) {
        Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();

        if(videoUrl == null) {
            log.error("视频URL为空");
            throw new GlobalExceptionHandler.BusinessException("视频URL为空");
        }

        final String DELAY_KEY = "video:play:delay";
        final int DELAY_TIME = 1000 * 5; // 5秒

        try{
            redissonClient.getScoredSortedSet(DELAY_KEY)
                .add(System.currentTimeMillis() + DELAY_TIME, videoUrl);            
            // 播放完成后，手动确认消息
            basicAck(deliveryTag, channel);
        }catch(Exception e){
            log.error("视频播放锁获取失败", e);
            throw new GlobalExceptionHandler.BusinessException("视频播放锁获取失败");
        }
    }

    public void basicAck(Long deliveryTag, Channel channel){
        try{
            channel.basicAck(deliveryTag, false);
        }catch(Exception e){
            log.error("ACK播放失败", e);
            throw new GlobalExceptionHandler.BusinessException("ACK播放失败");
        }
    }

    public void basicNack(Long deliveryTag, Channel channel, int retryCount, Map<String,Object> headers){
        try{
            if(retryCount < 3){
                headers.put("play-retry-count", retryCount + 1);
                channel.basicNack(deliveryTag, false, true);
            }
            if(retryCount >= 3){
                channel.basicNack(deliveryTag, false, false);
            }
        }catch(Exception e){
            log.error("NACK播放失败", e);
            throw new GlobalExceptionHandler.BusinessException("NACK播放失败");
        }
    }
}
