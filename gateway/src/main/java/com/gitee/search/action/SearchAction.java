package com.gitee.search.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * web interface for search
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

        String json = _search(request, type);
        if(json == null) {
            error(context.response(), HttpResponseStatus.BAD_REQUEST.code(), "Illegal parameter 'type' value.");
            return;
        }

        Map<String, Object> params = new HashMap();
        JsonNode node = new ObjectMapper().readTree(json);
        params.put("result", node);

        this.vm(context, "search.vm", params);
    }

}
