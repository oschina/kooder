package com.gitee.search.action;

import com.gitee.search.queue.QueueFactory;
import com.gitee.search.queue.QueueProvider;
import com.gitee.search.queue.QueueTask;

import static com.gitee.search.action.ActionUtils.getParam;

import java.util.List;
import java.util.Map;

/**
 * 内嵌式队列服务提供的 HTTP 接口
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueueAction {

    /**
     * 从队列中获取待索引任务
     * @param params
     * @param body
     * @return
     * @throws ActionException
     */
    public static StringBuilder fetch(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        QueueProvider queue = QueueFactory.getProvider();
        List<QueueTask> tasks = queue.pop(1);
        return (tasks.size() > 0) ? new StringBuilder(tasks.get(0).json()) : null;
    }

}
