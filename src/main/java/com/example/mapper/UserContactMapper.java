package com.example.mapper;

import com.example.entity.pojo.UserContact;
import com.example.entity.vo.UserUnionGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserContactMapper {

    Integer insertUserContact(UserContact userContact);

    Integer insertInitUserContact(UserContact userContact);

    Integer updateUserContact(UserContact userContact);

    Integer updateUserContactStatus(@Param("userId") String userId, @Param("contactId") String contactId,
        @Param("status") Integer status);

    Integer updateContactRemarks(@Param("contactId") String contactId, @Param("remark") String remark);

    UserContact selectUserContactById(@Param("userId") String userId, @Param("contactId") String contactId);

    List<UserUnionGroupVo> selectUserUnionGroupById(@Param("userId") String userId, @Param("searchId") String searchId);

    /*查询用户的联系人*/
    List<UserContact> selectContactUsers(@Param("userId") String userId, @Param("status") Integer status);

    List<String> selectUserContactIds(String userId);

}
