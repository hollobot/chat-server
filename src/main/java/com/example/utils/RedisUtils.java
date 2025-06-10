package com.example.utils;

import com.example.entity.constants.Constants;
import com.example.entity.dto.SysSettingDto;
import com.example.entity.dto.TokenUserInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtils {

    /**
     * redis 操作模板
     */
    private final RedisTemplate redisTemplate;

    private final StringRedisTemplate stringRedisTemplate;

    public RedisUtils(RedisTemplate redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 添加
     *
     * @param key
     * @param object
     * @param ms
     */
    public void set(String key, Object object, long ms) {
        redisTemplate.opsForValue().set(key, object, ms, TimeUnit.MILLISECONDS);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 添加
     *
     * @param key
     * @param value
     * @param ms
     */
    public void set(String key, String value, long ms) {
        stringRedisTemplate.opsForValue().set(key, value, ms, TimeUnit.MILLISECONDS);
    }

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取指定 key 的值 (字符串)
     *
     * @param key key
     * @return 值
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 获取指定 key 的值 (对象)
     *
     * @param key key
     * @return 值
     */
    public Object getO(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 通过key删除redis字段信息
     *
     * @param key
     * @return
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 将token信息对象存入redis
     *
     * @param tokenUserInfoDto
     */
    public void saveToken(TokenUserInfoDto tokenUserInfoDto) {
        set(Constants.REDS_KEY_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.TOKEN_TIME);
    }

    /**
     * 获取token信息
     *
     * @param token
     * @return
     */
    public TokenUserInfoDto getTokenInfo(String token) {
        Object o = getO(Constants.REDS_KEY_TOKEN + token);
        // 使用 Jackson 的 ObjectMapper 将 LinkedHashMFap 转换为 TokenUserInfoDto
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(o, TokenUserInfoDto.class);
    }

    /**
     * 根据用户id删除token
     *
     * @param userId
     */
    public Boolean delTokenInfoByUserId(String userId) {
        if (userId == null) {
            return false;
        }
        String tokenByUserId = getTokenByUserId(userId);
        if (tokenByUserId == null) {
            return false;
        }
        return delete(Constants.REDS_KEY_TOKEN + tokenByUserId);
    }

    /**
     * 将userId绑定token
     *
     * @param userId
     * @param token
     */
    public void saveUserIdAndToken(String userId, String token) {
        set(Constants.REDS_KEY_USERID + userId, token, Constants.TOKEN_TIME);
    }

    /**
     * 根据用户id获取token
     *
     * @param userId
     * @return
     */
    public String getTokenByUserId(String userId) {
        return get(Constants.REDS_KEY_USERID + userId);
    }

    /**
     * 保存心跳
     *
     * @param userId
     */
    public void saveHeartbeat(String userId) {
        set(Constants.REDS_KEY_HEARTBEAT + userId, System.currentTimeMillis(), Constants.HEARTBEAT_TIME);
    }

    /**
     * 获取心跳
     *
     * @param userId
     * @return
     */
    public Long getHeartbeat(String userId) {
        return (Long)getO(Constants.REDS_KEY_HEARTBEAT + userId);
    }

    /**
     * list 清空数据
     *
     * @param key
     */
    public void removeList(String key) {
        if (key == null) {
            return;
        }
        ListOperations listOperations = redisTemplate.opsForList();
        listOperations.trim(key, 1, 0);
    }

    /**
     * 单个数据插入
     *
     * @param key
     * @param string
     * @return
     */
    public Long rightPushToList(String key, String string) {
        if (string == null || string.equals("")) {
            return 0l;
        }
        ListOperations listOperations = redisTemplate.opsForList();
        return listOperations.rightPush(key, string);
    }

    /**
     * 批量插入
     *
     * @param key
     * @param list
     * @return
     */
    public Long rightPushToList(String key, List<String> list) {
        if (list == null || list.size() == 0) {
            return 0l;
        }
        ListOperations listOperations = redisTemplate.opsForList();
        return listOperations.rightPushAll(key, list);
    }

    /**
     * 获取列表数据
     *
     * @param key
     * @return
     */
    public List<String> allList(String key) {
        if (key == null) {
            return null;
        }
        ListOperations listOperations = redisTemplate.opsForList();
        return listOperations.range(key, 0, -1);
    }

    /**
     * 储存联系人列表
     *
     * @param key
     * @param userId
     * @param contactIds
     */
    public void setContactIds(String key, String userId, List<String> contactIds) {
        String redisKey = key + userId;
        /*1、清空数据*/
        removeList(redisKey);
        Long aLong = rightPushToList(redisKey, contactIds);
        if (aLong == 0) {
            return;
        }
        redisTemplate.expire(redisKey, Constants.TOKEN_TIME, TimeUnit.SECONDS);
    }

    /**
     * 获取联系人列表
     *
     * @param key
     * @param userId
     * @return
     */
    public List<String> getContactIds(String key, String userId) {
        return allList(key + userId);
    }

    /**
     * 联系人list追加单个元素
     *
     * @param key
     * @param userId
     * @param newContactId
     */
    public void addContactToList(String key, String userId, String newContactId) {

        String redisKey = key + userId;
        /*1、判断是否存在*/
        List<String> strings = allList(redisKey);
        if (strings != null && strings.contains(newContactId)) {
            return;
        }
        /*2、单个插入*/
        rightPushToList(redisKey, newContactId);
        /*3、刷新时间*/
        redisTemplate.expire(redisKey, Constants.TOKEN_TIME, TimeUnit.SECONDS);
    }

    public void remove(String key, Integer count, String userId) {
        ListOperations listOperations = redisTemplate.opsForList();
        listOperations.remove(key, count, userId);
        /*1、刷新时间*/
        redisTemplate.expire(key, Constants.TOKEN_TIME, TimeUnit.SECONDS);
    }

    /**
     * 在 executePipelined 的回调里，你每次调用 connection.rPush(...)、connection.lRem(...) 和 connection.expire(...) 时， 都会把这条 Redis
     * 命令放到本地管道缓冲区里，并不会马上发到服务器。 回调结束后，Spring 会一次性把缓冲区里的所有命令打包，批量发送给 Redis；这样可以极大地减少网络往返。
     *
     * @param keyPrefix
     * @param userIds
     * @param newContactId
     * @param type
     */
    public void batchAppendOrRemoverContacts(String keyPrefix, List<String> userIds, String newContactId,
        Integer type) {

        RedisSerializer<String> keySer = new StringRedisSerializer();
        RedisSerializer<Object> valSer = (RedisSerializer<Object>)redisTemplate.getValueSerializer();

        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>)connection -> {
            if (type == 1) {
                for (String userId : userIds) {
                    String redisKey = keyPrefix + userId;
                    byte[] keyBytes = keySer.serialize(redisKey);
                    byte[] newContactIdBytes = valSer.serialize(newContactId);
                    // 1、删除再添加，去重
                    connection.lRem(keyBytes, 0, newContactIdBytes);
                    // 2. 批量添加
                    connection.rPush(keyBytes, newContactIdBytes);
                    // 3. 统一设置过期时间
                    connection.expire(keyBytes, Constants.TOKEN_TIME);
                }
            } else {
                for (String userId : userIds) {
                    String redisKey = keyPrefix + userId;
                    byte[] keyBytes = keySer.serialize(redisKey);
                    byte[] newContactIdBytes = valSer.serialize(newContactId);
                    List<String> raw = redisTemplate.opsForList().range(keyPrefix + "U51006520250", 0, -1);
                    raw.forEach(System.out::println);

                    // 1. 批量删除
                    connection.lRem(keyBytes, 0, newContactIdBytes);
                    // 2. 统一设置过期时间
                    connection.expire(keyBytes, Constants.TOKEN_TIME);
                }
            }
            return null;
        });
        log.error("批量操作:\n{}", results);

    }

    /**
     * 获取系统设置
     *
     * @return
     */
    public SysSettingDto getSysSetting() {
        Object o = getO(Constants.SYSTEM_SETTING);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(o, SysSettingDto.class);
    }

    /**
     * 系统设置
     *
     * @param sysSettingDto
     */
    public void setSysSetting(SysSettingDto sysSettingDto) {
        sysSettingDto.setRobotUid(Constants.ROBOT_UID);
        set(Constants.SYSTEM_SETTING, sysSettingDto);
    }
}