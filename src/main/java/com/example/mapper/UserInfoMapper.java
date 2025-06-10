package com.example.mapper;

import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.pojo.UserContact;
import com.example.entity.pojo.UserInfo;
import com.example.entity.vo.GroupUserInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserInfoMapper {


    Integer insertUserInfo(UserInfo userInfo);

    Integer updateUserInfo(UserInfo userInfo);

    Integer updatePwd(@Param("userId") String userId,@Param("newPwd") String newPwd);

    Integer changeUpdateTime(String userId);

    Integer changeLastOffTime(@Param("userId") String userId,@Param("lastOffTime") Long lastOffTime);

    Integer updateStatus(String userId);

    UserInfo selectUserByUID(String userId);

    UserInfo selectUserByEmail(@Param("email") String email);

    List<GroupUserInfoVo> selectGroupUsers(@Param("groupId") String groupId, @Param("userId") String userId);

    Integer selectUserJoinTypeByID(String id);

    String selectNameById(String userId);

    UserInfo selectUserInfoById(String userId);

    List<UserInfo> conditionQuery(UserInfo userInfo);





}
