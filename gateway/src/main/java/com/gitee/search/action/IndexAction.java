package com.gitee.search.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.search.server.Action;
import com.gitee.search.query.QueryHelper;
import com.gitee.search.queue.QueueTask;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        HttpServerRequest request = context.request();
        String q = param(request, "q");
        String type = param(request,"type", "repo");

        String sort = param(request, "sort");
        int page = Math.max(1, param(request,"p", 1));
        String lang = param(request, "lang");

        Map<String, Object> params = new HashMap();

        params.putAll(params(request));
        params.put("request", request);

        if(StringUtils.isNotBlank(q)) {
            String json = null;
            switch (type) {
                case QueueTask.TYPE_REPOSITORY:
                    json = QueryHelper.REPOSITORY
                            .setSearchKey(q)
                            .setSort(sort)
                            .setPage(page)
                            .setPageSize(PAGE_SIZE)
                            .setFacets("lang", lang)
                            .search();
                    break;
                case QueueTask.TYPE_ISSUE:
                    json = QueryHelper.ISSUE
                            .setSearchKey(q)
                            .setSort(sort)
                            .setPage(page)
                            .setPageSize(PAGE_SIZE)
                            .search();
                    break;
                default:
                    error(context.response(), HttpResponseStatus.BAD_REQUEST.code());
                    return;
            }

            if(json != null) {
                JsonNode node = new ObjectMapper().readTree(json);
                params.put("result", node);
            }
        }
        this.vm(context, "index.vm", params);
    }

}
