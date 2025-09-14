package com.example.websocket;

import com.alibaba.fastjson2.JSON;
import com.example.entity.dto.PeerConnectionDataDto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WebSocketMessageService {

    @Autowired
    private ChannelContextUtils channelContextUtils;

    /**
     * 处理WebSocket消息的核心方法
     *
     * @param ctx WebSocket上下文
     * @param data 接收到的消息数据
     */
    public void handleMessage(ChannelHandlerContext ctx, PeerConnectionDataDto data) {
        try {
            Channel channel = ctx.channel();
            // 获取发送者信息
            String currentUserId = channelContextUtils.getUserId(channel);
            if (currentUserId == null) {
                sendErrorResponse(ctx, "用户未登录或连接无效");
                return;
            }

            // 设置真实的发送者ID（防止伪造）
            data.setSendUserId(currentUserId);

            // 验证消息数据
            if (!validateMessageData(ctx, data)) {
                return;
            }

            // 记录消息日志
            logMessage(data);

            // 根据消息类型处理
            String signalType = data.getSignalType();
            switch (signalType.toLowerCase()) {
                case "offer":
                    handleOfferMessage(ctx, data);
                    break;

                case "answer":
                    handleAnswerMessage(ctx, data);
                    break;

                case "candidate":
                    handleCandidateMessage(ctx, data);
                    break;

                case "end_call":
                    handleEndCallMessage(ctx, data);
                    break;

                case "heartbeat":
                case "ping":
                    handleHeartbeatMessage(ctx, data);
                    break;

                case "call_request":
                    handleCallRequest(ctx, data);
                    break;

                case "call_response":
                    handleCallResponse(ctx, data);
                    break;

                default:
                    handleUnknownMessage(ctx, data);
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息时发生异常", e);
            sendErrorResponse(ctx, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理Offer消息（发起通话）
     */
    private void handleOfferMessage(ChannelHandlerContext ctx, PeerConnectionDataDto data) {
        log.info("处理Offer消息: {} -> {}", data.getSendUserId(), data.getReceiveUserId());

        // 检查目标用户是否在线
        if (!isUserOnline(data.getReceiveUserId())) {
            sendErrorResponse(ctx, "目标用户不在线: " + data.getReceiveUserId());
            return;
        }

        // 转发Offer给目标用户
        boolean success = forwardMessageToUser(data);
        if (success) {
            // 向发送者确认消息已发送
            sendSuccessResponse(ctx, "offer", "呼叫请求已发送给 " + data.getReceiveUserId());
        } else {
            sendErrorResponse(ctx, "发送呼叫请求失败");
        }
    }

    /**
     * 处理Answer消息（应答通话）
     */
    private void handleAnswerMessage(ChannelHandlerContext ctx, PeerConnectionDataDto data) {
        log.info("处理Answer消息: {} -> {}", data.getSendUserId(), data.getReceiveUserId());

        // 转发Answer给发起方
        boolean success = forwardMessageToUser(data);
        if (success) {
            sendSuccessResponse(ctx, "answer", "通话应答已发送");
        } else {
            sendErrorResponse(ctx, "发送通话应答失败");
        }
    }

    /**
     * 处理ICE Candidate消息
     */
    private void handleCandidateMessage(ChannelHandlerContext ctx, PeerConnectionDataDto data) {
        log.debug("处理Candidate消息: {} -> {}", data.getSendUserId(), data.getReceiveUserId());

        // 直接转发ICE候选信息
        boolean success = forwardMessageToUser(data);
        if (!success) {
            log.warn("转发ICE候选信息失败: {} -> {}", data.getSendUserId(), data.getReceiveUserId());
        }
    }

    /**
     * 处理结束通话消息
     */
    private void handleEndCallMessage(ChannelHandlerContext ctx, PeerConnectionDataDto data) {
        log.info("处理EndCall消息: {} -> {}", data.getSendUserId(), data.getReceiveUserId());

        // 转发结束通话消息
        boolean success = forwardMessageToUser(data);
        if (success) {
            sendSuccessResponse(ctx, "end_call", "通话结束消息已发送");
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeatMessage(ChannelHandlerContext ctx, PeerConnectionDataDto data) {
        log.debug("处理心跳消息: {}", data.getSendUserId());

        // 回复心跳
        Map<String, Object> response = new HashMap<>();
        response.put("signalType", "pong");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "心跳正常");

        sendResponse(ctx, response);
    }


    /**
     * 处理通话请求
     */
    private void handleCallRequest(ChannelHandlerContext ctx, PeerConnectionDataDto data) {
        log.info("处理通话请求: {} 请求与 {} 通话", data.getSendUserId(), data.getReceiveUserId());

        if (!isUserOnline(data.getReceiveUserId())) {
            sendErrorResponse(ctx, "目标用户不在线");
            return;
        }

        // 转发通话请求
        boolean success = forwardMessageToUser(data);
        if (success) {
            sendSuccessResponse(ctx, "call_request", "通话请求已发送");
        } else {
            sendErrorResponse(ctx, "发送通话请求失败");
        }
    }

    /**
     * 处理通话响应
     */
    private void handleCallResponse(ChannelHandlerContext ctx, PeerConnectionDataDto data) {
        log.info("处理通话响应: {} 响应 {} 的通话请求", data.getSendUserId(), data.getReceiveUserId());

        // 转发通话响应
        forwardMessageToUser(data);
    }

    /**
     * 处理未知消息类型
     */
    private void handleUnknownMessage(ChannelHandlerContext ctx, PeerConnectionDataDto data) {
        log.warn("收到未知消息类型: {}", data.getSignalType());
        sendErrorResponse(ctx, "不支持的消息类型: " + data.getSignalType());
    }

    /**
     * 转发消息给目标用户
     */
    private boolean forwardMessageToUser(PeerConnectionDataDto data) {
        try {
            String receiveUserId = data.getReceiveUserId();
            String message = JSON.toJSONString(data);

            boolean sent = channelContextUtils.sendMessageToUser(receiveUserId, message);

            if (sent) {
                log.debug("消息转发成功: {} -> {}", data.getSendUserId(), receiveUserId);
            } else {
                log.warn("消息转发失败，目标用户可能不在线: {} -> {}", data.getSendUserId(), receiveUserId);
                // 把不在线消息发给自己
                data.setSignalType("notOnline");
                data.setReceiveUserId(data.getSendUserId());
                message = JSON.toJSONString(data);
                channelContextUtils.sendMessageToUser(data.getReceiveUserId(), message);
            }

            return sent;

        } catch (Exception e) {
            log.error("转发消息时发生异常", e);
            return false;
        }
    }

    /**
     * 验证消息数据
     */
    private boolean validateMessageData(ChannelHandlerContext ctx, PeerConnectionDataDto data) {
        // 检查信令类型
        if (data.getSignalType() == null || data.getSignalType().trim().isEmpty()) {
            sendErrorResponse(ctx, "消息类型不能为空");
            return false;
        }

        // 对于需要目标用户的消息类型，检查接收者ID
        String signalType = data.getSignalType().toLowerCase();
        if (needsReceiveUser(signalType)) {
            if (data.getReceiveUserId() == null || data.getReceiveUserId().trim().isEmpty()) {
                sendErrorResponse(ctx, "接收用户ID不能为空");
                return false;
            }

            // 不能给自己发送消息
            if (data.getSendUserId().equals(data.getReceiveUserId())) {
                sendErrorResponse(ctx, "不能给自己发送消息");
                return false;
            }
        }

        return true;
    }

    /**
     * 判断消息类型是否需要接收用户
     */
    private boolean needsReceiveUser(String signalType) {
        return !"heartbeat".equals(signalType) &&
                !"ping".equals(signalType) &&
                !"user_list".equals(signalType);
    }

    /**
     * 检查用户是否在线
     */
    private boolean isUserOnline(String userId) {
        Channel channel = channelContextUtils.getChannelByUserId(userId);
        return channel != null && channel.isActive();
    }

    /**
     * 发送成功响应
     */
    private void sendSuccessResponse(ChannelHandlerContext ctx, String type, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("type", type);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());

        sendResponse(ctx, response);
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("type", "error");
        response.put("message", error);
        response.put("timestamp", System.currentTimeMillis());

        sendResponse(ctx, response);
        log.warn("发送错误响应: {}", error);
    }

    /**
     * 发送响应消息
     */
    private void sendResponse(ChannelHandlerContext ctx, Map<String, Object> response) {
        try {
            String json = JSON.toJSONString(response);
            ctx.writeAndFlush(new TextWebSocketFrame(json));
        } catch (Exception e) {
            log.error("发送响应消息失败", e);
        }
    }

    /**
     * 记录消息日志
     */
    private void logMessage(PeerConnectionDataDto data) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("[{}] 信令消息 - 类型: {}, 发送者: {}, 接收者: {}",
                timestamp, data.getSignalType(), data.getSendUserId(), data.getReceiveUserId());

        // 如果是调试模式，还可以打印信令数据
        if (log.isDebugEnabled()) {
            log.debug("信令数据: {}",
                    data.getSignalData() != null ?
                            (data.getSignalData().length() > 200 ?
                                    data.getSignalData().substring(0, 200) + "..." :
                                    data.getSignalData()) : "null");
        }
    }

}