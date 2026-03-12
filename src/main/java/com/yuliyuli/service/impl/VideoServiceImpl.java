package com.yuliyuli.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.document.VideoDocument;
import com.yuliyuli.entity.Comment;
import com.yuliyuli.entity.User;
import com.yuliyuli.entity.UserHolder;
import com.yuliyuli.entity.Video;
import com.yuliyuli.entity.VideoCollection;
import com.yuliyuli.entity.VideoDelivery;
import com.yuliyuli.entity.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.init.SearchVideoInit;
import com.yuliyuli.init.VideoInfoInit;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.service.SearchService;
import com.yuliyuli.service.VideoService;
import com.yuliyuli.util.VideoConvertUtil;
import com.yuliyuli.vo.HotRecommendVideoVO;
import com.yuliyuli.vo.SearchVideoVO;
import com.yuliyuli.vo.VideoVO;
import com.yuliyuli.wrapper.VideoWrapper;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

  @Resource private RabbitTemplate rabbitTemplate;

  // 视频分发线程池
  @Resource private ExecutorService threadPoolExecutor;

  @Resource private RedissonClient redissonClient;

  @Resource private VideoMapper videoMapper;

  @Resource private VideoWrapper videoWrapper;

  @Resource private SearchService searchService;

  @Resource private VideoConvertUtil videoConvertUtil;

  @Resource private RedisTemplate<String, Object> redisTemplate;

  /*=======================================================👇消息发布者============================================================= */

  /**
   * 视频分发
   *
   * @param video 视频信息
   */
  @Override
  public String videoDeliver(VideoDelivery videoDelivery) {
    threadPoolExecutor.submit(
        () -> {
          User user = UserHolder.getUser();
          if (user == null) {
            return "请完成登录";
          }
          try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.VIDEO_QUEUE_NAME, videoDelivery);
          } catch (Exception e) {
            log.error("视频分发失败", e);
            return "视频上传失败";
          }
          return "视频上传成功";
        });
      return "视频上传成功";
  }

  /**
   * 视频点赞
   *
   * @param videoLike 视频点赞对象
   */
  @Override
  public String videoLike(VideoLike videoLike) {
    threadPoolExecutor.submit(
        () -> {
          User user = UserHolder.getUser();
          if (user == null) {
            return "请完成登录";
          }
          try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.LIKE_QUEUE_NAME, videoLike);
          } catch (Exception e) {
            log.error("视频点赞失败", e);
            return "视频点赞失败";
          }
          return "点赞成功";
        });
    return "点赞成功";
  }

  /**
   * 视频收藏
   *
   * @param videoCollect 视频收藏对象
   */
  @Override
  public String videoCollect(VideoCollection videoCollection) {
    threadPoolExecutor.submit(
        () -> {
          User user = UserHolder.getUser();
          if (user == null) {
            return "请完成登录";
          }
          try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.COLLECT_QUEUE_NAME, videoCollection);
          } catch (Exception e) {
            log.error("视频收藏失败", e);
            return "视频收藏失败";
          }
          return "收藏成功";
        });
    return "收藏成功";
  }

  /**
   * 视频评论
   *
   * @param comment 视频评论对象
   */
  @Override
  public String videoComment(Comment comment) {
    threadPoolExecutor.submit(
        () -> {
          User user = UserHolder.getUser();
          if (user == null) {
            return "请完成登录";
          }
          try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.COMMENT_QUEUE_NAME, comment);
          } catch (Exception e) {
            log.error("视频评论失败", e);
            return "视频评论失败";
          }
          return "评论成功";
        });
    return "评论成功";
  }

  /**
   * 用户点击视频后播放视频，发送至消费者进行播放统计
   *
   * @param videoUrl 视频URL
   */
  @Override
  public String hotVideoPlay(String videoUrl) {
    threadPoolExecutor.submit(
        () -> {
          try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.HOT_PLAY_QUEUE_NAME, videoUrl);
          } catch (Exception e) {
            log.error("视频播放失败", e);
            return "视频播放失败";
          }
          return "";
        });
    return "";
  }

  /**
   * 用户点击普通视频播放视频，发送至消费者进行播放统计
   *
   * @param videoUrl 视频URL
   */
  @Override
  public String videoPlay(String videoUrl) {
    threadPoolExecutor.submit(
        () -> {
          try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.PLAY_QUEUE_NAME, videoUrl);
          } catch (Exception e) {
            log.error("视频播放失败", e);
            return "视频播放失败";
          }
          return "";
        });
    return "";
  }

  /*=======================================================👇get方法============================================================= */

  /**
   * 获取视频列表,让前端获取视频，用于主页懒加载视频
   *
   * @param pageNum 页码
   * @param pageSize 每页数量
   * @return 视频列表
   */
  @Override
  public Page<VideoVO> getVideoList(int pageNum, int pageSize) {
    String listKey = VideoInfoInit.VIDEO_LIST_CACHE_KEY + pageNum;
    RBucket<List<Video>> listBucket = redissonClient.getBucket(listKey);

    try {
      // 1. 先查缓存
      if (listBucket.isExists()) {
        List<Video> videoList = listBucket.get();
        log.info("从缓存中获取视频列表成功,视频数量:{}", videoList.size());
        return convertToVOPage(videoList, pageNum, pageSize);
      }

      // 2. 获取分布式锁
      String lockKey = "lock:video:list:" + pageNum;
      RLock lock = redissonClient.getLock(lockKey);
      boolean isLock = lock.tryLock(3, 10, TimeUnit.SECONDS);

      if (isLock) {
        try {
          // 3. 双重检查（防止等待锁期间其他线程已加载缓存）
          if (listBucket.isExists()) {
            return convertToVOPage(listBucket.get(), pageNum, pageSize);
          }

          // 4. 从数据库查询
          log.info("从数据库中获取视频列表,页码:{}", pageNum);
          Page<Video> page =
              videoMapper.selectPage(new Page<>(pageNum, pageSize), videoWrapper.getInitVideo());

          // 5. 缓存结果
          Duration expireDuration = Duration.ofHours(VideoInfoInit.EXPIRE_TIME);
          if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            listBucket.set(page.getRecords(), expireDuration);
          } else {
            // 缓存空值，防止缓存穿透
            listBucket.set(new ArrayList<>(), Duration.ofMinutes(5));
          }

          return VideoConvertUtil.converPageToVideoVOList(page);
        } finally {
          if (lock.isHeldByCurrentThread()) {
            lock.unlock();
          }
        }
      } else {
        // 获取锁失败，降级处理：直接查数据库
        log.warn("获取视频列表锁失败,页码:{}", pageNum);
        Page<Video> page =
            videoMapper.selectPage(new Page<>(pageNum, pageSize), videoWrapper.getInitVideo());
        return VideoConvertUtil.converPageToVideoVOList(page);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new GlobalExceptionHandler.BusinessException("获取锁被中断");
    } catch (Exception e) {
      log.error("从数据库中获取视频列表失败", e);
      throw new GlobalExceptionHandler.BusinessException("从数据库中获取视频列表失败");
    }
  }

  /** 辅助方法：转换为视频VO分页对象 */
  private Page<VideoVO> convertToVOPage(List<Video> videoList, int pageNum, int pageSize) {
    Page<Video> page = new Page<>(pageNum, pageSize);
    page.setRecords(videoList);
    return VideoConvertUtil.converPageToVideoVOList(page);
  }

  /**
   * 用户点击搜索后根据传过来的标题来返回一堆相关的视频
   *
   * @param title 视频标题
   * @return 视频详情
   */
  @Override
  public Page<SearchVideoVO> getSearchVideoResults(String title) {
    try {
      List<SearchVideoVO> searchVideoResults = searchService.findByTitleSuggest(title);
      Page<SearchVideoVO> page = new Page<>(1, 20);
      page.setRecords(searchVideoResults);
      return page;
    } catch (Exception e) {
      log.error("根据标题搜索视频失败", e);
      throw new GlobalExceptionHandler.BusinessException("根据标题搜索视频失败");
    }
  }

  /**
   * 用户点击视频后打开视频详细页后来返回推荐热门视频
   *
   * @return 推荐热门视频，即右边的视频栏
   */
  @Override
  public List<HotRecommendVideoVO> getRecommendHotVideo() {
    // 1. 从ZSet获取前15个视频URL（按播放量降序）
    Set<Object> top15Urls =
        redisTemplate.opsForZSet().reverseRange(SearchVideoInit.HOT_ALL_KEY, 0, 14);

    // 2. 批量获取视频详细信息
    List<VideoDocument> hotVideoList = new ArrayList<>();
    for (Object urlObj : top15Urls) {
      String url = urlObj.toString();
      VideoDocument videoDocument =
          (VideoDocument)
              redisTemplate.opsForList().range(SearchVideoInit.HOT_ALL_KEY + url, 0, 0).get(0);
      if (videoDocument != null) {
        hotVideoList.add(videoDocument);
      }
    }
    log.info("获取推荐热门视频成功，数量: {}", hotVideoList.size());
    return VideoConvertUtil.convertVideoDocumentToHotRecommendVideoVO(hotVideoList);
  }
}
