package com.example.handler;

import com.example.entity.enums.ExceptionCodeEnum;
import lombok.Data;

@Data
public class CustomException extends RuntimeException {

    private ExceptionCodeEnum exceptionCodeEnum;

    public CustomException(ExceptionCodeEnum exceptionCodeEnum) {
        super(exceptionCodeEnum.getMessage());
        this.exceptionCodeEnum = exceptionCodeEnum;
    }
}
