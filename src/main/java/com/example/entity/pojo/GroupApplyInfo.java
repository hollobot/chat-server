package com.example.entity.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * 群聊申请信息表实体类
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GroupApplyInfo {
    /**
     * 申请人 ID
     */
    private String applicantUserId;

    /**
     * 群聊 ID
     */
    private String groupId;

    /**
     * 群主 ID
     */
    private String ownerId;

    /**
     * 群聊名称
     */
    private String groupName;

    /**
     * 状态：0 表示申请中，1 表示申请成功，2 表示申请失败
     */
    private Integer status;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp applyTime;

    /**
     * 申请信息
     */
    private String applyInfo;

    /*备注*/
    private String remark;
    /*申请人名称*/
    private String nickName;
    /*申请人性别*/
    private Integer applicantSex;
    /*类型 0：申请 1：邀请*/
    private Integer type;
}