package com.job.excrption;

import lombok.extern.slf4j.Slf4j;

import com.job.common.Result;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理参数校验异常(@Validated失败)
     **/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数校验异常: {}", e.getMessage());
        return Result.fail(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * 处理业务异常
     **/
    @ExceptionHandler(Exception.class)
    public Result<?> handleBusinessException(Exception e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    /**
     * 处理空指针异常
     **/
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }
}
