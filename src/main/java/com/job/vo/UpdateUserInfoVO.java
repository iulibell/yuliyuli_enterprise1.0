package com.job.vo;

import java.util.Date;

import lombok.Data;

@Data
public class UpdateUserInfoVO {
    private short gender;
    private Date birthday;
    private String sign;
}
