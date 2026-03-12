package com.yuliyuli.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// 统一返回结果类
@Data
public class Result<T> {

  @Schema(description = "响应状态码:200-成功,400-参数错误," + "401-未登录/Token过期,403-无权限,500-服务器内部错误")
  private Integer code;

  @Schema(description = "响应提示信息:成功返回success,失败返回具体错误信息")
  private String msg;

  @Schema(description = "响应数据:成功返回业务数据,失败返回null")
  private T data;

  /** 私有构造方法：禁止外部直接实例化，统一通过静态工厂方法创建 */
  private Result(Integer code, String msg, T data) {
    this.code = code;
    this.msg = msg;
    this.data = data;
  }

  // ======================== 通用成功返回方法 ========================
  /**
   * 成功返回（带数据）
   *
   * @param data 业务数据
   * @param <T> 数据类型
   * @return 统一返回结果
   */
  public static <T> Result<T> success(T data) {
    return new Result<>(200, "success", data);
  }

  /**
   * 成功返回（无数据，仅提示成功） 适用于新增/修改/删除等不需要返回数据的接口
   *
   * @param <T> 数据类型
   * @return 统一返回结果
   */
  public static <T> Result<T> success() {
    return new Result<>(200, "success", null);
  }

  /**
   * 成功返回（自定义提示语+数据） 适用于需要自定义成功提示的场景（如"修改成功"、"删除成功"）
   *
   * @param msg 自定义提示语
   * @param data 业务数据
   * @param <T> 数据类型
   * @return 统一返回结果
   */
  public static <T> Result<T> success(String msg, T data) {
    return new Result<>(200, msg, data);
  }

  // ======================== 通用失败返回方法 ========================
  /**
   * 失败返回（默认400状态码+自定义提示语） 适用于参数错误、业务逻辑失败等场景
   *
   * @param msg 错误提示语
   * @param <T> 数据类型
   * @return 统一返回结果
   */
  public static <T> Result<T> fail(String msg) {
    return new Result<>(400, msg, null);
  }

  /**
   * 失败返回（自定义状态码+提示语） 适用于401(未登录)、403(无权限)、500(服务器错误)等特殊异常场景
   *
   * @param code 自定义状态码
   * @param msg 错误提示语
   * @param <T> 数据类型
   * @return 统一返回结果
   */
  public static <T> Result<T> fail(Integer code, String msg) {
    return new Result<>(code, msg, null);
  }

  // ======================== 常用快捷失败方法（可选） ========================
  /**
   * 未登录/Token过期返回（401状态码）
   *
   * @param <T> 数据类型
   * @return 统一返回结果
   */
  public static <T> Result<T> unAuthorized(String msg) {
    return new Result<>(401, msg, null);
  }

  /**
   * 无权限返回（403状态码）
   *
   * @param <T> 数据类型
   * @return 统一返回结果
   */
  public static <T> Result<T> forbidden(String msg) {
    return new Result<>(403, msg, null);
  }

  // ======================== 常用快捷失败方法（可选） ========================
  /**
   * 服务器内部错误返回（500状态码）
   *
   * @param <T> 数据类型
   * @return 统一返回结果
   */
  public static <T> Result<T> serverError(String msg) {
    return new Result<>(500, msg, null);
  }
}
