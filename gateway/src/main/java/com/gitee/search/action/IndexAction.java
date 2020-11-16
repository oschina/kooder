package com.gitee.search.action;

import com.fasterxml.jackson.core.JsonFactory;
import com.gitee.search.queue.QueueTask;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;
import java.util.Map;

/**
 * 索引的维护
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexAction {

    private final static JsonFactory jackson = new JsonFactory();

    /**
     * 添加索引
     * @param params
     * @param body
     * @return
     */
    public static StringBuilder add(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        QueueTask task = new QueueTask();
        task.setAction(QueueTask.ACTION_ADD);
        task.setType(parseType(params));
        task.setBody(body.toString());
        return null;
    }

    private static String parseType(Map<String, List<String>> params) throws ActionException {
        try {
            String type = params.get("type").get(0);
            if(!QueueTask.isAvailType(type))
                throw new IllegalArgumentException(type);
            return type.toLowerCase();
        }catch(Exception e) {
            throw new ActionException(HttpResponseStatus.BAD_REQUEST);
        }
    }

    /**
     * 修改索引
     * @param params
     * @param body
     * @return
     */
    public static StringBuilder update(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

    /**
     * 删除索引
     * @param params
     * @param body
     * @return
     */
    public static StringBuilder delete(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

}
