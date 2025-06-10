package com.example.entity.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.sql.Timestamp;

/**
 * 应用版本管理表
 *
 * @author 系统生成
 * @since 2024-01-01
 */
@Data
@TableName("app_version")
public class AppVersion {

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 版本号
     */
    @TableField("version_code")
    private String versionCode;

    /**
     * 更新描述
     */
    @TableField("update_description")
    private String updateDescription;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;

    /**
     * 发布状态：0-未发布，1-灰度发布，2-全网发布
     */
    @TableField("status")
    private Integer status;

    /**
     * 灰度用户ID列表，多个用逗号分隔
     */
    @TableField("gray_uid")
    private String grayUid;

    /**
     * 文件类型：0-本地文件，1-外链
     */
    @TableField("file_type")
    private Integer fileType;

    /**
     * 外链地址
     */
    @TableField("external_url")
    private String externalUrl;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp updateTime;
}
