package com.job.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    /**
     * 成功时调用
     * @param data
     * @return
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }
    
    /**
     * 失败时调用
     * @param msg
     * @return
     */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(400, msg, null);
    }
}
