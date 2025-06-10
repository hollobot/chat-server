package com.example.entity.dto;

import java.io.Serializable;

public class MessageSendDto<T> implements Serializable {

    private static final long serialVersionUID = 1L; // 固定版本号

    /**
     * 消息 ID（主键）
     */
    private String uuid;

    /**
     * 会话 ID，表示该消息属于哪个会话
     */
    private String sessionId;

    /**
     * 发送用户 ID
     */
    private String sendUserId;

    /**
     * 发送用户昵称
     */
    private String sendUserNickName;

    /**
     * 接收用户 ID（或群聊 ID）
     */
    private String recipientId;

    /**
     * 消息内容（文本消息的具体内容，文件消息则为空）
     */
    private String messageContent;

    /**
     * 最后的消息
     */
    private String lastMessage;

    /**
     * 消息类型（0: 文本, 1: 图片, 2: 语音, 3: 文件等）
     */
    private Integer messageType;

    /**
     * 发送时间（时间戳，单位：毫秒）
     */
    private Long sendTime;

    /**
     * 接收人类型（0: 单聊, 1: 群聊）
     */
    private Integer recipientType;

    /**
     * 扩展消息
     */
    private T extendData;

    /**
     * 消息状态（0: 发送中, 1: 已发送）
     */
    private Integer status;

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
     * 群员人数
     */
    private Integer memberCount;

    /**
     * 联系人名称
     */
    private String contactName;

    private String contactIdTemp;



    public String getContactIdTemp() {
        return contactIdTemp;
    }

    public void setContactIdTemp(String contactIdTemp) {
        this.contactIdTemp = contactIdTemp;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getSendUserNickName() {
        return sendUserNickName;
    }

    public void setSendUserNickName(String sendUserNickName) {
        this.sendUserNickName = sendUserNickName;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getLastMessage() {
        if (this.lastMessage == null) {
            return messageContent;
        }
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(Integer recipientType) {
        this.recipientType = recipientType;
    }

    public T getExtendData() {
        return extendData;
    }

    public void setExtendData(T extendData) {
        this.extendData = extendData;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    @Override
    public String toString() {
        return "MessageSendDto{" + "uuid='" + uuid + '\'' + ", sessionId='" + sessionId + '\'' + ", sendUserId='" + sendUserId + '\'' + ", sendUserNickName='" + sendUserNickName + '\'' + ", recipientId='" + recipientId + '\'' + ", messageContent='" + messageContent + '\'' + ", lastMessage='" + lastMessage + '\'' + ", messageType=" + messageType + ", sendTime=" + sendTime + ", recipientType=" + recipientType + ", extendData=" + extendData + ", status=" + status + ", fileSize=" + fileSize + ", fileName='" + fileName + '\'' + ", fileType=" + fileType + ", memberCount=" + memberCount + ", contactName='" + contactName + '\'' + ", contactIdTemp='" + contactIdTemp + '\'' + '}';
    }
}
