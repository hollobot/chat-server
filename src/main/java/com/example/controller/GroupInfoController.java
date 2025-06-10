package com.example.controller;

import com.example.annotation.GlobalTokenInterceptor;
import com.example.entity.dto.BatchGroupContactDto;
import com.example.entity.dto.GroupInfoDto;
import com.example.entity.pojo.GroupInfo;
import com.example.entity.vo.ResultVo;
import com.example.service.GroupInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.*;

@Validated
@Api(tags = "群聊信息接口")  //名称可自定义
@Slf4j
@RestController
@RequestMapping("/group")
@GlobalTokenInterceptor
public class GroupInfoController {

    @Resource
    private GroupInfoService groupInfoService;

    /**
     * 添加群聊
     *
     * @param ownerId      所属人
     * @param joinType     加入方式
     * @param groupName    群名
     * @param announcement 群公告
     * @param avatarFile   头像文件
     * @return msg
     */
    @ApiOperation("添加群聊")
    @PostMapping("add")
    public ResultVo addGroup(@NotBlank String ownerId, @Min(0) @Max(1) Integer joinType, @NotBlank String groupName,
        @NotBlank String announcement, @NotNull MultipartFile avatarFile) {

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setOwnerId(ownerId);
        groupInfo.setJoinType(joinType);
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupAnnouncement(announcement);

        return groupInfoService.insertGroupService(groupInfo, avatarFile);
    }

    @ApiOperation("查询群聊详情")
    @GetMapping("info/{groupId}")
    public ResultVo getGroupInfo(@NotBlank @PathVariable String groupId) {
        return groupInfoService.selectGroupInfoByGroupId(groupId);
    }

    @ApiOperation("解散群聊")
    @PostMapping("disband")
    public ResultVo getGroupInfo(@RequestBody BatchGroupContactDto batchGroupContactDto, HttpServletRequest request) {
        return groupInfoService.disbandGroup(batchGroupContactDto, request);
    }

    @ApiOperation("修改群聊信息")
    @PostMapping("save")
    public ResultVo saveGroup(@NotBlank String groupId, @Min(0) @Max(1) Integer joinType, @NotBlank String groupName,
        @NotBlank String groupAnnouncement, MultipartFile avatarFile, HttpServletRequest request) {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setJoinType(joinType);
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupAnnouncement(groupAnnouncement);
        return groupInfoService.saverGroupInfo(groupInfo, avatarFile, request);
    }

}
