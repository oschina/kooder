package com.gitee.search.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.search.queue.QueueTask;
import com.gitee.search.server.Request;
import com.gitee.search.server.Response;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Default action
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexAction {

    /**
     * web searcher
     * @param request
     * @return
     */
    public static Response index(Request request) throws IOException {
        String q = request.param("q");
        String type = request.param("type", "repo");
        Map<String, Object> params = new HashMap();
        params.putAll(request.params());
        params.put("request", request);

        if(StringUtils.isNotBlank(q)) {
            String json = null;
            switch (type) {
                case QueueTask.TYPE_REPOSITORY:
                    json = SearchAction.repositories(request);
            }

            if(json != null) {
                JsonNode node = new ObjectMapper().readTree(json);
                params.put("result", node);
            }
        }
        return Response.vm("index.vm", params);
    }

    /**
     * test redirect
     * @param req
     * @return
     */
    public static Response test(Request req) {
        return Response.redirect("/", false);
    }

}
