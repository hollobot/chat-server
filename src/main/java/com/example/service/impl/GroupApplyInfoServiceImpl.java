package com.example.service.impl;

import com.example.entity.dto.GroupApplyInfoDto;
import com.example.entity.enums.MessageTypeEnum;
import com.example.entity.pojo.GroupApplyInfo;
import com.example.entity.pojo.GroupContact;
import com.example.entity.vo.ResultVo;
import com.example.mapper.GroupApplyInfoMapper;
import com.example.mapper.GroupContactMapper;
import com.example.service.GroupApplyInfoService;
import com.example.service.GroupContactService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;

@Service
public class GroupApplyInfoServiceImpl implements GroupApplyInfoService {

    @Resource
    private GroupApplyInfoMapper groupApplyInfoMapper;

    @Resource
    private GroupContactService groupContactService;

    @Resource
    private GroupContactMapper groupContactMapper;

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public ResultVo saveGroupApplyInfo(GroupApplyInfoDto groupApplyInfoDto) {
        /*1、封装申请数据*/
        GroupApplyInfo groupApplyInfo = new GroupApplyInfo();
        BeanUtils.copyProperties(groupApplyInfoDto, groupApplyInfo);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        groupApplyInfo.setApplyTime(timestamp);
        groupApplyInfo.setApplyInfo(groupApplyInfoDto.getApplyInfo());
        groupApplyInfo.setStatus(groupApplyInfoDto.getJoinType() == 0 ? 1 : 0);

        /*2、判断是否已经存在申请数据*/
        GroupApplyInfo existGroupApplyInfo =
            groupApplyInfoMapper.selectApplyInfoById(groupApplyInfo.getApplicantUserId(), groupApplyInfo.getGroupId());

        if (existGroupApplyInfo == null) {
            /*不存在直接插入数据*/
            groupApplyInfoMapper.insertApplyInfo(groupApplyInfo);
        } else {
            /*存在修改数据*/
            groupApplyInfoMapper.saveApplyInfo(groupApplyInfo);
        }

        /*3、封装群聊联系人数据*/
        GroupContact groupContact = new GroupContact();
        groupContact.setUserId(groupApplyInfo.getApplicantUserId());
        groupContact.setGroupId(groupApplyInfo.getGroupId());
        groupContact.setOwnerId(groupApplyInfo.getOwnerId());
        groupContact.setGroupRemarks(groupApplyInfo.getRemark());
        groupContact.setStatus(groupApplyInfo.getStatus());
        groupContact.setCreateTime(groupApplyInfo.getApplyTime());
        groupContact.setUpdateTime(groupApplyInfo.getApplyTime());

        /*4、初始化联系人数据*/
        return groupContactService.saveGroupContact(groupContact, groupApplyInfoDto.getNickName());
    }

    @Override
    public ResultVo selectGroupApplyList(String applicantUserId) {
        List<GroupApplyInfo> groupApplyInfos = groupApplyInfoMapper.selectApplyInfoList(applicantUserId);
        return ResultVo.success(groupApplyInfos);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo updateGroupApplyStatusById(String applicantUserId, String groupId, Integer status) {
        /*1、修改群聊申请数据状态*/
        Integer integer = groupApplyInfoMapper.updateApplyStatusById(applicantUserId, groupId, status);
        /*2、修改群聊联系人数据状态*/
        Integer integer1 = groupContactMapper.updateGroupContactStatus(applicantUserId, groupId, status == 1 ? 1 : 0);
        if (status == 1) {
            /*5、创建群聊会话、缓存群聊联系人、将用户channel添加到channelGroup里面。。。*/
            GroupApplyInfo groupApplyInfo = groupApplyInfoMapper.selectApplyInfoById(applicantUserId, groupId);
            String nickName = groupApplyInfo.getNickName();
            String msgContent = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), nickName);
            MessageTypeEnum addGroupTypeEnum = MessageTypeEnum.ADD_GROUP;
            addGroupTypeEnum.setInitMessage(msgContent);

            groupContactService.createOrAddGroupSession(applicantUserId, groupId, groupApplyInfo.getApplyInfo(),
                addGroupTypeEnum);
        }
        return ResultVo.success(integer == integer1);
    }
}
