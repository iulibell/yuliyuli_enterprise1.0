package com.job.dto;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Date;

@Data
@TableName("user_info")
public class UserInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Parameter(name = "用户id")
    private Long userId;
    
    @Parameter(name = "性别")
    private short gender;
    
    @Parameter(name = "生日")
    private Date birthday;
    
    @Parameter(name = "签名")
    private String sign;
}
