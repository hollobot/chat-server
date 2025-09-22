package com.example;
import com.example.entity.constants.Constants;
import com.example.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class ChatServiceApplicationTests {

    @Resource
    private RedisUtils redisUtils;

    @Test
    void contextLoads() {
        String keyPrefix = Constants.REDS_KEY_GROUPS_CONTACT;
        List<String> userIds = Arrays.asList("U51006520250", "U73005020250","U73005020250","U73005020250","U73005020250","U73005020250","U73005020250","U73005020250");
        redisUtils.batchAppendOrRemoverContacts(keyPrefix, userIds, "G99626920250", 0);
    }

    @Value("${project.folder}")
    private String path;


    @Test
    void fun() throws IOException {
        System.out.println(path);

        // 获取目标文件路径对象
        Path targetPath = Paths.get(path+"234234\\U73005020250");
        // 确保目标文件的父目录存在
        Path parentDir = targetPath.getParent();
        boolean exists = Files.exists(parentDir);

        if (parentDir != null && !exists) {
//            Files.createDirectories(parentDir);
        }
    }


}
