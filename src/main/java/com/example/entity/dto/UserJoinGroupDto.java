package com.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class UserJoinGroupDto {

    private String type; // 标识数据来源（user 或 group）
    private String id; // 用户 ID 或群组 ID
    private String userName; // 用户昵称
    private String userEmail; // 用户邮箱
    private String groupName; // 群组名称
    private String groupAnnouncement; // 群公告

}
