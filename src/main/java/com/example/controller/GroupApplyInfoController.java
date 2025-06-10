package com.example.controller;

import com.example.annotation.GlobalTokenInterceptor;
import com.example.entity.dto.GroupApplyInfoDto;
import com.example.entity.vo.ResultVo;
import com.example.service.GroupApplyInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

@Validated
@Api(tags = "申请群聊信息接口")  //名称可自定义
@Slf4j
@RestController
@RequestMapping("/groupApply")
@GlobalTokenInterceptor
public class GroupApplyInfoController {

    @Resource
    private GroupApplyInfoService groupApplyInfoService;

    @ApiOperation("申请添加群聊联系人")
    @PostMapping("add")
    public ResultVo addContact(@Validated @RequestBody GroupApplyInfoDto groupApplyInfoDto) {
        return groupApplyInfoService.saveGroupApplyInfo(groupApplyInfoDto);
    }

    @ApiOperation("展示申请群聊列表")
    @GetMapping("all/{applicantUserId}")
    public ResultVo all(@NotBlank @PathVariable String applicantUserId) {
        return groupApplyInfoService.selectGroupApplyList(applicantUserId);
    }

    @ApiOperation("审核用户申请群聊请求")
    @PostMapping("check")
    public ResultVo check(@NotBlank String applicantUserId, @NotBlank String groupId, Integer status) {
        return groupApplyInfoService.updateGroupApplyStatusById(applicantUserId,groupId,status);
    }



}
