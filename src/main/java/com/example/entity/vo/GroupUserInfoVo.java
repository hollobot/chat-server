package com.example.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GroupUserInfoVo {
    private String id;
    private String name;
    private String areaName;
    private Integer sex;
    private String type;
    private Integer joinType;
    private Integer status;
    private String personalSignature;
}
