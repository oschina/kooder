package com.gitee.search.server;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Gateway http server base on vert.x
 * @author Winter Lau<javayou@gmail.com>
 */
public class Gateway extends GatewayBase {

    private final static String pattern_static_file = "/.*\\.(css|ico|js|html|htm|jpg|png|gif)";

    private Gateway() {
        super();
    }

    @Override
    public void start() {
        //static files
        router.routeWithRegex(pattern_static_file).handler(new AutoContentTypeStaticHandler());
        //body parser
        router.route().handler(BodyHandler.create());
        //action handler
        router.route().handler(context -> {
            long ct = System.currentTimeMillis();
            try {
                ActionExecutor.execute(context);
            } finally {
                HttpServerResponse res = context.response();
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
     * 启动入口
     * @param args
     */
    public static void main(String[] args) {
        Gateway daemon = new Gateway();
        daemon.init(null);
        daemon.start();
    }

}