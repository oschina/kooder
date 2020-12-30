package com.gitee.search.queue;

import com.gitee.search.core.GiteeSearchConfig;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 用于测试队列中的任务获取
 * @author Winter Lau<javayou@gmail.com>
 */
public class TestQueueProvider implements QueueProvider {

    private final static Logger log = LoggerFactory.getLogger(TestQueueProvider.class);

    private ConcurrentLinkedQueue<QueueTask> g_tasks = new ConcurrentLinkedQueue<>();
    private String body;
    private boolean auto_generate_task;
    private List<String> types = new ArrayList<>();

    public TestQueueProvider() {
        Properties props = GiteeSearchConfig.getQueueProperties();
        this.auto_generate_task = Boolean.valueOf(props.getProperty("test.auto_generate_task", "false"));
        types.addAll(Arrays.asList(props.getProperty("test.types").split(",")));

        try {
            Path path = Paths.get("json/repo-example.json").toAbsolutePath().normalize();
            this.body = new String(Files.readAllBytes(path));
        } catch(IOException e) {
            log.error("Failed to loading example json", e);
        }
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
                log.info("{} tasks received :", tasks.size());
                tasks.forEach(t -> System.out.println(t.json()));
                tasks.forEach(task -> g_tasks.offer(task));
            }

            @Override
            public List<QueueTask> pop(int count) {
                List<QueueTask> tasks = new ArrayList<>();
                QueueTask task;
                while(tasks.size() < count && (task = g_tasks.poll()) != null)
                    tasks.add(task);
                for(int i=tasks.size();auto_generate_task && i< RandomUtils.nextInt(count);i++){
                    QueueTask rtask = new QueueTask();
                    rtask.setAction(QueueTask.ACTION_ADD);
                    rtask.setType(types.get(RandomUtils.nextInt(types.size())));
                    rtask.setBody(body);
                    tasks.add(rtask);
                }
                return tasks;
            }

            @Override
            public void close() throws Exception {}
        };
    }

    /**
     * 队列的唯一名称
     *
     * @return
     */
    @Override
    public String name() {
        return "test";
    }

    @Override
    public void close() {}

}
