package com.job.dto;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Date;

@Data
@TableName("video_collection")
public class VideoCollection {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Parameter(name = "视频id")
    private Long videoId;
    
    @Parameter(name = "用户id")
    private Long userId;
    
    @Parameter(name = "收藏时间")
    private Date createTime;
}
