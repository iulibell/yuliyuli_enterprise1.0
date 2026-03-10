package com.yuliyuli.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.entity.Comment;
import com.yuliyuli.entity.VideoCollection;
import com.yuliyuli.entity.VideoDelivery;
import com.yuliyuli.entity.VideoLike;
import com.yuliyuli.vo.HotRecommendVideoVO;
import com.yuliyuli.vo.SearchVideoVO;
import com.yuliyuli.vo.VideoVO;

public interface VideoService {
    /**
     * 插入视频
     * @param video 视频对象
     */
    void videoDeliver(VideoDelivery videoDelivery);
    
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

    /**
     * 视频评论
     * @param comment 视频评论对象
     */
    void videoComment(Comment comment);

    /**
     * 获取视频列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 视频列表
     */
    Page<VideoVO> getVideoList(int pageNum, int pageSize);

    /**
     * 用户点击搜索后根据传过来的标题来返回一堆相关的视频
     * @param title 视频标题
     * @return 视频详情
     */
    Page<SearchVideoVO> getSearchVideoResults(String title);

    /**
     * 用户点击视频后打开视频详细页后来返回推荐热门视频
     * @return 推荐热门视频，即右边的视频栏
     */
    List<HotRecommendVideoVO> getRecommendHotVideo();

    /**
     * 用户点击热门视频后播放视频，发送至消费者进行播放统计
     * @param videoUrl 视频URL
     */
    void hotVideoPlay(String videoUrl);

    /**
     * 用户点击普通视频播放视频，发送至消费者进行播放统计
     * @param videoUrl 视频URL
     */
    void videoPlay(String videoUrl);
}
