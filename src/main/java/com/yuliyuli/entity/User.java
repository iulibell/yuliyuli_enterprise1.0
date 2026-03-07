package com.yuliyuli.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2到20之间")
    @Parameter(name = "用户名")
    private String username;

    @Parameter(name = "用户ID")
    private Long userId;
    
    @Parameter(name = "账号")
    @NotBlank(message = "账号不能为空")
    @Size(min = 10, max = 10, message = "账号长度必须为10位")
    private String account;
    
    @Parameter(name = "密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 16, message = "密码长度必须在8到12之间")
    private String password;
    
    @Parameter(name = "昵称")
    private String nickname;
    
    @Parameter(name = "头像")
    private String avatar;
    
    @Parameter(name = "创建时间")
    private Date createTime;
    
    @Parameter(name = "更新时间")
    private Date updateTime;
}
