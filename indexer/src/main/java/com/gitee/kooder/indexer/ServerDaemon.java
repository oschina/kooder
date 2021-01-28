package com.gitee.kooder.indexer;

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

    private FetchTaskThread fetchTaskThread;

    public ServerDaemon() {}

    /**
     * 命令行启动服务
     * @param args
     */
    public static void main(String[] args) {
        ServerDaemon daemon = new ServerDaemon();
        daemon.init(null);
        daemon.start();
        log.info("Gitee Search Indexer started !");
    }

    @Override
    public void init(DaemonContext dc) {
        this.fetchTaskThread = new FetchTaskThread();
    }

    @Override
    public void start() {
        this.fetchTaskThread.start();
    }

    @Override
    public void stop() {
        this.fetchTaskThread.interrupt();
        try {
            this.fetchTaskThread.join(2000, 20);
        } catch (InterruptedException e) {}
    }

    @Override
    public void destroy() {
        log.info("Gitee Search Indexer exit.");
    }

}
