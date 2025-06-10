package com.example.controller.admin;

import com.example.annotation.GlobalTokenInterceptor;
import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.pojo.GroupInfo;
import com.example.entity.vo.ResultVo;
import com.example.service.GroupInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Validated
@GlobalTokenInterceptor
@Slf4j
@Api(tags = "管理群聊接口")  //名称可自定义
@RestController
@RequestMapping("/admin/group")
public class GroupController {

    @Resource
    private GroupInfoService groupInfoService;

    @ApiOperation("分页条件查询群聊列表")
    @PostMapping("/all")
    public ResultVo all(@RequestBody PageConditionQueryDto pageConditionQueryDto){
        return groupInfoService.pageConditionQuery(pageConditionQueryDto);
    }

    @ApiOperation("群聊解散")
    @PostMapping("/status")
    public ResultVo changeStatus(@RequestBody GroupInfo groupInfo, HttpServletRequest request){
        return groupInfoService.userChangeStatus(groupInfo,request);
    }


}
