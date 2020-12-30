package com.gitee.search.action;

import com.gitee.search.server.Action;
import com.gitee.search.queue.QueueFactory;
import com.gitee.search.queue.QueueTask;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;

import java.util.Arrays;

/**
 * index add/update/delete etc.
 * @author Winter Lau<javayou@gmail.com>
 */
public class TaskAction implements Action {

    /**
     * 添加索引
     * @param context
     */
    public void add(RoutingContext context) {
        pushTask(QueueTask.ACTION_ADD, context);
    }

    /**
     * 修改索引
     * @param context
     */
    public void update(RoutingContext context) {
        pushTask(QueueTask.ACTION_UPDATE, context);
    }

    /**
     * 删除索引
     * @param context
     */
    public void delete(RoutingContext context) {
        pushTask(QueueTask.ACTION_DELETE, context);
    }

    /**
     * push task to queue for later handler
     * @param action
     * @param context
     */
    private void pushTask(String action, RoutingContext context) {
        QueueTask task = new QueueTask();
        task.setAction(action);
        task.setType(getType(context));
        task.setBody(context.getBodyAsString());
        if(task.check())
            QueueFactory.getProvider().queue(task.getType()).push(Arrays.asList(task));
        else
            error(context.response(), HttpResponseStatus.NOT_ACCEPTABLE.code());
    }

}
