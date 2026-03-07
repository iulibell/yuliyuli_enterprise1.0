package com.yuliyuli.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.entity.Comment;
import com.yuliyuli.entity.VideoCollection;
import com.yuliyuli.entity.VideoDelivery;
import com.yuliyuli.entity.VideoLike;
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
}
