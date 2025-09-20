package com.example.utils;

import com.example.entity.constants.Constants;
import com.example.entity.enums.UserContactTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
public class ImageUtils {

    /**
     * 保存图片头像到本地
     *
     * @param id
     * @return
     */

    @Value("${project.folder}")
    private String folder;

    public Boolean saveImage(MultipartFile multipartFile, String id, String type) {
        String avatarPath = null;
        if (type.equals(UserContactTypeEnum.USER.getPrefix())) {
            avatarPath = folder + Constants.USER_AVATAR_FILE + id + Constants.USER_AVATAR_NAME_SUFFIX;
        }

        if (type.equals(UserContactTypeEnum.GROUP.getPrefix())) {
            avatarPath = folder + Constants.GROUP_AVATAR_FILE + id + Constants.GROUP_AVATAR_NAME_SUFFIX;
        }
        File file = isFile(avatarPath);
        try {
            /*会覆盖存在的文件*/
            multipartFile.transferTo(file);
        } catch (IOException e) {
            log.error("文件处理出现异常\n{}", e);
            return false;
        }
        return true;
    }

    /**
     * 判断文件是否存在，不存在创建
     *
     * @param path
     * @return
     */
    public File isFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 将Spring Boot静态资源文件复制到指定的目标路径
     *
     * @param staticResourcePath Spring Boot静态资源路径（包含文件名），例如: "static/images/logo.png"
     * @param targetFilePath     目标文件的绝对路径（包含文件名），例如: "/home/user/backup/logo.png"
     * @throws IOException 如果复制失败抛出异常
     */
    public static void copyStaticResourceToTarget(String staticResourcePath, String targetFilePath) throws IOException {
        // 加载静态资源
        ClassPathResource resource = new ClassPathResource(staticResourcePath);

        // 检查资源是否存在
        if (!resource.exists()) {
            throw new FileNotFoundException("静态资源文件不存在: " + staticResourcePath);
        }

        // 获取目标文件路径对象
        Path targetPath = Paths.get(targetFilePath);

        // 确保目标文件的父目录存在
        Path parentDir = targetPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // 将资源文件复制到目标位置
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
