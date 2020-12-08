package com.gitee.search.action;

import com.gitee.search.queue.QueueFactory;
import com.gitee.search.queue.QueueTask;
import com.gitee.search.server.Request;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Arrays;

/**
 * 索引的维护
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexAction {

    /**
     * 添加索引
     * @param request
     */
    public static void add(Request request) throws ActionException {
        pushTask(QueueTask.ACTION_ADD, request);
    }

    /**
     * 修改索引
     * @param request
     */
    public static void update(Request request) throws ActionException {
        pushTask(QueueTask.ACTION_UPDATE, request);
    }

    /**
     * 删除索引
     * @param request
     */
    public static void delete(Request request) throws ActionException {
        pushTask(QueueTask.ACTION_DELETE, request);
    }

    private static void pushTask(String action, Request request) throws ActionException {
        QueueTask task = new QueueTask();
        task.setAction(action);
        task.setType(parseType(request));
        task.setBody(request.getBody());
        if(task.check())
            QueueFactory.getProvider().push(Arrays.asList(task));
        else
            throw new ActionException(HttpResponseStatus.NOT_ACCEPTABLE);
    }

    /**
     * 从参数中解析对象类型字段，并判断值是否有效
     * @param request
     * @return
     * @throws ActionException
     */
    private static String parseType(Request request) throws ActionException {
        try {
            String type = request.param("type");
            if(!QueueTask.isAvailType(type))
                throw new IllegalArgumentException(type);
            return type.toLowerCase();
        }catch(Exception e) {
            throw new ActionException(HttpResponseStatus.BAD_REQUEST);
        }
    }

}
