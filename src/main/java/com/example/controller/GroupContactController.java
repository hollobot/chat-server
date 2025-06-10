package com.example.controller;
import com.example.annotation.GlobalTokenInterceptor;
import com.example.entity.dto.BatchGroupContactDto;
import com.example.entity.vo.ResultVo;
import com.example.service.GroupContactService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Validated
@Api(tags = "群聊联络人接口")  //名称可自定义
@Slf4j
@RestController
@RequestMapping("/groupContact")
@GlobalTokenInterceptor
public class GroupContactController {

    @Resource
    private GroupContactService groupContactService;

    @ApiOperation("查询通讯录群聊列表")
    @GetMapping("groups/{userId}")
    public ResultVo searchAllGroup(@NotBlank @PathVariable String userId) {
        return groupContactService.selectGroups(userId);
    }

    @ApiOperation("查询群聊里用户信息")
    @GetMapping("group/{groupId}/{userId}")
    public ResultVo searchGroup(@NotBlank @PathVariable String groupId, @NotBlank @PathVariable String userId) {
        return groupContactService.selectGroupUsersByGroupId(groupId, userId);
    }

    @ApiOperation("查询群聊人数")
    @GetMapping("count/{groupId}")
    public ResultVo getGroupNumber(@NotBlank @PathVariable String groupId) {
        return groupContactService.selectGroupUserNumber(groupId);
    }

    @ApiOperation("批量邀请好友加入群聊")
    @PostMapping("batchUsers")
    public ResultVo batchAddGroupContact(@NotNull @RequestBody BatchGroupContactDto batchGroupContactDto) {
        return groupContactService.batchGroupContact(batchGroupContactDto);
    }

}
