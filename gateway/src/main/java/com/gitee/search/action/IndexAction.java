package com.gitee.search.action;

import com.gitee.search.server.Action;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;

/**
 * Default action
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexAction implements Action {

    /**
     * web searcher
     * @param context
     * @return
     */
    public void index(RoutingContext context) throws IOException {
        this.vm(context, "index.vm");
    }

}
