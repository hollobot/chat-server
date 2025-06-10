package com.example.mapper;

import com.example.entity.pojo.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper {

    Integer insertOrUpdate(ChatSession chatSession);

    Integer update(ChatSession chatSession);
}
