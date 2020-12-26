package com.gitee.search.action;

import com.gitee.search.core.SearchHelper;
import com.gitee.search.http.Action;
import com.gitee.search.query.QueryHelper;
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
        String sort = param(request, "sort");
        int page = Math.max(1, param(request,"p", 1));
        String lang = param(request, "lang");
        if(StringUtils.isBlank(q)) {
            this.json(context.response(), "{}");
            return ;
        }
        String json = QueryHelper.searchRepositories(q, sort, lang, page, PAGE_SIZE);
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
        String json = QueryHelper.searchIssues(q, sort, page, PAGE_SIZE);
        this.json(context.response(), json);
    }

}
