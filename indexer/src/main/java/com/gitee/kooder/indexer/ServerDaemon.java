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
