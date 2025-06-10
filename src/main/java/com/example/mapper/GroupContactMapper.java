package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.BatchGroupContactDto;
import com.example.entity.pojo.GroupContact;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupContactMapper extends BaseMapper<GroupContact> {

    Integer insertGroupContact(GroupContact groupContact);

    Integer insertInitGroupContact(GroupContact groupContact);

    Integer updateGroupContact(GroupContact groupContact);

    Integer updateGroupContacts(GroupContact groupContact);

    Integer updateGroupContactStatus(@Param("userId") String userId, @Param("groupId") String groupId,
        @Param("status") Integer status);

    List<GroupContact> selectContactGroups(@Param("userId") String userId, @Param("ownerId") String ownerId);

    Integer selectGroupUserCount(String groupId);

    GroupContact selectContactGroup(@Param("userId") String userId, @Param("groupId") String groupId);

    List<String> selectGroupContactIds(String userId);

    Integer batchInsertOrUpdate(BatchGroupContactDto bgc);

}
