package com.example.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChatSessionUser {
    /**
     * 用户 ID（主键之一）
     */
    private String userId;


    /**
     * 联系人 ID（主键之一）
     */
    private String contactId;

    /**
     * 会话 ID，用于唯一标识用户与联系人之间的聊天会话
     */
    private String sessionId;

    /**
     * 联系人名称（可选字段）
     */
    private String contactName;

    /**
     * 最后发送消息
     */
    private String lastMessage;

    /**
     * 最后接收时间
     */
    private String lastReceiveTime;

    /**
     * 群组人数
     */
    private Integer memberCount;

    /**
     * 联系人类型 0用户 1群聊
     */
    private Integer contactType;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
        this.contactType = contactId.startsWith("U") ? 0 : 1;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastReceiveTime() {
        return lastReceiveTime;
    }

    public void setLastReceiveTime(String lastReceiveTime) {
        this.lastReceiveTime = lastReceiveTime;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getContactType() {
        return contactType;
    }
}
