package com.gitee.search.queue;

import java.util.List;
import java.util.Properties;

/**
 * 定义了获取索引任务的队列接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface QueueProvider {

    /**
     * 队列的唯一名称
     * @return
     */
    String name();

    /**
     * 添加任务到队列
     * @param tasks
     */
    void push(List<QueueTask> tasks) ;

    /**
     * 从队列获取任务
     * @return
     */
    List<QueueTask> pop(int count) ;

    /**
     * 关闭并释放资源
     */
    void close();

}
