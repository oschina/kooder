package com.gitee.search.action;

import com.gitee.search.queue.EmbedQueueProvider;
import com.gitee.search.queue.QueueFactory;
import com.gitee.search.queue.QueueTask;

/**
 * 内嵌式队列服务提供的 HTTP 接口
 * 使用该接口要求指定配置 queue.type = embed
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueueAction {

    /**
     * 从队列中获取待索引任务
     * @return
     * @throws ActionException
     */
    public static String fetch() {
        EmbedQueueProvider queue = (EmbedQueueProvider)QueueFactory.getProvider();
        QueueTask task = queue.innerPop();
        return (task != null) ? task.json() : null;
    }

}
