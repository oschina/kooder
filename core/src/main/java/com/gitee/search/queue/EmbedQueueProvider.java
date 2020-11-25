package com.gitee.search.queue;

import com.gitee.search.core.GiteeSearchConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.infobip.lib.popout.FileQueue;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 实现 Gitee Search 内嵌式的队列，不依赖第三方服务，通过 HTTP 方式提供对象获取
 * @author Winter Lau<javayou@gmail.com>
 */
public class EmbedQueueProvider implements QueueProvider {

    private final static Logger log = LoggerFactory.getLogger(EmbedQueueProvider.class);

    private FileQueue<QueueTask> fileQueue;
    private String fetchUrl;

    public EmbedQueueProvider(Properties props) {
        this.fetchUrl = props.getProperty("embed.url");
        if(StringUtils.isBlank(fetchUrl)) {
            String host = GiteeSearchConfig.getHttpBind();
            if(host == null)
                host = "localhost";
            int port = GiteeSearchConfig.getHttpPort();
            this.fetchUrl = String.format("http://%s:%d/queue/fetch", host, port);
        }
        int batch_size = NumberUtils.toInt(props.getProperty("embed.batch_size", "10000"), 10000);

        Path path = GiteeSearchConfig.getPath(props.getProperty("embed.path"));
        if(!Files.exists(path) || !Files.isDirectory(path)) {
            log.warn("Path '{}' for queue storage not exists, created it!", path);
            try {
                Files.createDirectories(path);
            } catch(IOException e) {
                log.error("Failed to create directory '{}'", path, e);
            }
        }
        this.fileQueue = FileQueue.<QueueTask>batched().name("gitee")
                .folder(path)
                .restoreFromDisk(true)
                .batchSize(batch_size)
                .build();
    }

    /**
     * 队列的唯一名称
     *
     * @return
     */
    @Override
    public String name() {
        return "embed";
    }

    /**
     * 添加任务到队列
     *
     * @param tasks
     */
    @Override
    public void push(List<QueueTask> tasks) {
        if(tasks != null)
            this.fileQueue.addAll(tasks);
    }

    /**
     * 从队列获取任务
     *
     * @param count
     * @return
     */
    @Override
    public List<QueueTask> pop(int count) {
        try {
            Connection.Response resp = Jsoup.connect(this.fetchUrl).ignoreContentType(true).execute();
            String body = resp.body();
            if(StringUtils.isNotBlank(body))
                return Arrays.asList(QueueTask.parse(body));
        } catch (IOException e) {
            log.error("Failed to fetch tasks from '{}'", this.fetchUrl, e);
        }
        return null;
    }

    public QueueTask innerPop() {
        return this.fileQueue.poll();
    }

    /**
     * 关闭并释放资源
     */
    @Override
    public void close() {
        this.fileQueue.close();
    }
}
