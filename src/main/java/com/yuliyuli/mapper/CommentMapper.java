package com.yuliyuli.mapper;

import org.apache.ibatis.annotations.Insert;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.Comment;

public interface CommentMapper extends BaseMapper<Comment>{
    @Insert("INSERT INTO comment (`video_id`, `user_id`, `content`, `parent_id`, `create_time`, `is_deleted`)"
    + "VALUES (#{video_id}, #{user_id}, #{content}, #{parent_id}, #{create_time}, #{is_deleted})")
    int insertComment(Comment comment);

}
