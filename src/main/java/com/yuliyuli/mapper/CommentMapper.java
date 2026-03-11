package com.yuliyuli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.Comment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

public interface CommentMapper extends BaseMapper<Comment> {
  @Insert(
      "INSERT INTO comment (`video_id`, `user_id`, `content`, `parent_id`, `create_time`, `is_deleted`)"
          + "VALUES (#{video_id}, #{user_id}, #{content}, #{parent_id}, #{create_time}, #{is_deleted})")
  int insertComment(Comment comment);

  @Delete("DELETE FROM comment WHERE video_id = #{videoId}")
  int deleteComment(String videoId);
}
