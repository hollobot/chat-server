package com.example.service.impl;

import com.example.entity.dto.MessageSendDto;
import com.example.entity.dto.UserApplyInfoDto;
import com.example.entity.enums.MessageTypeEnum;
import com.example.entity.pojo.UserApplyInfo;
import com.example.entity.pojo.UserContact;
import com.example.entity.vo.ResultVo;
import com.example.entity.vo.UserApplyInfoVo;
import com.example.mapper.UserApplyInfoMapper;
import com.example.mapper.UserContactMapper;
import com.example.service.UserApplyInfoService;
import com.example.service.UserContactService;
import com.example.websocket.ChannelContextUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;


@Service
public class UserApplyInfoServiceImpl implements UserApplyInfoService {

    @Resource
    private UserApplyInfoMapper userApplyInfoMapper;

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserContactMapper userContactMapper;


    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo saveUserApplyInfo(UserApplyInfoDto userApplyInfoDto) {

        /*1、封装申请数据*/
        UserApplyInfo userApplyInfo = new UserApplyInfo();
        BeanUtils.copyProperties(userApplyInfoDto, userApplyInfo);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        userApplyInfo.setApplyTime(timestamp);
        userApplyInfo.setApplyInfo(userApplyInfoDto.getApplyInfo());
        userApplyInfo.setStatus(userApplyInfoDto.getJoinType() == 0 ? 1 : 0);

        /*2、判断是否已经存在申请数据*/
        UserApplyInfo existUserApplyInfo = userApplyInfoMapper
                .selectApplyInfoById(userApplyInfo.getApplicantUserId(), userApplyInfo.getRecipientId());

        if (existUserApplyInfo == null) {
            /*不存在直接插入数据*/
            userApplyInfoMapper.insertApplyInfo(userApplyInfo);
        } else {
            /*存在修改数据*/
            userApplyInfoMapper.saveApplyInfo(userApplyInfo);
        }

        /*3、插入双方初始化联系人数据*/
        /*封装联系人数据*/
        UserContact userContact = new UserContact();
        userContact.setUserId(userApplyInfo.getApplicantUserId());
        userContact.setContactId(userApplyInfo.getRecipientId());
        userContact.setContactRemarks(userApplyInfoDto.getRemark());
        userContact.setStatus(userApplyInfo.getStatus());
        userContact.setCreateTime(userApplyInfo.getApplyTime());
        userContact.setUpdateTime(userApplyInfo.getApplyTime());

        /*4、初始化联系人数据*/
        return userContactService
                .saveUserContact(userContact, userApplyInfo.getNickName(), userApplyInfo.getApplyInfo());
    }

    @Override
    public ResultVo selectUserApplyList(String applicantUserId) {
        List<UserApplyInfoVo> userApplyInfoVos = userApplyInfoMapper.selectApplyInfoList(applicantUserId);
        return ResultVo.success(userApplyInfoVos);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo updateUserApplyStatusById(String applicantUserId, String recipientId, Integer status) {
        /*1、修改用户申请数据状态*/
        userApplyInfoMapper.updateApplyStatusById(applicantUserId, recipientId, status);
        /*2、修改用户联系人数据状态 会修改两条*/
        userContactMapper.updateUserContactStatus(applicantUserId, recipientId, status == 1 ? 1 : 0);
        if (status==1) {
            /*3、添加成功初始化会话*/
            UserApplyInfo userApplyInfo = userApplyInfoMapper.selectApplyInfoById(applicantUserId, recipientId);
            userContactService.initContactSession(applicantUserId,recipientId,userApplyInfo.getApplyInfo());
        }
        return ResultVo.success(1);
    }
}
