package com.job.vo;

import com.job.dto.User;
import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private User user;
}
