/*
 Navicat Premium Data Transfer

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80400
 Source Host           : localhost:3306
 Source Schema         : chat

 Target Server Type    : MySQL
 Target Server Version : 80400
 File Encoding         : 65001

 Date: 21/06/2025 19:02:26
*/
use chat;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_version
-- ----------------------------
DROP TABLE IF EXISTS `app_version`;
CREATE TABLE `app_version`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '自增ID',
  `version_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '版本号',
  `update_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '更新描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '发布状态：1-灰度发布，2-全网发布',
  `gray_uid` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '灰度用户ID列表，多个用逗号分隔',
  `file_type` tinyint NOT NULL DEFAULT 0 COMMENT '文件类型：0-本地文件，1-外链',
  `external_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '外链地址',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `version_code`(`version_code` ASC) USING BTREE COMMENT '唯一',
  INDEX `idx_version_code`(`version_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '应用版本管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息唯一标识符',
  `session_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话id',
  `message_type` tinyint(1) NOT NULL COMMENT '消息类型',
  `message_content` varchar(3000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息内容',
  `send_user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发送人id',
  `send_user_nick_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发送人昵称',
  `send_time` bigint NULL DEFAULT NULL COMMENT '发送时间',
  `recipient_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接收人id',
  `recipient_type` tinyint(1) NULL DEFAULT NULL COMMENT '接收人类型 0单聊 1群聊',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件名',
  `file_type` tinyint(1) NULL DEFAULT NULL COMMENT '文件类型',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态 0：正在发送、1：已经发送',
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for chat_session
-- ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session`  (
  `session_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话Id',
  `last_message` varchar(3000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后接收消息',
  `last_receive_time` bigint NULL DEFAULT NULL COMMENT '最后消息接收时间',
  PRIMARY KEY (`session_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for chat_session_user
-- ----------------------------
DROP TABLE IF EXISTS `chat_session_user`;
CREATE TABLE `chat_session_user`  (
  `user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `contact_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人Id',
  `session_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话id',
  `contact_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人名称',
  PRIMARY KEY (`user_id`, `contact_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for custom_account
-- ----------------------------
DROP TABLE IF EXISTS `custom_account`;
CREATE TABLE `custom_account`  (
  `custom_account_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '唯一id',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户邮箱，注册使用需要邮箱注册',
  `user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '自定义账户',
  `status` tinyint(1) NOT NULL COMMENT '自定义状态：0已被使用，1未被使用',
  PRIMARY KEY (`custom_account_id`) USING BTREE,
  UNIQUE INDEX `index_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `index_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '自定义账号表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for group_apply_info
-- ----------------------------
DROP TABLE IF EXISTS `group_apply_info`;
CREATE TABLE `group_apply_info`  (
  `applicant_user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申请人id',
  `group_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群聊id',
  `owner_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群主id',
  `group_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群聊名称',
  `status` bigint NOT NULL COMMENT '状态 0：申请中，1：申请成功，2：申请失败',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `apply_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申请信息',
  `remark` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '备注',
  `nick_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申请人名称',
  `applicant_sex` bigint NOT NULL COMMENT '申请人性别',
  `type` bigint NOT NULL COMMENT '类型 0：申请 1：邀请',
  PRIMARY KEY (`applicant_user_id`, `group_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for group_contact
-- ----------------------------
DROP TABLE IF EXISTS `group_contact`;
CREATE TABLE `group_contact`  (
  `user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `group_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群聊id',
  `owner_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群主id',
  `group_remarks` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群聊备注',
  `status` tinyint NOT NULL COMMENT '状态 0：离群 1：在群',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '跟新时间',
  PRIMARY KEY (`user_id`, `group_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for group_info
-- ----------------------------
DROP TABLE IF EXISTS `group_info`;
CREATE TABLE `group_info`  (
  `group_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群id\r\n',
  `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群名',
  `owner_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群主id',
  `group_announcement` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '群公告',
  `join_type` tinyint(1) NOT NULL COMMENT '0:直接加入 1：管理员同意',
  `status` tinyint NOT NULL COMMENT '0:解散状态 1：正常状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`group_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '群信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_apply_info
-- ----------------------------
DROP TABLE IF EXISTS `user_apply_info`;
CREATE TABLE `user_apply_info`  (
  `applicant_user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申请人id',
  `recipient_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接受人id',
  `status` bigint NOT NULL COMMENT '状态 0：申请中，1：申请成功，2：申请失败',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `apply_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申请信息',
  `remark` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '备注',
  `nick_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申请人名称',
  `applicant_sex` bigint NOT NULL COMMENT '申请人性别',
  `recipient_sex` bigint NOT NULL COMMENT '接收人性别',
  PRIMARY KEY (`applicant_user_id`, `recipient_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户申请信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_contact
-- ----------------------------
DROP TABLE IF EXISTS `user_contact`;
CREATE TABLE `user_contact`  (
  `user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `contact_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人id',
  `contact_remarks` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人备注',
  `status` tinyint(1) NOT NULL COMMENT '状态 0：非好友 ，1：好友，2：已删除好友，3：被好友删除，4：已拉黑好友，5：被好友拉黑',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '跟新时间',
  PRIMARY KEY (`user_id`, `contact_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '联系人表（用户+群聊）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户邮箱',
  `nick_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `join_type` tinyint(1) NULL DEFAULT NULL COMMENT '0:直接加入，1：同意后加入',
  `sex` tinyint(1) NULL DEFAULT NULL COMMENT '性别  0：女  1：男',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码',
  `personal_signature` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个性签名',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态 ',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登入时间',
  `area_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地区',
  `area_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地区编号',
  `last_off_time` bigint NULL DEFAULT NULL COMMENT '最后离线时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `index_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
