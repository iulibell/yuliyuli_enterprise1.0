package com.yuliyuli.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Date;

@Data
@TableName("video_like")
public class VideoLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Parameter(name = "视频id")
    private Long videoId;
    
    @Parameter(name = "用户id")
    private Long userId;
    
    @Parameter(name = "点赞时间")
    private Date createTime;
}
