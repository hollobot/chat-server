package com.example.aspect;
import com.example.entity.dto.SysSettingDto;
import com.example.entity.enums.ExceptionCodeEnum;
import com.example.handler.CustomException;
import com.example.utils.RedisUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;

/**
 * aop 属性修改器
 */
@Aspect
@Component
public class PropertyModifierAspect {

    @Resource
    private RedisUtils redisUtils;

    /*这种写法可以将modifyProperty传递给下面的方法参数里*/
    @Before("@annotation(com.example.annotation.ModifyProperty)")
    public void modifyProperty(JoinPoint joinPoint) throws Exception {

        Object target = joinPoint.getTarget();

        /*注意这里必须做异常处理，一定会报错*/
        try{
            // 使用反射修改属性
            // target.getClass() - 获取目标对象的 Class 对象
            //getDeclaredField("setSettingDto") - 根据字段名获取该类声明的字段（包括私有字段）
            Field field = target.getClass().getDeclaredField("sysSettingDto");
            field.setAccessible(true); //设置字段可访问

            // 获取redis跟新后的配置信息
            SysSettingDto sysSetting = redisUtils.getSysSetting();
            if (sysSetting != null) {
                // 更新字段值
                field.set(target, sysSetting);
            }

        }catch (Exception e){
            throw new CustomException(ExceptionCodeEnum.CODE_500.setMessage("属性修改失败"));
        }

    }
}