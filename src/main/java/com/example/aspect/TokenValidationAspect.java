package com.example.aspect;

import com.example.entity.dto.TokenUserInfoDto;
import com.example.entity.enums.ExceptionCodeEnum;
import com.example.handler.CustomException;
import com.example.utils.RedisUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Aspect 声明当前类为一个切面类 （它告诉 Spring 这个类是一个切面类） Component 见这个类用于 spring 管理
 */
@Aspect
@Component
public class TokenValidationAspect {

    @Resource
    private RedisUtils redisUtils;
    /**
     * Spring 如何管理 HttpServletRequest 在 Spring 中，HttpServletRequest 并不像普通的 Spring Bean 一样通过 @Autowired 注解直接注入。它是由
     * Servlet 容器（如 Tomcat、 Jetty 等）提供并管理的。每次 HTTP 请求进入时，Spring 会将当前请求的 HttpServletRequest 绑定到当前的线程中，以便后续处理。
     *
     * Web 环境中的请求作用域 Spring 在 web 环境下有一个称为 请求作用域（Request scope） 的概念， 这意味着 HttpServletRequest 对象是与当前的 HTTP 请求关联的，
     * 并且每个请求都会有一个独立的 HttpServletRequest 实例。
     */
    private final HttpServletRequest request;

    // 使用构造器注入 HttpServletRequest
    public TokenValidationAspect(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * @return
     * @annotation注解
     * @within注解类
     */
    @Before(
        "@annotation(com.example.annotation.GlobalTokenInterceptor)||" + "@within(com.example.annotation.GlobalTokenInterceptor)")
    public void validateToken() {
        /*1、判断是否携带token*/
        String token = request.getHeader("authorization");
        if (token == null) {
            throw new CustomException(ExceptionCodeEnum.CODE_401);
        }
        TokenUserInfoDto tokenUserInfoDto = redisUtils.getTokenInfo(token);
        /*2、判断是否过时*/
        if (tokenUserInfoDto == null) {
            throw new CustomException(ExceptionCodeEnum.CODE_402);
        }

        String tokenByUserId = redisUtils.getTokenByUserId(tokenUserInfoDto.getUserId());

        /*3、判断token是否已经被替换*/
        if (!tokenByUserId.equals(token)) {
            throw new CustomException(ExceptionCodeEnum.CODE_402);
        }

    }
}
