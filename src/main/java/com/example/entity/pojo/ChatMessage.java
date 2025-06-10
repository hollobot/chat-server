package com.example.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    /**
     * 消息 ID（主键）
     */
    private String uuid;

    /**
     * 会话 ID，表示该消息属于哪个会话
     */
    private String sessionId;

    /**
     * 消息类型（0: 文本, 1: 图片, 2: 语音, 3: 文件等）
     */
    private Integer messageType;

    /**
     * 消息内容（文本消息的具体内容，文件消息则为空）
     */
    private String messageContent;

    /**
     * 发送用户 ID
     */
    private String sendUserId;

    /**
     * 发送用户昵称
     */
    private String sendUserNickName;

    /**
     * 发送时间（时间戳，单位：毫秒）
     */
    private Long sendTime;

    /**
     * 接收用户 ID（或群聊 ID）
     */
    private String recipientId;

    /**
     * 接收人类型（0: 单聊, 1: 群聊）
     */
    private Integer recipientType;

    /**
     * 文件大小（仅文件类型消息适用，单位：字节）
     */
    private Long fileSize;

    /**
     * 文件名（仅文件类型消息适用）
     */
    private String fileName;

    /**
     * 文件类型（仅文件类型消息适用，例如：图片、音频、视频等）
     */
    private Integer fileType;

    /**
     * 消息状态（0: 发送中, 1: 已发送）
     */
    private Integer status;
}
