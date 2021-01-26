package com.gitee.search.action;

import com.gitee.search.server.Action;
import io.vertx.ext.web.RoutingContext;

/**
 * Handle webhook
 * http://localhost:8080/webhook/gitlab_system   System hook  (new project/project updated etc)
 * http://localhost:8080/webhook/gitlab_project  Project hook (issue\push\pr etc)
 * @author Winter Lau<javayou@gmail.com>
 */
public class WebhookAction implements Action {

    /**
     * handle webhook from gitee
     * @param context
     */
    public void gitee(RoutingContext context) {

    }

    /**
     * handle webhook from gitea
     * @param context
     */
    public void gitea(RoutingContext context) {

    }

}
