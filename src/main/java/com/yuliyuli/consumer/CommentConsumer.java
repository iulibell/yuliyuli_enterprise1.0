package com.yuliyuli.consumer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.entity.Comment;
import com.yuliyuli.entity.Video;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.CommentMapper;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.wrapper.VideoWrapper;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommentConsumer {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private VideoWrapper videoWrapper;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;
    
    @RabbitListener(queues = RabbitMqConfig.COMMENT_QUEUE_NAME)
    public void commentConsumer(Comment comment, Channel channel, Message mqMessage) throws Exception{
        Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        RLock lock = null;

        final String LOCK_KEY_PREFIX = "comment:lock:";

        if(comment == null || comment.getUserId() == null){
            channel.basicReject(deliveryTag, false);
            return;
        }

        // 从消息头中获取重试次数,如果没有则默认0
        Map<String,Object> headers = mqMessage.getMessageProperties().getHeaders();
        Integer retryCount = (Integer) headers.getOrDefault("x-retry-count",0);

        // 使用雪花算法生成评论id
        Long commentId = snowflakeIdGenerator.nextId();
        String lockKey = LOCK_KEY_PREFIX + comment.getUserId();
        lock = redissonClient.getLock(lockKey);
        boolean isLock = lock.tryLock(3, 10, TimeUnit.SECONDS);
        if(!isLock){
            channel.basicNack(deliveryTag, false, true);
            log.info("用户{}评论锁被其他线程占用,已重新放入队列", comment.getUserId());
            return;
        }
        try{
            LambdaQueryWrapper<Video> getCommentCountWrapper = videoWrapper.getCommentCount(comment.getVideoId().toString());
            comment.setCommentId(commentId);
            commentMapper.insert(comment);
            int commentCount = Integer.valueOf(videoMapper.selectById(getCommentCountWrapper).getCommentCount());
            videoMapper.updateVideoCommentCount(commentCount, comment.getVideoId().toString());
            channel.basicAck(deliveryTag, false);
        }catch(Exception e){
            try{
                basicNack(deliveryTag, channel, retryCount, headers);
            }catch(Exception e2){
                Long userId = comment.getUserId() != null ? comment.getUserId() : 0L;
                String videoId = comment.getVideoId() != null ? comment.getVideoId().toString() : "";
                log.error("评论队列消费者异常,用户ID:{} 视频ID:{},重试次数:{}", userId, videoId, retryCount, e2);
                throw new GlobalExceptionHandler.BusinessException("评论失败");
            }
        }finally{
            if(lock != null && lock.isHeldByCurrentThread()){
                lock.unlock();
                log.info("用户{}评论锁已释放", comment.getUserId());
            }
        }
    }

    @RabbitListener(queues = RabbitMqConfig.COMMENT_DEAD_QUEUE_NAME)
    public void commentDeadConsumer(Comment comment, Channel channel, Message mqMessage) throws Exception{
        log.info("评论死信消费者");
        Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        try{
            String userId = comment.getUserId() != null ? comment.getUserId().toString() : "";
            String videoId = comment.getVideoId() != null ? comment.getVideoId().toString() : "";
            log.info("评论死信消费者,用户ID:{} 视频ID:{}", userId, videoId);
        }catch(Exception e){
            try{
                channel.basicNack(deliveryTag, false, false);
            }catch(Exception e2){
                log.error("确认评论失败");
                throw new GlobalExceptionHandler.BusinessException("确认评论失败");
            }
        }
    }

    public void basicAck(Long deliveryTag, Channel channel){
        try{
            channel.basicAck(deliveryTag, false);
        }catch(Exception e){
            log.error("ACK评论失败", e);
            throw new GlobalExceptionHandler.BusinessException("ACK评论失败");
        }
    }

    public void basicNack(Long deliveryTag, Channel channel, int retryCount, Map<String,Object> headers){
        try{
            if(retryCount < 3){
                headers.put("comment-retry-count", retryCount + 1);
                channel.basicNack(deliveryTag, false, true);
            }
            if(retryCount >= 3){
                channel.basicNack(deliveryTag, false, false);
            }
        }catch(Exception e){
            log.error("NACK评论失败", e);
            throw new GlobalExceptionHandler.BusinessException("NACK评论失败");
        }
    }
}
