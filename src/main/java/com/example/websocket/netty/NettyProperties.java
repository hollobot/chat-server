package com.example.websocket.netty;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ToString
@Component
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {
    private Integer boss;
    private Integer worker;
    private Integer timeout;
    private Integer port;       // 修改 String -> Integer
    private Integer portSalve;  // 服务器备用端口
    private String host;

}

