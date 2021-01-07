package com.gitee.search.indexer;

import com.gitee.search.code.*;
import com.gitee.search.core.GiteeSearchConfig;
import com.gitee.search.queue.QueueFactory;
import com.gitee.search.queue.QueueProvider;
import com.gitee.search.queue.QueueTask;
import com.gitee.search.storage.StorageFactory;
import com.gitee.search.utils.BatchTaskRunner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
        this.no_task_interval   = NumberUtils.toInt(props.getProperty("no_task_interval"),  1000);
        this.batch_fetch_count  = NumberUtils.toInt(props.getProperty("batch_fetch_count"), 10);
        this.tasks_per_thread   = NumberUtils.toInt(props.getProperty("tasks_per_thread"),  1);
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
                        //如果 tasks_per_thread < 0 ，则单线程处理
                        int threshold = (tasks_per_thread>0)?tasks_per_thread:tasks.size();
                        BatchTaskRunner.execute(tasks, threshold, list -> handleTasks(list, writer, taxonomyWriter));
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

    /**
     * 批量处理统一类型的任务
     * @param tasks
     * @param writer
     * @param taxonomyWriter
     */
    private void handleTasks(List<QueueTask> tasks, IndexWriter writer, TaxonomyWriter taxonomyWriter) {
        tasks.forEach( task -> {
            try {
                //代码类型的任务需要单独处理，而且需要区分对待公开和私有仓库
                if(!task.isCodeTask())
                    handleCodeTask(task, writer, taxonomyWriter);
                else
                    task.write(writer, taxonomyWriter);
            } catch (Exception e) {
                log.error("Failed writing task to index repository", e);
            }
        });
    }

    /**
     * 处理代码索引任务
     * 由于代码的索引任务繁重，因此从 QueueTask 中将逻辑剥离开来
     * @param task
     * @param writer
     * @param taxonomyWriter
     */
    private void handleCodeTask(QueueTask task, IndexWriter writer, TaxonomyWriter taxonomyWriter) {
        List<CodeRepository> repos = RepositoryFactory.getRepositoryFromTask(task);
        switch(task.getAction()){
        case QueueTask.ACTION_ADD:
        case QueueTask.ACTION_UPDATE:
            FileTraveler fileTraveler = new CodeFileTraveler(writer, taxonomyWriter);
            for(CodeRepository newRepo : repos) {
                //Read saved CodeRepository from persistent storage
                CodeRepository repo = RepositoryManager.INSTANCE.get(newRepo.getId());
                if (repo != null) {
                    if(StringUtils.isNotBlank(newRepo.getName()))
                        repo.setName(newRepo.getName());
                    if(StringUtils.isNotBlank(newRepo.getScm()))
                        repo.setScm(newRepo.getScm());
                    if(StringUtils.isNotBlank(newRepo.getUrl()))
                        repo.setUrl(newRepo.getUrl());
                    repo.setUsername(newRepo.getUsername());
                    repo.setPassword(newRepo.getPassword());
                } else {
                    repo = newRepo;
                }
                //pull repository from remote and build index for it
                RepositoryFactory.getProvider(repo.getScm()).pull(repo, fileTraveler);
                //write repository status to persistent storage
                RepositoryManager.INSTANCE.save(repo);
            }
            break;

        case QueueTask.ACTION_DELETE:
            for(CodeRepository newRepo : repos) {
                CodeRepository repo = RepositoryManager.INSTANCE.get(newRepo.getId());
                if (repo != null) {
                    RepositoryFactory.getProvider(repo.getScm()).delete(repo);
                    RepositoryManager.INSTANCE.delete(repo.getId());
                }
            }
        }
    }
}
