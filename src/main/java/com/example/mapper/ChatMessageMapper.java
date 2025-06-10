package com.example.mapper;

import com.example.entity.pojo.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    Integer insertChatMessage(ChatMessage chatMessage);

    Integer batchInsertMessages(@Param("chatMessageList") List<ChatMessage> chatMessageList);

    Integer updateStatus(@Param("uuid") String uuid, @Param("oldStatus") Integer oldStatus,
        @Param("newStatus") Integer newStatus);

    List<ChatMessage> selectChatMessageList(@Param("sendTime") Long sendTime,
        @Param("recipientIds") List<String> recipientIds);

    ChatMessage selectByUUID(String uuid);

}
