package com.example.service.impl;

import com.example.annotation.ModifyProperty;
import com.example.entity.constants.Constants;
import com.example.entity.dto.MessageSendDto;
import com.example.entity.dto.SysSettingDto;
import com.example.entity.dto.TokenUserInfoDto;
import com.example.entity.enums.ExceptionCodeEnum;
import com.example.entity.enums.MessageStatusEnum;
import com.example.entity.enums.MessageTypeEnum;
import com.example.entity.enums.UserContactTypeEnum;
import com.example.entity.pojo.ChatMessage;
import com.example.entity.pojo.ChatSession;
import com.example.handler.CustomException;
import com.example.mapper.ChatMessageMapper;
import com.example.mapper.ChatSessionMapper;
import com.example.service.ChatMessageService;
import com.example.utils.ArrayUtils;
import com.example.utils.DateFormatUtils;
import com.example.utils.RedisUtils;
import com.example.utils.StringUtils;
import com.example.websocket.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Value("${project.folder}")
    private String folder;

    @Resource
    private ArrayUtils<String> stringArrayUtils;

    @Resource
    private ArrayUtils<Integer> integerArrayUtils;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private ArrayUtils<String> arrayUtils;

    private SysSettingDto sysSettingDto;

    @Resource
    private RedisUtils redisUtils;

    /*容器初始化后执行*/
    @PostConstruct
    public void init() {
        SysSettingDto sysSetting = redisUtils.getSysSetting();
        if (sysSetting == null) {
            sysSetting = new SysSettingDto();
        }
        this.sysSettingDto = sysSetting;
    }


    @ModifyProperty
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public MessageSendDto sendMessage(ChatMessage chatMessage) {
        log.error("{}", chatMessage.getSendUserId());
        log.error("{}", chatMessage.getRecipientId());
        /*1、判断发送人和接受人状态 如果是机器人的话就直接发送，不需要判断和接收人的状态*/
        if (!chatMessage.getSendUserId().equals(
            sysSettingDto.getRobotUid())) {
            List<String> contactIds = null;
            ExceptionCodeEnum exceptionCodeEnum = null;
            String sessionId = null;
            if (chatMessage.getRecipientType() == UserContactTypeEnum.USER.getType()) {
                contactIds = redisUtils.getContactIds(Constants.REDS_KEY_USERS_CONTACT, chatMessage.getSendUserId());
                exceptionCodeEnum = ExceptionCodeEnum.CODE_901;
                sessionId = StringUtils.generateSessionId(chatMessage.getSendUserId(), chatMessage.getRecipientId());

            } else {
                contactIds = redisUtils.getContactIds(Constants.REDS_KEY_GROUPS_CONTACT, chatMessage.getSendUserId());
                exceptionCodeEnum = ExceptionCodeEnum.CODE_902;
                sessionId = StringUtils.generateSessionId(chatMessage.getRecipientId());
            }
            chatMessage.setSessionId(sessionId);

            if (contactIds == null || !contactIds.contains(chatMessage.getRecipientId())) {
                throw new CustomException(exceptionCodeEnum);
            }
        }

        /*2、判断发送文件类型，只处理普通文件消息，和媒体文件消息*/
        if (!integerArrayUtils.contains(
            new Integer[] {MessageTypeEnum.CHAT.getType(), MessageTypeEnum.MEDIA_CHAT.getType()},
            chatMessage.getMessageType())) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);
        }
        Integer status = chatMessage.getMessageType() == MessageTypeEnum.CHAT.getType() ? 1 : 0;
        chatMessage.setStatus(status);

        // 处理换行
        //        chatMessage.setMessageContent(StringUtils.cleanHtmlTag(chatMessage.getMessageContent()));
        /*3、跟新会话信息*/
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(chatMessage.getSessionId());
        chatSession.setLastReceiveTime(chatMessage.getSendTime());
        chatSession.setLastMessage(chatMessage.getMessageContent());
        if (chatMessage.getRecipientType() == UserContactTypeEnum.GROUP.getType()) {
            chatSession.setLastMessage(chatMessage.getSendUserNickName() + ":" + chatMessage.getMessageContent());
        }
        chatSessionMapper.update(chatSession);
        chatMessageMapper.insertChatMessage(chatMessage);

        /*4、发送messageSendDto消息*/
        MessageSendDto messageSendDto = new MessageSendDto();
        BeanUtils.copyProperties(chatMessage, messageSendDto);

        /*5、判断是否是机器人*/
        if (Constants.ROBOT_UID.equals(chatMessage.getRecipientId())) {
            chatMessage.setRecipientId(chatMessage.getSendUserId());
            chatMessage.setSendUserId(Constants.ROBOT_UID);
            chatMessage.setSendUserNickName(sysSettingDto.getRobotNickName());
            chatMessage.setMessageContent(sysSettingDto.getRobotDefaultMessage());
            chatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
            chatMessage.setStatus(1);
            chatMessage.setUuid(String.valueOf(UUID.randomUUID()));
            sendMessage(chatMessage);
        } else {
            messageHandler.sendMessage(messageSendDto);
        }
        return messageSendDto;
    }

    @ModifyProperty
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void saveMessageFile(String userId, String uuid, MultipartFile file, MultipartFile cover) {
        ChatMessage chatMessage = chatMessageMapper.selectByUUID(uuid);
        /*1、判断数据请求是否规范*/
        if (chatMessage == null) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);
        }

        if (!chatMessage.getSendUserId().equals(userId)) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);
        }
        String fileName = file.getOriginalFilename();
        /*2、获取文件后缀名*/
        String fileSuffix = StringUtils.getFileSuffix(fileName);
        /*3、判断文件后缀是否存在*/
        if (StringUtils.isEmpty(fileSuffix)) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);
        }

        log.error("fileSize:{}", file.getSize());
        /*4、校验文件大小*/
        if (stringArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST,
            fileSuffix.toLowerCase()) && file.getSize() / Constants.FILE_SIZE_MB > sysSettingDto.getMaxImageSize()) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);

        } else if (stringArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST,
            fileSuffix.toLowerCase()) && file.getSize() / Constants.FILE_SIZE_MB > sysSettingDto.getMaxVideoSize()) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);

        } else if (file.getSize() / Constants.FILE_SIZE_MB > sysSettingDto.getMaxFileSize()) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);
        }

        String fileRealName = uuid + fileSuffix;
        String dateFolder = DateFormatUtils.format(chatMessage.getSendTime(), "yyyyMM");
        File fileMode = new File(folder + Constants.MESSAGE_FILE_PATH + dateFolder + "/" + fileRealName);
        /*5、判断文件目录是否存在，不存在创建*/
        if (!fileMode.exists()) {
            fileMode.mkdirs();
        }

        /*6、复制文件*/
        try {
            file.transferTo(fileMode);
            if (cover != null) {
                cover.transferTo(new File(
                    folder + Constants.MESSAGE_FILE_PATH + dateFolder + "/" + uuid + Constants.COVER_IMAGE_SUFFIX));
            }

        } catch (IOException e) {
            log.error("消息文件上传失败:{}", e);
            throw new CustomException(ExceptionCodeEnum.CODE_500);
        }

        /*7、发送消息，跟新媒体消息发送状态*/
        chatMessageMapper.updateStatus(uuid, chatMessage.getStatus(), MessageStatusEnum.SEND.getStatus());
        MessageSendDto<Object> sendDto = new MessageSendDto<>();
        sendDto.setStatus(MessageStatusEnum.SEND.getStatus());
        sendDto.setUuid(uuid);
        sendDto.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        sendDto.setRecipientId(chatMessage.getRecipientId());
        sendDto.setRecipientType(chatMessage.getRecipientType());
        messageHandler.sendMessage(sendDto);
    }

    @Override
    public void downloadFile(TokenUserInfoDto tokenInfo, HttpServletResponse response, String fileId,
        Boolean showCover) {
        File file = null;
        /*1、判断是下载消息文件还是头像文件*/
        if (fileId.length() > 12) {
            /*下载消息文件*/
            file = getMessageFile(tokenInfo, fileId, showCover);
        } else {
            /*下载头像*/
            file = getAvatarFile(fileId, showCover);
        }

        response.setContentType("application/octet-stream;charset=UTF-8"); // 二进制流 + UTF-8
        // attachment：表示响应体应作为附件下载，而不是在页面中显示。
        // filename="file.txt"：指定下载时保存的默认文件名（用户仍可修改）。
        response.setHeader("Content-Disposition", "attachment; filename=\"file.jpg\"");
        // 设置 HTTP 响应头 Content-Length，声明要返回给客户端的数据长度（单位：字节）。
        response.setContentLengthLong(file.length());
        FileInputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = response.getOutputStream();
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
        } catch (IOException e) {
            log.error("文件下载失败:{}", e);
        } finally {
            /*关闭流资源*/
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("IO流异常:{}", e);
                }

            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("IO流异常:{}", e);
                }
            }
        }

    }

    @ModifyProperty
    @Override
    public SysSettingDto fileConfig() {
        return sysSettingDto;
    }

    private File getAvatarFile(String fileId, Boolean showCover) {
        String path = folder;
        /*1、判断是用户还是群聊头像*/
        if (UserContactTypeEnum.getByPrefix(fileId) == UserContactTypeEnum.USER) {
            path += Constants.USER_AVATAR_FILE + fileId;
        } else {
            path += Constants.GROUP_AVATAR_FILE + fileId;
        }

        if (showCover) {
            path += Constants.COVER_IMAGE_SUFFIX;
        } else {
            path += Constants.GROUP_AVATAR_NAME_SUFFIX;
        }

        File file = new File(path);

        if (!file.exists()) {
            log.error("文件不存在：{}", fileId);
            throw new CustomException(ExceptionCodeEnum.CODE_602);
        }

        return file;
    }

    private File getMessageFile(TokenUserInfoDto userInfoDto, String fileId, Boolean showCover) {
        String path = folder;
        ChatMessage chatMessage = chatMessageMapper.selectByUUID(fileId);
        Integer recipientType = chatMessage.getRecipientType();
        String recipientId = chatMessage.getRecipientId();
        String sendUserId = chatMessage.getSendUserId();
        String userId = userInfoDto.getUserId();
        if (recipientType == UserContactTypeEnum.USER.getType() && !arrayUtils.contains(
            new String[] {recipientId, sendUserId}, userId)) {
            throw new CustomException(ExceptionCodeEnum.CODE_601);
        }

        if (recipientType == UserContactTypeEnum.GROUP.getType()) {
            // 判断是否在群
            List<String> contactIds = redisUtils.getContactIds(Constants.REDS_KEY_GROUPS_CONTACT, userId);
            // TODO 应该是群id
            if (!contactIds.contains(recipientId)) {
                throw new CustomException(ExceptionCodeEnum.CODE_601);
            }
        }

        String dateFolder = DateFormatUtils.format(chatMessage.getSendTime(), "yyyyMM") + "/";
        path += Constants.MESSAGE_FILE_PATH + dateFolder;

        if (showCover) {
            path += fileId + Constants.COVER_IMAGE_SUFFIX;
        } else {
            path += fileId + chatMessage.getFileName().substring(chatMessage.getFileName().lastIndexOf("."));
        }

        File file = new File(path);
        if (!file.exists()) {
            log.error("文件不存在：{}", fileId);
            throw new CustomException(ExceptionCodeEnum.CODE_602);

        }

        return file;
    }

}
