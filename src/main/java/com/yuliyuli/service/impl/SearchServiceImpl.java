package com.yuliyuli.service.impl;

import com.yuliyuli.document.VideoDocument;
import com.yuliyuli.init.SearchVideoInit;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.repository.VideoRepository;
import com.yuliyuli.service.SearchService;
import com.yuliyuli.util.VideoConvertUtil;
import com.yuliyuli.vo.SearchVideoVO;
import com.yuliyuli.wrapper.VideoWrapper;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

  @Resource private VideoRepository videoRepository;

  @Resource private VideoMapper videoMapper;

  @Resource private VideoWrapper videoWrapper;

  @Resource private RedisTemplate<String, VideoDocument> redisTemplate;

  public List<SearchVideoVO> getTopTenVideo() {
    try {
      List<VideoDocument> topTenVideo =
          redisTemplate.opsForList().range(SearchVideoInit.HOT_TOP_KEY, 0, 9);
      List<SearchVideoVO> topTenVOList =
          VideoConvertUtil.convertVideoDocumentListToSearchVideoVOList(topTenVideo);
      return topTenVOList;
    } catch (Exception e) {
      log.error("获取热门视频失败", e);
      throw new IllegalArgumentException("获取热门视频失败");
    }
  }

  @Override
  public List<SearchVideoVO> findByTitleSuggest(String title) {
    List<VideoDocument> videoDocuments = null;
    try {
      videoDocuments =
          videoRepository.findByTitleSuggest(title, PageRequest.of(0, 10)).getContent();
      List<SearchVideoVO> videoVOList =
          VideoConvertUtil.convertVideoDocumentListToSearchVideoVOList(videoDocuments);
      return videoVOList;
    } catch (Exception e) {
      log.error("根据标题建议查询视频失败", e);
      throw new IllegalArgumentException("根据标题建议查询视频失败");
    }
  }
}
