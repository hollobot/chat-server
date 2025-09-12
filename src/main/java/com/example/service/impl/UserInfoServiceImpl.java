package com.example.service.impl;

import com.example.entity.constants.Constants;
import com.example.entity.dto.MessageSendDto;
import com.example.entity.dto.PageConditionQueryDto;
import com.example.entity.dto.TokenUserInfoDto;
import com.example.entity.dto.UserInfoDto;
import com.example.entity.enums.ExceptionCodeEnum;
import com.example.entity.enums.MessageTypeEnum;
import com.example.entity.enums.UserContactTypeEnum;
import com.example.entity.enums.UserStatusEnum;
import com.example.entity.pojo.CustomAccount;
import com.example.entity.pojo.UserContact;
import com.example.entity.pojo.UserInfo;
import com.example.entity.vo.ResultVo;
import com.example.entity.vo.UserInfoVo;
import com.example.handler.CustomException;
import com.example.mapper.*;
import com.example.service.UserContactService;
import com.example.service.UserInfoService;
import com.example.utils.ImageUtils;
import com.example.utils.RedisUtils;
import com.example.utils.StringUtils;
import com.example.websocket.ChannelContextUtils;
import com.example.websocket.MessageHandler;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private CustomAccountMapper customAccountMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private GroupContactMapper groupContactMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private ImageUtils imageUtils;

    @Resource
    private UserContactService userContactService;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Value("${admin.email}")
    private String adminEmails;

    @Resource
    private MessageHandler messageHandler;
    @Autowired
    private ChatSessionUserMapper chatSessionUserMapper;

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo registerService(UserInfoDto userInfoDto) {

        //1、判断验证码是否正确
        String code = redisUtils.get(Constants.REDS_KEY_CHECK_CODE + userInfoDto.getCodeKey());
        if (!userInfoDto.getCheckCode().equals(code)) {
            return ResultVo.error("验证码错误");
        }

        //2、判断是否存在邮箱
        UserInfo userInfo = userInfoMapper.selectUserByEmail(userInfoDto.getEmail());
        if (userInfo != null) {
            return ResultVo.error("邮箱已经注册");
        }

        /*封装数据*/
        userInfo = new UserInfo();
        userInfo.setEmail(userInfoDto.getEmail());
        userInfo.setNickName(userInfoDto.getNickName());
        /*加密*/
        userInfo.setPassword(DigestUtils.md5DigestAsHex(userInfoDto.getPassword().getBytes()));
        /*当前时间*/
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        userInfo.setCreateTime(timestamp);
        userInfo.setLastLoginTime(timestamp);
        userInfo.setLastOffTime(System.currentTimeMillis());

        String userId = StringUtils.generateUserId();
        //3、判断是否是自定义邮箱
        CustomAccount customAccount = customAccountMapper.selectCustomAccountByEmail(userInfo.getEmail());
        if (customAccount != null && customAccount.getStatus() == 1) {
            userId = UserContactTypeEnum.USER.getPrefix() + customAccount.getUserId();
        }
        userInfo.setUserId(userId);
        userInfo.setNickName("用户:" + userId);

        //4、插入数据
        Integer integer = userInfoMapper.insertUserInfo(userInfo);
        if (customAccount != null && customAccount.getStatus() == 1 && integer == 1) {
            /*修改自定义账户，已经被使用*/
            customAccountMapper.updateStatusByEmail(0, customAccount.getEmail());
        }

        /*5、添加自己为联系人*/
        UserContact userContact = new UserContact(userInfo.getUserId(), userInfo.getUserId(), userInfo.getNickName(), 1,
            userInfo.getCreateTime(), userInfo.getCreateTime());
        userContactMapper.insertUserContact(userContact);

        /*6、添加机器人*/
        userContactService.addContactRobot(userContact.getUserId(),userInfo.getNickName());
        return ResultVo.success("注册成功");
    }

    @Override
    public ResultVo loginService(UserInfoDto userInfoDto) {
        /*1、判断验证码是否正确*/
        String code = redisUtils.get(Constants.REDS_KEY_CHECK_CODE + userInfoDto.getCodeKey());
        if (!userInfoDto.getCheckCode().equals(code)) {
            return ResultVo.error("验证码错误");
        }

        /*2、判断账户密码是否正确*/
        UserInfo userInfo = userInfoMapper.selectUserByEmail(userInfoDto.getEmail());
        String password = DigestUtils.md5DigestAsHex(userInfoDto.getPassword().getBytes());
        if (userInfo == null || !userInfo.getPassword().equals(password)) {
            return ResultVo.error("账户或密码错误");
        }

        /*3、判断账户是否被禁用*/
        if (userInfo.getStatus() == UserStatusEnum.DISABLE.getStatus()) {
            return ResultVo.error("账户已禁用");
        }

        /*4、判断账户是否在别处登录*/
        Long heartbeat = redisUtils.getHeartbeat(userInfo.getUserId());
        if (heartbeat != null) {
            return ResultVo.error("账户已经在别处登录，请退出后登录");
        }

        /*5、初始化联系人缓存*/
        List<String> contactUserIds = userContactMapper.selectUserContactIds(userInfo.getUserId());
        List<String> contactGroupIds = groupContactMapper.selectGroupContactIds(userInfo.getUserId());
        redisUtils.setContactIds(Constants.REDS_KEY_USERS_CONTACT, userInfo.getUserId(), contactUserIds);
        redisUtils.setContactIds(Constants.REDS_KEY_GROUPS_CONTACT, userInfo.getUserId(), contactGroupIds);

        /*6、跟新最后登录时间*/
        userInfoMapper.changeUpdateTime(userInfo.getUserId());

        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        /*7、判断是否为管理员账户*/
        String[] emails = adminEmails.split(",");
        tokenUserInfoDto.setIsAdmin(Arrays.asList(emails).contains(userInfo.getEmail()));

        /*8、生成token*/
        String token = StringUtils.generateToken();

        /*9、账户密码正确生成token存入redis*/
        BeanUtils.copyProperties(userInfo, tokenUserInfoDto);
        tokenUserInfoDto.setToken(token);
        redisUtils.saveToken(tokenUserInfoDto);

        /*10、将userId 与 token 关联上存入redis*/
        redisUtils.saveUserIdAndToken(userInfo.getUserId(), token);

        /*11、封装返回客户端的数据*/
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        userInfoVo.setToken(token);
        userInfoVo.setIsAdmin(tokenUserInfoDto.getIsAdmin());

        return ResultVo.success("登入成功", userInfoVo);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo saveUserInfo(UserInfo userInfo, MultipartFile avatarFile) {
        /*1、插入数据*/
        userInfoMapper.updateUserInfo(userInfo);

        /*2、修改联系人的昵称 & 会话昵称*/
        if (userInfo.getNickName() != null) {
            /*修改联系人的昵称*/
            userContactMapper.updateContactRemarks(userInfo.getUserId(), userInfo.getNickName());
            /*会话昵称*/
            chatSessionUserMapper.updateName(userInfo.getNickName(), userInfo.getUserId());
        }

        /*3、保存头像*/
        if (avatarFile == null) {
            return ResultVo.success("修改成功");
        }
        Boolean aBoolean = imageUtils.saveImage(avatarFile, userInfo.getUserId(), UserContactTypeEnum.USER.getPrefix());
        if (!aBoolean) {
            throw new CustomException(ExceptionCodeEnum.CODE_400);
        }

        return ResultVo.success("修改成功");
    }

    @Override
    public ResultVo getUserInfo(String userId, String isAdmin, HttpServletRequest request) {
        /*1、查询数据*/
        UserInfo userInfo = userInfoMapper.selectUserInfoById(userId);
        /*2、封装数据*/
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        String token = request.getHeader("authorization");
        userInfoVo.setToken(token);
        userInfoVo.setIsAdmin(Boolean.parseBoolean(isAdmin));

        return ResultVo.success(userInfoVo);
    }

    @Override
    public ResultVo changeUserPwd(String userId, String oldPwd, String newPwd) {
        /*1、根据UID判断账户密码是否正确*/
        UserInfo userInfo = userInfoMapper.selectUserInfoById(userId);
        String password = DigestUtils.md5DigestAsHex(oldPwd.getBytes());
        if (userInfo == null || !userInfo.getPassword().equals(password)) {
            return ResultVo.error("原密码错误");
        }
        /*2、修改密码*/
        Integer integer = userInfoMapper.updatePwd(userId, DigestUtils.md5DigestAsHex(newPwd.getBytes()));
        if (integer != 1) {
            return ResultVo.error("修改失败，请稍后再次尝试");
        }
        return ResultVo.success("修改成功");
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo userChangeStatus(String userId) {
        return ResultVo.success(userInfoMapper.updateStatus(userId));
    }

    /**
     * 下线处理
     *
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResultVo userOffLine(String userId) {
        /*1、发送强制下线处理信息*/
        MessageSendDto<Object> sendDto = new MessageSendDto<>();
        sendDto.setRecipientId(userId);
        sendDto.setMessageType(MessageTypeEnum.FORCE_OFF_LINE.getType());
        sendDto.setRecipientType(UserContactTypeEnum.USER.getType());
        /*发送消息会处理 强制下线 的操作*/
        messageHandler.sendMessage(sendDto);
        return ResultVo.success();
    }

    @Override
    public ResultVo logoutUser(HttpServletRequest request) {
        /*1、获取请求的token*/
        String token = request.getHeader("authorization");
        TokenUserInfoDto tokenInfo = redisUtils.getTokenInfo(token);
        /*2、删除redis的token信息*/
        redisUtils.delete(Constants.REDS_KEY_TOKEN + token);
        /*2、删除redis的与该token绑定的userId*/
        channelContextUtils.closeContext(tokenInfo.getUserId());
        return ResultVo.success(true);
    }

    /**
     * 分页条件查询所有用户
     *
     * @param pageInfo
     * @return
     */
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
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(pageInfo, userInfo);
        List<UserInfo> users = userInfoMapper.conditionQuery(userInfo);
        // 用 PageInfo 包装结果（包含分页信息）
        PageInfo<UserInfo> userInfoPageInfo = new PageInfo<>(users);

        return ResultVo.success(userInfoPageInfo);
    }
}
