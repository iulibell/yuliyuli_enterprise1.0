package com.yuliyuli.vo;

import com.yuliyuli.dto.User;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private User user;
}
