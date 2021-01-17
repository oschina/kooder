package com.gitee.search.action;

import com.gitee.search.server.Action;
import io.vertx.ext.web.RoutingContext;
import org.gitlab4j.api.systemhooks.SystemHookManager;

/**
 * Handle webhook
 * http://localhost:8080/webhook/gitlab
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
     * handle webhook from gitlab
     * @param context
     */
    public void gitlab(RoutingContext context) {
        new SystemHookManager();
    }

    /**
     * handle webhook from gitea
     * @param context
     */
    public void gitea(RoutingContext context) {

    }

}
