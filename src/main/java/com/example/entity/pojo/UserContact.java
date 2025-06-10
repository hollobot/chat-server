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
public class UserContact {
    private String userId; // 用户 ID
    private String contactId; // 联系人 ID
    private String contactRemarks; // 联系人备注
    private Integer status; // 状态 0：非好友 ，1：好友，2：已删除好友，3：被好友删除，4：已拉黑好友，5：被好友拉黑
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp updateTime; // 更新时间

    public UserContact(String userId, String contactId, String contactRemarks, Integer status, Timestamp createTime,
        Timestamp updateTime) {
        this.userId = userId;
        this.contactId = contactId;
        this.contactRemarks = contactRemarks;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    private Integer sex;
    private String areaName;
    private Integer joinType;
}
