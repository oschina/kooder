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

    public static void main(String[] args) throws Exception {
        ServerDaemon daemon = new ServerDaemon();
        daemon.init(null);
        daemon.start();
    }

}
