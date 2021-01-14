package com.gitee.search.action;

import com.gitee.search.core.Constants;
import com.gitee.search.query.QueryFactory;
import com.gitee.search.server.Action;
import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Action for search, both for web and api
 * @author Winter Lau<javayou@gmail.com>
 */
public interface SearchActionBase extends Action {

    /**
     * execute search
     * @param request
     * @param type
     * @return
     * @throws IOException
     */
    default String _search(HttpServerRequest request, String type) throws IOException {
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
