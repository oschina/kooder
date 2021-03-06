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
package com.gitee.kooder.queue;

import com.gitee.kooder.core.KooderConfig;
import org.apache.commons.lang3.math.NumberUtils;
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

    public EmbedQueueProvider(Properties props) {
        int batch_size = NumberUtils.toInt(props.getProperty("embed.batch_size", "10000"), 10000);

        Path path = checkoutPath(KooderConfig.getPath(props.getProperty("embed.path")));
        for(String type : getAllTypes()) {
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
