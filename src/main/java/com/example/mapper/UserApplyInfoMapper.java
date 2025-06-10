package com.example.mapper;

import com.example.entity.pojo.UserApplyInfo;
import com.example.entity.vo.UserApplyInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserApplyInfoMapper {

    Integer insertApplyInfo(UserApplyInfo userApplyInfo);

    Integer saveApplyInfo(UserApplyInfo userApplyInfo);

    Integer updateApplyStatusById(@Param("applicantUserId") String applicantUserId,
        @Param("recipientId") String recipientId, @Param("status") Integer status);

    UserApplyInfo selectApplyInfoById(@Param("applicantUserId") String applicantUserId,
        @Param("recipientId") String recipientId);

    List<UserApplyInfoVo> selectApplyInfoList(String applicantUserId);

    Integer selectApplyCount(@Param("userId") String userId, @Param("status") Integer status,
        @Param("lastOffTime") Long lastOffTime);
}
