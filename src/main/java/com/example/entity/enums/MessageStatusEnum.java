package com.example.entity.enums;

public enum MessageStatusEnum {
    SENDING(0,"正在发送"),
    SEND(1,"已经发送");

    private Integer status;
    private String desc;

    MessageStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
