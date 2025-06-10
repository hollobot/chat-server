package com.example.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoDto {
    /**
     * 群ID
     */
    @NotBlank
    private String groupId;

    /**
     * 群名
     */
    @NotBlank
    private String groupName;

    /**
     * 群主ID
     */
    private String ownerId;

    /**
     * 群公告
     */
    @NotBlank
    private String groupAnnouncement;

    /**
     * 加入类型：0表示直接加入，1表示需要管理员同意
     */
    @NotNull
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

    private MultipartFile avatarFile;
}
