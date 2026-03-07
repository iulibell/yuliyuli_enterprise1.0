package com.yuliyuli.service.impl;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuliyuli.entity.ExistPhone;
import com.yuliyuli.entity.User;
import com.yuliyuli.entity.UserHolder;
import com.yuliyuli.entity.UserInfo;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.ExistPhoneMapper;
import com.yuliyuli.mapper.UserInfoMapper;
import com.yuliyuli.mapper.UserMapper;
import com.yuliyuli.service.UserService;
import com.yuliyuli.util.JwtUtil;
import com.yuliyuli.wrapper.UserWrapper;

import lombok.extern.slf4j.Slf4j;

import com.yuliyuli.vo.LoginVO;
import com.yuliyuli.vo.UpdateUserInfoVO;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ExistPhoneMapper existPhoneMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserWrapper userWrapper;

    @Autowired
    private JwtUtil jwtUtil;

    // Redis验证码缓存前缀
    private static final String SMS_CODE_PREFIX = "register:code:";
    // 验证码有效期5分钟
    private static final long SMS_CODE_EXPIRE = 1;
    // 用户登录缓存前缀
    private static final String LOGIN_TOKEN_PREFIX = "login:token:";

    // 用于判断输入时的验证码是否正确
    String registerCode = "";
    String username = "";

    public LoginVO login(String account, String password) {
        //参数校验
        if(!StringUtils.hasText(account) || !StringUtils.hasText(password)){
            throw new GlobalExceptionHandler.BusinessException("账号或密码不能为空");
        }
        if(password.length() < 8 || password.length() > 16){
            throw new GlobalExceptionHandler.BusinessException("密码长度必须在8到16之间");
        }
        LambdaQueryWrapper<User> queryWrapper = userWrapper.buildUserByAccount(account);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.error("账号不存在,account: {}", account);
            throw new GlobalExceptionHandler.BusinessException("账号不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("密码错误,account: {}", account);
            throw new GlobalExceptionHandler.BusinessException("密码错误");
        }
        String token = jwtUtil.generateToken(user.getUserId());
        log.info("登录成功,token: {}", token);
        String redisKey = LOGIN_TOKEN_PREFIX + token;
        // 保存用户信息到 Redis
        redisTemplate.opsForValue().set(redisKey, user, 1, TimeUnit.HOURS);
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(user);
        return loginVO;
    }

    @Override
    public String getCode(String phone) {
        //参数校验
        if(!StringUtils.hasText(phone) || phone.length() != 11){
            throw new GlobalExceptionHandler.BusinessException("请输入有效的11位手机号");
        }
        LambdaQueryWrapper<ExistPhone> queryWrapper = userWrapper.buildUserByPhone(phone);
        ExistPhone existPhone = existPhoneMapper.selectOne(queryWrapper);
        if (existPhone == null) {
            log.error("手机号不存在,phone: {}", phone);
            throw new GlobalExceptionHandler.BusinessException("手机号不存在");
        }
        username = existPhone.getUsername();
        // 3. 生成6位随机验证码
        String code = String.valueOf((int) (Math.random() * 900000 + 100000));
        String redisKey = SMS_CODE_PREFIX + phone;

        // 4. 保存验证码到Redis（替换内存存储，解决多线程问题）
        redisTemplate.opsForValue().set(redisKey, code, SMS_CODE_EXPIRE, TimeUnit.MINUTES);
        log.info("手机号{}生成验证码：{}，有效期{}分钟", phone, code, SMS_CODE_EXPIRE);
        return code;
    }

    @Override
    public User register(String account, String code, String password) {
        // 1. 参数校验
        if (!StringUtils.hasText(account) || !StringUtils.hasText(code) || !StringUtils.hasText(password)) {
            throw new GlobalExceptionHandler.BusinessException("账号、验证码、密码不能为空");
        }
        if (password.length() < 6 || password.length() > 12) {
            throw new GlobalExceptionHandler.BusinessException("密码长度必须大于等于6位且小于等于12位");
        }
        if (!account.matches("^1[3-9]\\d{9}$")) {
            throw new GlobalExceptionHandler.BusinessException("请输入有效的11位账号");
        }

        // 2. 从Redis获取验证码并校验
        String redisKey = SMS_CODE_PREFIX + account;
        String cacheCode = (String) redisTemplate.opsForValue().get(redisKey);
        if (cacheCode == null) {
            throw new GlobalExceptionHandler.BusinessException("验证码已过期，请重新获取");
        }
        if (!code.equals(cacheCode)) {
            throw new GlobalExceptionHandler.BusinessException("验证码错误");
        }

        try {
            // 3. 生成用户ID,用雪花算法/数据库自增策略
            // 此处临时用时间戳+随机数，实际项目建议用雪花算法生成ID
            Long userId = System.currentTimeMillis() + (long) (Math.random() * 1000);

            // 4. 创建用户
            User user = new User();
            user.setUserId(userId);
            user.setAccount(account);
            user.setPassword(passwordEncoder.encode(password));
            // 从ExistPhone获取用户名
            LambdaQueryWrapper<ExistPhone> phoneWrapper = userWrapper.buildUserByPhone(account);
            ExistPhone existPhone = existPhoneMapper.selectOne(phoneWrapper);
            user.setUsername(existPhone.getUsername());
            
            userMapper.insert(user);
            log.info("用户{}注册成功，用户ID：{}", account, userId);

            // 5. 创建用户信息
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userId);
            userInfoMapper.insert(userInfo);

            // 6. 注册成功后删除验证码
            redisTemplate.delete(redisKey);

            return user;
        } catch (Exception e) {
            log.error("用户{}注册失败", account, e);
            throw new GlobalExceptionHandler.BusinessException("注册失败：" + e.getMessage());
        }
    }

    @Override
    public UpdateUserInfoVO modifyInfo(short gender, Date birthday, String sign) {
        // 从 UserHolder 获取当前登录用户
        User user = UserHolder.getUser();
        if (user == null) {
            throw new GlobalExceptionHandler.BusinessException("请完成登录");
        }
        Long userId = user.getUserId();
        try{
            userWrapper.buildUpdateUserInfoByUserId(userId, gender, birthday, sign);
            UpdateUserInfoVO updateUserInfoVO = new UpdateUserInfoVO();
            updateUserInfoVO.setGender(gender);
            updateUserInfoVO.setBirthday(birthday);
            updateUserInfoVO.setSign(sign);
            return updateUserInfoVO;
        }catch(Exception e){
            log.error("用户{}修改信息失败", userId, e);
            throw new GlobalExceptionHandler.BusinessException("修改信息失败：" + e.getMessage());
        }
    }
}
