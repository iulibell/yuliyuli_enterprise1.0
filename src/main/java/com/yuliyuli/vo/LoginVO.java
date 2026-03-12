package com.yuliyuli.vo;

import com.yuliyuli.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {
  @Schema(description = "登录凭证")
  private String token;

  @Schema(description = "用户信息")
  private User user;
}
