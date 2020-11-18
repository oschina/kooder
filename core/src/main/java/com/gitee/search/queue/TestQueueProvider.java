package com.gitee.search.queue;

import org.apache.commons.lang.math.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public class TestQueueProvider implements QueueProvider {
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
        for(int i=0;i< RandomUtils.nextInt(count);i++){
            QueueTask task = new QueueTask();
            task.setAction(QueueTask.ACTION_ADD);
            task.setType(QueueTask.types.get(RandomUtils.nextInt(QueueTask.types.size())));
            task.setBody("{\"name\":\"Winter Lau\"");
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
