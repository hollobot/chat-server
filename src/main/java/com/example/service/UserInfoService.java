package com.example.service;

import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.dto.UserInfoDto;
import com.example.entity.pojo.UserInfo;
import com.example.entity.vo.ResultVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface UserInfoService {

    ResultVo registerService(UserInfoDto userInfoDto);

    ResultVo loginService(UserInfoDto userInfoDto);

    ResultVo saveUserInfo(UserInfo userInfo, MultipartFile avatarFile);

    ResultVo getUserInfo(String userId, String isAdmin, HttpServletRequest request);

    ResultVo changeUserPwd(String userId, String oldPwd, String newPwd);

    ResultVo userChangeStatus(String userId);

    ResultVo userOffLine(String userId);

    ResultVo logoutUser(HttpServletRequest request);

    ResultVo pageConditionQuery(PageConditionQueryDto pageConditionQueryDto);
}
