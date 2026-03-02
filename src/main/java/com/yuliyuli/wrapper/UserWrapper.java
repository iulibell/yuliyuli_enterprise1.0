package com.yuliyuli.wrapper;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuliyuli.dto.ExistPhone;

import com.yuliyuli.dto.User;
import com.yuliyuli.dto.UserInfo; 

/**
 * 用户相关查询/更新条件构造器接口
 * 统一封装User、UserInfo的MyBatis-Plus条件，避免业务层重复写Wrapper逻辑
 *
 * @author 你的名字
 * @date 2026-03
 */
public interface UserWrapper {

    /**
     * 根据用户名构造查询条件（用于登录/校验用户名是否存在）
     * @param userName 用户名
     * @return LambdaQueryWrapper<User> 查询条件
     */
    default LambdaQueryWrapper<User> buildUserByUsername(String userName) {
        return new LambdaQueryWrapper<User>()
                .eq(User::getUsername, userName) // 假设User实体有username字段
                .last("LIMIT 1"); // 只查一条，提升性能
    }

    /**
     * 根据账号构造查询条件（账号可等价于用户名/手机号，根据你的业务调整）
     * @param account 账号（用户名/手机号）
     * @return LambdaQueryWrapper<User> 查询条件
     */
    default LambdaQueryWrapper<User> buildUserByAccount(String account) {
        return new LambdaQueryWrapper<User>()
                .eq(User::getAccount, account) // 假设User实体有account字段
                .last("LIMIT 1");
    }

    /**
     * 根据手机号构造查询条件（校验手机号是否存在）
     * @param phone 手机号
     * @return LambdaQueryWrapper<User> 查询条件（统一基于User实体，而非ExistPhone DTO）
     */
    default LambdaQueryWrapper<ExistPhone> buildUserByPhone(String phone) {
        return new LambdaQueryWrapper<ExistPhone>()
                .eq(ExistPhone::getPhone, phone) // 假设ExistPhone实体有phone字段
                .select(ExistPhone::getId) // 只查ID，减少数据传输
                .last("LIMIT 1");
    }

    /**
     * 根据用户ID查询用户详情（UserInfo）
     * @param userId 用户ID
     * @return LambdaQueryWrapper<UserInfo> 查询条件
     */
    default LambdaQueryWrapper<UserInfo> buildUserInfoByUserId(Long userId) {
        return new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserId, userId) // 假设UserInfo关联userId
                .last("LIMIT 1");
    }

    /**
     * 根据用户ID构造用户详情更新条件
     * 核心修复：更新操作返回LambdaUpdateWrapper，而非查询包装器
     * @param userId 用户ID
     * @return LambdaUpdateWrapper<UserInfo> 更新条件
     */
    default LambdaUpdateWrapper<UserInfo> buildUpdateUserInfoByUserId(Long userId, short gender, 
        Date birthday, String sign) {
            return new LambdaUpdateWrapper<UserInfo>()
                .eq(UserInfo::getUserId, userId)
                .set(UserInfo::getGender, gender)
                .set(UserInfo::getBirthday, birthday)
                .set(UserInfo::getSign, sign);
    }
}
