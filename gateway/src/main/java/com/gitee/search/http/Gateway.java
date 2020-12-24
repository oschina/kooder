package com.gitee.search.http;

import com.gitee.search.core.GiteeSearchConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Gateway http server base on vert.x
 * @author Winter Lau<javayou@gmail.com>
 */
public class Gateway extends GatewayBase {

    private final static String pattern_static_file = "/.*\\.(css|ico|js|html|htm|jpg|png|gif)";

    private VertxOptions vOptions;

    private Gateway() {
        super();
        this.vOptions = new VertxOptions();
        this.vOptions.setWorkerPoolSize(this.workerPoolSize);
        this.vOptions.setBlockedThreadCheckInterval(1000 * 60 * 60);
    }

    @Override
    public void start() {
        this.vertx = Vertx.vertx(this.vOptions);
        this.server = vertx.createHttpServer();
        Router router = Router.router(this.vertx);
        router.allowForward(AllowForwardHeaders.X_FORWARD);
        //global headers
        router.route().handler( context -> {
            this.writeGlobalHeaders(context.response());
            context.next();
        });
        //static files
        router.routeWithRegex(pattern_static_file).handler(new AutoContentTypeStaticHandler());
        //body parser
        router.route().handler(BodyHandler.create());
        //action handler
        router.route().handler(context -> {
            long ct = System.currentTimeMillis();
            HttpServerResponse res = context.response();
            try {
                ActionExecutor.execute(context);
            } finally {
                if(!res.ended())
                    res.end();
                if(!res.closed())
                    res.close();
            }
            writeAccessLog(context.request(), System.currentTimeMillis() - ct);
        });

        this.server.requestHandler(router).listen(port).onSuccess(server -> {
            log.info("READY (:{})!", port);
        });
    }

    /**
     * 全局 headers
     * @param res
     */
    private void writeGlobalHeaders(HttpServerResponse res) {
        res.putHeader("server", VERSION);
        res.putHeader("date", new Date().toString());
    }

    /**
     * 启动入口
     * @param args
     */
    public static void main(String[] args) {
        Gateway daemon = new Gateway();
        daemon.init(null);
        daemon.start();
    }

}

/**
 * 把 Gateway 底层逻辑移到 GatewayBase
 */
abstract class GatewayBase implements Daemon {

    final static Logger log = LoggerFactory.getLogger("GSearch");

    public final static String VERSION = "GSearch/1.0";

    String bind;
    int port;
    int workerPoolSize;

    Vertx vertx;
    HttpServer server;

    List<MessageFormat> log_patterns = new ArrayList<>();

    public GatewayBase() {
        this.bind = GiteeSearchConfig.getHttpBind();
        this.port = GiteeSearchConfig.getHttpPort();
        this.workerPoolSize = NumberUtils.toInt(GiteeSearchConfig.getProperty("http.worker.pool.size"), 16);

        String logs_pattern = GiteeSearchConfig.getProperty("http.log.pattern");
        if(logs_pattern != null) {
            Arrays.stream(logs_pattern.split(",")).forEach(pattern -> {
                log_patterns.add(new MessageFormat(StringUtils.replace(pattern, "*", "{0}")));
            });
        }
    }

    @Override
    public void init(DaemonContext daemonContext) {}

    @Override
    public void stop() {
        this.server.close();
    }

    @Override
    public void destroy() {
        log.info("EXIT!");
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