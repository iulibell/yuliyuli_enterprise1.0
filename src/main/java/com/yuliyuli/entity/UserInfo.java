package com.yuliyuli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;

@Data
@TableName("user_info")
public class UserInfo {
  @TableId(type = IdType.AUTO)
  private Long id;

  @Parameter(name = "用户id")
  private Long userId;

  @Parameter(name = "性别")
  @Schema(description = "0未知,1男,2女")
  private short gender;

  @Parameter(name = "生日")
  private Date birthday;

  @Parameter(name = "签名")
  private String sign;
}
