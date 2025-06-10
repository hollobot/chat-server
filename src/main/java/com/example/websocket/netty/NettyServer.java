package com.example.websocket.netty;

import com.example.entity.enums.ExceptionCodeEnum;
import com.example.handler.CustomException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class NettyServer {

    @Resource
    private NettyProperties nettyProperties;

    @Resource
    private ServerListenerHandler serverListenerHandler;


    @Async
    public void run() {
        /*bossGroup 负责接收客户端的连接请求。*/
        EventLoopGroup bossGroup = new NioEventLoopGroup();/*默认 CPU 核心数 * 1 个线程*/
        /*workerGroup 负责处理客户端连接的数据读写、业务逻辑等。*/
        EventLoopGroup workerGroup = new NioEventLoopGroup();/*默认2个线程*/
        try {
            //1、创建服务端的启动对象，设置参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //2、设置两个线程组boosGroup和workerGroup，添加处理器
            serverBootstrap.group(bossGroup, workerGroup)
                    //设置服务端通道实现类型,指定了服务器将使用哪种类型的 Channel 来处理网络 I/O 操作
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            /**
                             * HttpServerCodec：是一个编解码器，用于处理 HTTP 请求和响应的编解码。
                             * 它将入站的 ByteBuf 解码为 HttpRequest 或 HttpContent。
                             * 它将出站的 HttpResponse 或 HttpContent 编码为 ByteBuf。
                             * 为什么需要它：WebSocket 协议是基于 HTTP 的，因此需要先处理 HTTP 请求和响应。
                             */
                            pipeline.addLast(new HttpServerCodec());
                            /**
                             * HttpObjectAggregator：用于将 HTTP 消息聚合为完整的 FullHttpRequest 或 FullHttpResponse。
                             * HTTP 消息可能被分块传输（例如，大文件上传），HttpObjectAggregator 将这些分块聚合为一个完整的消息。
                             * 64 * 1024 表示最大聚合大小为 64KB。如果消息超过此大小，会抛出异常。
                             * 为什么需要它：WebSocket 握手过程需要完整的 HTTP 请求和响应，因此需要聚合 HTTP 消息。
                             */
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            /**
                             * IdleStateHandler：用于检测连接的空闲状态。
                             * 参数说明：
                             * 6：表示读空闲时间（6 秒）。如果 6 秒内没有读取到数据，会触发 IdleStateEvent.READER_IDLE 事件。
                             * 0：表示写空闲时间（0 秒）。如果 0 秒内没有写入数据，会触发 IdleStateEvent.WRITER_IDLE 事件。
                             * 0：表示读写空闲时间（0 秒）。如果 0 秒内没有读取或写入数据，会触发 IdleStateEvent.ALL_IDLE 事件。
                             * TimeUnit.SECONDS：时间单位（秒）。
                             * 为什么需要它：用于检测客户端是否断开连接。如果客户端长时间没有发送数据，可以关闭连接以释放资源。
                             */
                            pipeline.addLast(new IdleStateHandler(6, 0, 0, TimeUnit.SECONDS));
                            /**
                             * WebSocketServerProtocolHandler：用于处理 WebSocket 握手和帧解码。
                             * 参数说明：
                             * "/ws"：表示 WebSocket 的路径。客户端连接时需要指定此路径（例如 ws://localhost:8080/ws）。
                             * 功能：
                             * 处理 WebSocket 握手过程（HTTP 升级为 WebSocket 协议）。
                             * 将 WebSocket 帧解码为 TextWebSocketFrame、BinaryWebSocketFrame 等。
                             * 将 TextWebSocketFrame、BinaryWebSocketFrame 等编码为 WebSocket 帧。
                             * 为什么需要它：WebSocket 协议是基于 HTTP 的，但握手后使用独立的帧格式传输数据。WebSocketServerProtocolHandler 负责处理这些细节。
                             */
                            pipeline.addLast(
                                    new WebSocketServerProtocolHandler("/ws", null, true, 64 * 1024, true, true,
                                            10000L));

                            /*自定义 WebSocket 处理器*/
                            pipeline.addLast(serverListenerHandler);
                        }
                    });

            Integer port = nettyProperties.getPort();
            String wsPort = System.getProperty("ws.port");
            if(wsPort!=null){
                port = Integer.parseInt(wsPort);
            }
            log.error("wsPort:{}",port);
            /*3、绑定端口号，启动服务端*/
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            System.out.println("启动成功");
            /*对关闭通道进行监听*/
            channelFuture.channel().closeFuture().sync();
            System.out.println("关闭");
        } catch (Exception e) {
            System.out.println(e);
            throw new CustomException(ExceptionCodeEnum.CODE_500);
        } finally {
            /*优雅地关闭线程组，释放资源。会等待所有任务完成后再关闭线程池。*/
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
