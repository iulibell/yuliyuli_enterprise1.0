package com.yuliyuli.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yuliyuli.common.RateLimit;
import com.yuliyuli.common.Result;
import com.yuliyuli.dto.User;
import com.yuliyuli.dto.UserHolder;
import com.yuliyuli.dto.VideoCollection;
import com.yuliyuli.dto.VideoDelivery;
import com.yuliyuli.dto.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.service.VideoService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/video")
@Api(tags = "视频模块")
@Slf4j
public class VideoController {

    @Autowired
    private VideoService videoService;

    private void checkLogin() {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new GlobalExceptionHandler.BusinessException("请完成登录");
        }
    }
    
    @RateLimit(limit = 10, window = 60, key = "delivery")
    @PostMapping("/delivery")
    @ApiOperation("视频投递")
    public Result<Object> deliveryVideo(@ApiParam(value = "传递的视频对象", required = true)
        @Validated @RequestBody VideoDelivery video) {
            checkLogin();
        try{
            videoService.videoDiliver(video);
            log.info("视频投递成功,视频ID:{}", video.getVideo().getUrl());
        }catch(Exception e){
            log.error("视频投递失败", e);
            throw new GlobalExceptionHandler.BusinessException("视频投递失败");
        }
        return Result.success();
    }
    
    @RateLimit(limit = 10, window = 60, key = "like")
    @PostMapping("/like")
    @ApiOperation("视频点赞")
    public Result<Object> likeVideo(@ApiParam(value = "传递的视频对象", required = true)
        @Validated @RequestBody VideoLike videoLike) {
            checkLogin();
        try{
            videoService.videoLike(videoLike);
            log.info("视频点赞成功,视频ID:{},用户ID:{}", videoLike.getVideoId(), videoLike.getUserId());
        }catch(Exception e){
            log.error("视频点赞失败", e);
            throw new GlobalExceptionHandler.BusinessException("视频点赞失败");
        }
        return Result.success();
    }
    
    @RateLimit(limit = 10, window = 60, key = "collect")
    @PostMapping("/collect")
    @ApiOperation("视频收藏")
    public Result<Object> collectVideo(@ApiParam(value = "传递的视频对象", required = true)
        @Validated @RequestBody VideoCollection videoCollect) {
            checkLogin();
        try{
            videoService.videoCollect(videoCollect);
            log.info("视频收藏成功,视频ID:{},用户ID:{}", videoCollect.getVideoId(), videoCollect.getUserId());
        }catch(Exception e){
            log.error("视频收藏失败", e);
            throw new GlobalExceptionHandler.BusinessException("视频收藏失败");
        }
        return Result.success();
    }
}
