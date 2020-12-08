package com.gitee.search.server;

import java.util.Map;

/**
 * HTTP Response
 * @author Winter Lau<javayou@gmail.com>
 */
public class Response {

    public final static String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    public final static String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";

    private String contentType;
    private String body;

    public Response(){}

    public Response(String contentType, String body) {
        this.contentType = contentType;
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long length() {
        return (body != null) ? body.length() : 0;
    }

    /**
     * Make a html response
     * @param html
     * @return
     */
    public final static Response html(String html) {
        return new Response(CONTENT_TYPE_HTML, html);
    }

    /**
     * Make a json response
     * @param json
     * @return
     */
    public final static Response json(String json) {
        return new Response(CONTENT_TYPE_JSON, json);
    }

    /**
     * Execute velocity template
     * @param vm
     * @param params
     * @return
     */
    public final static Response vm(String vm, Map params) {
        return Response.html(TemplateEngine.render(vm, params));
    }
}
