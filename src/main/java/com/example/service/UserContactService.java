package com.example.service;

import com.example.entity.pojo.UserContact;
import com.example.entity.vo.ResultVo;

import javax.servlet.http.HttpServletRequest;

public interface UserContactService {

    ResultVo saveUserContact(UserContact userContact,String nickName,String applyContext);

    ResultVo changeContactStatus(String userId,String contactId,Integer status);

    ResultVo selectUserUnionGroupList(HttpServletRequest request,String id);

    ResultVo selectUsers(String userId,Integer status);

    ResultVo selectUser(String userId);

    void addContactRobot(String userId, String thisName);

    void initContactSession(String applyUserId,String receptionId,String applyInfo);




}
