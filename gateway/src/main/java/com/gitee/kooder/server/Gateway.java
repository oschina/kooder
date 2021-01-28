package com.gitee.kooder.server;

import com.gitee.kooder.core.GiteeSearchConfig;
import com.gitee.kooder.indexer.FetchTaskThread;
import com.gitee.kooder.indexer.GiteaIndexThread;
import com.gitee.kooder.indexer.GiteeIndexThread;
import com.gitee.kooder.indexer.GitlabIndexThread;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Gateway http server base on vert.x
 * @author Winter Lau<javayou@gmail.com>
 */
public class Gateway extends GatewayBase {

    private final static String pattern_static_file = "/.*\\.(css|ico|js|html|htm|jpg|png|gif)";
    private final static Map<String, Thread> startupTasks = new HashMap<>(){{
        put("indexer", new FetchTaskThread());
        put("gitlab", new GitlabIndexThread());
        put("gitee", new GiteeIndexThread());
        put("gitea", new GiteaIndexThread());
    }};

    private Gateway() {
        super();
    }

    @Override
    public void start() {
        //static files
        router.routeWithRegex(pattern_static_file).handler(new AutoContentTypeStaticHandler());
        //body parser
        router.route().handler(BodyHandler.create().setHandleFileUploads(false));
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

        InetSocketAddress address = (bind==null)?new InetSocketAddress(this.port):new InetSocketAddress(this.bind, this.port);

        this.server.requestHandler(router).listen(SocketAddress.inetSocketAddress(address)).onSuccess(server -> {
            Runtime.getRuntime().addShutdownHook(new Thread(() ->{
                super.stop();
                for(Thread task : startupTasks.values()){
                    task.interrupt();
                }
                super.destroy();
            }));
            log.info("READY ({}:{})!", (this.bind==null)?"*":this.bind, this.port);
        });

        this.startInitTasks();
    }

    /**
     * 启动配置文件中指定的初始任务
     */
    private void startInitTasks() {
        String tasks = GiteeSearchConfig.getProperty("http.startup.tasks");
        if(tasks == null)
            return ;
        String[] taskNames = tasks.split(",");
        int tc = 0;
        for(String taskName : taskNames) {
            if(StringUtils.isNotBlank(taskName)) {
                Thread thread = startupTasks.computeIfAbsent(taskName, key -> {
                    try {
                        return (Thread) Class.forName(key).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        log.error("Failed to load startup task named: " + taskName, e);
                    }
                    return null;
                });//.get(taskName);
                if (thread != null) {
                    thread.start();
                    tc++;
                }
            }
        }
        if(tc > 0)
            log.info("Tasks [{}] started.", tasks);
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