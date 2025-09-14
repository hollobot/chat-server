package com.example.entity.dto;

import lombok.Data;

/**
 * 视频通话数据
 */
@Data
public class PeerConnectionDataDto {
    private String sendUserId;
    private String receiveUserId;
    private String signalType;
    private String signalData;
    private Integer messageType; // 14 视频通话
}
