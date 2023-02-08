package com.cesiumai.jtt1078server.netty;

import com.cesiumai.jtt1078server.netty.handler.H264ToFlvEncoder;
import com.cesiumai.jtt1078server.netty.handler.HttpFlvHandler;
import com.cesiumai.jtt1078server.netty.handler.MessageDecoder;
import com.cesiumai.jtt1078server.netty.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.util.ResourceLeakDetector;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wuxiongbin
 */
@Slf4j
public class NettyMediaServer {

    public static void start(int httpFlvPort, int jtt1078Port) {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
        EventLoopGroup listenGrp = new NioEventLoopGroup(1);
        EventLoopGroup workGrp = new NioEventLoopGroup(4);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(listenGrp, workGrp)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_RCVBUF, 1024 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 64 * 1024)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024))
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new MessageDecoder());
                            socketChannel.pipeline().addLast(new ServerHandler());
                        }
                    });

            ServerBootstrap httpStrap = b.clone();
            httpStrap.childOption(ChannelOption.SO_RCVBUF, 64 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 1024 * 1024)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024 * 1024 / 2, 1024 * 1024))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build();
                            socketChannel.pipeline()
                                    .addLast(new HttpResponseEncoder())
                                    .addLast(new HttpRequestDecoder())
                                    .addLast(new HttpObjectAggregator(64 * 1024))
                                    .addLast(new CorsHandler(corsConfig))//.addLast(new ChunkedWriteHandler())
                                    .addLast(new H264ToFlvEncoder())
                                    .addLast(new HttpFlvHandler());
                        }
                    });
            httpStrap.bind(httpFlvPort).sync();

            ChannelFuture f = b.bind(jtt1078Port).sync();
            log.info("Service started successfully !!!");
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            listenGrp.shutdownGracefully();
            workGrp.shutdownGracefully();
        }
    }
}
