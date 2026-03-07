package com.yuliyuli.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

import javax.validation.constraints.Size;

@Data
@TableName("video")
public class Video {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @NotBlank(message = "视频用户id不能为空")
    @Parameter(name = "视频所属用户id")
    private Long userId;

    @Size(min = 10, max = 100, message = "视频标题长度必须在10到100之间")
    @Parameter(name = "视频标题")
    private String title;
    
    @Parameter(name = "视频简介")
    private String intro;
    
    @NotBlank(message = "请上传视频")
    @Parameter(name = "视频url")
    private String url;
    
    @NotBlank(message = "请上传视频封面")
    @Parameter(name = "视频封面")
    private String cover;
    
    @Parameter(name = "视频类型id")
    private int typeId;
    
    @Parameter(name = "播放次数")
    private int playCount;
    
    @Parameter(name = "点赞次数")
    private int likeCount;
    
    @Parameter(name = "收藏次数")
    private int collectionCount;
    
    @Parameter(name = "创建时间")
    private Date createTime;
    
    @Parameter(name = "更新时间")
    private Date updateTime;
    
    @Parameter(name = "是否被删除")
    private short isDelete;

    @Parameter(name = "作者昵称")
    private String authorName;

    @Parameter(name = "评论数量")
    private int commentCount;
}
