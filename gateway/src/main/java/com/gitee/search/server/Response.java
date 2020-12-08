package com.gitee.search.server;

import io.netty.handler.codec.http.HttpHeaderNames;

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

    public final static Response html(String html) {
        return new Response(CONTENT_TYPE_HTML, html);
    }

    public final static Response json(String json) {
        return new Response(CONTENT_TYPE_JSON, json);
    }
}
