package com.gitee.search.server;

import com.gitee.search.core.GiteeSearchConfig;
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Make gateway as a daemon
 * @author Winter Lau<javayou@gmail.com>
 */
public class ServerDaemon implements Daemon , AccessLogger {

    private final static Logger log = LoggerFactory.getLogger(ServerDaemon.class);

    private ServerBootstrap server;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private String bind;
    private int port;
    private List<MessageFormat> log_patterns = new ArrayList<>();

    public ServerDaemon() {
        this.server = new ServerBootstrap();
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.bind = GiteeSearchConfig.getHttpBind();
        this.port = GiteeSearchConfig.getHttpPort();
        String logs_pattern = GiteeSearchConfig.getProperty("http.log.pattern");
        if(logs_pattern != null) {
            Arrays.stream(logs_pattern.split(",")).forEach(pattern -> {
                log_patterns.add(new MessageFormat(StringUtils.replace(pattern, "*", "{0}")));
            });
        }
    }

    @Override
    public void writeAccessLog(String uri, String msg) {
        for(MessageFormat fmt : log_patterns) {
            try {
                fmt.parse(uri);
                log.info(msg);
                break;
            } catch(ParseException e) {}
        }
    }

    /**
     * 命令行启动服务
     * @param args
     */
    public static void main(String[] args) {
        ServerDaemon daemon = new ServerDaemon();
        daemon.init(null);
        daemon.start();
    }

    @Override
    public void init(DaemonContext dc) {
        final AccessLogger logger = this;
        this.server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("decoder", new HttpRequestDecoder());
                        p.addLast("encoder", new HttpResponseEncoder());
                        //IMPORTANT!!! Aggregate all partial http content
                        p.addLast("aggregator", new HttpObjectAggregator(GiteeSearchConfig.getHttpMaxContentLength()));
                        p.addLast("handler", new HttpHandler(logger));
                    }
                });
    }

    @Override
    public void start() {
        try {
            ChannelFuture f = (bind!=null)?server.bind(bind,port):server.bind(port).sync();
            log.info("Gitee Search Gateway READY !");
            f.channel().closeFuture().sync();
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
        log.info("Gitee Search Gateway exit.");
    }

}
