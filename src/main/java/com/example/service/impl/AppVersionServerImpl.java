package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.enums.ExceptionCodeEnum;
import com.example.entity.enums.ReleaseStatusEnum;
import com.example.entity.pojo.AppVersion;
import com.example.entity.pojo.CustomAccount;
import com.example.entity.vo.ResultVo;
import com.example.handler.CustomException;
import com.example.mapper.AppVersionMapper;
import com.example.service.AppVersionServer;
import com.example.utils.ArrayUtils;
import com.example.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;

@Service
public class AppVersionServerImpl implements AppVersionServer {

    @Resource
    private AppVersionMapper appVersionMapper;

    @Resource
    private ArrayUtils<String> arrayUtils;

    /**
     * 获取最新发布版本
     */
    @Override
    public ResultVo getLatestPublishedVersion(String userId, String currentVersion) {
        /*1、查询最新版本*/
        QueryWrapper<AppVersion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0).notIn("status", 0).orderByDesc("update_time").last("LIMIT 1");
        AppVersion appVersion = appVersionMapper.selectOne(queryWrapper);
        /*2、查询用户是否被支持该版本*/
        if (appVersion == null || currentVersion == appVersion.getVersionCode()) {
            return ResultVo.success(0);
        }
        if (appVersion.getStatus() == ReleaseStatusEnum.FULL_RELEASE.getCode()) {
            return ResultVo.success(appVersion);
        }
        String[] grays = appVersion.getGrayUid().split(",");
        if (arrayUtils.contains(grays, userId)) {
            return ResultVo.success(appVersion);
        }

        return ResultVo.success(0);
    }

    /**
     * 添加应用版本
     *
     * @param appVersion
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo addAppVersion(AppVersion appVersion) {
        /*1、判断版本号是否冲突*/
        QueryWrapper<AppVersion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("version_code", appVersion.getVersionCode());
        AppVersion app = appVersionMapper.selectOne(queryWrapper);

        if (app != null) {
            return ResultVo.error("版本号存在");
        }
        /*2、添加版本数据*/
        Timestamp nowTime = new Timestamp(System.currentTimeMillis());
        appVersion.setUpdateTime(nowTime);
        appVersion.setCreateTime(nowTime);
        appVersion.setIsDeleted(0);
        int insert = appVersionMapper.insert(appVersion);
        if (insert != 1) {
            return ResultVo.success("添加失败");
        }
        return ResultVo.success("添加成功");
    }

    /**
     * 修改并发布的版本信息
     *
     * @param appVersion
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo saveAppVersion(AppVersion appVersion) {
        /*1、判断是否存在该版本，是否发布了*/
        QueryWrapper<AppVersion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", appVersion.getId()).eq("status", ReleaseStatusEnum.UNPUBLISHED.getCode());
        AppVersion app = appVersionMapper.selectOne(queryWrapper);
        if (app == null) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);
        }
        Timestamp nowTime = new Timestamp(System.currentTimeMillis());
        appVersion.setUpdateTime(nowTime);
        int i = appVersionMapper.updateById(appVersion);
        if (i != 1) {
            throw new CustomException(ExceptionCodeEnum.CODE_400.setMessage("操作失败"));
        }
        return ResultVo.success("发布成功");
    }

    /**
     * 分页条件查询应用版本列表
     *
     * @param pageInfo
     * @return
     */
    @Override
    public ResultVo pageConditionQuery(PageConditionQueryDto pageInfo) {
        Integer pageNum = pageInfo.getPageNum();
        Integer pageSize = pageInfo.getPageSize();
        /*1、校验参数*/
        if (pageNum <= 0 || pageSize <= 0) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);
        }
        /*2、设置分页参数*/
        PageHelper.startPage(pageNum, pageSize);
        /*3、条件查询数据*/
        QueryWrapper<AppVersion> queryWrapper = new QueryWrapper<>();
        if (pageInfo.getStatus() != null) {
            queryWrapper.eq("status", pageInfo.getStatus());
        }
        queryWrapper.eq("is_deleted", 0).like("update_description", pageInfo.getUpdateDescription())
            .orderByDesc("update_time");
        List<AppVersion> appVersions = appVersionMapper.selectList(queryWrapper);
        // 用 PageInfo 包装结果（包含分页信息）
        PageInfo<AppVersion> appVersionPageInfo = new PageInfo<>(appVersions);
        return ResultVo.success(appVersionPageInfo);
    }

    /**
     * 版本删除
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo deleteVersion(String id) {
        UpdateWrapper<AppVersion> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).eq("is_deleted", 0).eq("status", ReleaseStatusEnum.UNPUBLISHED.getCode())
            .set("is_deleted", 1);
        int update = appVersionMapper.update(updateWrapper);
        if (update != 1) {
            return ResultVo.error("删除失败");
        }
        return ResultVo.success("删除成功");
    }

}
