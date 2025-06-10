package com.example.service;

import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.pojo.CustomAccount;
import com.example.entity.vo.ResultVo;

public interface CustomAccountService {

    ResultVo queryAll(PageConditionQueryDto pageConditionQueryDto);

    ResultVo addAccount(CustomAccount customAccount);

    ResultVo delAccount(String customAccountId);

}
