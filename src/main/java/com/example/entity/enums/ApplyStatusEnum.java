package com.example.entity.enums;

public enum ApplyStatusEnum {

    APPLYING(0,"申请中"),
    SUCCESS(1,"申请成功"),
    ERROR(2,"申请失败");

    private Integer type;
    private String desc;

    ApplyStatusEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
