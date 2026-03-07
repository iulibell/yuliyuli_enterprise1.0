package com.yuliyuli.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.Comment;

public interface CommentMapper extends BaseMapper<Comment>{
    @Insert("INSERT INTO comment (#{video_id}, #{user_id}, #{content}, #{parent_id}, #{create_time}, #{is_deleted})"
    + "VALUES (#{video_id}, #{user_id}, #{content}, #{parent_id}, #{create_time}, #{is_deleted})")
    int insertComment(Comment comment);

    /**
     * 更新视频评论数
     * @param videoId 视频ID
     * @param commentCount 评论数
     * @return 影响行数
     */
    @Update("UPDATE video SET comment_count = #{commentCount} + 1 WHERE url = #{url}")
    int updateVideoCommentCount(int commentCount, String url);
}
