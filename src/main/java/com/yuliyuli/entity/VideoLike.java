package com.yuliyuli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Date;
import lombok.Data;

@Data
@TableName("video_like")
public class VideoLike {
  @TableId(type = IdType.AUTO)
  private Long id;

  @Parameter(name = "视频id")
  private String videoId;

  @Parameter(name = "用户id")
  private Long userId;

  @Parameter(name = "点赞时间")
  private Date createTime;
}
