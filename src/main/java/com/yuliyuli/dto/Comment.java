package com.yuliyuli.dto;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.Parameter;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Parameter(name = "评论所属视频id")
    private Long videoId;
    
    @Parameter(name = "评论用户id")
    private Long userId;

    @Parameter(name = "评论内容")
    private String content;

    @Parameter(name = "父评论id")
    private Long parentId;

    @Parameter(name = "评论时间")
    private Date createTime;

    @Parameter(name = "是否被删除")
    private short isDelete;
}
