package com.example.service;

import com.example.entity.dto.BatchGroupContactDto;
import com.example.entity.enums.MessageTypeEnum;
import com.example.entity.pojo.GroupContact;
import com.example.entity.vo.ResultVo;

public interface GroupContactService {

    ResultVo saveGroupContact(GroupContact groupContact,String applicationName);

    ResultVo selectGroups(String userId);

    ResultVo selectGroupUserNumber(String groupId);

    ResultVo selectGroupUsersByGroupId(String groupId, String userId);

    void createOrAddGroupSession(String applyUserId, String groupId, String applyInfo, MessageTypeEnum messageTypeEnum);

    ResultVo batchGroupContact(BatchGroupContactDto bgc);


}
