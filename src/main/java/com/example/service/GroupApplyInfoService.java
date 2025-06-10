package com.example.service;
import com.example.entity.dto.GroupApplyInfoDto;
import com.example.entity.vo.ResultVo;

public interface GroupApplyInfoService {


    ResultVo saveGroupApplyInfo(GroupApplyInfoDto groupApplyInfoDto);

    ResultVo selectGroupApplyList(String applicantUserId);

    ResultVo updateGroupApplyStatusById(String applicantUserId, String groupId, Integer status);
}
