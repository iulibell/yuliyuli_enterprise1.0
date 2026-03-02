package com.job.service;

import com.job.dto.User;

public interface UserService {
    /**
     * 登录
     * @param account 账号
     * @param password 密码
     * @return 用户信息
     */
    User login(String account, String password);
    
    /**
     * 注册
     * @param phone 手机号
     * @return 用户信息
     */
    String getCode(String phone);

    /**
     * 注册
     * @param phone 手机号
     * @param code 验证码
     * @return 用户信息
     */
    User register(String account, String code ,String password);
}
