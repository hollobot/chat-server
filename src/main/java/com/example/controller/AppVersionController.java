package com.example.controller;

import com.example.annotation.GlobalTokenInterceptor;
import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.pojo.AppVersion;
import com.example.entity.vo.ResultVo;
import com.example.service.AppVersionServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

@Validated
@GlobalTokenInterceptor
@Slf4j
@Api(tags = "应用版本接口")
@RestController
@RequestMapping("/version")
public class AppVersionController {

    @Resource
    private AppVersionServer appVersionServer;

    @ApiOperation("获取最新发布版本")
    @GetMapping("latest/{userId}/{currentVersion}")
    public ResultVo latest(@NotBlank @PathVariable("userId") String userId,
        @NotBlank @PathVariable("currentVersion") String currentVersion) {
        return appVersionServer.getLatestPublishedVersion(userId, currentVersion);
    }

    @ApiOperation("添加应用版本")
    @PostMapping("add")
    public ResultVo add(@RequestBody AppVersion appVersion) {
        return appVersionServer.addAppVersion(appVersion);
    }

    @ApiOperation("修改并发布的版本信息")
    @PostMapping("save")
    public ResultVo save(@RequestBody AppVersion appVersion) {
        return appVersionServer.saveAppVersion(appVersion);
    }

    @ApiOperation("分页条件查询应用版本列表")
    @PostMapping("all")
    public ResultVo all(@RequestBody PageConditionQueryDto pageConditionQueryDto) {
        return appVersionServer.pageConditionQuery(pageConditionQueryDto);
    }

    /**
     * 删除版本
     */
    @ApiOperation("版本删除")
    @DeleteMapping("del/{id}")
    public ResultVo del(@PathVariable("id") String id) {
        return appVersionServer.deleteVersion(id);
    }

}
