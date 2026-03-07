package com.yuliyuli.vo;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
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
