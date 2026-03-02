package com.job.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.job.dto.User;
import com.job.dto.UserInfo;
import com.job.dto.ExistPhone;

public interface Wrapper {
    LambdaQueryWrapper<User> findUsername(String userName);
    LambdaQueryWrapper<User> findAccount(String account);
    LambdaQueryWrapper<ExistPhone> findPhone(String phone);
    LambdaQueryWrapper<UserInfo> findUserInfoByUserId(Long userId); 
}
