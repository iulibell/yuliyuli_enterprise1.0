package com.yuliyuli.service.impl;

import com.yuliyuli.service.VideoService;
import com.yuliyuli.util.VideoConvertUtil;
import com.yuliyuli.vo.VideoVO;
import com.yuliyuli.wrapper.VideoWrapper;

import jakarta.annotation.Resource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.entity.Comment;
import com.yuliyuli.entity.User;
import com.yuliyuli.entity.UserHolder;
import com.yuliyuli.entity.Video;
import com.yuliyuli.entity.VideoCollection;
import com.yuliyuli.entity.VideoDelivery;
import com.yuliyuli.entity.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.init.VideoInfoInit;
import com.yuliyuli.mapper.VideoMapper;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    // 视频分发线程池
    @Resource
    private ExecutorService threadPoolExecutor;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private VideoWrapper videoWrapper;
    /**
     * 视频分发
     * @param video 视频信息
     */

    @Override
    public void videoDeliver(VideoDelivery videoDelivery) {
        threadPoolExecutor.submit(() -> {
            User user = UserHolder.getUser();
            if(user == null){
                throw new GlobalExceptionHandler.BusinessException("请完成登录");
            }
            try{
                rabbitTemplate.convertAndSend(RabbitMqConfig.VIDEO_QUEUE_NAME, videoDelivery);
                log.info("视频分发成功,视频ID:{}", videoDelivery.getVideo().getUrl());  
            }catch(Exception e){
                log.error("视频分发失败", e);
                throw new GlobalExceptionHandler.BusinessException("视频分发失败");
            }finally{
            }
        });
    }

    /**
     * 视频点赞
     * @param videoLike 视频点赞对象
     */
    @Override
    public void videoLike(VideoLike videoLike) {
        threadPoolExecutor.submit(() -> {
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
        });
    }

    /**
     * 视频收藏
     * @param videoCollect 视频收藏对象
     */
    @Override
    public void videoCollect(VideoCollection videoCollection) {
        threadPoolExecutor.submit(() -> {
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
        });
    }

    /**
     * 视频评论
     * @param comment 视频评论对象
     */
    @Override
    public void videoComment(Comment comment) {
        threadPoolExecutor.submit(() -> {
            User user = UserHolder.getUser();
            if(user == null){
                throw new GlobalExceptionHandler.BusinessException("请完成登录");
            }
            try{
                rabbitTemplate.convertAndSend(RabbitMqConfig.COMMENT_QUEUE_NAME, comment);
                log.info("视频评论成功,视频ID:{}", comment.getVideoId());
            }catch(Exception e){
                log.error("视频评论失败", e);
                throw new GlobalExceptionHandler.BusinessException("视频评论失败");
            }            
        });
    }

    /**
     * 获取视频列表,让前端获取视频，用于主页懒加载视频
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 视频列表
     */
    @Override
    public Page<VideoVO> getVideoList(int pageNum, int pageSize) {
        // 从缓存中获取视频列表
        String listKey = VideoInfoInit.VIDEO_LIST_CACHE_KEY + pageNum;
        RBucket<List<Video>> listBucket = redissonClient.getBucket(listKey);
        if(listBucket.isExists()){
            List<Video> videoList = listBucket.get();
            log.info("从缓存中获取视频列表成功,视频数量:{}", videoList.size());
            Page<Video> page = new Page<>(pageNum, pageSize);
            page.setRecords(videoList);
            // 转换为视频VO类列表
            Page<VideoVO> videoVOPageList = VideoConvertUtil.converToVideoVOList(page);
            return videoVOPageList;
        }

        //redis未命中，从数据库查询
        log.info("从数据库中获取视频列表,页码:{}", pageNum);
        Page<Video> page = videoMapper.selectPage(new Page<>(pageNum, pageSize), 
        videoWrapper.getInitVideo());
        Duration expireDuration = Duration.ofHours(VideoInfoInit.EXPIRE_TIME);
        // 缓存到redis
        if(page.getRecords() != null && !page.getRecords().isEmpty()){
            listBucket.set(page.getRecords(), 
            expireDuration);
        }
        // 转换为视频VO类列表
        Page<VideoVO> videoVOPageList = VideoConvertUtil.converToVideoVOList(page);
        return videoVOPageList;
    }
}
