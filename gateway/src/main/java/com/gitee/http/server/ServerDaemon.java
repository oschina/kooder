package com.gitee.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make gateway as a daemon
 * @author Winter Lau<javayou@gmail.com>
 */
public class ServerDaemon implements Daemon {

    private final static Logger log = LoggerFactory.getLogger(ServerDaemon.class);

    private ServerBootstrap server;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private String bind;
    private int port;

    public ServerDaemon() {
        this.server = new ServerBootstrap();
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.bind = GatewayConfig.getBind();
        this.port = GatewayConfig.getPort();
    }

    @Override
    public void init(DaemonContext dc) {
        this.server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("decoder", new HttpRequestDecoder());
                        p.addLast("encoder", new HttpResponseEncoder());
                        //IMPORTANT!!! Aggregate all partial http content
                        p.addLast("aggregator", new HttpObjectAggregator(GatewayConfig.getMaxContentLength()));
                        p.addLast("handler", new HttpHandler());
                    }
                });
    }

    @Override
    public void start() {
        try {
            ChannelFuture f = (bind!=null)?server.bind(bind,port):server.bind(port).sync();
            f.channel().closeFuture().sync();
            log.info("Gitee Search Gateway READY !");
        } catch (InterruptedException e) {
            log.error("Gateway interrupted by controller.", e);
        }
    }

    @Override
    public void stop() {
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
    }

    @Override
    public void destroy() {
    }

}
