package com.gitee.kooder.action;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.core.GiteeSearchConfig;
import com.gitee.kooder.gitee.GiteeException;
import com.gitee.kooder.server.Action;
import com.gitee.kooder.server.GiteeWebHookManager;
import io.vertx.ext.web.RoutingContext;

/**
 * Handle Gitee webhook
 * http://localhost:8080/gitee   web hook  (new project/project updated etc)
 *
 * @author Winter Lau<javayou@gmail.com>
 */
public class GiteeAction implements Action {

    private static final String SECRET_TOKEN = GiteeSearchConfig.getProperty("gitlab.secret_token", Constants.DEFAULT_SECRET_TOKEN);

    /**
     * Gitee webhook handler
     *
     * @param context
     */
    public void index(RoutingContext context) throws GiteeException {
        GiteeWebHookManager.handleEvent(SECRET_TOKEN, context);
    }

}
