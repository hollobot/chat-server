package com.example.service;
import com.example.entity.dto.MessageSendDto;
import com.example.entity.dto.SysSettingDto;
import com.example.entity.dto.TokenUserInfoDto;
import com.example.entity.pojo.ChatMessage;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;

public interface ChatMessageService {

    MessageSendDto sendMessage(ChatMessage chatMessage);

    void saveMessageFile(String userId, String uuid, MultipartFile file,MultipartFile cover);

    void downloadFile(TokenUserInfoDto tokenInfo, HttpServletResponse response,String fileId,Boolean showCover);

    SysSettingDto fileConfig();


}
