package com.gitee.search.http;

import com.gitee.search.core.GiteeSearchConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gateway http server base on vert.x
 * @author Winter Lau<javayou@gmail.com>
 */
public class Gateway extends AbstractVerticle {

    private final static Logger log = LoggerFactory.getLogger(Gateway.class);

    private String bind;
    private int port;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.bind = GiteeSearchConfig.getHttpBind();
        this.port = GiteeSearchConfig.getHttpPort();
    }

    @Override
    public void start() throws Exception {
        // Create the HTTP server
        HttpServer httpServer = vertx.createHttpServer().requestHandler(context -> {
            context.response().putHeader("Content-Type", "text/plain").end("some text");
        });
        httpServer.listen(this.port, this.bind).onSuccess(server -> {});

        log.info("Gitee Search Gateway READY ({}:{})!", (bind!=null)?bind:"", this.port);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) {
        int port = GiteeSearchConfig.getHttpPort();
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(400));

        HttpServer httpServer = vertx.createHttpServer().requestHandler(context -> {
            context.response().putHeader("Content-Type", "text/plain").end("some text");
        });
        httpServer.listen(port).onSuccess(server -> {});

        log.info("Gitee Search Gateway READY (:{})!", port);
    }

}
