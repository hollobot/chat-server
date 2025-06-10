package com.example.service.impl;

import com.example.entity.constants.Constants;
import com.example.entity.dto.BatchGroupContactDto;
import com.example.entity.dto.MessageSendDto;
import com.example.entity.dto.WsInitData;

import com.example.entity.enums.ExceptionCodeEnum;
import com.example.entity.enums.MessageTypeEnum;
import com.example.entity.enums.UserContactTypeEnum;
import com.example.entity.pojo.*;
import com.example.entity.vo.GroupUserInfoVo;
import com.example.entity.vo.ResultVo;
import com.example.handler.CustomException;
import com.example.mapper.*;
import com.example.service.GroupContactService;
import com.example.utils.RedisUtils;
import com.example.utils.StringUtils;
import com.example.websocket.ChannelContextUtils;
import com.example.websocket.MessageHandler;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class GroupContactServiceImpl implements GroupContactService {

    @Resource
    private GroupContactMapper groupContactMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private GroupApplyInfoMapper groupApplyInfoMapper;

    @Override
    public ResultVo selectGroupUserNumber(String groupId) {
        Integer count = groupContactMapper.selectGroupUserCount(groupId);
        return ResultVo.success(count);
    }

    @Override
    public ResultVo saveGroupContact(GroupContact groupContact, String applicationName) {
        /*1、判断是否存在群联系人数据*/
        GroupContact existGroupContact =
            groupContactMapper.selectContactGroup(groupContact.getUserId(), groupContact.getGroupId());
        if (existGroupContact == null) {
            /*不存在初始化群联系人数据，status=0*/
            groupContactMapper.insertInitGroupContact(groupContact);
        } else {
            /*存在跟新数据不跟新status*/
            groupContactMapper.updateGroupContact(groupContact);
        }

        /*3、判断是否可以添加成为联系人，修改status==1成为联系人*/
        if (groupContact.getStatus() == 1) {
            groupContactMapper.updateGroupContactStatus(groupContact.getUserId(), groupContact.getGroupId(),
                groupContact.getStatus());
            /*4、创建群聊会话、缓存群聊联系人、将用户channel添加到channelGroup里面。。。*/
            GroupApplyInfo groupApplyInfo =
                groupApplyInfoMapper.selectApplyInfoById(groupContact.getUserId(), groupContact.getGroupId());
            /*处理初始加入群聊消息 %s加入了群组*/
            String msgContent = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), applicationName);
            MessageTypeEnum addGroupTypeEnum = MessageTypeEnum.ADD_GROUP;
            addGroupTypeEnum.setInitMessage(msgContent);

            createOrAddGroupSession(groupContact.getUserId(), groupContact.getGroupId(), groupApplyInfo.getApplyInfo(),
                addGroupTypeEnum);
            return ResultVo.success("添加成功！。", 1);
        }

        /*4、发送群聊申请添加消息*/
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.CONTACT_GROUP_APPLY.getType());
        messageSendDto.setRecipientId(groupContact.getOwnerId());
        messageSendDto.setSendUserId(groupContact.getGroupId());
        messageSendDto.setRecipientType(UserContactTypeEnum.USER.getType());
        messageHandler.sendMessage(messageSendDto);
        return ResultVo.success("申请成功！等待审核。", 0);
    }

    @Override
    public ResultVo selectGroups(String userId) {
        /*1、查询我的群聊列表*/
        List<GroupContact> myGroupContacts = groupContactMapper.selectContactGroups(userId, userId);
        /*2、查询我加入群聊列表*/
        List<GroupContact> joinGroupContacts = groupContactMapper.selectContactGroups(userId, null);
        /*3、封装数据返回给前端页面*/
        HashMap<String, List<GroupContact>> stringListHashMap = new HashMap<>();
        stringListHashMap.put("myGroupContacts", myGroupContacts);
        stringListHashMap.put("joinGroupContacts", joinGroupContacts);
        return ResultVo.success(stringListHashMap);
    }

    @Override
    public ResultVo selectGroupUsersByGroupId(String groupId, String userId) {
        List<GroupUserInfoVo> userContacts = userInfoMapper.selectGroupUsers(groupId, userId);
        return ResultVo.success(userContacts);
    }

    @Override
    public void createOrAddGroupSession(String applyUserId, String groupId, String applyInfo,
        MessageTypeEnum messageTypeEnum) {
        long time = System.currentTimeMillis();
        GroupInfo groupInfo = groupInfoMapper.selectGroupById(groupId);

        /*1、添加群聊到redis缓存里*/
        redisUtils.addContactToList(Constants.REDS_KEY_GROUPS_CONTACT, applyUserId, groupId);

        // 插入会话
        String sessionId = StringUtils.generateSessionId(groupId);
        ChatSession chatSession = new ChatSession(sessionId, applyInfo, time);
        chatSessionMapper.insertOrUpdate(chatSession);

        //申请人
        ChatSessionUser applyChatSessionUser = new ChatSessionUser();
        applyChatSessionUser.setUserId(applyUserId);
        applyChatSessionUser.setContactId(groupId);
        applyChatSessionUser.setSessionId(sessionId);
        applyChatSessionUser.setContactName(groupInfo.getGroupName());
        // 插入用户会话
        chatSessionUserMapper.insertOrUpdate(applyChatSessionUser);

        /*4、添加消息表数据*/
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUuid(String.valueOf(UUID.randomUUID()));
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(messageTypeEnum.getType());
        chatMessage.setMessageContent(messageTypeEnum.getInitMessage());
        chatMessage.setSendUserId(applyUserId);
        chatMessage.setSendTime(time);
        chatMessage.setRecipientId(groupId);
        chatMessage.setRecipientType(UserContactTypeEnum.GROUP.getType());
        chatMessageMapper.insertChatMessage(chatMessage);

        /*5、将用户channel添加到channelGroup里面*/
        Channel channel = channelContextUtils.getChannelByUserId(applyUserId);
        channelContextUtils.addOrDelGroupChannel(groupId, channel, 1);

        /*6、发送添加成功初始消息*/
        //发送给接受人
        MessageSendDto<WsInitData> sendDto = new MessageSendDto<>();
        BeanUtils.copyProperties(chatMessage, sendDto);
        //查询群名
        sendDto.setContactName(groupInfo.getGroupName());
        //查询群人数
        Integer count = groupContactMapper.selectGroupUserCount(groupId);
        sendDto.setMemberCount(count);
        messageHandler.sendMessage(sendDto);

    }

    /**
     * 群主批量操作群聊联系人
     *
     * @param bgc
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public ResultVo batchGroupContact(BatchGroupContactDto bgc) {
        ArrayList<String> userIds = (ArrayList<String>)bgc.getUserIds();
        ArrayList<String> names = (ArrayList<String>)bgc.getNames();
        String groupId = bgc.getGroupId();
        String groupName = bgc.getGroupName();
        Long nowTime = System.currentTimeMillis();
        String sessionId = bgc.getGroupSessionId();
        /*1、添加or删除群聊为联系人*/
        groupContactMapper.batchInsertOrUpdate(bgc);
        /*2、redis 批量添加or删除群聊联系人*/
        redisUtils.batchAppendOrRemoverContacts(Constants.REDS_KEY_GROUPS_CONTACT, userIds, groupId, bgc.getType());
        List<ChatSessionUser> chatSessionUsers = new ArrayList<>();
        List<ChatMessage> chatMessages = new ArrayList<>();
        List<MessageSendDto<WsInitData>> messageSends = new ArrayList<>();
        if (bgc.getType() == 1) {
            /*3、批量添加会话,消息*/
            for (int i = 0; i < userIds.size(); i++) {
                // 封装会话对象
                String userId = userIds.get(i);
                String name = names.get(i);
                ChatSessionUser chatSessionUser = new ChatSessionUser();
                chatSessionUser.setUserId(userId);
                chatSessionUser.setContactId(groupId);
                chatSessionUser.setSessionId(sessionId);
                chatSessionUser.setContactName(groupName);
                chatSessionUsers.add(chatSessionUser);
                //封装消息对象
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setUuid(UUID.randomUUID().toString());
                chatMessage.setSessionId(sessionId);
                chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
                chatMessage.setMessageContent(String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), name));
                chatMessage.setSendUserId(userId);
                chatMessage.setSendUserNickName(name);
                chatMessage.setSendTime(nowTime);
                chatMessage.setRecipientId(groupId);
                chatMessage.setRecipientType(UserContactTypeEnum.GROUP.getType());
                chatMessage.setStatus(1);
                chatMessages.add(chatMessage);
                //封装ws消息
                MessageSendDto<WsInitData> sendDto = new MessageSendDto<>();
                BeanUtils.copyProperties(chatMessage, sendDto);
                sendDto.setContactName(groupName);
                messageSends.add(sendDto);
                //将用户添加到groupChannel
                Channel channel = channelContextUtils.getChannelByUserId(userId);
                channelContextUtils.addOrDelGroupChannel(groupId, channel, 1);

            }
            chatSessionUserMapper.insertOrUpdateBatch(chatSessionUsers);
            chatMessageMapper.batchInsertMessages(chatMessages);
            /*4、发送ws消息、跟新群聊人数*/
            Integer count = groupContactMapper.selectGroupUserCount(groupId);
            for (MessageSendDto send : messageSends) {
                send.setMemberCount(count);
                log.error("{}",send.toString());
                messageHandler.sendMessage(send);
            }
        } else {

            /*3、批量删除会话*/
            chatSessionUserMapper.deleteBatch(userIds, groupId);
            /*4、批量添加消息*/
            for (int i = 0; i < userIds.size(); i++) {
                // 封装消息对象
                String userId = userIds.get(i);
                String name = names.get(i);
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setUuid(UUID.randomUUID().toString());
                chatMessage.setSessionId(sessionId);
                if (bgc.getBreakType() == 1) {
                    chatMessage.setMessageType(MessageTypeEnum.LEAVE_GROUP.getType());
                    chatMessage.setMessageContent(String.format(MessageTypeEnum.LEAVE_GROUP.getInitMessage(), name));
                } else {
                    chatMessage.setMessageType(MessageTypeEnum.REMOVE_GROUP.getType());
                    chatMessage.setMessageContent(String.format(MessageTypeEnum.REMOVE_GROUP.getInitMessage(), name));
                }
                chatMessage.setSendUserId(userId);
                chatMessage.setSendUserNickName(name);
                chatMessage.setSendTime(nowTime);
                chatMessage.setRecipientId(groupId);
                chatMessage.setRecipientType(UserContactTypeEnum.GROUP.getType());
                chatMessage.setStatus(1);
                chatMessages.add(chatMessage);
                // 封装ws消息
                MessageSendDto<WsInitData> sendDto = new MessageSendDto<>();
                BeanUtils.copyProperties(chatMessage, sendDto);
                sendDto.setContactName(groupName);
                messageSends.add(sendDto);

            }
            chatMessageMapper.batchInsertMessages(chatMessages);

            /*5、发送ws消息提示被群主提出群聊、跟新群聊人数*/
            Integer count = groupContactMapper.selectGroupUserCount(groupId);
            for (MessageSendDto send : messageSends) {
                send.setMemberCount(count);
                messageHandler.sendMessage(send);
            }
        }
        return ResultVo.success("操作成功");
    }

}
