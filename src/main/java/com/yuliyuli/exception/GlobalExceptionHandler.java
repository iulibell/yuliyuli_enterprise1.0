package com.yuliyuli.exception;

import com.yuliyuli.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  /** 处理参数校验异常(@Validated失败) */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    log.error("参数校验异常: {}", e.getMessage());
    return Result.fail(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
  }

  /** 处理业务异常 */
  @ExceptionHandler(BusinessException.class)
  public Result<?> handleBusinessException(BusinessException e) {
    log.error("业务异常: {}", e.getMessage());
    return Result.fail(e.getMessage());
  }

  /** 处理空指针异常 */
  @ExceptionHandler(NullPointerException.class)
  public Result<?> handleNullPointerException(NullPointerException e) {
    log.error("空指针异常: {}", e.getMessage());
    return Result.fail(e.getMessage());
  }

  /** 处理所有未捕获的通用异常（必须放在最后） */
  @ExceptionHandler(Exception.class)
  public Result<?> handleGlobalException(Exception e) {
    String errorMsg = "系统繁忙，请稍后再试";
    log.error("全局通用异常", e); // 打印完整堆栈
    return Result.fail(errorMsg);
  }

  /** 自定义业务异常类（建议放在同一个包下） */
  public static class BusinessException extends RuntimeException {
    public BusinessException(String message) {
      super(message);
    }

    public BusinessException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
