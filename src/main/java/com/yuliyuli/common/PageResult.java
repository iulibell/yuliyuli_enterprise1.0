package com.yuliyuli.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

// 分页返回结果类
@Data
public class PageResult<T> {

  @Schema(description = "总记录数")
  private Long total;

  @Schema(description = "总页数")
  private int totalPage;

  @Schema(description = "每页记录数")
  private int pageSize;

  @Schema(description = "当前页码")
  private int currentPage;

  @Schema(description = "分页传递数据列表")
  private List<T> list;

  @Schema(description = "是否有下一页")
  private boolean hasNextPage;
}
