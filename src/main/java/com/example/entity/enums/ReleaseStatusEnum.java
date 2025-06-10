package com.example.entity.enums;

public enum ReleaseStatusEnum {
    UNPUBLISHED(0, "未发布"),
    GRAY_RELEASE(1, "灰度发布"),
    FULL_RELEASE(2, "全网发布");

    private final Integer code;
    private final String desc;

    ReleaseStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}
