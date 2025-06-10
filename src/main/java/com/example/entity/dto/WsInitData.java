package com.example.entity.dto;

import com.example.entity.pojo.ChatMessage;
import com.example.entity.pojo.ChatSessionUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class WsInitData {

    private List<ChatSessionUser> chatSessionUserList;

    private List<ChatMessage> chatMessageList;

    /**
     * 群聊消息
     */
    private Integer applyUserCount;

    /**
     * 联系人
     */
    private Integer applyGroupCount;


}
