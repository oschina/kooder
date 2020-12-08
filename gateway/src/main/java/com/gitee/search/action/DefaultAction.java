package com.gitee.search.action;

import com.gitee.search.server.Request;
import com.gitee.search.server.Response;
import com.gitee.search.server.TemplateEngine;

/**
 * Default action
 * @author Winter Lau<javayou@gmail.com>
 */
public class DefaultAction {

    /**
     * 测试输出
     * @param request
     * @return
     */
    public static Response index(Request request) {
        return Response.html(TemplateEngine.render("index.vm", null));
    }

}
