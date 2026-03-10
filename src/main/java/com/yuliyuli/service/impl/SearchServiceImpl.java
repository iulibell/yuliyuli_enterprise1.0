package com.yuliyuli.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.yuliyuli.document.VideoDocument;
import com.yuliyuli.init.SearchVideoInit;
import com.yuliyuli.repository.VideoRepository;
import com.yuliyuli.service.SearchService;
import com.yuliyuli.util.VideoConvertUtil;
import com.yuliyuli.vo.SearchVideoVO;

import jakarta.annotation.Resource;

@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private VideoRepository videoRepository;

    @Resource
    private RedisTemplate<String, VideoDocument> redisTemplate;

    public List<SearchVideoVO> getTopTenVideo(){
        List<VideoDocument> topTenVideo = redisTemplate.opsForList().
            range(SearchVideoInit.HOT_TOP_KEY, 0, 9);
        List<SearchVideoVO> topTenVOList = VideoConvertUtil.convertSearchVideoVOList(topTenVideo);
        return topTenVOList;
    }

    @Override
    public List<SearchVideoVO> findByTitleSuggest(String title) {
        List<VideoDocument> videoDocuments = videoRepository.findByTitleSuggest(title, 
            PageRequest.of(0, 10)).getContent();
        List<SearchVideoVO> videoVOList = VideoConvertUtil.convertSearchVideoVOList(videoDocuments);
        return videoVOList;
    }
}
