package com.example.controller.admin;

import com.example.annotation.GlobalTokenInterceptor;
import com.example.entity.dto.SysSettingDto;
import com.example.entity.vo.ResultVo;
import com.example.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@Validated
@GlobalTokenInterceptor
@Slf4j
@Api(tags = "系统设置接口")  //名称可自定义
@RestController
@RequestMapping("/admin/setting")
public class SysSettingController {

    @Resource
    private RedisUtils redisUtils;


    public SysSettingDto init() {
        SysSettingDto sysSetting = redisUtils.getSysSetting();
        if (sysSetting == null) {
            sysSetting = new SysSettingDto();
        }
        return sysSetting;
    }

    @ApiOperation("查询设置参数")
    @GetMapping("/all")
    public ResultVo all() {
        return ResultVo.success(init());
    }

    @ApiOperation("修改系统设置")
    @PostMapping("/save")
    public ResultVo save(@RequestBody SysSettingDto data) {
        redisUtils.setSysSetting(data);
        return ResultVo.success();
    }
}
