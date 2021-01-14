package com.gitee.search.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.search.core.Constants;
import com.gitee.search.server.Action;
import com.gitee.search.query.QueryFactory;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 搜索接口
 * @author Winter Lau<javayou@gmail.com>
 */
public class SearchAction implements Action {

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

        Map<String, Object> params = new HashMap();

        params.putAll(params(request));
        params.put("request", request);

        String json = _search(request, type);
        if(json == null) {
            error(context.response(), HttpResponseStatus.BAD_REQUEST.code());
            return;
        }

        JsonNode node = new ObjectMapper().readTree(json);
        params.put("result", node);

        this.vm(context, "search.vm", params);
    }

    /**
     * search git repositories
     * https://<search-server>/search/repositories?q=xxxx&sort=xxxx
     * @param context
     * @return
     */
    public void repositories(RoutingContext context) throws IOException {
        String json = _search(context.request(), Constants.TYPE_REPOSITORY);
        this.json(context.response(), json);
    }

    /**
     * web interface for issue search
     * https://<search-server>/search/issues?q=xxxx&sort=xxxx
     * @param context
     * @return
     */
    public void issues(RoutingContext context) throws IOException {
        String json = _search(context.request(), Constants.TYPE_ISSUE);
        this.json(context.response(), json);
    }

    /**
     * web interface for source code search
     * https://<search-server>/search/codes?q=xxxx
     * @param context
     * @throws IOException
     */
    public void codes(RoutingContext context) throws IOException {
        String json = _search(context.request(), Constants.TYPE_CODE);
        this.json(context.response(), json);
    }

    /**
     * execute search
     * @param request
     * @param type
     * @return
     * @throws IOException
     */
    private String _search(HttpServerRequest request, String type) throws IOException {
        String q = param(request, "q");
        if(StringUtils.isBlank(q))
            return Constants.EMPTYJSON;

        String json = null;
        int page = Math.max(1, param(request,"p", 1));

        String sort = param(request, "sort");
        String lang = param(request, "lang");

        switch (type) {
            case Constants.TYPE_REPOSITORY:
                json = QueryFactory.REPO()
                        .setSearchKey(q)
                        .addFacets(Constants.FIELD_LANGUAGE, lang)
                        .setSort(sort)
                        .setPage(page)
                        .setPageSize(PAGE_SIZE)
                        .search();
                break;

            case Constants.TYPE_ISSUE:
                json = QueryFactory.ISSUE()
                        .setSearchKey(q)
                        .setSort(sort)
                        .setPage(page)
                        .setPageSize(PAGE_SIZE)
                        .search();
                break;

            case Constants.TYPE_CODE:
                json = QueryFactory.CODE()
                        .setSearchKey(q)
                        .addFacets(Constants.FIELD_LANGUAGE, lang)
                        .addFacets(Constants.FIELD_REPO_NAME, param(request, Constants.FIELD_REPO_NAME))
                        .addFacets(Constants.FIELD_CODE_OWNER, param(request, Constants.FIELD_CODE_OWNER))
                        .setSort(sort)
                        .setPage(page)
                        .setPageSize(PAGE_SIZE)
                        .search();
        }
        return json;
    }

}
