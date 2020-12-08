package com.gitee.search.action;

import com.gitee.search.server.Request;
import com.gitee.search.server.Response;

import java.util.Map;

/**
 * Default action
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexAction {

    /**
     * 测试输出
     * @param request
     * @return
     */
    public static Response index(Request request) {
        String q = request.param("q");
        Map params = (q!=null&&q.trim().length()>0)?request.params():null;
        return Response.vm("index.vm", params);
    }

}
