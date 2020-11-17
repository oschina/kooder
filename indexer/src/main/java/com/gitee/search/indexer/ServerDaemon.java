package com.gitee.search.indexer;

import com.gitee.search.queue.QueueFactory;
import com.gitee.search.queue.QueueProvider;
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
    }

    @Override
    public void init(DaemonContext dc) {
        this.fetchTaskThread = new FetchTaskThread();
        this.fetchTaskThread.setDaemon(true);
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
