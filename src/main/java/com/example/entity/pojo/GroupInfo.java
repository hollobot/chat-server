package com.example.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.sql.Timestamp;

/**
 * 群信息表实体类
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TableName("`group_info`")
public class GroupInfo {
    /**
     * 群ID
     */
    @TableId
    private String groupId;

    /**
     * 群名
     */
    private String groupName;

    /**
     * 群主ID
     */
    private String ownerId;

    /**
     * 群公告
     */
    private String groupAnnouncement;

    /**
     * 加入类型：0表示直接加入，1表示需要管理员同意
     */
    private Integer joinType;

    /**
     * 群状态：0表示解散状态，1表示正常状态
     */
    private Integer status;

    /**
     * 群创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;

}
