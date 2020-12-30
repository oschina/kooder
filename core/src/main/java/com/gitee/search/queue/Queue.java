package com.gitee.search.queue;

import java.util.Collection;
import java.util.List;

/**
 * 定义了获取索引任务的队列接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface Queue extends AutoCloseable{

    /**
     * 队列的唯一名称
     * @return
     */
    String type();

    /**
     * 添加任务到队列
     * @param tasks
     */
    void push(Collection<QueueTask> tasks) ;

    /**
     * 从队列获取任务
     * @return
     */
    List<QueueTask> pop(int count) ;

}
