package com.job.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.job.dto.User;
import com.job.dto.ExistPhone;

public class Wrapper {
    
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
}