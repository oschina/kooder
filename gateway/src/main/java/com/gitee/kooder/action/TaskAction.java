/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.action;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.queue.QueueFactory;
import com.gitee.kooder.queue.QueueTask;
import io.vertx.ext.web.RoutingContext;

import java.util.Arrays;

/**
 * index task related api
 * @author Winter Lau<javayou@gmail.com>
 */
public class TaskAction {

    /**
     * Repository index task
     * @param context
     */
    public void repositories(RoutingContext context) {
        String jsonBody = context.getBodyAsString();
        String action = getAction(context);
        _pushTask(Constants.TYPE_REPOSITORY, action, jsonBody);
    }

    /**
     * Code index task api
     * @param context
     */
    public void codes(RoutingContext context) {
        String jsonBody = context.getBodyAsString();
        String action = getAction(context);
        _pushTask(Constants.TYPE_CODE, action, jsonBody);
    }

    /**
     * Issue index task api
     * @param context
     */
    public void issues(RoutingContext context) {
        String jsonBody = context.getBodyAsString();
        String action = getAction(context);
        _pushTask(Constants.TYPE_ISSUE, action, jsonBody);
    }

    /**
     * push task to queue for later handling
     * @param type
     * @param action
     * @param body
     */
    private void _pushTask(String type, String action, String body) {
        QueueTask task = new QueueTask();
        task.setType(type);
        task.setAction(action);
        task.setJsonObjects(body);
        QueueFactory.getProvider().queue(task.getType()).push(Arrays.asList(task));
    }

    /**
     * Turn http method to action
     * @param context
     * @return
     */
    private String getAction(RoutingContext context) {
        switch(context.request().method().name()){
            case "POST":
            case "PUT":
                return QueueTask.ACTION_ADD;
            case "UPDATE":
                return QueueTask.ACTION_UPDATE;
            case "DELETE":
                return QueueTask.ACTION_DELETE;
        }
        return QueueTask.ACTION_ADD;
    }

}
