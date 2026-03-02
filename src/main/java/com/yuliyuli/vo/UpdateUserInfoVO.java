package com.yuliyuli.vo;

import java.util.Date;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UpdateUserInfoVO {

    @Parameter(name = "性别")
    @Pattern(regexp = "^[012]$", message = "性别只能是0(保密)、1(男)、2(女)")
    private short gender;

    @Parameter(name = "生日")
    @Past(message = "生日不许超过当前日期")
    private Date birthday;

    @Parameter(name = "签名")
    @Size(min = 10, max = 100, message = "签名长度必须在10到100之间")
    private String sign;
}
