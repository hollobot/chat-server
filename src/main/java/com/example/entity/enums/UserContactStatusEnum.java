package com.example.entity.enums;

public enum UserContactStatusEnum {
    UN_FRIENDS(0,"非好友"),
    FRIENDS(1,"好友"),
    DELETE_FRIENDS(2,"已删除好友"),
    DELETE_BY_FRIENDS(3,"已被好友删除"),
    BLACK_FRIENDS(4,"已拉黑好友"),
    BLACK_BY_FRIENDS(5,"已被好友拉黑");

    private Integer status;
    private String desc;

    UserContactStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
