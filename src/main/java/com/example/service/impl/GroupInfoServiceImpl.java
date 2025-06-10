package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.annotation.ModifyProperty;
import com.example.entity.constants.Constants;
import com.example.entity.dto.*;
import com.example.entity.enums.ExceptionCodeEnum;
import com.example.entity.enums.MessageTypeEnum;
import com.example.entity.enums.UserContactStatusEnum;
import com.example.entity.enums.UserContactTypeEnum;
import com.example.entity.pojo.*;
import com.example.entity.vo.ResultVo;
import com.example.handler.CustomException;
import com.example.mapper.ChatMessageMapper;
import com.example.mapper.ChatSessionUserMapper;
import com.example.mapper.GroupContactMapper;
import com.example.mapper.GroupInfoMapper;
import com.example.service.GroupContactService;
import com.example.service.GroupInfoService;
import com.example.utils.ArrayUtils;
import com.example.utils.ImageUtils;
import com.example.utils.RedisUtils;
import com.example.utils.StringUtils;
import com.example.websocket.MessageHandler;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sun.xml.internal.ws.resources.SenderMessages;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupInfoServiceImpl implements GroupInfoService {

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private GroupContactMapper groupContactMapper;

    @Resource
    private ImageUtils imageUtils;

    @Resource
    private GroupContactService groupContactService;

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
    private RedisUtils redisUtils;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private MessageHandler messageHandler;

    @Value("${admin.email}")
    private String adminEmails;

    @Resource
    private ArrayUtils<String> stringArrayUtils;

    @ModifyProperty
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public ResultVo insertGroupService(GroupInfo groupInfo, MultipartFile avatarFile) {

        /*1、判断当前用户创建是否超过系统设置最大创建群数量*/
        Integer count = groupInfoMapper.selectCountByOwnerId(groupInfo.getOwnerId());

        if (count >= sysSettingDto.getMaxGroupCount()) {
            return ResultVo.error("创建失败，超出最大创建群数量");
        }

        /*2、添加群聊*/
        Timestamp newTime = new Timestamp(System.currentTimeMillis());
        groupInfo.setGroupId(StringUtils.generateGroupId());
        groupInfo.setStatus(1);
        groupInfo.setCreateTime(newTime);
        groupInfoMapper.insertGroupInfo(groupInfo);

        /*3、将群聊添加为群主联系人*/
        GroupContact groupContact = new GroupContact();
        groupContact.setUserId(groupInfo.getOwnerId());
        groupContact.setGroupId(groupInfo.getGroupId());
        groupContact.setGroupRemarks(groupInfo.getGroupName());
        groupContact.setStatus(UserContactStatusEnum.FRIENDS.getStatus());
        groupContact.setCreateTime(newTime);
        groupContact.setUpdateTime(newTime);
        groupContact.setOwnerId(groupInfo.getOwnerId());
        /*插入群聊联系人*/
        groupContactMapper.insertGroupContact(groupContact);

        /*4、保存头像文件到本地*/
        Boolean aBoolean =
            imageUtils.saveImage(avatarFile, groupInfo.getGroupId(), UserContactTypeEnum.GROUP.getPrefix());
        if (!aBoolean) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);
        }

        /*5、创建群聊会话、缓存群聊联系人、将用户channel添加到channelGroup里面。。。*/
        groupContactService.createOrAddGroupSession(groupInfo.getOwnerId(), groupInfo.getGroupId(), null,
            MessageTypeEnum.GROUP_CREATE);

        return ResultVo.success("创建成功");
    }

    @Override
    public ResultVo userChangeStatus(GroupInfo groupInfo,HttpServletRequest request) {
        BatchGroupContactDto bgc = new BatchGroupContactDto();
        BeanUtils.copyProperties(groupInfo,bgc);
        bgc.setType(0);
        String sessionId = StringUtils.generateSessionId(bgc.getGroupId());
        bgc.setGroupSessionId(sessionId);
        /*查询符合是数据*/
        QueryWrapper<GroupContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", bgc.getGroupId())
            .select("user_id");
        List<GroupContact> groupContacts = groupContactMapper.selectList(queryWrapper);

        // 然后提取user_id
        List<String> userIds = groupContacts.stream()
            .map(GroupContact::getUserId)
            .collect(Collectors.toList());
        bgc.setUserIds(userIds);
        return disbandGroup(bgc,request);
    }

    @Override
    public ResultVo selectGroupInfoByGroupId(String groupId) {
        return ResultVo.success(groupInfoMapper.selectGroupById(groupId));
    }

    /**
     * 解散群聊
     *
     * @param bgc
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo disbandGroup(BatchGroupContactDto bgc, HttpServletRequest request) {
        /*1、判断权限是否是群主操作*/
        String ownerId = bgc.getOwnerId();
        String groupId = bgc.getGroupId();
        Integer status = bgc.getType();
        String sessionId = bgc.getGroupSessionId();
        List<String> userIds = bgc.getUserIds();
        String token = request.getHeader("authorization");
        TokenUserInfoDto tokenInfo = redisUtils.getTokenInfo(token);
        Long nowTime = System.currentTimeMillis();
        String[] admins = adminEmails.split(",");
        if (!tokenInfo.getUserId().equals(ownerId) && !stringArrayUtils.contains(admins, tokenInfo.getEmail())) {
            throw new CustomException(ExceptionCodeEnum.CODE_403.setMessage("无权限操作"));
        }
        /*2、逻辑删除群聊*/
        groupInfoMapper.updateGroupStatus(groupId, status);
        /*3、将该群聊里的所有用户删除群聊联系人*/
        groupContactMapper.batchInsertOrUpdate(bgc);
        /*4、redis操作*/
        redisUtils.batchAppendOrRemoverContacts(Constants.REDS_KEY_GROUPS_CONTACT, userIds, groupId, bgc.getType());
        /*5、批量删除会话*/
        chatSessionUserMapper.deleteBatch(userIds, groupId);
        List<ChatMessage> chatMessages = new ArrayList<>();
        /*6、添加消息*/
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUuid(UUID.randomUUID().toString());
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.DISSOLUTION_GROUP.getType());
        chatMessage.setMessageContent(MessageTypeEnum.DISSOLUTION_GROUP.getInitMessage());
        chatMessage.setSendUserId(ownerId);
        chatMessage.setSendTime(nowTime);
        chatMessage.setRecipientId(groupId);
        chatMessage.setRecipientType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(1);
        chatMessages.add(chatMessage);
        chatMessageMapper.batchInsertMessages(chatMessages);
        /*7、发送ws消息、删除群聊channel*/
        MessageSendDto<WsInitData> sendDto = new MessageSendDto<>();
        BeanUtils.copyProperties(chatMessage, sendDto);
        sendDto.setContactName(bgc.getGroupName());
        messageHandler.sendMessage(sendDto);
        return ResultVo.success("操作成功");
    }

    /**
     * 修改群聊信息
     *
     * @param groupInfo
     * @param avatarFile
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo saverGroupInfo(GroupInfo groupInfo, MultipartFile avatarFile, HttpServletRequest request) {
        /*1、判断权限*/
        String token = request.getHeader("authorization");
        TokenUserInfoDto tokenInfo = redisUtils.getTokenInfo(token);
        String groupId = groupInfo.getGroupId();
        String userId = tokenInfo.getUserId();
        Long nowTime = System.currentTimeMillis();
        GroupInfo data = groupInfoMapper.selectGroupById(groupId);
        if (!userId.equals(data.getOwnerId())) {
            throw new CustomException(ExceptionCodeEnum.CODE_403);
        }
        /*2、更改头像*/
        if (avatarFile != null) {
            Boolean aBoolean = imageUtils.saveImage(avatarFile, groupId, UserContactTypeEnum.GROUP.getPrefix());
            if (!aBoolean) {
                throw new CustomException(ExceptionCodeEnum.CODE_400);
            }
        }
        /*3、更改群聊表信息*/
        groupInfoMapper.updateInfo(groupInfo);
        /*4、更改用户联系人备注*/
        GroupContact groupContact = new GroupContact();
        groupContact.setGroupId(groupId);
        groupContact.setUserId(userId);
        groupContact.setGroupRemarks(groupInfo.getGroupName());
        groupContact.setUpdateTime(new Timestamp(nowTime));
        groupContactMapper.updateGroupContacts(groupContact);
        /*5、修改会话信息*/
        chatSessionUserMapper.updateName(groupInfo.getGroupName(), groupId);
        /*6、添加消息*/
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUuid(UUID.randomUUID().toString());
        String sessionId = StringUtils.generateSessionId(groupId);
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.GROUP_NAME_UPDATE.getType());
        chatMessage.setMessageContent(MessageTypeEnum.GROUP_NAME_UPDATE.getInitMessage());
        chatMessage.setSendUserId(userId);
        chatMessage.setSendUserNickName(tokenInfo.getNickName());
        chatMessage.setSendTime(nowTime);
        chatMessage.setRecipientId(groupId);
        chatMessage.setRecipientType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(1);
        chatMessageMapper.insertChatMessage(chatMessage);
        /*7、发送ws消息*/
        MessageSendDto sendMsg = new MessageSendDto<>();
        BeanUtils.copyProperties(chatMessage, sendMsg);
        sendMsg.setContactName(groupInfo.getGroupName());
        messageHandler.sendMessage(sendMsg);
        return ResultVo.success("修改成功");
    }

    @Override
    public ResultVo pageConditionQuery(PageConditionQueryDto pageInfo) {
        Integer pageNum = pageInfo.getPageNum();
        Integer pageSize = pageInfo.getPageSize();
        /*1、校验参数*/
        if (pageNum <= 0 || pageSize <= 0) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);
        }
        /*2、设置分页参数*/
        PageHelper.startPage(pageNum, pageSize);
        /*3、条件查询数据*/
        QueryWrapper<GroupInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("group_id", pageInfo.getGroupId()).like("group_name", pageInfo.getGroupName());
        List<GroupInfo> groupInfos = groupInfoMapper.selectList(queryWrapper);
        // 用 PageInfo 包装结果（包含分页信息）
        PageInfo<GroupInfo> groupInfosPageInfo = new PageInfo<>(groupInfos);
        return ResultVo.success(groupInfosPageInfo);
    }

}
