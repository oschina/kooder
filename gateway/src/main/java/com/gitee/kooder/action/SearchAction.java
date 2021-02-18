/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.action;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.models.QueryResult;
import com.gitee.kooder.query.QueryFactory;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

        QueryResult result = _search(context, type);
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
        String q = param(context.request(), "q");
        String sort = param(context.request(), "sort");
        String lang = param(context.request(), Constants.FIELD_LANGUAGE);
        int page = Math.max(1, param(context.request(),"p", 1));
        int eid = param(context.request(), Constants.FIELD_ENTERPRISE_ID, 0);

        //Specified repositories search
        List<String> repos = Arrays.asList(param(context.request(), Constants.FIELD_REPO_ID, "").split(","));
        String body = context.getBodyAsString();
        if(body != null)
            repos.addAll(Arrays.asList(body.split(",")));
        List<Integer> iRepos = repos.stream().map(r -> NumberUtils.toInt(r, 0)).filter(r -> (r > 0)).collect(Collectors.toList());

        QueryResult result = QueryFactory.REPO()
                .setEnterpriseId(eid)
                .addRepositories(iRepos)
                .setSearchKey(q)
                .addFacets(Constants.FIELD_LANGUAGE, lang)
                .setSort(sort)
                .setPage(page)
                .setPageSize(PAGE_SIZE)
                .execute();

        this.json(context.response(), result.json());
    }

    /**
     * Search Issues
     * @param context
     * @throws IOException
     */
    public void issues(RoutingContext context) throws IOException {
        String q = param(context.request(), "q");
        String sort = param(context.request(), "sort");
        int page = Math.max(1, param(context.request(),"p", 1));
        QueryResult result = QueryFactory.ISSUE()
                .setEnterpriseId(param(context.request(), Constants.FIELD_ENTERPRISE_ID, 0))
                .setSearchKey(q)
                .setSort(sort)
                .setPage(page)
                .setPageSize(PAGE_SIZE)
                .execute();
        this.json(context.response(), result.json());
    }

    /**
     * Search Codes
     * @param context
     * @throws IOException
     */
    public void codes(RoutingContext context) throws IOException {
        String q = param(context.request(), "q");
        String sort = param(context.request(), "sort");
        String lang = param(context.request(), Constants.FIELD_LANGUAGE);
        int page = Math.max(1, param(context.request(),"p", 1));
        int eid = param(context.request(), Constants.FIELD_ENTERPRISE_ID, 0);

        //Specified repositories search
        List<String> repos = Arrays.asList(param(context.request(), Constants.FIELD_REPO_ID, "").split(","));
        String body = context.getBodyAsString();
        if(body != null)
            repos.addAll(Arrays.asList(body.split(",")));
        List<Integer> iRepos = repos.stream().map(r -> NumberUtils.toInt(r, 0)).filter(r -> (r > 0)).collect(Collectors.toList());

        QueryResult result = QueryFactory.CODE()
                .setEnterpriseId(eid)
                .addRepositories(iRepos)
                .setSearchKey(q)
                .addFacets(Constants.FIELD_LANGUAGE, lang)
                .addFacets(Constants.FIELD_REPO_NAME, param(context.request(), Constants.FIELD_REPO_NAME))
                .addFacets(Constants.FIELD_CODE_OWNER, param(context.request(), Constants.FIELD_CODE_OWNER))
                .setSort(sort)
                .setPage(page)
                .setPageSize(PAGE_SIZE)
                .execute();

        this.json(context.response(), result.json());
    }

}
