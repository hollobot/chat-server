package com.example.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChatSession {
    /** 会话 ID（主键） */
    private String sessionId;
    /** 最后一条接收到的消息（可为空） */
    private String lastMessage;
    /** 最后一条消息的接收时间（时间戳，单位：毫秒，可为空） */
    private Long lastReceiveTime;
}
