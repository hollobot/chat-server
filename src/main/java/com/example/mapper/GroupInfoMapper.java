package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.pojo.CustomAccount;
import com.example.entity.pojo.GroupInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupInfoMapper extends BaseMapper<GroupInfo> {

    Integer insertGroupInfo(GroupInfo groupInfo);

    Integer updateGroupNumber(@Param("groupId") String groupId, @Param("number") Integer number);

    Integer updateGroupStatus(@Param("groupId") String groupId, @Param("status") Integer status);

    Integer updateInfo(GroupInfo groupInfo);

    Integer selectCountByOwnerId(String userId);

    GroupInfo selectGroupById(String groupId);

}
