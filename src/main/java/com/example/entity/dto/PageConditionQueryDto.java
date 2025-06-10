package com.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageConditionQueryDto {
    private Integer pageNum;
    private Integer pageSize;
    private String userId;  // 用户id
    private String nickName;  // 用户id

    private String email;

    private String groupId;
    private String groupName;

    /**
     * 是否发布
     */
    private String status;

    /**
     * 跟新描述
     */
    private String updateDescription;


}
