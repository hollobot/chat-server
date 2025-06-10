package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.enums.ExceptionCodeEnum;
import com.example.entity.pojo.CustomAccount;
import com.example.entity.pojo.UserInfo;
import com.example.entity.vo.ResultVo;
import com.example.handler.CustomException;
import com.example.mapper.CustomAccountMapper;
import com.example.mapper.UserInfoMapper;
import com.example.service.CustomAccountService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CustomAccountServiceImpl implements CustomAccountService {

    @Resource
    private CustomAccountMapper customAccountMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public ResultVo queryAll(PageConditionQueryDto pageInfo) {
        Integer pageNum = pageInfo.getPageNum();
        Integer pageSize = pageInfo.getPageSize();
        /*1、校验参数*/
        if (pageNum <= 0 || pageSize <= 0) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);
        }
        /*2、设置分页参数*/
        PageHelper.startPage(pageNum, pageSize);
        /*3、条件查询数据*/
        QueryWrapper<CustomAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("user_id", pageInfo.getUserId());
        queryWrapper.like("email", pageInfo.getEmail());
        List<CustomAccount> customAccounts = customAccountMapper.selectList(queryWrapper);
        // 用 PageInfo 包装结果（包含分页信息）
        PageInfo<CustomAccount> userInfoPageInfo = new PageInfo<>(customAccounts);
        return ResultVo.success(userInfoPageInfo);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo addAccount(CustomAccount customAccount) {
        /*查询UUID是否存在*/
        String s = userInfoMapper.selectNameById(customAccount.getUserId());
        QueryWrapper<CustomAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", customAccount.getUserId()).or().eq("email", customAccount.getEmail());
        CustomAccount data = customAccountMapper.selectOne(queryWrapper);
        if (s != null || data != null) {
            return ResultVo.error("已存在");
        }
        customAccount.setStatus(1);
        Integer insert = customAccountMapper.insert(customAccount);
        return ResultVo.success("添加成功", insert);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo delAccount(String customAccountId) {
        CustomAccount customAccount = customAccountMapper.selectById(customAccountId);
        if (customAccount.getStatus() == 0) {
            return ResultVo.error("已经使用无法删除");
        }
        Integer i = customAccountMapper.deleteById(customAccountId);
        return ResultVo.success("删除成功", i);
    }

}
