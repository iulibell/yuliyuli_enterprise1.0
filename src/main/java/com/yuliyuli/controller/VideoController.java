package com.yuliyuli.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.annotation.RateLimit;
import com.yuliyuli.common.Result;
import com.yuliyuli.entity.Comment;
import com.yuliyuli.entity.User;
import com.yuliyuli.entity.UserHolder;
import com.yuliyuli.entity.VideoCollection;
import com.yuliyuli.entity.VideoDelivery;
import com.yuliyuli.entity.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.service.VideoService;
import com.yuliyuli.vo.VideoVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/video")
@Tag(name = "视频模块")
@Slf4j
public class VideoController {

    @Resource
    private VideoService videoService;

    private void checkLogin() {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new GlobalExceptionHandler.BusinessException("请完成登录");
        }
    }

    @RateLimit(limit = 10, window = 60, key = "delivery")
    @PostMapping("/delivery")
    @Operation(summary = "视频投递")
    public Result<Object> deliveryVideo(@Parameter(description = "传递的视频对象", required = true)
        @Validated @RequestBody VideoDelivery video) {
            checkLogin();
        try{
            videoService.videoDeliver(video);
            log.info("视频投递成功,视频ID:{}", video.getVideo().getUrl());
        }catch(Exception e){
            log.error("视频投递失败", e);
            throw new GlobalExceptionHandler.BusinessException("视频投递失败");
        }
        return Result.success();
    }

    @RateLimit(limit = 10, window = 60, key = "like")
    @PostMapping("/like")
    @Operation(summary = "视频点赞")
    public Result<Object> likeVideo(@Parameter(description = "传递的视频对象", required = true)
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
    @Operation(summary = "视频收藏")
    public Result<Object> collectVideo(@Parameter(description = "传递的视频对象", required = true)
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

    @RateLimit(limit = 10, window = 60, key = "comment")
    @PostMapping("/comment")
    @Operation(summary = "视频评论")
    public Result<Object> commentVideo(@Parameter(description = "传递的评论对象", required = true)
        @RequestBody Comment comment) {
            checkLogin();
        try{
            videoService.videoComment(comment);
            log.info("视频评论成功,视频ID:{},用户ID:{}",comment.getVideoId(),comment.getUserId());
        }catch(Exception e){
            log.error("视频评论失败", e);
            throw new GlobalExceptionHandler.BusinessException("视频评论失败");
        }
        return Result.success();
    }

    @GetMapping("/videoList")
    @Operation(summary = "获取视频列表")
    public Result<Page<VideoVO>> getVideoList(@Parameter(description = "页码") @RequestParam(defaultValue = "1")
        int pageNum,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "10")
        int pageSize) {
        try{
            Page<VideoVO> page = videoService.getVideoList(pageNum, pageSize);
            return Result.success(page);
        }catch(Exception e){
            log.error("获取视频列表失败", e);
            throw new GlobalExceptionHandler.BusinessException("获取视频列表失败");
        }
    }
}
