package com.example.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GroupApplyInfoDto {


    /**
     * 申请人 ID
     */
    @NotBlank
    private String applicantUserId;

    /**
     * 群聊 ID
     */
    @NotBlank
    private String groupId;

    /**
     * 群主 ID
     */
    @NotBlank
    private String ownerId;

    /**
     * 群聊名称
     */
    @NotBlank
    private String groupName;


    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp applyTime;

    /**
     * 申请信息
     */
    @NotBlank
    private String applyInfo;


    /*备注*/
    @NotBlank
    private String remark;
    /*申请人名称*/
    @NotBlank
    private String nickName;
    /*申请人性别*/
    @Min(0)
    @Max(1)
    private Integer applicantSex;

    @Min(0)
    @Max(1)
    private Integer joinType; //0:表示直接加入 1：表示要审核

    /**
     * 类别 0：申请、1：邀请
     */
    @Min(0)
    @Max(1)
    private Integer type;
}
