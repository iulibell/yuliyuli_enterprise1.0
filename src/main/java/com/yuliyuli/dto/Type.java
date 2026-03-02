package com.job.dto;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.Parameter;

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
