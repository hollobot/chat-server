package com.example.websocket.netty;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.dto.PeerConnectionDataDto;
import com.example.entity.dto.TokenUserInfoDto;
import com.example.entity.enums.MessageTypeEnum;
import com.example.utils.RedisUtils;
import com.example.utils.StringUtils;
import com.example.websocket.ChannelContextUtils;
import com.example.websocket.WebSocketMessageService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Sharable 是 Netty 提供的一个注解，用于标记一个 ChannelHandler 是 可共享的。默认情况下， Netty 会为每个 Channel 创建一个新的 ChannelHandler 实例。如果一个
 *     ChannelHandler 被 标记为 @Sharable，Netty 会复用同一个实例来处理多个 Channel 的事件。
 */
@Slf4j
@Component
@Sharable
public class ServerListenerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private WebSocketMessageService webSocketMessageService;

    /**
     * 收到消息
     *
     * @param ctx
     * @param message
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame message) throws Exception {
        Channel channel = ctx.channel();
        String userId = channelContextUtils.getUserId(channel);
        String msg = message.text();
//        log.error("用户{}发的消息：{}", userId, msg);
        /*刷新心跳*/
        redisUtils.saveHeartbeat(userId);

        // 判断是否为JSON格式
        if (!isValidJson(msg)) {
            return;
        }

        // 解析为通用的 JSONObject
        JSONObject jsonObject = JSON.parseObject(msg);

        // 判断是否包含视频通话字段
        if (!hasRequiredFields(jsonObject)) {
            return;
        }

        try {
            // 解析消息
            PeerConnectionDataDto data = JSON.parseObject(msg, PeerConnectionDataDto.class);
            data.setSendUserId(userId); // 设置发送者ID
            data.setMessageType(MessageTypeEnum.VIDEO_CALL.getType()); // 设置视频通话数据类型

            // 处理消息
            webSocketMessageService.handleMessage(ctx, data);

        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "消息处理失败: " + e.getMessage());
            ctx.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(errorMap)));
        }

    }

    private boolean hasRequiredFields(JSONObject jsonObject) {
        // 根据你的 PeerConnectionDataDto 定义必要字段
        return jsonObject.containsKey("signalType");
    }

    // 判断是否为json格式数据
    private boolean isValidJson(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return false;
        }

        String trimmed = msg.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return false;
        }

        try {
            JSON.parseObject(msg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 通道连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.error("连接成功");
    }

    /**
     * 通道断开
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelContextUtils.removeContext(channel);
        log.error("通道断开");
    }

    /**
     * userEventTriggered 是 Netty 中的一个方法，用于处理用户自定义事件或 Netty 内置的特殊事件（如 IdleStateEvent）
     *
     * @param ctx 表示当前处理器的上下文，可以用于操作 Channel 或触发其他事件。
     * @param evt 触发的事件对象。可以是用户自定义事件，也可以是 Netty 内置的事件（如 IdleStateEvent）。
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        Channel channel = ctx.channel();
        /*1、处理连接空闲*/
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;
            switch (event.state()) {
                case READER_IDLE:
                    log.error("用户{}心跳超时关闭连接", channelContextUtils.getUserId(channel));
                    channel.close();
                    break;
                case WRITER_IDLE:
                    log.error("写空闲");
                    break;
                case ALL_IDLE:
                    log.error("读写空闲");
                    break;
            }
            return;
        }

        /*2、处理WebSocket 握手成功完成*/
        if (evt instanceof HandshakeComplete) {
            HandshakeComplete event = (HandshakeComplete)evt;

            /*1、分割得到token*/
            String token = analyzeToken(event.requestUri());

            if (token == null) {
                channel.close(); // 关闭连接
                return;
            }

            /*2、校验token*/
            TokenUserInfoDto tokenInfo = redisUtils.getTokenInfo(token);
            if (tokenInfo == null) {
                channel.close(); // 关闭连接
                log.error("未授权,请重新登录");
                return;
            }


            /*4、初始化连接用户*/
            channelContextUtils.addContext(tokenInfo.getUserId(), channel);

        }

    }

    public String analyzeToken(String path) {

        Integer index = path.lastIndexOf("?token=");

        if (index == -1) {
            log.error("token 异常");
            return null;
        }

        return path.substring(index + 7);
    }
}
