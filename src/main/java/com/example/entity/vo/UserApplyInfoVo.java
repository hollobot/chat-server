package com.example.entity.vo;

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
public class UserApplyInfoVo {

    private Integer type;                // 常量值 1:发送 或 0:接收
    private String applicantUserId;  // 申请人用户ID
    private String recipientId;      // 接收人ID
    private String status;           // 状态
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp applyTime;          // 申请时间
    private String applyInfo;        // 申请信息
    /*备注*/
    private String remark;
    /*申请人名称*/
    private String nickName;
    /*申请人性别*/
    private Integer applicantSex;
    /*接收人性别*/
    private Integer recipientSex;
}
