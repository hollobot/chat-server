package com.example.entity.enums;


/**
 * 联络人类型 枚举
 */
public enum UserContactTypeEnum {
    USER(0, "U", "好友"),
    GROUP(1, "G", "群聊"),
    ;


    private Integer type;
    private String prefix;
    private String desc;

    UserContactTypeEnum(Integer type, String prefix, String desc) {
        this.type = type;
        this.prefix = prefix;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDesc() {
        return desc;
    }



    public static UserContactTypeEnum getByPrefix(String uid) {
        if (uid == null || uid.trim().length() == 0) {
            return null;
        }
        String prefix = uid.substring(0, 1);
        for (UserContactTypeEnum typeEnum : UserContactTypeEnum.values()){
            if (typeEnum.getPrefix().equals(prefix)){
                return typeEnum;
            }
        }
        return null;
    }
}
