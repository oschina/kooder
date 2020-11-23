package com.gitee.search.indexer;

import com.gitee.search.core.GiteeSearchConfig;
import com.gitee.search.queue.QueueFactory;
import com.gitee.search.queue.QueueProvider;
import com.gitee.search.queue.QueueTask;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 用于从队列中获取待办任务的线程
 * @author Winter Lau<javayou@gmail.com>
 */
public class FetchTaskThread extends Thread {

    private final static Logger log = LoggerFactory.getLogger(FetchTaskThread.class);

    private QueueProvider provider;
    private int no_task_interval    = 1000; //从队列中获取不到任务时的休眠时间
    private int batch_fetch_count   = 10;   //一次从队列中获取任务的数量

    public FetchTaskThread() {
        this.provider = QueueFactory.getProvider();
        Properties props = GiteeSearchConfig.getIndexerProperties();
        this.no_task_interval = NumberUtils.toInt(props.getProperty("no_task_interval"), 1000);
        this.batch_fetch_count = NumberUtils.toInt(props.getProperty("batch_fetch_count"), 10);
    }

    /**
     * FIXME: 此方法可能导致人工中断进程时候，某些任务还在构建中会有丢失的情况
     */
    @Override
    public void run() {
        while(!this.isInterrupted()) {
            long startTime = System.currentTimeMillis();
            Map<String, ExecutorService> executors = new HashMap<>(); //每一个类型的任务使用一个独立的线程来处理
            List<String> taskNames = new ArrayList<>();
            List<QueueTask> tasks = provider.pop(batch_fetch_count);
            tasks.forEach( task -> {
                taskNames.add(task.getType());
                ExecutorService executor = executors.computeIfAbsent(task.getType(), v -> Executors.newSingleThreadExecutor());
                executor.submit(()-> {
                    try {
                        task.write();
                    } catch (Exception e) {
                        log.error("Failed writing task to index repository", e);
                    }
                });
            });

            //waiting all threads finished
            taskNames.forEach(type -> {
                try {
                    executors.get(type).shutdown();
                    executors.get(type).awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {}
            });

            if(tasks.size() > 0)
                log.info(tasks.size() + " tasks finished in " + (System.currentTimeMillis()-startTime) + " ms");

            if(tasks.size() == 0) {
                try {
                    Thread.sleep(no_task_interval);
                } catch (InterruptedException e) {
                    break;
                }
            }
            break;
        }
    }

}
