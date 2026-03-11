package com.yuliyuli.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;

@Data
public class UpdateUserInfoVO {

  @Schema(description = "用户性别")
  private short gender;

  @Schema(description = "用户生日")
  private Date birthday;

  @Schema(description = "用户个人签名")
  private String sign;
}
