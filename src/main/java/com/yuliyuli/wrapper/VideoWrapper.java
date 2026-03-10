package com.yuliyuli.wrapper;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuliyuli.entity.Video;

@Component
public class VideoWrapper {

    /**
     * 根据视频url构造查询条件（用于查询视频的评论数）
     * @param url 视频url
     * @return LambdaQueryWrapper<Video>获取视频的评论数
     */
    public LambdaQueryWrapper<Video> getCommentCount(String url){
        return new LambdaQueryWrapper<Video>().eq(Video::getCommentCount, url);
    }

    /**
     * 初始化查询视频,只isDelete=0的视频
     * @return LambdaQueryWrapper<Video>初始化查询视频
     */
    public LambdaQueryWrapper<Video> getInitVideo(){
        return new LambdaQueryWrapper<Video>().eq(Video::getIsDelete,0);
    }

    /**
     * 获取相关视频,根据视频类型id排序
     * @param typeId 视频类型id
     * @return LambdaQueryWrapper<Video>获取相关视频
     */
    public LambdaQueryWrapper<Video> getRelatedVideo(int typeId){
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
            .eq(Video::getIsDelete,0);
        if(typeId!=0){
            wrapper.eq(Video::getTypeId,typeId);
        }
        return wrapper;
    }

    /**
     * 获取作者上传的视频,根据视频id排序
     * @param userId 用户id
     * @return LambdaQueryWrapper<Video>获取作者上传的视频
     */
    public LambdaQueryWrapper<Video> getAuthorPageVideo(Long userId){
        return new LambdaQueryWrapper<Video>()
            .eq(Video::getIsDelete,0)
            .eq(Video::getUserId,userId);
    }
}
