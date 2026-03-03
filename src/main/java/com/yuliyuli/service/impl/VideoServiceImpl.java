package com.yuliyuli.service.impl;

import com.yuliyuli.service.VideoService;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.dto.User;
import com.yuliyuli.dto.UserHolder;
import com.yuliyuli.dto.VideoCollection;
import com.yuliyuli.dto.VideoDelivery;
import com.yuliyuli.dto.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 视频分发
     * @param video 视频信息
     */
    @Override
    public void videoDiliver(VideoDelivery videoDilivery) {
        User user = UserHolder.getUser();
        if(user == null){
            throw new GlobalExceptionHandler.BusinessException("请完成登录");
        }
        try{
            rabbitTemplate.convertAndSend(RabbitMqConfig.VIDEO_QUEUE_NAME, videoDilivery);
            log.info("视频分发成功,视频ID:{}", videoDilivery.getVideo().getUrl());
        }catch(Exception e){
            log.error("视频分发失败", e);
            throw new GlobalExceptionHandler.BusinessException("视频分发失败");
        }
    }

    /**
     * 视频点赞
     * @param videoLike 视频点赞对象
     */
    @Override
    public void videoLike(VideoLike videoLike) {
        User user = UserHolder.getUser();
        if(user == null){
            throw new GlobalExceptionHandler.BusinessException("请完成登录");
        }
        try{
            rabbitTemplate.convertAndSend(RabbitMqConfig.LIKE_QUEUE_NAME, videoLike);
            log.info("视频点赞成功,视频ID:{}", videoLike.getVideoId());
        }catch(Exception e){
            log.error("视频点赞失败", e);
            throw new GlobalExceptionHandler.BusinessException("视频点赞失败");
        }
    }

    /**
     * 视频收藏
     * @param videoCollect 视频收藏对象
     */
    @Override
    public void videoCollect(VideoCollection videoCollection) {
        User user = UserHolder.getUser();
        if(user == null){
            throw new GlobalExceptionHandler.BusinessException("请完成登录");
        }
        try{
            rabbitTemplate.convertAndSend(RabbitMqConfig.COLLECT_QUEUE_NAME, videoCollection);
            log.info("视频收藏成功,视频ID:{}", videoCollection.getVideoId());
        }catch(Exception e){
            log.error("视频收藏失败", e);
            throw new GlobalExceptionHandler.BusinessException("视频收藏失败");
        }
    }
}
