package com.gitee.search.http;

import com.gitee.search.core.GiteeSearchConfig;
import com.gitee.search.server.AccessLogger;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gateway http server base on vert.x
 * @author Winter Lau<javayou@gmail.com>
 */
public class Gateway implements Daemon, AccessLogger {

    private final static Logger log = LoggerFactory.getLogger(Gateway.class);

    private Vertx vertx;
    private HttpServer server;

    private String bind;
    private int port;
    private int workerPoolSize;
    private List<MessageFormat> log_patterns = new ArrayList<>();

    private Gateway() {
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
    public void writeAccessLog(String uri, String msg) {
        for(MessageFormat fmt : log_patterns) {
            try {
                fmt.parse(uri);
                log.info(msg);
                break;
            } catch(ParseException e) {}
        }
    }

    @Override
    public void init(DaemonContext daemonContext) {
        this.vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(this.workerPoolSize));
        this.server = vertx.createHttpServer().requestHandler(request -> ActionExecutor.execute(request));
    }

    @Override
    public void start() {
        this.server.listen(port).onSuccess(server -> {
            log.info("Gitee Search Gateway READY (:{})!", port);
        });
    }

    @Override
    public void stop() {
        this.server.close();
    }

    @Override
    public void destroy() {
        log.info("Gitee Search Gateway exit.");
    }

    public static void main(String[] args) {
        Gateway daemon = new Gateway();
        daemon.init(null);
        daemon.start();
    }

}
