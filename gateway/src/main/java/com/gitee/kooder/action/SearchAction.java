package com.gitee.kooder.action;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.models.QueryResult;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Kooder search api
 * @author Winter Lau<javayou@gmail.com>
 */
public class SearchAction implements SearchActionBase {

    /**
     * controller for search.vm
     * @param context
     * @throws IOException
     */
    public void index(RoutingContext context) throws IOException {
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

    /**
     * Search Repositories
     * @param context
     * @throws IOException
     */
    public void repositories(RoutingContext context) throws IOException {
        QueryResult result = _search(context.request(), Constants.TYPE_REPOSITORY);
        this.json(context.response(), result.json());
    }

    /**
     * Search Issues
     * @param context
     * @throws IOException
     */
    public void issues(RoutingContext context) throws IOException {
        QueryResult result = _search(context.request(), Constants.TYPE_ISSUE);
        this.json(context.response(), result.json());
    }

    /**
     * Search Codes
     * @param context
     * @throws IOException
     */
    public void codes(RoutingContext context) throws IOException {
        QueryResult result = _search(context.request(), Constants.TYPE_CODE);
        this.json(context.response(), result.json());
    }

}
