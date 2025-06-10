package com.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BatchGroupContactDto {
    /**
     * 群ID
     */
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
     * 1:批量添加,0：批量删除
     */
    private Integer type;

    private List<String> userIds;

    private List<String> names;

    private String groupSessionId;

    /**
     * 1:自己退出 0：群主操作
     */
    private Integer breakType;

}
