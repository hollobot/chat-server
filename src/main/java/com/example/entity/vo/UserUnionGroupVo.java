package com.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class UserUnionGroupVo {
    private String type; // 标识数据来源（user 或 group）
    private String id; // 用户 ID 或群组 ID
    private String name;//名称
    private String areaName; //地区
    private String groupAnnouncement; // 群公告
    private Integer sex;//性别
    private Integer status; //状态   null也是非好友 状态 0：非好友 ，1：好友，2：已删除好友，3：被好友删除，4：已拉黑好友，5：被好友拉
    private Integer joinType; //加入方式 0:直接加入 1：需要同意
    private String ownerId; //群主ID
}
