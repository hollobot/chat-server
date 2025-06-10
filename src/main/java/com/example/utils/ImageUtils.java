package com.example.utils;

import com.example.entity.constants.Constants;
import com.example.entity.enums.UserContactTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class ImageUtils {

    /**
     * 保存图片头像到本地
     * @param id
     * @return
     */

    @Value("${project.folder}")
    private  String folder;

    public Boolean saveImage(MultipartFile multipartFile,String id,String type){
        String avatarPath=null;
        if (type.equals(UserContactTypeEnum.USER.getPrefix())){
            avatarPath = folder+ Constants.USER_AVATAR_FILE+id+Constants.USER_AVATAR_NAME_SUFFIX;
        }

        if(type.equals(UserContactTypeEnum.GROUP.getPrefix())){
            avatarPath = folder+ Constants.GROUP_AVATAR_FILE+id+Constants.GROUP_AVATAR_NAME_SUFFIX;
        }
        File file = isFile(avatarPath);
        try {
            /*会覆盖存在的文件*/
            multipartFile.transferTo(file);
        } catch (IOException e) {
            log.error("文件处理出现异常\n{}",e);
           return false;
        }
        return true;
    }

    /**
     * 判断文件是否存在，不存在创建
     * @param path
     * @return
     */
    public File isFile(String path){
        File file = new File(path);
        if (!file.exists()){
            file.mkdirs();
        }
        return file;
    }

}
