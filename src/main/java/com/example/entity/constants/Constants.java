package com.example.entity.constants;

public class Constants {

    /**
     * 验证码
     */
    public static final String REDS_KEY_CHECK_CODE = "chat-service:checkCode:";
    public static final long CODE_TIME = 1000 * 60l;

    /**
     * token信息
     */
    public static final String REDS_KEY_TOKEN = "chat-service:token:";
    public static final long TOKEN_TIME = 1000 * 60 * 60 * 24l;

    /**
     * userid:token
     */
    public static final String REDS_KEY_USERID = "chat-service:userId:";

    /**
     * robot 机器人
     */
    public static final String ROBOT_UID = "U00000000000";


    /**
     * 群聊头像图片位置
     */
    public static final String GROUP_AVATAR_FILE = "groupAvatar/";
    public static final String GROUP_AVATAR_NAME_SUFFIX = ".png";

    /**
     * 用户头像图片位置
     */
    public static final String USER_AVATAR_FILE = "userAvatar/";
    public static final String USER_AVATAR_NAME_SUFFIX = ".png";

    /**
     * 消息文件路径
     */
    public static final String MESSAGE_FILE_PATH = "file/";

    /**
     * 媒体消息文件封面
     */
    public static final String COVER_IMAGE_SUFFIX = "_cover.jpg";

    /**
     * 用户心跳
     */
    public static final String REDS_KEY_HEARTBEAT = "chat-service:heartbeat:";
    public static final long HEARTBEAT_TIME = 1000 * 6;

    /**
     * 联系人列表
     */
    public static final String REDS_KEY_USERS_CONTACT = "chat-service:userIds:";

    /**
     * 群聊列表
     */
    public static final String REDS_KEY_GROUPS_CONTACT = "chat-service:groupIds:";

    /**
     * 3天时间
     */
    public static final Long MILLIS_3_DAYS = 1000 * 60 * 60 * 24 * 3l;

    /**
     * 图片视频列表
     */
    public static final String[] IMAGE_SUFFIX_LIST = new String[] {".jpeg", ".jpg", ".png", ".gif", ".bmp", ".webp"};
    public static final String[] VIDEO_SUFFIX_LIST = new String[] {".mp4", ".avi", ".rmvb", ".mkv", ".mov"};

    /**
     * byte -> mb
     */
    public static final Long FILE_SIZE_MB = 1024 * 1024l;

    /**
     * 系统设置
     */
    public static final String SYSTEM_SETTING = "chat-service:setting";
}
