package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.pojo.CustomAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomAccountMapper extends BaseMapper<CustomAccount> {

    Integer updateStatusByEmail(@Param("status") Integer status, @Param("email") String email);

    CustomAccount selectCustomAccountByEmail(String email);

}
