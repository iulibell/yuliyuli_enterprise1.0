package com.yuliyuli.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;

import java.util.Date;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("video_id")
    @Parameter(name = "评论所属视频id")
    private Long videoId;
    
    @TableField("user_id")
    @Parameter(name = "评论用户id")
    private Long userId;

    @TableField("content")
    @NotBlank(message = "评论内容不能为空")
    @Parameter(name = "评论内容")
    private String content;
    
    @TableField("parent_id")
    @Parameter(name = "父评论id")
    private Long parentId;
    
    @TableField("create_time")
    @Parameter(name = "评论时间")
    private Date createTime;
    
    @TableField("is_delete")
    @Parameter(name = "是否被删除,0-未删除,1-已删除")
    private short isDelete;

    @TableField("comment_id")
    @Parameter(name = "评论id")
    private Long commentId;
}
