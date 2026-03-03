package com.yuliyuli.service;

import com.yuliyuli.dto.VideoCollection;
import com.yuliyuli.dto.VideoDelivery;
import com.yuliyuli.dto.VideoLike;

public interface VideoService {
    /**
     * 插入视频
     * @param video 视频对象
     */
    void videoDiliver(VideoDelivery videoDilivery);
    
    /**
     * 视频点赞
     * @param videoLike 视频点赞对象
     */
    void videoLike(VideoLike videoLike);

    /**
     * 视频收藏
     * @param videoCollect 视频收藏对象
     */
    void videoCollect(VideoCollection videoCollect);
}
