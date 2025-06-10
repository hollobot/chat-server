package com.example.mapper;

import com.example.entity.pojo.GroupApplyInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupApplyInfoMapper {

    Integer insertApplyInfo(GroupApplyInfo groupApplyInfo);

    Integer saveApplyInfo(GroupApplyInfo groupApplyInfo);

    Integer updateApplyStatusById(@Param("applicantUserId") String applicantUserId,
                                  @Param("groupId") String groupId, @Param("status") Integer status);

    GroupApplyInfo selectApplyInfoById(@Param("applicantUserId") String applicantUserId,
                                       @Param("groupId") String groupId);

    List<GroupApplyInfo> selectApplyInfoList(String applicantUserId);

    Integer selectApplyCount(@Param("userId") String userId, @Param("status") Integer status, @Param("lastOffTime") Long lastOffTime);

}
