/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.server;

import com.gitee.kooder.core.KooderConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

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
    private boolean httpLog = true;

    Vertx vertx;
    VertxOptions vOptions;
    HttpServer server;
    Router router;

    public GatewayBase() {
        this.bind = KooderConfig.getHttpBind();
        if(StringUtils.isBlank(this.bind))
            this.bind = null;
        this.port = KooderConfig.getHttpPort();
        this.workerPoolSize = NumberUtils.toInt(KooderConfig.getProperty("http.worker.pool.size"), Runtime.getRuntime().availableProcessors());

        this.vOptions = new VertxOptions();
        this.vOptions.setWorkerPoolSize(this.workerPoolSize);
        this.vOptions.setBlockedThreadCheckInterval(1000 * 60 * 60);
        this.httpLog = !"off".equalsIgnoreCase(KooderConfig.getProperty("http.log"));
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
     * @param context
     * @param time
     */
    protected void writeAccessLog(RoutingContext context, long time) {
        if(httpLog) {
            HttpServerRequest req = context.request();
            String ua = req.getHeader("User-Agent");
            if (ua == null)
                ua = "-";
            String params = req.params().entries().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(","));
            String msg = String.format("%s - \"%s %s %s %s\" %d %d - %dms - \"%s\"",
                    req.remoteAddress().hostAddress(),
                    req.method().name(),
                    req.uri(),
                    params,
                    context.getBodyAsString(),
                    req.response().getStatusCode(),
                    req.response().bytesWritten(),
                    time,
                    ua);
            log.info(msg);
        }
    }

}