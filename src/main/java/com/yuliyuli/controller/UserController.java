package com.yuliyuli.controller;

import com.yuliyuli.common.Result;
import com.yuliyuli.entity.ExistPhone;
import com.yuliyuli.entity.User;
import com.yuliyuli.entity.UserInfo;
import com.yuliyuli.service.UserService;
import com.yuliyuli.util.JwtUtil;
import com.yuliyuli.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * 提供用户登录、校验、注册等接口，校验已在业务层实现
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户模块")
@Slf4j
public class UserController {

  @Resource private UserService userService;

  @Resource private JwtUtil jwtUtil;

  /**
   * 用户登录接口
   *
   * @param loginUser 登录参数（账号+密码）
   * @return 登录结果（Token+用户信息）
   */
  @Operation(summary = "用户登录")
  @PostMapping("/login")
  public Result<Object> login(
      @Parameter(description = "登录参数（账号+密码）", required = true) @Validated @RequestBody
          User loginDto) {
    log.info("【用户登录】手机号：{}", loginDto.getPhone());
    try {
      LoginVO loginVO = userService.login(loginDto.getPhone(), loginDto.getPassword());
      if (loginVO == null) {
        log.info("【用户登录】账号或密码错误!");
        return Result.fail("账号或密码错误!");
      }
      // 生成token
      String token = jwtUtil.generateToken(loginVO.getUser().getUserId());
      Map<String, Object> map = new HashMap<>();
      map.put("token", token);
      map.put("user", loginVO.getUser());
      return Result.success(map);
    } catch (Exception e) {
      log.error("登录异常!", e);
      return Result.fail("登录失败,请稍后重试");
    }
  }

  /**
   * 校验模块
   *
   * @param existPhoneDto 校验参数（手机号）
   * @return 校验结果（验证码）
   */
  @Operation(summary = "校验模块")
  @PostMapping("/getCode")
  public Result<Object> getCode(
      @Parameter(description = "校验参数（手机号）", required = true) @Validated @RequestBody
          ExistPhone existPhone) {
    String phone = existPhone.getPhone();
    log.info("手机号：{}", existPhone.getPhone());
    try {
      String message = userService.getCode(phone);
      log.info("验证码发送成功");
      return Result.success(message);
    } catch (Exception e) {
      log.error("获取验证码异常!", e);
      return Result.fail("获取验证码失败,请稍后重试");
    }
  }

  /**
   * 注册模块,已获取验证码的情况下,用户注册
   *
   * @param registerDto 注册参数（账号+验证码+密码）
   * @param code 校验参数（验证码）
   * @return 注册结果（用户信息）
   */
  @Operation(summary = "注册模块")
  @PostMapping("/register")
  public Result<Object> register(
      @Parameter(description = "注册参数（账号+密码）", required = true) @Validated @RequestBody
          User registerDto,
      @Parameter(description = "校验参数（验证码）", required = true) @RequestParam String code) {
    try {
      String message = userService.register(registerDto.getPhone(), code, registerDto.getPassword());
      if (message == null) {
        return Result.fail("验证码错误!");
      }
      if (message.equals("请输入有效的11位手机号")) {
        return Result.fail(message);
      }
      if (message.equals("密码长度必须大于等于6位且小于等于12位")) {
        return Result.fail(message);
      }
      if (message.equals("手机号、验证码、密码不能为空")) {
        return Result.fail(message);
      }
      return Result.success(message);
    } catch (Exception e) {
      log.error("【注册模块】注册异常!", e);
      return Result.fail("注册失败,请稍后重试");
    }
  }

  /**
   * 修改模块,用户修改个人信息
   *
   * @param userInfoDto 修改参数（性别+生日+签名）
   * @return 修改结果（用户信息）
   */
  @Operation(summary = "修改模块")
  @PostMapping("/modifyInfo")
  public Result<Object> modifyInfo(
      @Parameter(description = "修改参数（性别+生日+签名）", required = true) @Validated @RequestBody
          UserInfo userInfoDto) {
    // 修改用户信息
    try {
      String message =
        userService.modifyInfo(
            userInfoDto.getGender(), userInfoDto.getBirthday(), userInfoDto.getSign());
      if (message == null) {
        return Result.fail("修改失败!");
      }
      return Result.success(message);
    } catch (Exception e) {
      log.error("【修改模块】修改异常!", e);
      return Result.fail("修改失败,请稍后重试");
    }
  }
}
