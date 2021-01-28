package com.gitee.search.action;

import com.gitee.search.server.Action;
import io.vertx.ext.web.RoutingContext;

/**
 * Handle Gitea webhook
 * http://localhost:8080/gitea   web hook  (new project/project updated etc)
 * @author Winter Lau<javayou@gmail.com>
 */
public class GiteaAction implements Action {

    /**
     * Gitea webhook handler
     * @param context
     */
    public void index(RoutingContext context) {

    }
}
