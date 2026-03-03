package com.yuliyuli.mapper;

import org.apache.ibatis.annotations.Insert;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.dto.Video;
import com.yuliyuli.dto.VideoLike;

public interface VideoMapper extends BaseMapper<Video> {

    /**
     * 插入视频
     * @param video 视频信息
     * @return 影响行数
     */
    @Insert("INSERT INTO video (`user_id`,`title`,`intro`,`url`,`cover`,`type_id`)"
    + " VALUES (#{userId},#{title},#{intro},#{url},#{cover},#{typeId})")
    int insertVideo(Video video);

    /**
     * 插入视频点赞
     * @param videoLike 视频点赞信息
     * @return 影响行数
     */
    @Insert("INSERT INTO video_like (`video_id`,`user_id`)"
    + " VALUES (#{videoId},#{userId})")
    int insertVideoLike(VideoLike videoLike);
    
}
