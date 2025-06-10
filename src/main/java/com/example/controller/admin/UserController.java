package com.example.controller.admin;

import com.example.annotation.GlobalTokenInterceptor;
import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.vo.ResultVo;
import com.example.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Validated
@GlobalTokenInterceptor
@Slf4j
@Api(tags = "管理用户接口")  //名称可自定义
@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("分页条件查询用户列表")
    @PostMapping("/all")
    public ResultVo all(@RequestBody PageConditionQueryDto pageConditionQueryDto){
        return userInfoService.pageConditionQuery(pageConditionQueryDto);
    }

    @ApiOperation("账户状态设置")
    @GetMapping("/status/{userId}")
    public ResultVo changeStatus(@PathVariable String userId){
        return userInfoService.userChangeStatus(userId);
    }

    @ApiOperation("强制下线")
    @GetMapping("/offLine/{userId}")
    public ResultVo offLine(@PathVariable String userId){
        return userInfoService.userOffLine(userId);
    }

}
