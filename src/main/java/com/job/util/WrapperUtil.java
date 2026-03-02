package com.job.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.job.common.Wrapper;

import com.job.dto.User;
import com.job.dto.UserInfo;
import com.job.dto.ExistPhone;

public class WrapperUtil implements Wrapper {
    
    public LambdaQueryWrapper<User> findUsername(String userName) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        return queryWrapper.eq(User::getUsername, userName);
    }

    public LambdaQueryWrapper<User> findAccount(String account) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        return queryWrapper.eq(User::getAccount, account);
    }

    public LambdaQueryWrapper<ExistPhone> findPhone(String phone) {
        LambdaQueryWrapper<ExistPhone> queryWrapper = new LambdaQueryWrapper<>();
        return queryWrapper.eq(ExistPhone::getPhone, phone);
    }

    public LambdaQueryWrapper<UserInfo> findUserInfoByUserId(Long userId) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        return queryWrapper.eq(UserInfo::getUserId, userId);
    }
}