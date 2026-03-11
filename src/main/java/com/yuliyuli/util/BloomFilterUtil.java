package com.yuliyuli.util;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/** 布隆过滤器工具类 用于筛选不存在的视频URL 预防缓存穿透 */
@Component
public class BloomFilterUtil {

  // 布隆过滤器，用于筛选不存在的视频URL
  private BloomFilter<String> videoFilter;

  @SuppressWarnings("null")
  @PostConstruct
  public void init() {
    videoFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 1000000, 0.01);
  }

  /**
   * 添加视频URL到布隆过滤器
   *
   * @param videoUrl 视频URL
   */
  @SuppressWarnings("null")
  public void addVideoUrl(String videoUrl) {
    videoFilter.put(videoUrl);
  }

  /**
   * 检查视频URL是否存在
   *
   * @param videoUrl 视频URL
   * @return 如果视频URL不存在，则返回true；否则返回false
   */
  @SuppressWarnings("null")
  public boolean checkVideoExists(String videoUrl) {
    return videoFilter.mightContain(videoUrl);
  }
}
