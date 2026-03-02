package com.job.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import com.job.dto.User;
import com.job.dto.ExistPhone;
import com.job.dto.UserInfo;
import com.job.common.Result;
import com.job.service.UserService;
import com.job.util.JwtUtil;
import com.job.vo.LoginVO;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
@Api(tags = "用户模块")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @ApiOperation("登录模块")
    @RequestMapping("/login")
    public Result<Object> login(@Validated @RequestBody User loginDto) {

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

    @ApiOperation("校验模块")
    @RequestMapping("/getCode")
    public Result<Object> check(@Validated @RequestBody ExistPhone existPhoneDto) {
        // 校验用户
        String code = userService.getCode(existPhoneDto.getPhone());
        if (code == null) {
            return Result.fail("手机号不存在!");
        }
        return Result.success(code);
    }

    @ApiOperation("注册模块")
    @RequestMapping("/register")
    public Result<Object> register(@Validated @RequestBody User registerDto, String code) {
        // 注册用户
        User user = userService.register(registerDto.getAccount(), code, registerDto.getPassword());
        if (user == null) {
            return Result.fail("验证码错误!");
        }
        return Result.success(user);
    }

    @ApiOperation("修改模块")
    @RequestMapping("/modifyInfo")
    public Result<Object> modifyInfo(@Validated @RequestBody UserInfo userInfoDto) {
        // 修改用户信息
        UserInfo userInfo = userService.modifyInfo(
            userInfoDto.getGender(),
            userInfoDto.getBirthday(),
            userInfoDto.getSign());
        if (userInfo == null) {
            return Result.fail("修改失败!");
        }
        return Result.success(userInfo);
    }
}
