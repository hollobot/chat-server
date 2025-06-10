package com.example.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
public class GroupContact {
    @TableId(type = IdType.INPUT) // 保留一个作为主键标识
    private String userId; // 用户 ID
    /**
     * 这个也是主键
     */
    private String groupId; // 群聊 ID
    private String ownerId; // 群主 ID
    private String groupRemarks; // 联系人备注
    private Integer status; // 状态 0：离群 1：在群
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp updateTime; // 更新时间
}
