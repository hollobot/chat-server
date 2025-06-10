package com.example.service;

import com.example.entity.dto.BatchGroupContactDto;
import com.example.entity.dto.GroupInfoDto;
import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.dto.UserUpdateInfoDto;
import com.example.entity.pojo.GroupInfo;
import com.example.entity.vo.ResultVo;
import com.oracle.deploy.update.UpdateInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface GroupInfoService {

    ResultVo insertGroupService(GroupInfo groupInfo, MultipartFile avatarFile);

    ResultVo userChangeStatus(GroupInfo groupInfo,HttpServletRequest request);

    ResultVo selectGroupInfoByGroupId(String groupId);

    ResultVo disbandGroup(BatchGroupContactDto batchGroupContactDto, HttpServletRequest request);

    ResultVo saverGroupInfo(GroupInfo groupInfo, MultipartFile avatarFile,HttpServletRequest request);

    ResultVo pageConditionQuery(PageConditionQueryDto pageConditionQueryDto);
}
