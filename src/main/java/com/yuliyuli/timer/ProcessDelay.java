package com.yuliyuli.timer;

import java.util.Collection;
import java.util.stream.Collectors;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yuliyuli.entity.VideoLike;
import com.yuliyuli.mapper.VideoMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProcessDelay {
    
    @Resource
    private RedissonClient redissonClient;
    
    @Resource
    private VideoMapper videoMapper;
    
    // 处理延时点赞
    @Scheduled(fixedRate = 1000) // 每秒检查一次
    public void processDelayLikes() {
        String delayKey = "video:like:delay";
        long currentTime = System.currentTimeMillis();
        
        // 获取所有到时间的点赞操作
        RScoredSortedSet<VideoLike> sortedSet = redissonClient.getScoredSortedSet(delayKey);
        Collection<VideoLike> expiredLikes = sortedSet.entryRange(0, true, 
                currentTime, true).stream()
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
            log.info("延时处理点赞：用户{}，视频{}，点赞数{}", videoLike.getUserId(), videoLike.getVideoId(), finallyCount);
        } catch (Exception e) {
            log.error("处理延时点赞失败: {}", videoLike, e);
        }
    }

    // 处理延时热门视频播放
    @Scheduled(fixedRate = 15000) // 每15秒检查一次
    public void processDelayHotPlay() {
        String delayKey = "hot:video:play:delay";
        long currentTime = System.currentTimeMillis();
        // 从有序集合获取所有到期的 videoUrl
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(delayKey);
        Collection<String> expiredVideoUrls = sortedSet.entryRange(0, true, currentTime, true)
                .stream()
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
            RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
            Long finallyCount = counter.incrementAndGet();
            videoMapper.updateVideoPlayCount(finallyCount.intValue(), videoUrl);
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
        Collection<String> expiredVideoUrls = sortedSet.entryRange(0, true, currentTime, true)
                .stream()
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
            RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
            Long finallyCount = counter.incrementAndGet();
            videoMapper.updateVideoPlayCount(finallyCount.intValue(), videoUrl);
        } catch (Exception e) {
            log.error("处理延时视频播放失败: {}", videoUrl, e);
        }
    }
}
