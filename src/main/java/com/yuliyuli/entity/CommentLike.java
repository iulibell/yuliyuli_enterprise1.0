package com.yuliyuli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Date;
import lombok.Data;

@Data
@TableName("comment_like")
public class CommentLike {
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @Parameter(name = "评论id")
  private Long commentId;

  @Parameter(name = "用户id")
  private Long userId;

  @Parameter(name = "创建时间")
  private Date createTime;
}
