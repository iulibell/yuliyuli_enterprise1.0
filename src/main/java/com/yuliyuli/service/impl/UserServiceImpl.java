package com.yuliyuli.service.impl;

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
import com.yuliyuli.vo.LoginVO;
import com.yuliyuli.wrapper.UserWrapper;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

  @Resource private UserMapper userMapper;

  @Resource private UserInfoMapper userInfoMapper;

  @Resource private PasswordEncoder passwordEncoder;

  @Resource private ExistPhoneMapper existPhoneMapper;

  @Resource private RedisTemplate<String, Object> redisTemplate;

  @Resource private UserWrapper userWrapper;

  @Resource private JwtUtil jwtUtil;

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
    // 参数校验
    if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
      return null;
    }
    if (password.length() < 8 || password.length() > 16) {
      return null;
    }
    LambdaQueryWrapper<User> queryWrapper = userWrapper.buildUserByAccount(account);
    User user = userMapper.selectOne(queryWrapper);
    if (user == null) {
      log.error("账号不存在,account: {}", account);
      return null;
    }
    if (!passwordEncoder.matches(password, user.getPassword())) {
      log.error("密码错误,account: {}", account);
      return null;
    }
    try {
      String token = jwtUtil.generateToken(user.getUserId());
      log.info("登录成功,token: {}", token);
      String redisKey = LOGIN_TOKEN_PREFIX + token;
      // 保存用户信息到 Redis
      redisTemplate.opsForValue().set(redisKey, user, 1, TimeUnit.HOURS);
      LoginVO loginVO = new LoginVO(token, user);
      return loginVO;
    } catch (Exception e) {
      log.error("生成token异常", e);
      return null;
    }
  }

  @Override
  public String getCode(String phone) {
    // 参数校验
    if (!StringUtils.hasText(phone) || phone.length() != 11) {
      return "请输入有效的11位手机号";
    }
    LambdaQueryWrapper<ExistPhone> queryWrapper = userWrapper.buildUserByPhone(phone);
    ExistPhone existPhone = existPhoneMapper.selectOne(queryWrapper);
    if (existPhone == null) {
      log.error("手机号不存在,phone: {}", phone);
      return "手机号不存在";
    }
    username = existPhone.getUsername();
    // 3. 生成6位随机验证码
    try {
      String code = String.valueOf((int) (Math.random() * 900000 + 100000));
      String redisKey = SMS_CODE_PREFIX + phone;
    // 4. 保存验证码到Redis（替换内存存储，解决多线程问题）
    redisTemplate.opsForValue().set(redisKey, code, SMS_CODE_EXPIRE, TimeUnit.MINUTES);
    System.out.println("验证码：" + code);
    log.info("手机号{}生成验证码：{}，有效期{}分钟", phone, code, SMS_CODE_EXPIRE);
    return "验证码发送成功!";
    } catch (Exception e) {
      log.error("生成验证码异常", e);
      return "获取验证码失败,请稍后重试";
    }
  }

  @Override
  public String register(String phone, String code, String password) {
    // 1. 参数校验
    if (!StringUtils.hasText(phone)
        || !StringUtils.hasText(code)
        || !StringUtils.hasText(password)) {
      return "手机号、验证码、密码不能为空";
    }
    if (password.length() < 6 || password.length() > 12) {
      return "密码长度必须大于等于6位且小于等于12位";
    }
    if (!phone.matches("^1[3-9]\\d{9}$")) {
      return "请输入有效的11位手机号";
    }

    // 2. 从Redis获取验证码并校验
    String redisKey = SMS_CODE_PREFIX + phone;
    String cacheCode = (String) redisTemplate.opsForValue().get(redisKey);
    if (cacheCode == null) {
      return "验证码已过期，请重新获取";
    }
    if (!code.equals(cacheCode)) {
      return "验证码错误!";
    }

    try {
      // 3. 生成用户ID,用雪花算法/数据库自增策略
      // 此处临时用时间戳+随机数，实际项目建议用雪花算法生成ID
      Long userId = System.currentTimeMillis() + (long) (Math.random() * 1000);

      // 4. 创建用户
      User user = new User();
      user.setUserId(userId);
      user.setPhone(phone);
      user.setPassword(passwordEncoder.encode(password));
      // 从ExistPhone获取用户名
      LambdaQueryWrapper<ExistPhone> phoneWrapper = userWrapper.buildUserByPhone(phone);
      ExistPhone existPhone = existPhoneMapper.selectOne(phoneWrapper);
      user.setUsername(existPhone.getUsername());

      userMapper.insert(user);
      log.info("用户{}注册成功:用户ID:{}", phone, userId);

      // 5. 创建用户信息
      UserInfo userInfo = new UserInfo();
      userInfo.setUserId(userId);
      userInfoMapper.insert(userInfo);

      // 6. 注册成功后删除验证码
      redisTemplate.delete(redisKey);

      return "注册成功!";
    } catch (Exception e) {
      log.error("用户{}注册失败", phone, e);
      throw new GlobalExceptionHandler.BusinessException("注册失败：" + e.getMessage());
    }
  }

  @Override
  public String modifyInfo(short gender, Date birthday, String sign) {
    // 从 UserHolder 获取当前登录用户
    User user = UserHolder.getUser();
    if (user == null) {
      return "请先完成登录!";
    }
    Long userId = user.getUserId();
    try {
      userWrapper.buildUpdateUserInfoByUserId(userId, gender, birthday, sign);
      return "修改成功!";
    } catch (Exception e) {
      log.error("用户{}修改信息失败", userId, e);
      throw new GlobalExceptionHandler.BusinessException("修改信息失败：" + e.getMessage());
    }
  }
}
