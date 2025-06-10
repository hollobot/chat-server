package com.example.controller;

import com.example.annotation.GlobalTokenInterceptor;
import com.example.entity.vo.ResultVo;
import com.example.service.UserContactService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Validated
@Api(tags = "用户联络人接口")  //名称可自定义
@Slf4j
@RestController
@RequestMapping("/userContact")
@GlobalTokenInterceptor
public class UserContactController {

    @Resource
    private UserContactService userContactService;

    @ApiOperation("根据id查找用户和群聊")
    @GetMapping("search/{id}")
    public ResultVo searchUserUnionGroup(HttpServletRequest request, @NotBlank @PathVariable String id) {
        return userContactService.selectUserUnionGroupList(request, id);
    }

    @ApiOperation("查询通讯录联系人")
    @GetMapping("users/{id}/{status}")
    public ResultVo searchAllUser(@NotBlank @PathVariable String id, @PathVariable String status) {
        Integer type = status.equals("null") ? null : Integer.parseInt(status);
        return userContactService.selectUsers(id, type);
    }

    @ApiOperation("查询通讯录用户信息")
    @GetMapping("user/{id}")
    public ResultVo searchUser(@NotBlank @PathVariable String id) {
        return userContactService.selectUser(id);
    }

    @ApiOperation("删除拉黑联系人用户")
    @PostMapping("status")
    public ResultVo changeStatus(@NotBlank String userId, @NotBlank String contactId,
        @Min(value = 2) @Max(value = 4) Integer status) {
        return userContactService.changeContactStatus(userId, contactId, status);
    }

}
