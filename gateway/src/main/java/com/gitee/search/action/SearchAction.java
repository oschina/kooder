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
     * 搜索页面
     * @param context
     * @throws IOException
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
        this.vm(context, "search.vm", params);
    }

    /**
     * search git repositories
     * https://<search-server>/search/repositories?q=xxxx&sort=xxxx
     * @param context
     * @return
     */
    public void repositories(RoutingContext context) throws IOException {
        HttpServerRequest request = context.request();
        String q = param(request, "q");
        if(StringUtils.isBlank(q)) {
            this.json(context.response(), "{}");
            return ;
        }
        String sort = param(request, "sort");
        int page = Math.max(1, param(request,"p", 1));
        String lang = param(request, "lang");
        String json = QueryFactory.REPO()
                                .setSearchKey(q)
                                .setSort(sort)
                                .setPage(page)
                                .setPageSize(PAGE_SIZE)
                                .addFacets(Constants.FIELD_LANGUAGE, lang)
                                .search();
        this.json(context.response(), json);
    }

    /**
     * search git issues
     * https://<search-server>/search/issues?q=xxxx&sort=xxxx
     * @param context
     * @return
     */
    public void issues(RoutingContext context) throws IOException {
        HttpServerRequest request = context.request();
        String q = param(request, "q");
        String sort = param(request, "sort");
        int page = Math.max(1, param(request,"p", 1));
        if(StringUtils.isBlank(q)) {
            this.json(context.response(), "{}");
            return ;
        }
        String json = QueryFactory.ISSUE()
                                .setSearchKey(q)
                                .setSort(sort)
                                .setPage(page)
                                .setPageSize(PAGE_SIZE)
                                .search();
        this.json(context.response(), json);
    }

}
