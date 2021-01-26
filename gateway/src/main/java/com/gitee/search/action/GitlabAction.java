package com.gitee.search.action;

import com.gitee.search.server.Action;
import io.vertx.ext.web.RoutingContext;
import org.gitlab4j.api.systemhooks.SystemHookManager;
import org.gitlab4j.api.webhook.WebHookManager;

/**
 * Handle gitlab webhook
 * http://localhost:8080/gitlab/system   System hook  (new project/project updated etc)
 * http://localhost:8080/gitlab/project  Project hook (issue\push\pr etc)
 * @author Winter Lau<javayou@gmail.com>
 */
public class GitlabAction implements Action {

    /**
     * handle system webhook from gitlab
     * @param context
     */
    public void system(RoutingContext context) {
        new SystemHookManager();
    }

    /**
     * handle project webhook from gitlab
     * @param context
     */
    public void project(RoutingContext context) {
        new WebHookManager();
    }

}
