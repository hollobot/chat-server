package com.example.websocket;

import com.alibaba.fastjson.JSON;
import com.example.entity.constants.Constants;
import com.example.entity.dto.MessageSendDto;
import com.example.entity.dto.WsInitData;
import com.example.entity.enums.*;
import com.example.entity.pojo.ChatMessage;
import com.example.entity.pojo.ChatSessionUser;
import com.example.entity.pojo.UserInfo;
import com.example.mapper.*;
import com.example.utils.RedisUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChannelContextUtils {

    // 定义一个全局 AttributeKey
    private static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");

    private static final ConcurrentHashMap<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap();

    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CHANNEL_MAP = new ConcurrentHashMap();

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private UserApplyInfoMapper userApplyInfoMapper;

    @Resource
    private GroupApplyInfoMapper groupApplyInfoMapper;

    public void addContext(String userId, Channel channel) {
        /*1、channel注册userId属性*/
        setUserId(userId, channel);
        /*2、装入内存map里*/
        USER_CHANNEL_MAP.put(userId, channel);
        /*3、将注册的用户channel添加到联系人群聊channelGroup里*/
        List<String> contactIds = redisUtils.getContactIds(Constants.REDS_KEY_GROUPS_CONTACT, userId);
        log.error("contactIds:{}", contactIds);
        for (String groupId : contactIds) {
            if (groupId.startsWith(UserContactTypeEnum.GROUP.getPrefix())) {
                addOrDelGroupChannel(groupId, channel, 1);
            }
        }
        /**
         *   4、给用户发送收到的消息（最多最近三天）
         */
        /*1、获取最后离线时间*/
        UserInfo userInfo = userInfoMapper.selectUserInfoById(userId);
        Long lastOffTime = userInfo.getLastOffTime();

        if (lastOffTime != null && System.currentTimeMillis() - Constants.MILLIS_3_DAYS > lastOffTime) {
            lastOffTime = System.currentTimeMillis() - Constants.MILLIS_3_DAYS;
        }
        WsInitData wsInitData = new WsInitData();
        /*1、查询会话消息*/
        List<ChatSessionUser> chatSessionUsers = chatSessionUserMapper.selectAll(userId);
        wsInitData.setChatSessionUserList(chatSessionUsers);
        /*2、查询所有消息*/
        List<String> groupIds = redisUtils.getContactIds(Constants.REDS_KEY_GROUPS_CONTACT, userId);
        groupIds.add(userId);
        List<ChatMessage> chatMessages = chatMessageMapper.selectChatMessageList(lastOffTime, groupIds);
        wsInitData.setChatMessageList(chatMessages);

        /*3、查询联系人申请数据 离线时间<申请时间*/
        Integer userApplyCount =
            userApplyInfoMapper.selectApplyCount(userId, ApplyStatusEnum.APPLYING.getType(), lastOffTime);
        Integer groupApplyCount =
            groupApplyInfoMapper.selectApplyCount(userId, ApplyStatusEnum.APPLYING.getType(), lastOffTime);
        wsInitData.setApplyUserCount(userApplyCount);
        wsInitData.setApplyGroupCount(groupApplyCount);

        /*4、发送消息*/
        MessageSendDto<WsInitData> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setSendUserId(userId);
        messageSendDto.setRecipientId(userId);
        messageSendDto.setRecipientType(UserContactTypeEnum.USER.getType());
        messageSendDto.setExtendData(wsInitData);

        sendMsg(messageSendDto);
    }

    /**
     * 发送消息通用方法
     *
     * @param messageSendDto
     */
    public void sendMsg(MessageSendDto messageSendDto) {
        Integer recipientType = messageSendDto.getRecipientType();
        switch (recipientType) {
            case 0:
                sendToUser(messageSendDto);
                break;
            case 1:
                sendToGroup(messageSendDto);
                break;

        }
    }

    public void sendToUser(MessageSendDto messageSendDto) {
        String recipientId = messageSendDto.getRecipientId();
        if (recipientId == null) {
            return;
        }
        Channel channel = USER_CHANNEL_MAP.get(recipientId);
        if (channel == null) {
            return;
        }

        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageSendDto)));
        /*判断消息数据类型是否为强制下线*/
        if (messageSendDto.getMessageType() == MessageTypeEnum.FORCE_OFF_LINE.getType()) {
            closeContext(recipientId);
        }
    }

    /**
     * 群聊发送消息
     *
     * @param messageSendDto
     */
    public void sendToGroup(MessageSendDto messageSendDto) {
        String recipientId = messageSendDto.getRecipientId();
        if (recipientId == null) {
            return;
        }
        /*1、获取map集合里的ChannelGroup*/
        ChannelGroup channelGroup = GROUP_CHANNEL_MAP.get(recipientId);
        if (channelGroup == null) {
            return;
        }
        /*2、发送群聊消息*/
        channelGroup.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageSendDto)));
    }

    /**
     * 将用户的channel注册到群聊channelGroup里
     *
     * @param groupId
     * @param channel
     */
    public void addOrDelGroupChannel(String groupId, Channel channel, Integer type) {
        ChannelGroup channelGroup = GROUP_CHANNEL_MAP.get(groupId);
        if (channelGroup == null) {
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CHANNEL_MAP.put(groupId, channelGroup);
        }

        if (channel == null) {
            return;
        }

        if (type == 1) {
            channelGroup.add(channel);

        } else if(type == 0){
            channelGroup.remove(channel);
        }

    }

    /**
     * 退出下线处理
     *
     * @param userId
     */
    public void closeContext(String userId) {
        /*处理channel*/
        removeContext(getChannelByUserId(userId));
        log.error("退出下线");
        /*处理token*/
        redisUtils.delTokenInfoByUserId(userId);
    }

    /**
     * 掉线处理数据
     *
     * @param channel
     */
    public void removeContext(Channel channel) {
        String userId = getUserId(channel);
        if (userId == null) {
            return;
        }
        /*1、删除map里的channel*/
        USER_CHANNEL_MAP.remove(userId);
        channel.close();
        /*2、跟新用户最后离线时间*/
        userInfoMapper.changeLastOffTime(userId, System.currentTimeMillis());
    }

    // 获取 userId
    public String getUserId(Channel channel) {
        if (channel == null) {
            return null;
        }
        return channel.attr(USER_ID_KEY).get();
    }

    // 绑定 userId 到 Channel
    public void setUserId(String userId, Channel channel) {
        channel.attr(USER_ID_KEY).set(userId);
    }

    public Channel getChannelByUserId(String userId) {
        return USER_CHANNEL_MAP.get(userId);
    }

    // 删除群聊channel
    public ChannelGroup removerGroupChannel(String groupId) {
        return GROUP_CHANNEL_MAP.remove(groupId);
    }

}
