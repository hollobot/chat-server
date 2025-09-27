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

    /** 消息会话 */
    private List<ChatSessionUser> chatSessionUserList;

    /** 消息列表 */
    private List<ChatMessage> chatMessageList;

    /** 群聊未读申请数 */
    private Integer applyUserCount;

    /** 联系人未读申请数 */
    private Integer applyGroupCount;

}
