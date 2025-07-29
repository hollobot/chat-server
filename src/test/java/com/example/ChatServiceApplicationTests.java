package com.example;

import com.example.entity.constants.Constants;
import com.example.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
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

}
