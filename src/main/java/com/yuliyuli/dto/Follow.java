package com.yuliyuli.dto;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Date;

@Data
@TableName("follow")
public class Follow {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Parameter(name = "关注用户id")
    private Long followUserId;
    
    @Parameter(name = "粉丝用户id")
    private Long fanUserId;
    
    @Parameter(name = "关注时间")
    private Date createTime;
}
