package com.example.controller;

import com.example.annotation.GlobalTokenInterceptor;
import com.example.entity.constants.Constants;
import com.example.entity.dto.UserInfoDto;
import com.example.entity.dto.UserUpdateInfoDto;
import com.example.entity.pojo.UserInfo;
import com.example.entity.vo.ResultVo;
import com.example.service.UserInfoService;
import com.example.utils.RedisUtils;
import com.example.websocket.MessageHandler;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.util.*;

@Api(tags = "用户账号接口")  //名称可自定义
@Slf4j
@RestController
@RequestMapping("/account")
@Validated
public class UserInfoController {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private MessageHandler messageHandler;

    @ApiOperation("获取验证码")
    @GetMapping("code")
    public ResultVo checkCode() {
        //1、获取验证码对象
        ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(100, 43);
        String code = arithmeticCaptcha.text();
        code = "1";
        String codeBase64 = arithmeticCaptcha.toBase64();
        //2、将验证码存入redis
        String uuid = UUID.randomUUID().toString();
        redisUtils.set(Constants.REDS_KEY_CHECK_CODE + uuid, code, Constants.CODE_TIME);
        //3、封装数据
        Map<String, String> stringStringMap = new HashMap<>();
        stringStringMap.put("codeKey", uuid);
        stringStringMap.put("codeBase64", codeBase64);

        return ResultVo.success(stringStringMap);
    }

    @ApiOperation("登入")
    @PostMapping("login")
    public ResultVo login(@Validated @RequestBody UserInfoDto userInfoDto) {
        return userInfoService.loginService(userInfoDto);
    }

    @ApiOperation("注册")
    @PostMapping("register")
    public ResultVo register(@Validated @RequestBody UserInfoDto userInfoDto) {
        return userInfoService.registerService(userInfoDto);
    }

    @GlobalTokenInterceptor
    @ApiOperation("修改信息")
    @PostMapping("update")
    public ResultVo updateInfo(@Validated @ModelAttribute UserUpdateInfoDto userUpdateInfoDto) {
        /*1、封装数据*/
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userUpdateInfoDto, userInfo);
        userInfo.setAreaName(String.join(" ", userUpdateInfoDto.getAreaName()));
        /*2、调用业务*/
        return userInfoService.saveUserInfo(userInfo, userUpdateInfoDto.getAvatarFile());
    }

    @GlobalTokenInterceptor
    @ApiOperation("查询用户信息")
    @GetMapping("user/{userId}/{isAdmin}")
    public ResultVo getUser(@PathVariable("userId") String userId, @PathVariable("isAdmin") String isAdmin,
        HttpServletRequest request) {
        return userInfoService.getUserInfo(userId, isAdmin, request);
    }

    @GlobalTokenInterceptor
    @ApiOperation("修改密码")
    @PostMapping("changePwd")
    public ResultVo changePwd(@NotBlank String userId, @NotBlank String oldPwd, @NotBlank String newPwd) {
        return userInfoService.changeUserPwd(userId, oldPwd, newPwd);
    }

    @GlobalTokenInterceptor
    @ApiOperation("退出登录")
    @GetMapping("logout")
    public ResultVo logout(HttpServletRequest request) {
        return userInfoService.logoutUser(request);
    }



}
