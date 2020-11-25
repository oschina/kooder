package com.gitee.search.queue;

import com.gitee.search.core.GiteeSearchConfig;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 用于测试队列中的任务获取
 * @author Winter Lau<javayou@gmail.com>
 */
public class TestQueueProvider implements QueueProvider {

    private final static Logger log = LoggerFactory.getLogger(TestQueueProvider.class);

    private List<QueueTask> g_tasks = new ArrayList<>();
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
     * 队列的唯一名称
     *
     * @return
     */
    @Override
    public String name() {
        return "test";
    }

    /**
     * 添加任务到队列
     *
     * @param tasks
     */
    @Override
    public void push(List<QueueTask> tasks) {
        log.info("{} tasks received :", tasks.size());
        tasks.forEach(t -> System.out.println(t.json()));
        g_tasks.addAll(tasks);
    }

    /**
     * 从队列获取任务
     *
     * @param count
     * @return
     */
    @Override
    public List<QueueTask> pop(int count) {
        List<QueueTask> tasks = new ArrayList<>();
        while(g_tasks.size()>0 && tasks.size() < count){
            tasks.add(g_tasks.remove(0));
        }
        for(int i=tasks.size();auto_generate_task && i< RandomUtils.nextInt(count);i++){
            QueueTask task = new QueueTask();
            task.setAction(QueueTask.ACTION_ADD);
            task.setType(types.get(RandomUtils.nextInt(types.size())));
            task.setBody(this.body);
            tasks.add(task);
        }
        return tasks;
    }

    /**
     * 关闭并释放资源
     */
    @Override
    public void close() {
    }

}
