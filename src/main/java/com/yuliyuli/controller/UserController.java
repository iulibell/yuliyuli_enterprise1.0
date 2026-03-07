package com.yuliyuli.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yuliyuli.common.Result;
import com.yuliyuli.entity.ExistPhone;
import com.yuliyuli.entity.User;
import com.yuliyuli.entity.UserInfo;
import com.yuliyuli.service.UserService;
import com.yuliyuli.util.JwtUtil;
import com.yuliyuli.vo.LoginVO;
import com.yuliyuli.vo.UpdateUserInfoVO;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Api(tags = "用户模块")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录接口
     * @param loginUser 登录参数（账号+密码）
     * @return 登录结果（Token+用户信息）
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Object> login(@ApiParam(value = "登录参数（账号+密码）", required = true) 
                                @Validated @RequestBody User loginDto) {
        log.info("【用户登录】账号：{}", loginDto.getAccount());
        //查询用户账号
        LoginVO loginVO = userService.login(loginDto.getAccount(), loginDto.getPassword());
        if (loginVO == null) {
            return Result.fail("账号或密码错误!");
        }

        // 生成token
        String token = jwtUtil.generateToken(loginVO.getUser().getUserId());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("user", loginVO.getUser());
        return Result.success(map);
    }

    /**
     * 校验模块
     * @param existPhoneDto 校验参数（手机号）
     * @return 校验结果（验证码）
     */
    @ApiOperation("校验模块")
    @PostMapping("/getCode")
    public Result<Object> check(@ApiParam(value = "校验参数（手机号）", required = true) 
                                @Validated @RequestBody ExistPhone existPhoneDto) {
        log.info("【校验模块】手机号：{}", existPhoneDto.getPhone());
        // 校验用户
        String code = userService.getCode(existPhoneDto.getPhone());
        if (code == null) {
            return Result.fail("手机号不存在!");
        }
        return Result.success(code);
    }
    
    /**
     * 注册模块,已获取验证码的情况下,用户注册
     * @param registerDto 注册参数（账号+验证码+密码）
     * @param code 校验参数（验证码）
     * @return 注册结果（用户信息）
     */
    @ApiOperation("注册模块")
    @PostMapping("/register")
    public Result<Object> register(@ApiParam(value = "注册参数（账号+验证码+密码）", required = true) 
                                @Validated @RequestBody User registerDto, 
                                @ApiParam(value = "校验参数（验证码）", required = true) String code) {
        // 注册用户
        User user = userService.register(registerDto.getAccount(), code, registerDto.getPassword());
        if (user == null) {
            return Result.fail("验证码错误!");
        }
        return Result.success(user);
    }

    /**
     * 修改模块,用户修改个人信息
     * @param userInfoDto 修改参数（性别+生日+签名）
     * @return 修改结果（用户信息）
     */
    @ApiOperation("修改模块")
    @PostMapping("/modifyInfo")
    public Result<Object> modifyInfo(@ApiParam(value = "修改参数（性别+生日+签名）", required = true) 
                                @Validated @RequestBody UserInfo userInfoDto) {
        // 修改用户信息
        UpdateUserInfoVO updateUserInfoVO = userService.modifyInfo(
            userInfoDto.getGender(),
            userInfoDto.getBirthday(),
            userInfoDto.getSign());
        if (updateUserInfoVO == null) {
            return Result.fail("修改失败!");
        }
        return Result.success(updateUserInfoVO);
    }
}
