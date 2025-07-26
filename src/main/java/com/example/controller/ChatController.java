package com.example.controller;

import com.example.annotation.GlobalTokenInterceptor;
import com.example.entity.dto.MessageSendDto;
import com.example.entity.dto.SysSettingDto;
import com.example.entity.dto.TokenUserInfoDto;
import com.example.entity.enums.UserContactTypeEnum;
import com.example.entity.pojo.ChatMessage;
import com.example.entity.vo.ResultVo;
import com.example.service.ChatMessageService;
import com.example.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Validated
@GlobalTokenInterceptor
@Slf4j
@Api(tags = "聊天接口")  //名称可自定义
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private ChatMessageService chatMessageService;

    @ApiOperation("发送消息")
    @PostMapping("send")
    public ResultVo sendMessage(HttpServletRequest request, @NotBlank String uuid, @NotBlank String contactId,
        @NotBlank String messageContent, @NotNull Integer messageType, Long fileSize, String fileName,
        Integer fileType) {
        long timeMillis = System.currentTimeMillis();
        String token = request.getHeader("authorization");
        TokenUserInfoDto tokenInfo = redisUtils.getTokenInfo(token);
        /*1、判断是否是给自己发送消息*/
        if (tokenInfo.getUserId().equals(contactId)) {
            return ResultVo.success(null);
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUuid(uuid);
        chatMessage.setMessageType(messageType);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setSendUserId(tokenInfo.getUserId());
        chatMessage.setSendUserNickName(tokenInfo.getNickName());
        chatMessage.setSendTime(timeMillis);
        chatMessage.setRecipientId(contactId);
        UserContactTypeEnum byPrefix = UserContactTypeEnum.getByPrefix(contactId);
        chatMessage.setRecipientType(byPrefix.getType());
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        chatMessage.setFileType(fileType);
        MessageSendDto messageSendDto = chatMessageService.sendMessage(chatMessage);

        return ResultVo.success(messageSendDto);
    }

    @ApiOperation("上传文件")
    @PostMapping("upload")
    public ResultVo uploadFile(HttpServletRequest request, @NotNull String uuid, @NotNull MultipartFile file,
        MultipartFile cover) {
        String token = request.getHeader("authorization");
        TokenUserInfoDto tokenInfo = redisUtils.getTokenInfo(token);
        chatMessageService.saveMessageFile(tokenInfo.getUserId(), uuid, file, cover);
        return ResultVo.success("上传成功");
    }

    @ApiOperation("下载文件")
    @PostMapping("download")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, @NotBlank String fileId,
        @NotNull Boolean showCover) {
        System.out.println("下载文件"+fileId);
        String token = request.getHeader("authorization");
        TokenUserInfoDto tokenInfo = redisUtils.getTokenInfo(token);
        chatMessageService.downloadFile(tokenInfo, response, fileId, showCover);
    }

    @ApiOperation("获取文件限制信息")
    @GetMapping("fileConfig")
    public ResultVo<SysSettingDto> getFileConfig() {
        return ResultVo.success(chatMessageService.fileConfig());
    }

}
