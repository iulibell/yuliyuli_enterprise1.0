package com.yuliyuli.vo;

import java.sql.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// 视频VO类
@Data
public class VideoVO {
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "视频标题")
    private String title;
    @Schema(description = "视频简介")
    private String intro;
    @Schema(description = "视频URL")
    private String url;
    @Schema(description = "视频封面")
    private String cover;
    @Schema(description = "视频类型ID")
    private Long typeId;
    @Schema(description = "视频播放量")
    private int playCount;  
    @Schema(description = "视频点赞量")
    private int likeCount;
    @Schema(description = "视频收藏量")
    private int collectionCount;
    @Schema(description = "视频创建时间")
    private Date createTime;
    @Schema(description = "视频作者名称")   
    private String authorName;
    @Schema(description = "视频评论量")
    private int commentCount;
}
