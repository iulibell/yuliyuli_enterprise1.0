package com.yuliyuli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;
import lombok.Data;

@Data
@TableName("follow")
public class Follow {
  @TableId(type = IdType.AUTO)
  private Long id;

  @Parameter(name = "关注用户id")
  @NotBlank(message = "关注用户id不能为空")
  private Long followUserId;

  @Parameter(name = "粉丝用户id")
  @NotBlank(message = "粉丝用户id不能为空")
  private Long fanUserId;

  @Parameter(name = "关注时间")
  private Date createTime;
}
