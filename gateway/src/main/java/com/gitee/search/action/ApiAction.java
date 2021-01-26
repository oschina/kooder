package com.gitee.search.action;

import com.gitee.search.models.QueryResult;
import com.gitee.search.queue.QueueFactory;
import com.gitee.search.queue.QueueTask;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.util.Arrays;

/**
 * Gitee Search API
 * @author Winter Lau<javayou@gmail.com>
 */
public class ApiAction implements SearchActionBase {

    /**
     * search objects according to param type
     * <code>https://<search-server>/api/search?type=xxx&q=xxxx&sort=xxxx</code>
     * @param context
     * @throws IOException
     */
    public void search(RoutingContext context) throws IOException {
        String type = context.request().getParam("type");
        QueryResult result = _search(context.request(), type);
        if(result == null) {
            error(context.response(), HttpResponseStatus.BAD_REQUEST.code(),
                    "Illegal parameter 'type' value.");
            return;
        }
        this.json(context.response(), result.json());
    }

    /**
     * add/update/delete task
     * @param context
     * @throws IOException
     */
    public void task(RoutingContext context) throws IOException {
        String action = context.request().getParam("action");
        this._pushTask(action, context);
    }

    /**
     * push task to queue for later handling
     * @param action
     * @param context
     */
    private void _pushTask(String action, RoutingContext context) {
        QueueTask task = new QueueTask();
        task.setAction(action);
        task.setType(getType(context));
        task.setBody(context.getBodyAsString());
        if(!task.check()) {
            this.error(context.response(), HttpResponseStatus.BAD_REQUEST.code());
            return;
        }
        QueueFactory.getProvider().queue(task.getType()).push(Arrays.asList(task));
    }

}
