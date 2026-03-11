package com.yuliyuli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
@TableName("exist_phone")
public class ExistPhone {
  @TableId(type = IdType.AUTO)
  @Parameter(name = "主键id")
  private Long id;

  @Parameter(name = "号主")
  private String username;

  @Parameter(name = "手机号")
  private String phone;
}
