package com.yuliyuli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Data;

@Data
@TableName("user")
public class User {
  @TableId(type = IdType.AUTO)
  private Long id;

  @Size(min = 2, max = 50, message = "用户名长度必须在2到20之间")
  @Parameter(name = "用户名")
  private String username;

  @Parameter(name = "用户ID")
  private Long userId;

  @Parameter(name = "账号")
  @NotBlank(message = "账号不能为空")
  @Size(min = 11, max = 11, message = "账号长度必须为11位")
  // b站手机号和账号是共用的，所以这里用phone表示
  private String phone;

  @Parameter(name = "密码")
  @NotBlank(message = "密码不能为空")
  @Size(min = 8, max = 16, message = "密码长度必须在8到12之间")
  private String password;

  @Parameter(name = "昵称")
  private String nickname;

  @Parameter(name = "头像")
  private String avatar;

  @Parameter(name = "创建时间")
  private Date createTime;

  @Parameter(name = "更新时间")
  private Date updateTime;

  @Parameter(name = "关注数量")
  private Long followCount;

  @Parameter(name = "粉丝数量")
  private Long fansCount;
}
