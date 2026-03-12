package com.yuliyuli.timer;

import com.yuliyuli.entity.VideoLike;
import com.yuliyuli.mapper.CommentMapper;
import com.yuliyuli.mapper.VideoMapper;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProcessDelay {

  @Resource private RedisTemplate<String, Object> redisTemplate;

  @Resource private RedissonClient redissonClient;

  @Resource private VideoMapper videoMapper;

  @Resource private CommentMapper commentMapper;

  @Resource private ElasticsearchOperations elasticsearchOperations;

  // 处理延时点赞
  @Scheduled(fixedRate = 1000) // 每秒检查一次
  @Async
  public void processDelayLikes() {
    String delayKey = "video:like:delay";
    long currentTime = System.currentTimeMillis();

    // 获取所有到时间的点赞操作
    RScoredSortedSet<VideoLike> sortedSet = redissonClient.getScoredSortedSet(delayKey);
    Collection<VideoLike> expiredLikes =
        sortedSet.entryRange(0, true, currentTime, true).stream()
            .map(ScoredEntry::getValue)
            .collect(Collectors.toList());

    for (VideoLike videoLike : expiredLikes) {
      try {
        processLike(videoLike);
        sortedSet.remove(videoLike);
      } catch (Exception e) {
        log.error("处理延时点赞失败", e);
      }
    }
  }

  // 处理延时点赞同步到数据库
  private void processLike(VideoLike videoLike) {
    final String COUNTER_KEY_PREFIX = "video:like:";
    final String USER_KEY_PREFIX = "user:like:";

    String counterKey = COUNTER_KEY_PREFIX + videoLike.getVideoId();
    String userKey = USER_KEY_PREFIX + videoLike.getUserId();

    try {
      RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
      RSet<Long> userSet = redissonClient.getSet(userKey);
      Long finallyCount = null;

      if (userSet.contains(videoLike.getUserId())) {
        // 取消点赞
        userSet.remove(videoLike.getUserId());
        finallyCount = counter.decrementAndGet();
        videoMapper.deleteVideoLike(videoLike.getVideoId(), videoLike.getUserId());
      } else {
        // 点赞
        userSet.add(videoLike.getUserId());
        finallyCount = counter.incrementAndGet();
      }

      // 更新数据库
      videoMapper.updateVideoLikeCount(finallyCount.intValue(), videoLike.getVideoId().toString());
      videoMapper.insertVideoLike(videoLike);
      log.info(
          "延时处理点赞：用户{}，视频{}，点赞数{}", videoLike.getUserId(), videoLike.getVideoId(), finallyCount);
    } catch (Exception e) {
      log.error("处理延时点赞失败: {}", videoLike, e);
    }
  }

  // 处理延时热门视频播放
  @Scheduled(fixedRate = 15000) // 每15秒检查一次
  @Async
  public void processDelayHotPlay() {
    String delayKey = "hot:video:play:delay";
    long currentTime = System.currentTimeMillis();
    // 从有序集合获取所有到期的 videoUrl
    RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(delayKey);
    Collection<String> expiredVideoUrls =
        sortedSet.entryRange(0, true, currentTime, true).stream()
            .map(entry -> entry.getValue())
            .collect(Collectors.toList());
    // 遍历处理每个到期的 videoUrl
    for (String videoUrl : expiredVideoUrls) {
      try {
        processHotPlay(videoUrl);
        sortedSet.remove(videoUrl);
      } catch (Exception e) {
        log.error("处理延时热门视频播放失败: {}", videoUrl, e);
      }
    }
  }

  // 处理延时热门视频播放同步到数据库
  private void processHotPlay(String videoUrl) {
    final String COUNTER_KEY_PREFIX = "video:hot:play:";

    String counterKey = COUNTER_KEY_PREFIX + videoUrl;
    try {
      // 计数器，记录热门视频播放次数
      RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
      Long finallyCount = counter.incrementAndGet();
      processPlayCountToES(videoUrl);
      videoMapper.updateVideoPlayCount(finallyCount.intValue(), videoUrl);
      log.info("延时处理热门视频播放：视频{}，播放数{}", videoUrl, finallyCount);
    } catch (Exception e) {
      log.error("处理延时热门视频播放失败: {}", videoUrl, e);
    }
  }

  // 处理延时视频播放
  @Scheduled(fixedRate = 5000) // 每5秒检查一次
  public void processDelayPlay() {
    String delayKey = "video:play:delay";
    long currentTime = System.currentTimeMillis();
    // 从有序集合获取所有到期的 videoUrl
    RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(delayKey);
    Collection<String> expiredVideoUrls =
        sortedSet.entryRange(0, true, currentTime, true).stream()
            .map(entry -> entry.getValue())
            .collect(Collectors.toList());
    // 遍历处理每个到期的 videoUrl
    for (String videoUrl : expiredVideoUrls) {
      try {
        processPlay(videoUrl);
        sortedSet.remove(videoUrl);
      } catch (Exception e) {
        log.error("处理延时视频播放失败: {}", videoUrl, e);
      }
    }
  }

  // 处理延时视频播放同步到数据库
  private void processPlay(String videoUrl) {
    final String COUNTER_KEY_PREFIX = "video:play:";

    String counterKey = COUNTER_KEY_PREFIX + videoUrl;
    try {
      // 计数器，记录播放次数
      RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
      Long finallyCount = counter.incrementAndGet();
      processPlayCountToES(videoUrl);
      videoMapper.updateVideoPlayCount(finallyCount.intValue(), videoUrl);
      log.info("延时处理视频播放：视频{}，播放数{}", videoUrl, finallyCount);
    } catch (Exception e) {
      log.error("处理延时视频播放失败: {}", videoUrl, e);
    }
  }

  @Scheduled(fixedRate = 5000) // 每5秒检查一次
  @Async
  protected void processDelayDelete() {
    String delayKey = "video:delete:delay";
    long currentTime = System.currentTimeMillis();
    // 从有序集合获取所有到期的 videoUrl
    RScoredSortedSet<Map<String, Object>> sortedSet = redissonClient.getScoredSortedSet(delayKey);
    Collection<Map<String, Object>> expiredVideoUrls =
        sortedSet.entryRange(0, true, currentTime, true).stream()
            .map(entry -> entry.getValue())
            .collect(Collectors.toList());
    // 遍历处理每个到期的 videoUrl
    for (Map<String, Object> map : expiredVideoUrls) {
      try {
        processDelete(map);
        sortedSet.remove(map);
      } catch (Exception e) {
        log.error("处理延时视频删除失败: {}", map, e);
      }
    }
  }

  private void processDelete(Map<String, Object> map) {
    // 从map中获取videoUrl和userId
    String videoUrl = map.get("videoUrl").toString();
    String userId = map.get("userId").toString();
    String hotVideoKey = "video:hot:all:" + videoUrl;
    String videoKey = "video:info:" + videoUrl;
    try {
      // 删除热门视频缓存
      if (redisTemplate.hasKey(hotVideoKey)) {
        redisTemplate.delete(hotVideoKey);
      }
      // 删除视频信息缓存
      if (redisTemplate.hasKey(videoKey)) {
        redisTemplate.delete(videoKey);
      }
      // 删除数据库中的视频记录
      videoMapper.deleteVideo(videoUrl, Long.parseLong(userId));
      commentMapper.deleteComment(videoUrl);
      // 删除ES中的视频记录
      processDeleteES(videoUrl);
    } catch (Exception e) {
      log.error("处理延时视频删除失败: {}", videoUrl, e);
    }
  }

  private void processPlayCountToES(String videoUrl) {
    if (videoUrl == null) {
      return;
    }
    try {
      // 定义核心参数（文档ID直接用videoUrl，无需替换特殊字符，ES支持特殊字符作为文档ID）
      String docId = videoUrl; // 要更新的ES文档ID
      String scriptSource = "ctx._source.playCount = (ctx._source.playCount ?: 0) + 1"; // 原子更新脚本
      // 构建 UpdateQuery（适配所有 Spring Data Elasticsearch 版本的通用写法）
      UpdateQuery updateQuery =
          UpdateQuery.builder(docId)
              .withScript(scriptSource) // 传入内联脚本内容（字符串）
              .withScriptType(ScriptType.INLINE) // 明确指定脚本类型为内联（关键！）
              .build();
      // 批量更新ES文档
      elasticsearchOperations.update(updateQuery, IndexCoordinates.of("video"));
    } catch (Exception e) {
      log.error("处理延时视频播放同步到ES失败: {}", videoUrl, e);
    }
  }

  private void processDeleteES(String videoUrl) {
    if (videoUrl == null) {
      return;
    }
    try {
      // 定义核心参数（文档ID直接用videoUrl，无需替换特殊字符，ES支持特殊字符作为文档ID）
      String docId = videoUrl; // 要更新的ES文档ID
      String scriptSource = "ctx._source.playCount = (ctx._source.playCount ?: 0) - 1"; // 原子更新脚本
      // 构建 UpdateQuery（适配所有 Spring Data Elasticsearch 版本的通用写法）
      UpdateQuery updateQuery =
          UpdateQuery.builder(docId)
              .withScript(scriptSource) // 传入内联脚本内容（字符串）
              .withScriptType(ScriptType.INLINE) // 明确指定脚本类型为内联（关键！）
              .build();
      // 批量更新ES文档
      elasticsearchOperations.update(updateQuery, IndexCoordinates.of("video"));
    } catch (Exception e) {
      log.error("处理延时视频删除同步到ES失败: {}", videoUrl, e);
    }
  }
}
