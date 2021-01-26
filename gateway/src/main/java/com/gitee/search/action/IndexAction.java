package com.gitee.search.action;

import com.gitee.search.models.QueryResult;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Default action for web
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexAction implements SearchActionBase {

    /**
     * web searcher
     * @param context
     * @return
     */
    public void index(RoutingContext context) throws IOException {
        this.vm(context, "index.vm");
    }

    /**
     * controller for search.vm
     * @param context
     * @throws IOException
     */
    public void search(RoutingContext context) throws IOException {

        HttpServerRequest request = context.request();
        String q = param(request, "q");
        if(StringUtils.isBlank(q)) {
            this.redirect(context, "/");
            return ;
        }

        String type = param(request,"type", "repo");

        QueryResult result = _search(request, type);
        if(result == null) {
            error(context.response(), HttpResponseStatus.BAD_REQUEST.code(), "Illegal parameter 'type' value.");
            return;
        }

        Map<String, Object> params = new HashMap();
        params.put("result", result);

        this.vm(context, "search.vm", params);
    }

}
