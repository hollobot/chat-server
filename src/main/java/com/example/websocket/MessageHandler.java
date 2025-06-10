package com.example.websocket;

import com.alibaba.fastjson.JSON;
import com.example.entity.dto.MessageSendDto;
import com.example.entity.enums.MessageTypeEnum;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Component
public class MessageHandler {

    private static final String MESSAGE_TOPIC = "message.topic";

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ChannelContextUtils channelContextUtils;

    /**
     * redisson客户端监听redis服务器接受的数据
     *
     * @PostConstruct 容器初始后马上执行改注解标记的方法
     */
    @PostConstruct
    public void lisMessage() {
        log.error("Redisson 是否活跃: {}", !redissonClient.isShutdown());
        // 2. 注册监听器
        RTopic topic = redissonClient.getTopic(MESSAGE_TOPIC);
        topic.addListener(MessageSendDto.class, (charSequence, msg) -> {
            log.info("收到消息 - 频道: {}, 内容: {}", charSequence, JSON.toJSONString(msg));
            channelContextUtils.sendMsg(msg);
            // 处理删除channel
            if (msg.getMessageType() == MessageTypeEnum.LEAVE_GROUP.getType() || msg.getMessageType() == MessageTypeEnum.REMOVE_GROUP.getType()) {
                //将用户channel从groupChannel删除
                Channel channel = channelContextUtils.getChannelByUserId(msg.getSendUserId());
                channelContextUtils.addOrDelGroupChannel(msg.getRecipientId(), channel, 0);
            } else if (msg.getMessageType() == MessageTypeEnum.DISSOLUTION_GROUP.getType()) {
                //群聊解散 删除群聊channel
                String recipientId = msg.getRecipientId();
                channelContextUtils.removerGroupChannel(recipientId);
            }
        });
    }

    /**
     * redisson客户端发送广播数据到redis服务器
     *
     * @param messageSendDto
     */
    public void sendMessage(MessageSendDto messageSendDto) {
        RTopic topic = redissonClient.getTopic(MESSAGE_TOPIC);
        topic.publish(messageSendDto);
    }

}
