package com.example.service;

import com.example.entity.dto.UserApplyInfoDto;
import com.example.entity.vo.ResultVo;

public interface UserApplyInfoService {

    ResultVo saveUserApplyInfo(UserApplyInfoDto userApplyInfoDto);

    ResultVo selectUserApplyList(String applicantUserId);

    ResultVo updateUserApplyStatusById(String applicantUserId, String recipientId, Integer status);
}
