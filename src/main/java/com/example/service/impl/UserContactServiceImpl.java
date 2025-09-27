package com.example.service.impl;

import com.example.annotation.ModifyProperty;
import com.example.entity.constants.Constants;
import com.example.entity.dto.MessageSendDto;
import com.example.entity.dto.SysSettingDto;
import com.example.entity.dto.TokenUserInfoDto;
import com.example.entity.dto.WsInitData;
import com.example.entity.enums.ExceptionCodeEnum;
import com.example.entity.enums.MessageTypeEnum;
import com.example.entity.enums.UserContactStatusEnum;
import com.example.entity.enums.UserContactTypeEnum;
import com.example.entity.pojo.*;
import com.example.entity.vo.ResultVo;
import com.example.entity.vo.UserUnionGroupVo;
import com.example.handler.CustomException;
import com.example.mapper.*;
import com.example.service.UserContactService;
import com.example.utils.RedisUtils;
import com.example.utils.StringUtils;
import com.example.websocket.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserContactServiceImpl implements UserContactService {

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private RedisUtils redisUtils;

    private SysSettingDto sysSettingDto;

    /*容器初始化后执行*/
    @PostConstruct
    public void init() {
        SysSettingDto sysSetting = redisUtils.getSysSetting();
        if (sysSetting == null) {
            sysSetting = new SysSettingDto();
        }
        this.sysSettingDto = sysSetting;
    }

    @Resource
    private MessageHandler messageHandler;

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public ResultVo saveUserContact(UserContact userContact, String nickName, String applyContext) {

        Integer integer = 0;
        Integer integer1 = 0;

        /*1、封装联系人数据，联系人是双方同时持有的，需要插入两条数据*/
        UserContact userContact1 = new UserContact();
        BeanUtils.copyProperties(userContact, userContact1);
        userContact1.setUserId(userContact.getContactId());
        userContact1.setContactId(userContact.getUserId());
        userContact1.setContactRemarks(nickName);

        /*2、判断是否存在联系人数据*/
        UserContact existUserContact =
            userContactMapper.selectUserContactById(userContact.getUserId(), userContact.getContactId());
        if (existUserContact == null) {
            /*不存在初始化联系人数据，status=0*/
            integer = userContactMapper.insertInitUserContact(userContact);
            integer1 = userContactMapper.insertInitUserContact(userContact1);
        } else {
            /*存在跟新数据不跟新status*/
            integer = userContactMapper.updateUserContact(userContact);
            integer1 = userContactMapper.updateUserContact(userContact1);
        }

        if (integer != integer1) {
            /*使用异常回滚，保持数据一致性*/
            throw new CustomException(ExceptionCodeEnum.CODE_500);
        }

        /*3、判断是否可以添加成为联系人，修改status==1成为联系人*/
        if (userContact.getStatus() == 1) {
            userContactMapper.updateUserContactStatus(userContact.getUserId(), userContact.getContactId(),
                userContact.getStatus());
            /*添加成功初始化会话*/
            initContactSession(userContact.getUserId(), userContact.getContactId(), applyContext);
            return ResultVo.success("添加成功！。", 1);
        }
        /*4、发送申请添加消息*/
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
        messageSendDto.setRecipientId(userContact.getContactId());
        messageSendDto.setMessageContent(applyContext);
        messageSendDto.setRecipientType(UserContactTypeEnum.USER.getType());

        messageHandler.sendMessage(messageSendDto);
        return ResultVo.success("申请成功！等待审核。", 0);

    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo changeContactStatus(String userId, String contactId, Integer status) {
        Integer integer = userContactMapper.updateUserContactStatus(userId, contactId, status);
        if (integer != 2) {
            throw new CustomException(ExceptionCodeEnum.CODE_400.setMessage("请求异常，请联系管理员"));
        }
        redisUtils.remove(Constants.REDS_KEY_USERS_CONTACT + userId, 1, contactId);
        redisUtils.remove(Constants.REDS_KEY_USERS_CONTACT + contactId, 1, userId);
        // TODO 从我的好友列表缓存中删除好友
        // TODO 从好友的列表缓存中删除我
        return ResultVo.success(status == 2 ? "删除成功" : "拉黑成功");
    }

    @Override
    public ResultVo selectUserUnionGroupList(HttpServletRequest request, String id) {
        String token = request.getHeader("Authorization");
        TokenUserInfoDto tokenInfo = redisUtils.getTokenInfo(token);
        if (tokenInfo == null) {
            throw new CustomException(ExceptionCodeEnum.CODE_402);
        }
        List<UserUnionGroupVo> userUnionGroupVos =
            userContactMapper.selectUserUnionGroupById(tokenInfo.getUserId(), id);
        return ResultVo.success(userUnionGroupVos);
    }

    @Override
    public ResultVo selectUsers(String userId, Integer status) {
        List<UserContact> userContacts = userContactMapper.selectContactUsers(userId, status);
        return ResultVo.success(userContacts);
    }

    @ModifyProperty
    @Override
    public ResultVo selectUser(String userId) {
        UserInfo userInfo = null;
        if (userId.equals(sysSettingDto.getRobotUid())) {
            userInfo =
                new UserInfo(sysSettingDto.getRobotUid(), sysSettingDto.getRobotNickName(), sysSettingDto.getRobotSex(),
                    sysSettingDto.getRobotPersonalSignature(), sysSettingDto.getRobotAreaName());
        } else {
            userInfo = userInfoMapper.selectUserByUID(userId);

        }
        return ResultVo.success(userInfo);
    }

    /**
     * 添加机器人
     *
     * @param userId
     */
    @ModifyProperty
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void addContactRobot(String userId, String thisName) {
        long timeMillis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(timeMillis);
        String contactId = sysSettingDto.getRobotUid();
        String contactName = sysSettingDto.getRobotNickName();
        String sendMessage = sysSettingDto.getRobotWelcome();
        sendMessage = StringUtils.cleanHtmlTag(sendMessage);
        /*1、添加机器人好友*/
        UserContact userContact = new UserContact();
        userContact.setUserId(userId);
        userContact.setContactId(contactId);
        userContact.setContactRemarks(contactName);
        userContact.setCreateTime(timestamp);
        userContact.setUpdateTime(timestamp);
        userContact.setStatus(UserContactStatusEnum.FRIENDS.getStatus());
        userContactMapper.insertUserContact(userContact);
        /*2、添加会话信息*/
        String sessionId = StringUtils.generateSessionId(userId, contactId);
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(sendMessage);
        chatSession.setLastReceiveTime(timeMillis);
        chatSessionMapper.insertOrUpdate(chatSession);
        /*3、添加用户会话信息*/
        ChatSessionUser chatSessionUser = new ChatSessionUser();
        chatSessionUser.setUserId(userId);
        chatSessionUser.setContactId(contactId);
        chatSessionUser.setSessionId(sessionId);
        chatSessionUser.setContactName(contactName);
        chatSessionUserMapper.insertOrUpdate(chatSessionUser);
        /*4、添加聊天消息*/
        ChatMessage chatMessage =
            new ChatMessage(String.valueOf(UUID.randomUUID()), sessionId, MessageTypeEnum.CHAT.getType(), sendMessage,
                contactId, contactName, timeMillis, userId, 0, null, null, null, 1);
        chatMessageMapper.insertChatMessage(chatMessage);
        /*5、添加自己的会话*/
        String thisSessionID = StringUtils.generateSessionId(userId, userId);
        ChatSession thisChatSession = new ChatSession(thisSessionID, null, timeMillis);
        chatSessionMapper.insertOrUpdate(thisChatSession);
        /*6、添加自己会话信息*/
        ChatSessionUser thisChatSessionUser = new ChatSessionUser();
        thisChatSessionUser.setUserId(userId);
        thisChatSessionUser.setContactId(userId);
        thisChatSessionUser.setSessionId(thisSessionID);
        thisChatSessionUser.setContactName(thisName);
        chatSessionUserMapper.insertOrUpdate(thisChatSessionUser);
    }

    @Override
    public void initContactSession(String applyUserId, String receptionId, String applyInfo) {
        long time = System.currentTimeMillis();
        /*1、添加双方联系人到redis缓存里*/
        redisUtils.addContactToList(Constants.REDS_KEY_USERS_CONTACT, applyUserId, receptionId);
        redisUtils.addContactToList(Constants.REDS_KEY_USERS_CONTACT, receptionId, applyUserId);
        /*2、添加双方会话*/
        // 插入会话
        String sessionId = StringUtils.generateSessionId(applyUserId, receptionId);
        ChatSession chatSession = new ChatSession(sessionId, applyInfo, time);
        chatSessionMapper.insertOrUpdate(chatSession);

        List<ChatSessionUser> chatSessionUsers = new ArrayList<>();
        //申请人
        ChatSessionUser applyChatSessionUser = new ChatSessionUser();
        applyChatSessionUser.setUserId(applyUserId);
        applyChatSessionUser.setContactId(receptionId);
        applyChatSessionUser.setSessionId(sessionId);
        String applyName = userInfoMapper.selectNameById(applyUserId);
        applyChatSessionUser.setContactName(applyName);
        chatSessionUsers.add(applyChatSessionUser);
        //接受人
        ChatSessionUser receptionChatSessionUser = new ChatSessionUser();
        receptionChatSessionUser.setUserId(receptionId);
        receptionChatSessionUser.setContactId(applyUserId);
        receptionChatSessionUser.setSessionId(sessionId);
        String receptionName = userInfoMapper.selectNameById(receptionId);
        receptionChatSessionUser.setContactName(receptionName);
        chatSessionUsers.add(receptionChatSessionUser);
        // 批量插入用户会话
        chatSessionUserMapper.insertOrUpdateBatch(chatSessionUsers);

        /*3、添加消息表数据*/
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUuid(String.valueOf(UUID.randomUUID()));
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
        chatMessage.setMessageContent(applyInfo);
        chatMessage.setSendUserId(applyUserId);
        chatMessage.setSendUserNickName(applyName);
        chatMessage.setSendTime(time);
        chatMessage.setRecipientId(receptionId);
        chatMessage.setRecipientType(UserContactTypeEnum.USER.getType());
        chatMessageMapper.insertChatMessage(chatMessage);

        /*4、发送添加成功初始消息*/
        //发送给接受人
        MessageSendDto<WsInitData> sendDto = new MessageSendDto<>();
        BeanUtils.copyProperties(chatMessage, sendDto);
        sendDto.setStatus(1);
        messageHandler.sendMessage(sendDto);
        //发送给申请人
        sendDto.setContactIdTemp(sendDto.getRecipientId());
        sendDto.setRecipientId(sendDto.getSendUserId());
        // 切换联系人名称
        sendDto.setSendUserNickName(receptionName);
        messageHandler.sendMessage(sendDto);
    }

}
