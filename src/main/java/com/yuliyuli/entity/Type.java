package com.yuliyuli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
@TableName("type")
public class Type {
  @TableId(type = IdType.AUTO)
  private Long id;

  @Parameter(name = "类型名称")
  private String name;

  @Parameter(name = "类型排序")
  private int sort;
}
