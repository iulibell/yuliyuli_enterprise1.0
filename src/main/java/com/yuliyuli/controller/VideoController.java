package com.yuliyuli.controller;

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
import com.yuliyuli.mapper.CommentMapper;
import com.yuliyuli.service.SearchService;
import com.yuliyuli.service.VideoService;
import com.yuliyuli.vo.HotRecommendVideoVO;
import com.yuliyuli.vo.SearchVideoVO;
import com.yuliyuli.vo.VideoVO;
import com.yuliyuli.wrapper.CommentWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 视频模块
 *
 * @author Dima
 * @date 2026-03-02
 */
@RestController
@RequestMapping("/api/video")
@Tag(name = "视频模块")
@Slf4j
public class VideoController {

  @Resource private CommentMapper commentMapper;

  @Resource private CommentWrapper commentWrapper;

  @Resource private VideoService videoService;

  @Resource private SearchService searchService;

  @Resource private RedisTemplate<String, Object> redisTemplate;

  // 检查用户是否登录
  private void checkLogin() {
    User user = UserHolder.getUser();
    if (user == null) {
      throw new GlobalExceptionHandler.BusinessException("请完成登录");
    }
  }

  /**
   * 视频投递
   *
   * @param video
   * @return 处理结果
   */
  @RateLimit(limit = 10, window = 60, key = "delivery")
  @PostMapping("/delivery")
  @Operation(summary = "视频投递")
  public Result<Object> deliveryVideo(
      @Parameter(description = "传递的视频对象", required = true) @Validated @RequestBody
          VideoDelivery video) {
    checkLogin();
    try {
      String message = videoService.videoDeliver(video);
      log.info("视频投递成功,视频ID:{}", video.getVideo().getUrl());
      return Result.success(message);
    } catch (Exception e) {
      log.error("视频投递失败", e);
      return Result.fail("视频上传失败,请稍后重试");
    }
  }

  @RateLimit(limit = 10, window = 60, key = "like")
  @PostMapping("/like")
  @Operation(summary = "视频点赞")
  public Result<Object> likeVideo(
      @Parameter(description = "传递的视频对象", required = true) @Validated @RequestBody
          VideoLike videoLike) {
    checkLogin();
    try {
      String message = videoService.videoLike(videoLike);
      log.info("视频点赞成功,视频ID:{},用户ID:{}", videoLike.getVideoId(), videoLike.getUserId());
      return Result.success(message);
    } catch (Exception e) {
      log.error("视频点赞失败", e);
      return Result.fail("视频点赞失败,请稍后重试");
    }
  }

  /**
   * 视频收藏
   *
   * @param videoCollect
   * @return 处理结果
   */
  @RateLimit(limit = 10, window = 60, key = "collect")
  @PostMapping("/collect")
  @Operation(summary = "视频收藏")
  public Result<Object> collectVideo(
      @Parameter(description = "传递的视频对象", required = true) @Validated @RequestBody
          VideoCollection videoCollect) {
    checkLogin();
    try {
      String message = videoService.videoCollect(videoCollect);
      log.info("视频收藏成功,视频ID:{},用户ID:{}", videoCollect.getVideoId(), videoCollect.getUserId());
      return Result.success(message); 
    } catch (Exception e) {
      log.error("视频收藏失败", e);
      return Result.fail("视频收藏失败,请稍后重试");
    }
  }

  /**
   * 视频评论
   *
   * @param comment
   * @return 处理结果
   */
  @RateLimit(limit = 10, window = 60, key = "comment")
  @PostMapping("/comment")
  @Operation(summary = "视频评论")
  public Result<Object> commentVideo(
      @Parameter(description = "传递的评论对象", required = true) @RequestBody Comment comment) {
    checkLogin();
    try {
      String message = videoService.videoComment(comment);
      log.info("视频评论成功,视频ID:{},用户ID:{}", comment.getVideoId(), comment.getUserId());
      return Result.success(message);
    } catch (Exception e) {
      log.error("视频评论失败", e);
      return Result.fail("视频评论失败,请稍后重试");
    }
  }

  /**
   * 获取视频列表
   *
   * @param pageNum 页码
   * @param pageSize 每页数量
   * @return 视频列表
   */
  @GetMapping("/videoList")
  @Operation(summary = "获取视频列表")
  public Result<Page<VideoVO>> getVideoList(
      @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
      @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
    try {
      Page<VideoVO> page = videoService.getVideoList(pageNum, pageSize);
      return Result.success(page);
    } catch (Exception e) {
      log.error("获取视频列表失败", e);
      throw new GlobalExceptionHandler.BusinessException("获取视频列表失败");
    }
  }

  /**
   * 用户点击搜索后根据传过来的标题来返回一堆相关的视频
   *
   * @param title 视频标题
   * @return 视频详情
   */
  @GetMapping("/clickSearch")
  @Operation(summary = "根据视频标题获取相关视频")
  public Result<Page<SearchVideoVO>> getVideoDetail(
      @Parameter(description = "视频标题") @RequestParam String title) {
    try {
      Page<SearchVideoVO> page = videoService.getSearchVideoResults(title);
      return Result.success(page);
    } catch (Exception e) {
      log.error("根据标题搜索视频失败", e);
      throw new GlobalExceptionHandler.BusinessException("根据标题搜索视频失败");
    }
  }

  /**
   * 固定返回15个从100个热门缓存中获取的视频
   *
   * @param videoId 视频ID
   * @return 相关视频，即右边的视频栏
   */
  @GetMapping("/clickVideo/{videoUrl}")
  @Operation(summary = "根据视频ID获取相关视频")
  public Result<Map<String, Object>> getRelatedVideo(
      @PathVariable String videoUrl,
      @Parameter(description = "上一页最后一条评论的id") @RequestParam(required = false) Long lastId) {
    // 先对热门视频进行播放计数，再返回相关视频
    if (redisTemplate.opsForValue().get(videoUrl) != null) {
      videoService.hotVideoPlay(videoUrl);
      // 返回的右侧热门推荐视频栏
      List<HotRecommendVideoVO> hotVideoVOList = videoService.getRecommendHotVideo();
      // 分页获取评论列表
      Page<Comment> commentPage = new Page<>(1, 10);
      commentMapper.selectPage(
          commentPage, commentWrapper.getCommentListByCursor(videoUrl, lastId, 10));
      Map<String, Object> map = new HashMap<>();
      map.put("hotVideoVOList", hotVideoVOList);
      // 传递评论列表
      map.put("commentList", commentPage.getRecords());
      return Result.success(map);
    } else {
      // 先对视频进行播放计数，再返回相关视频
      videoService.videoPlay(videoUrl);
      // 返回的右侧热门推荐视频栏
      List<HotRecommendVideoVO> hotVideoVOList = videoService.getRecommendHotVideo();
      // 分页获取评论列表
      Page<Comment> commentPage = new Page<>(1, 10);
      commentMapper.selectPage(
          commentPage, commentWrapper.getCommentListByCursor(videoUrl, lastId, 10));
      Map<String, Object> map = new HashMap<>();
      map.put("videoVOList", hotVideoVOList);
      // 传递评论列表
      map.put("commentList", commentPage.getRecords());
      return Result.success(map);
    }
  }
}
