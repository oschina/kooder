package com.gitee.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http Server
 * @author Winter Lau<javayou@gmail.com>
 */
public class Server {

    private final static Logger log = LoggerFactory.getLogger(Server.class);
    private int port;

    public Server(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        new Server(port).run();
    }

    public void run() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("decoder", new HttpRequestDecoder());
                            p.addLast("encoder", new HttpResponseEncoder());
                            //IMPORTANT!!! Aggregate all partial http content
                            p.addLast("aggregator", new HttpObjectAggregator(512 * 1024));
                            p.addLast("handler", new HttpHandler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();

            log.info("Gitee Search Gateway READY !");

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
