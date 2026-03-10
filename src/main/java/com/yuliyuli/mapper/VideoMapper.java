package com.yuliyuli.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.Video;
import com.yuliyuli.entity.VideoLike;

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

    /**
     * 更新视频点赞数
     * @param videoId 视频ID
     * @param likeCount 点赞数
     * @return 影响行数
     */
    @Update("UPDATE video SET like_count = like_count + #{likeCount} WHERE url = #{url}")
    int updateVideoLikeCount(int likeCount, String url);

    /**
     * 更新视频收藏数
     * @param videoId 视频ID
     * @param collectionCount 收藏数
     * @return 影响行数
     */
    @Update("UPDATE video SET collection_count = collection_count + #{collectionCount} WHERE url = #{url}")
    int updateVideoCollectCount(int collectionCount, String url);

    /**
     * 更新视频评论数
     * @param videoId 视频ID
     * @param commentCount 评论数
     * @return 影响行数
     */
    @Update("UPDATE video SET comment_count = comment_count + #{commentCount} WHERE url = #{url}")
    int updateVideoCommentCount(int commentCount, String url);

    /**
     * 更新视频播放数
     * @param videoId 视频ID
     * @param playCount 播放数
     * @return 影响行数
     */
    @Update("UPDATE video SET play_count = play_count + #{playCount} WHERE url = #{url}")
    int updateVideoPlayCount(int playCount, String url);

    /**
     * 删除视频点赞
     * @param videoId 视频ID
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM video_like WHERE video_id = #{videoId} AND user_id = #{userId}")
    int deleteVideoLike(Long videoId, Long userId);
}
