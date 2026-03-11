package com.yuliyuli.init;

import com.yuliyuli.document.VideoDocument;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/** 搜索视频初始化类 */
@Component
@Slf4j
public class SearchVideoInit implements CommandLineRunner {

  // 缓存的前十个热门视频的前缀
  public static final String HOT_TOP_KEY = "video:hot:top";
  // 缓存的所有热门视频，100条，用于处理前十热门视频的过期
  public static final String HOT_ALL_KEY = "video:hot:all:";
  // 缓存的热门视频过期时间，随机10-20小时
  private static final int EXPIRE_TIME = 11;

  @Resource private ElasticsearchOperations elasticsearchOperations;

  @Resource RedisTemplate<String, Object> redisTemplate;

  @Override
  public void run(String... args) {
    try {
      IndexOperations indexOps = elasticsearchOperations.indexOps(VideoDocument.class);
      if (!indexOps.exists()) {
        createIndex(indexOps);
      } else {
        log.info("搜索视频索引已存在");
        syncInitHotVideoDataToRedis();
      }
    } catch (Exception e) {
      log.error("初始化搜索视频失败", e);
      throw new RuntimeException("初始化搜索视频失败", e);
    }
  }

  public void createIndex(IndexOperations indexOps) {
    Map<String, Object> settings =
        Map.of(
            "number_of_shards", 1,
            "number_of_replicas", 0,
            "analysis",
                Map.of(
                    // 1. 配置ngram分词器（核心：拆分字符为n-gram）
                    "analyzer",
                        Map.of(
                            // 用于联想的分词器：最小2个字符，最大10个字符
                            "video_suggest_analyzer",
                                Map.of(
                                    "type", "custom",
                                    "tokenizer", "video_ngram_tokenizer",
                                    "filter", "lowercase" // 小写化，避免大小写敏感
                                    ),
                            // 保留IK分词器（用于全文搜索）
                            "ik_max_word", Map.of("type", "ik_max_word")),
                    // 2. 配置ngram分词器的tokenizer
                    "tokenizer",
                        Map.of(
                            "video_ngram_tokenizer",
                            Map.of(
                                "type", "ngram",
                                "min_gram", 2, // 最小拆分长度（输入2个字符开始联想）
                                "max_gram", 10 // 最大拆分长度
                                ))));
    indexOps.create(settings);
    indexOps.putMapping();
    log.info("搜索视频索引创建成功");
  }

  /** 同步视频数据到Redis */
  private void syncInitHotVideoDataToRedis() {
    try {
      log.info("开始同步视频数据到Redis");
      Criteria criteria = Criteria.where("playCount").greaterThan(10000);
      CriteriaQuery query =
          CriteriaQuery.builder(criteria)
              .withPageable(PageRequest.of(0, 100))
              .withSort(Sort.by("playCount").descending())
              .build();
      SearchHits<VideoDocument> searchHits =
          elasticsearchOperations.search(query, VideoDocument.class);
      List<VideoDocument> videoDocuments =
          searchHits.getSearchHits().stream().map(hit -> hit.getContent()).toList();
      for (VideoDocument videoDocument : videoDocuments) {
        String hotVideoKey = HOT_ALL_KEY;
        redisTemplate
            .opsForZSet()
            .add(hotVideoKey, videoDocument.getUrl(), videoDocument.getPlayCount());
        redisTemplate.opsForList().leftPush(hotVideoKey + videoDocument.getUrl(), videoDocument);
        redisTemplate.expire(hotVideoKey, EXPIRE_TIME + new Random().nextInt(3), TimeUnit.HOURS);
        redisTemplate.expire(
            hotVideoKey + videoDocument.getUrl(),
            EXPIRE_TIME + new Random().nextInt(3),
            TimeUnit.HOURS);
      }
      log.info("同步视频数据到Redis成功");
    } catch (Exception e) {
      log.error("同步视频数据到Redis失败", e);
      throw new RuntimeException("同步视频数据到Redis失败", e);
    }
  }

  /** 刷新前10缓存 */
  public void refreshTopTenCache() {
    // 从所有视频中获取前10个
    try {
      Set<Object> topTenUrls = redisTemplate.opsForZSet().reverseRange(HOT_ALL_KEY, 0, 9);
      List<VideoDocument> topTenVideos =
          redisTemplate
              .opsForList()
              .range(
                  HOT_ALL_KEY
                      + topTenUrls.stream().map(obj -> obj.toString()).collect(Collectors.toList()),
                  0,
                  9)
              .stream()
              .map(obj -> (VideoDocument) obj)
              .toList();
      if (topTenUrls != null && !topTenUrls.isEmpty()) {
        // 清除旧的top10缓存
        redisTemplate.delete(HOT_TOP_KEY);
        // 重新设置前10个（带过期时间）
        for (Object videoUrlObj : topTenUrls) {
          String url = videoUrlObj.toString();
          VideoDocument videoDocument =
              topTenVideos.stream()
                  .filter(video -> video.getUrl().equals(url))
                  .findFirst()
                  .orElse(null);
          if (videoDocument != null) {
            // 每个视频设置过期时间（随机10-20小时）
            redisTemplate.opsForList().leftPush(HOT_TOP_KEY, videoDocument);
            redisTemplate.expire(
                HOT_TOP_KEY, EXPIRE_TIME + new Random().nextInt(3), TimeUnit.HOURS);
          }
        }
        log.info("刷新前10热门视频缓存成功");
      }
    } catch (Exception e) {
      log.error("刷新前10热门视频缓存失败", e);
      throw new RuntimeException("刷新前10热门视频缓存失败", e);
    }
  }
}
