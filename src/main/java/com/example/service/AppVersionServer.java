package com.example.service;

import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.pojo.AppVersion;
import com.example.entity.vo.ResultVo;

public interface AppVersionServer {

    /**
     * 获取最新发布版本
     */
    ResultVo getLatestPublishedVersion(String userId,String currentVersion );

    ResultVo addAppVersion(AppVersion appVersion);

    ResultVo saveAppVersion(AppVersion appVersion);

    ResultVo pageConditionQuery(PageConditionQueryDto pageConditionQueryDto);

    ResultVo deleteVersion(String id);
}
