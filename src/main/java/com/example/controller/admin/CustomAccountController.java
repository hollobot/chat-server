package com.example.controller.admin;

import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.pojo.CustomAccount;
import com.example.entity.vo.ResultVo;
import com.example.service.CustomAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "自定义账户接口")  //名称可自定义
@Slf4j
@RestController
@RequestMapping("/admin/custom")
public class CustomAccountController {

    @Resource
    private CustomAccountService customAccountService;

    @ApiOperation("分页条件查询自定义账户列表")
    @PostMapping("/all")
    public ResultVo all(@RequestBody PageConditionQueryDto pageConditionQueryDto) {
        return customAccountService.queryAll(pageConditionQueryDto);
    }

    @ApiOperation("添加自定义账户")
    @PostMapping("/add")
    public ResultVo add(@RequestBody @Validated CustomAccount customAccount) {
        return customAccountService.addAccount(customAccount);
    }

    @ApiOperation("删除自定义账户")
    @GetMapping("/del/{customAccountId}")
    public ResultVo del(@PathVariable String customAccountId) {
        return customAccountService.delAccount(customAccountId);
    }
}
