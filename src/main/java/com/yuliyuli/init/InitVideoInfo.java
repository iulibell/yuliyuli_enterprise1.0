package com.yuliyuli.init;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.entity.Video;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.util.VideoConvertUtil;
import com.yuliyuli.vo.VideoVO;
import com.yuliyuli.wrapper.VideoWrapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Component
@Slf4j
public class InitVideoInfo {

    @Resource
    private VideoWrapper videoWrapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private RedissonClient redissonClient;


    public static final int PAGE_SIZE = 20;
    public static final int EXPIRE_TIME = 24;

    public static final String VIDEO_CACHE_PREFIX = "video:info:";
    public static final String VIDEO_LIST_CACHE_KEY = "video:info:list";

    /**
     * 初始化视频信息
     */
    @PostConstruct
    public void init(){
        log.info("初始化视频信息");
        asyncInitVideoInfo();
    }

    @Async("asyncThreadPoolExecutor")
    public CompletableFuture<Void> asyncInitVideoInfo(){
        return CompletableFuture.runAsync(() -> {
            try{
                // 初始化当前页码
                int currentPage = 1;
                while(true){
                    Page<Video> videoPage = videoMapper.selectPage(
                        new Page<>(currentPage, PAGE_SIZE),
                        videoWrapper.getInitVideo()
                    );
                    List<Video> videos = videoPage.getRecords();
                    if(videos.isEmpty()){
                        break;
                    }
                    cacheVideosToRedis(videos, currentPage);
                    currentPage++;
                    if(!videoPage.hasNext()){
                        break;
                    }
                }
            }catch(Exception e){
                log.error("异步初始化视频信息失败",e);
                throw new RuntimeException("异步初始化视频信息失败",e);
            }
        });
    }

    private void cacheVideosToRedis(List<Video> videos, int pageNum){
        for(Video video: videos){
            VideoVO videoVO = VideoConvertUtil.convertToVideoVO(video);
            String videoKey = VIDEO_CACHE_PREFIX + videoVO.getUrl();
            RBucket<VideoVO> videoBucket = redissonClient.getBucket(videoKey);
            videoBucket.set(videoVO, EXPIRE_TIME, TimeUnit.HOURS);
        }
        String listKey = VIDEO_LIST_CACHE_KEY + pageNum;
        RBucket<List<VideoVO>> listBucket = redissonClient.getBucket(listKey);
        listBucket.set(videos
            .stream()
            .map(VideoConvertUtil::convertToVideoVO).toList(), 
            EXPIRE_TIME, TimeUnit.HOURS);
    }
}
