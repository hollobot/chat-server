package com.example.controller;

import com.example.annotation.GlobalTokenInterceptor;
import com.example.entity.dto.UserApplyInfoDto;
import com.example.entity.vo.ResultVo;
import com.example.service.UserApplyInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

@Validated
@Api(tags = "申请用户信息接口")  //名称可自定义
@Slf4j
@RestController
@RequestMapping("/userApply")
@GlobalTokenInterceptor
public class UserApplyInfoController {

    @Resource
    private UserApplyInfoService userApplyInfoService;

    @ApiOperation("申请添加用户联系人")
    @PostMapping("add")
    public ResultVo addContact(@Validated @RequestBody UserApplyInfoDto userApplyInfoDto) {
        return userApplyInfoService.saveUserApplyInfo(userApplyInfoDto);
    }

    @ApiOperation("展示用户申请列表")
    @GetMapping("all/{applicantUserId}")
    public ResultVo all(@NotBlank @PathVariable String applicantUserId) {
        return userApplyInfoService.selectUserApplyList(applicantUserId);
    }

    @ApiOperation("审核用户申请好友请求")
    @PostMapping("check")
    public ResultVo check(@NotBlank String applicantUserId, @NotBlank String recipientId, Integer status) {
        return userApplyInfoService.updateUserApplyStatusById(applicantUserId,recipientId,status);
    }

}
