package com.yuliyuli.wrapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuliyuli.entity.Video;

public interface VideoWrapper {

    /**
     * 根据视频url构造查询条件（用于查询视频的评论数）
     * @param url 视频url
     * @return LambdaQueryWrapper<Video>获取视频的评论数
     */
    default LambdaQueryWrapper<Video> getCommentCount(String url){
        return new LambdaQueryWrapper<Video>().eq(Video::getCommentCount, url);
    }

    /**
     * 初始化查询视频,只isDelete=0的视频
     * @return LambdaQueryWrapper<Video>初始化查询视频
     */
    default LambdaQueryWrapper<Video> getInitVideo(){
        return new LambdaQueryWrapper<Video>().eq(Video::getIsDelete,0);
    }
}
