package com.gitee.search.queue;

import com.gitee.search.core.GiteeSearchConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.infobip.lib.popout.FileQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现 Gitee Search 内嵌式的队列，不依赖第三方服务，通过 HTTP 方式提供对象获取
 * @author Winter Lau<javayou@gmail.com>
 */
public class EmbedQueueProvider implements QueueProvider {

    private final static Logger log = LoggerFactory.getLogger(EmbedQueueProvider.class);

    private Map<String, FileQueue<QueueTask>> fileQueues = new ConcurrentHashMap<>();
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

        Path path = checkoutPath(GiteeSearchConfig.getPath(props.getProperty("embed.path")));
        for(String type : types()) {
            Path typePath = checkoutPath(path.resolve(type));
            fileQueues.put(type, FileQueue.<QueueTask>batched().name(type)
                    .folder(typePath)
                    .restoreFromDisk(true)
                    .batchSize(batch_size)
                    .build());
        }
    }

    private static Path checkoutPath(Path path) {
        if(!Files.exists(path) || !Files.isDirectory(path)) {
            log.warn("Path '{}' for queue storage not exists, created it!", path);
            try {
                Files.createDirectories(path);
            } catch(IOException e) {
                log.error("Failed to create directory '{}'", path, e);
            }
        }
        return path;
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
     * 获取某个任务类型的队列
     *
     * @param type
     * @return
     */
    @Override
    public Queue queue(String type) {
        return new Queue() {
            @Override
            public String type() {
                return type;
            }

            @Override
            public void push(Collection<QueueTask> tasks) {
                fileQueues.get(type).addAll(tasks);
            }

            @Override
            public List<QueueTask> pop(int count) {
                List<QueueTask> tasks = new ArrayList<>();
                QueueTask task;
                while(tasks.size() < count && (task = fileQueues.get(type).poll()) != null)
                    tasks.add(task);
                return tasks;
            }

            @Override
            public void close() {
                fileQueues.get(type).close();
            }
        };
    }

    @Override
    public void close() {
        fileQueues.values().forEach(q -> q.close());
    }
}
