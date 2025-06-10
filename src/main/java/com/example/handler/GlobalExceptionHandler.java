package com.example.handler;

import com.example.entity.enums.ExceptionCodeEnum;
import com.example.entity.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResultVo handleException(CustomException e,HttpServletRequest request) {
        ExceptionCodeEnum exceptionCodeEnum = e.getExceptionCodeEnum();
        log.error("自定义异常，请求地址：{}，错误信息：", request.getRequestURI(), e);
        return ResultVo.error(exceptionCodeEnum);
    }

    /**
     * 参数校验异常
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler({ValidationException.class,MethodArgumentNotValidException.class})
    public ResultVo handleException(RuntimeException e, HttpServletRequest request) {
        log.error("请求参数不规范，请求地址：{}，错误信息：", request.getRequestURI(), e);
        return ResultVo.error(ExceptionCodeEnum.CODE_400);
    }

    /**
     * 请求体为空
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResultVo handleException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.error("请求参数不规范，请求地址：{}，错误信息：", request.getRequestURI(), e);
        return ResultVo.error(ExceptionCodeEnum.CODE_400);
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResultVo handleException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.error("请求参数不规范，请求地址：{}，错误信息：", request.getRequestURI(), e);
        return ResultVo.error(ExceptionCodeEnum.CODE_400);
    }

    @ExceptionHandler(Exception.class)
    public ResultVo handleException(Exception e, HttpServletRequest request) {
        log.error("请求错误，请求地址：{}，错误信息：", request.getRequestURI(), e);
        return ResultVo.error(ExceptionCodeEnum.CODE_500);
    }

}
