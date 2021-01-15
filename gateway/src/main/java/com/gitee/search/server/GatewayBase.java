package com.gitee.search.server;

import com.gitee.search.core.GiteeSearchConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.ext.web.Router;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

/**
 * 把 Gateway 底层逻辑移到 GatewayBase
 * @author Winter Lau<javayou@gmail.com>
 */
public abstract class GatewayBase implements Daemon {

    final static Logger log = LoggerFactory.getLogger("[gateway]");

    public final static String VERSION = "GSearch/1.0";

    String bind;
    int port;
    int workerPoolSize;

    Vertx vertx;
    VertxOptions vOptions;
    HttpServer server;
    Router router;

    List<MessageFormat> log_patterns = new ArrayList<>();

    public GatewayBase() {
        this.bind = GiteeSearchConfig.getHttpBind();
        if(StringUtils.isBlank(this.bind))
            this.bind = null;
        this.port = GiteeSearchConfig.getHttpPort();
        this.workerPoolSize = NumberUtils.toInt(GiteeSearchConfig.getProperty("http.worker.pool.size"), 16);

        this.vOptions = new VertxOptions();
        this.vOptions.setWorkerPoolSize(this.workerPoolSize);
        this.vOptions.setBlockedThreadCheckInterval(1000 * 60 * 60);

        String logs_pattern = GiteeSearchConfig.getProperty("http.log.pattern");
        if(logs_pattern != null) {
            Arrays.stream(logs_pattern.split(",")).forEach(pattern -> {
                log_patterns.add(new MessageFormat(StringUtils.replace(pattern, "*", "{0}")));
            });
        }
    }

    @Override
    public void init(DaemonContext daemonContext) {
        this.vertx = Vertx.vertx(this.vOptions);
        this.server = vertx.createHttpServer();
        this.router = Router.router(this.vertx);
        router.allowForward(AllowForwardHeaders.X_FORWARD);
        //global headers
        router.route().handler( context -> {
            this.writeGlobalHeaders(context.response());
            context.next();
        });
    }

    @Override
    public void stop() {
        this.server.close();
        this.vertx.close();
    }

    @Override
    public void destroy() {
        log.info("EXIT!");
    }

    /**
     * 全局 headers
     * @param res
     */
    protected void writeGlobalHeaders(HttpServerResponse res) {
        res.putHeader("server", VERSION);
        res.putHeader("date", new Date().toString());
    }

    /**
     * 记录日志
     * @param req
     * @param time
     */
    protected void writeAccessLog(HttpServerRequest req, long time) {
        String ua = req.getHeader("User-Agent");
        if(ua == null)
            ua = "-";
        String msg = String.format("%s - \"%s %s\" %d %d - %dms - \"%s\"",
                req.remoteAddress().hostAddress(),
                req.method().name(),
                req.uri(),
                req.response().getStatusCode(),
                req.response().bytesWritten(),
                time,
                ua);

        for(MessageFormat fmt : log_patterns) {
            try {
                fmt.parse(req.path());
                log.info(msg);
                break;
            } catch(ParseException e) {}
        }
    }

}