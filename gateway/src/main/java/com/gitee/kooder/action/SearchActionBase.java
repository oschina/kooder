package com.gitee.kooder.action;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.models.QueryResult;
import com.gitee.kooder.query.QueryFactory;
import com.gitee.kooder.server.Action;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * Action for search, both for web and api
 * @author Winter Lau<javayou@gmail.com>
 */
public interface SearchActionBase extends Action {

    /**
     * execute search
     * @param context
     * @param type
     * @return
     * @throws IOException
     */
    default QueryResult _search(RoutingContext context, String type) throws IOException {
        String q = param(context.request(), "q");
        if(StringUtils.isBlank(q))
            return null;

        QueryResult result = null;
        int page = Math.max(1, param(context.request(),"p", 1));

        String sort = param(context.request(), "sort");
        String lang = param(context.request(), Constants.FIELD_LANGUAGE);

        switch (type) {
            case Constants.TYPE_REPOSITORY:
                result = QueryFactory.REPO()
                        .setEnterpriseId(param(context.request(), Constants.FIELD_ENTERPRISE_ID, 0))
                        .setSearchKey(q)
                        .addFacets(Constants.FIELD_LANGUAGE, lang)
                        .setSort(sort)
                        .setPage(page)
                        .setPageSize(PAGE_SIZE)
                        .execute();
                break;

            case Constants.TYPE_ISSUE:
                result = QueryFactory.ISSUE()
                        .setEnterpriseId(param(context.request(), Constants.FIELD_ENTERPRISE_ID, 0))
                        .setSearchKey(q)
                        .setSort(sort)
                        .setPage(page)
                        .setPageSize(PAGE_SIZE)
                        .execute();
                break;

            case Constants.TYPE_CODE:
                result = QueryFactory.CODE()
                        .setSearchKey(q)
                        .addFacets(Constants.FIELD_LANGUAGE, lang)
                        .addFacets(Constants.FIELD_REPO_NAME, param(context.request(), Constants.FIELD_REPO_NAME))
                        .addFacets(Constants.FIELD_CODE_OWNER, param(context.request(), Constants.FIELD_CODE_OWNER))
                        .setSort(sort)
                        .setPage(page)
                        .setPageSize(PAGE_SIZE)
                        .execute();
        }
        return result;
    }

}
