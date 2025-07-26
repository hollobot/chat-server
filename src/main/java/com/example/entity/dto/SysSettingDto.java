package com.example.entity.dto;

import com.example.entity.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 系统设置dto对象，存入redis
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Component
public class SysSettingDto implements Serializable {

    /**
     * 最大群组数量
     */
    private Integer maxGroupCount = 25;

    /**
     * 群聊最大人数
     */
    private Integer maxGroupMemberCount = 100;

    /**
     * 图片大小20mb
     */
    private Integer maxImageSize = 20;

    /**
     * 视频大小80mb
     */
    private Integer maxVideoSize = 80;

    /**
     * 文件大小80mb
     */
    private Integer maxFileSize = 80;

    /**
     * 机器人UID
     */
    private String robotUid = Constants.ROBOT_UID;

    /**
     * 机器人昵称
     */
    private String robotNickName = "robot";

    /**
     * 机器人欢迎语
     */
    private String robotWelcome = "欢迎使用SwiftChat";

    /**
     * 机器人回复
     */
    private String robotDefaultMessage = "我是机器人，无法聊天";

    private Integer robotSex = 1;
    private String robotPersonalSignature = "欢迎使用SwiftChat";
    private String robotAreaName = "湖南省 衡阳";

}
