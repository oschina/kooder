package com.gitee.search.action;

import com.gitee.search.core.Constants;
import com.gitee.search.server.Action;
import com.gitee.search.query.QueryFactory;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * 搜索接口
 * @author Winter Lau<javayou@gmail.com>
 */
public class SearchAction implements Action {

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
