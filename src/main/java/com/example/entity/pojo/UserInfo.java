package com.example.entity.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private String userId;  // 用户id
    private String email;    // 用户邮箱
    private String nickName; // 昵称
    private Integer joinType;  // 加入类型
    private Integer sex;   // 性别
    private String password;  // 密码
    private String personalSignature;  // 个性签名
    private Integer status;  // 状态

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;   // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp lastLoginTime; // 最后登录时间
    private String areaName;    // 地区
    private String areaCode;    // 地区编号
    private Long lastOffTime;   // 最后离线时间

    public UserInfo(String userId, String nickName, Integer sex, String personalSignature, String areaName) {
        this.userId = userId;
        this.nickName = nickName;
        this.sex = sex;
        this.personalSignature = personalSignature;
        this.areaName = areaName;
    }

    /**
     * 1:在线，0：下线
     */
    private Integer onlineStatus;
}
