package com.job.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.job.common.Wrapper;

import com.job.dto.User;
import com.job.dto.UserInfo;
import com.job.dto.ExistPhone;
import com.job.vo.LoginVO;

import com.job.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import com.job.util.JwtUtil;
import com.job.mapper.UserMapper;
import com.job.mapper.ExistPhoneMapper;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ExistPhoneMapper existPhoneMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private JwtUtil jwtUtil;

    // 用于判断输入时的验证码是否正确
    String registerCode = "";
    String username = "";

    public LoginVO login(String account, String password) {
        Wrapper wrapper = new Wrapper();
        LambdaQueryWrapper<User> queryWrapper = wrapper.findAccount(account);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return null;
        }
        String token = jwtUtil.generateToken(user.getUserId());
        String redisKey = "login:token:" + token;
        // 保存用户信息到 Redis
        redisTemplate.opsForValue().set(redisKey, user, 1, TimeUnit.HOURS);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(user);
        return loginVO;
    }

    @Override
    public String getCode(String phone) {
        Wrapper wrapper = new Wrapper();
        LambdaQueryWrapper<ExistPhone> queryWrapper = wrapper.findPhone(phone);
        ExistPhone existPhone = existPhoneMapper.selectOne(queryWrapper);
        if (existPhone == null) {
            return null;
        }
        username = existPhone.getUsername();
        // 生成验证码
        String code = String.valueOf((int) (Math.random() * 900000 + 100000));
        // 保存验证码
        registerCode = code;
        return code;
    }

    @Override
    public User register(String account, String code ,String password) {
        if (!code.equals(registerCode)) {
            return null;
        }
        // 注册用户
        User user = new User();
        Long userId = Long.parseLong(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
        user.setUserId(userId);
        user.setAccount(account);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(username);
        userMapper.insert(user);
        return user;
    }
}
