package com.gitee.search.action;

import com.gitee.search.server.Action;
import com.gitee.search.queue.EmbedQueueProvider;
import com.gitee.search.queue.QueueFactory;
import com.gitee.search.queue.QueueTask;
import io.vertx.ext.web.RoutingContext;

/**
 * 内嵌式队列服务提供的 HTTP 接口
 * 使用该接口要求指定配置 queue.type = embed
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueueAction implements Action {

    /**
     * 从队列中获取待索引任务
     * @param context
     * @return
     */
    public void fetch(RoutingContext context) {
        EmbedQueueProvider queue = (EmbedQueueProvider)QueueFactory.getProvider();
        QueueTask task = queue.innerPop();
        context.json(task);
        this.json(context.response(), task.json());
    }

}
