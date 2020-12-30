package com.gitee.search.indexer;

import com.gitee.search.core.GiteeSearchConfig;
import com.gitee.search.queue.QueueFactory;
import com.gitee.search.queue.QueueProvider;
import com.gitee.search.queue.QueueTask;
import com.gitee.search.storage.StorageFactory;
import com.gitee.search.utils.BatchTaskRunner;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 用于从队列中获取待办任务的线程
 * @author Winter Lau<javayou@gmail.com>
 */
public class FetchTaskThread extends Thread {

    private final static Logger log = LoggerFactory.getLogger("[indexer]");

    private QueueProvider provider;         //队列
    private int no_task_interval    = 1000; //从队列中获取不到任务时的休眠时间
    private int batch_fetch_count   = 10;   //一次从队列中获取任务的数量
    private int tasks_per_thread    = 1;    //每个线程处理的任务数

    public FetchTaskThread() {
        this.provider = QueueFactory.getProvider();
        Properties props = GiteeSearchConfig.getIndexerProperties();
        this.no_task_interval = NumberUtils.toInt(props.getProperty("no_task_interval"), 1000);
        this.batch_fetch_count = NumberUtils.toInt(props.getProperty("batch_fetch_count"), 10);
        this.tasks_per_thread = NumberUtils.toInt(props.getProperty("tasks_per_thread"), 1);
    }

    @Override
    public void run() {
        while(!this.isInterrupted()) {
            long startTime = System.currentTimeMillis();
            final AtomicInteger taskCount = new AtomicInteger(0);
            BatchTaskRunner.execute(provider.types(), 1, types -> {
                String type = types.get(0);
                List<QueueTask> tasks = provider.queue(type).pop(batch_fetch_count);
                if(tasks != null && tasks.size() > 0) {
                    try (
                        IndexWriter writer = StorageFactory.getIndexWriter(type);
                        TaxonomyWriter taxonomyWriter = StorageFactory.getTaxonomyWriter(type))
                    {
                        BatchTaskRunner.execute(tasks, tasks_per_thread, list -> {
                            list.forEach(task -> {
                                try {
                                    //System.out.printf("%s --> %s\n", Thread.currentThread().getName(), task.getType());
                                    task.write(writer, taxonomyWriter);
                                } catch (Exception e) {
                                    log.error("Failed writing task to index repository", e);
                                }
                            });
                        });
                        log.info("{} tasks<{}> finished in {} ms", tasks.size(), type, System.currentTimeMillis() - startTime);
                        taskCount.addAndGet(tasks.size());
                    } catch ( IOException e ) {
                        log.error("Failed to write tasks<"+type+"> to indexes.", e);
                    }
                }
            });

            if (taskCount.get() == 0){
                try {
                    Thread.sleep(no_task_interval);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

}
