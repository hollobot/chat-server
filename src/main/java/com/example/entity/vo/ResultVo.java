package com.example.entity.vo;

import com.example.entity.enums.ExceptionCodeEnum;
import lombok.Data;

@Data
public class ResultVo<T> {

    private Integer code;
    private String status;
    private String message;
    private T data;

    // 默认构造器
    public ResultVo() {
    }

    // 构造器
    public ResultVo(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResultVo(Integer code, String status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public ResultVo(Integer code, String status, String message, T data) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static ResultVo success() {
        return new ResultVo(200, "success", "操作成功", null);
    }

    public static <T> ResultVo<T> success(T data) {
        return new ResultVo<T>(200, "success", null, data);
    }

    public static ResultVo success(String message) {
        return new ResultVo(200, "success", message, null);
    }

    // 工厂方法 - 成功的响应
    public static <T> ResultVo<T> success(String message, T data) {
        return new ResultVo<T>(200, "success", message, data);
    }

    // 工厂方法 - 失败的响应
    public static ResultVo error(String message) {
        return new ResultVo(400, "error", message, null);
    }

    public static ResultVo error(ExceptionCodeEnum exceptionCodeEnum) {
        return new ResultVo(exceptionCodeEnum.getCode(), "error", exceptionCodeEnum.getMessage(), null);
    }
}
