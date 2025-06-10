package com.example;

import com.example.websocket.netty.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

@SpringBootApplication
public class ChatServiceApplication {

    private static NettyServer nettyServer;  // 静态字段

    @Resource
    public void setNettyServer(NettyServer nettyServer) {
        ChatServiceApplication.nettyServer = nettyServer;  // 手动赋值
    }

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
        nettyServer.run();
    }


}
