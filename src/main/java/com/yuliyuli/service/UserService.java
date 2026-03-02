package com.job.service;

import java.util.Date;

import com.job.dto.User;
import com.job.dto.UserInfo;
import com.job.vo.LoginVO;

public interface UserService {
    /**
     * 登录
     * @param account 账号
     * @param password 密码
     * @return 用户信息
     */
    LoginVO login(String account, String password);
    
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

    /**
     * 修改信息
     * @param gender 性别
     * @param birthday 生日
     * @param sign 签名
     * @return 用户信息
     */
    UserInfo modifyInfo(short gender, Date birthday,String sign);
}
