package com.gitee.search.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.NoSuchFileException;
import java.util.Map;

/**
 * HTTP Response
 * @author Winter Lau<javayou@gmail.com>
 */
public class Response {

    private final static Logger log = LoggerFactory.getLogger(Response.class);

    public final static String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    public final static String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";

    private HttpResponseStatus status = HttpResponseStatus.OK;
    private String contentType;
    private ByteBuf body = Unpooled.EMPTY_BUFFER;

    public Response(){}

    public Response(String contentType, String body) {
        this(contentType, (body==null||body.length()==0)?null:Unpooled.copiedBuffer(body, CharsetUtil.UTF_8));
    }

    public Response(String contentType, ByteBuf body) {
        this.contentType = contentType;
        this.body = (body==null)?Unpooled.EMPTY_BUFFER:body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public ByteBuf getBody() {
        return body;
    }

    public void setBody(ByteBuf body) {
        this.body = body;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public void setStatus(HttpResponseStatus status) {
        this.status = status;
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

    /**
     * export http error status
     * @param errorCode
     * @return
     */
    public final static Response error(HttpResponseStatus errorCode) {
        Response resp = new Response(CONTENT_TYPE_HTML, (ByteBuf)null);
        resp.setStatus(errorCode);
        return resp;
    }

    /**
     * Serve static file
     * @param path
     * @return
     */
    public final static Response file(String path) {
        Response response = new Response();
        response.setContentType(StaticFileService.getMimeType(path));
        try {
            ByteBuf body = StaticFileService.read(path);
            response.setBody(body);
        } catch (NoSuchFileException e) {
            response.setStatus(HttpResponseStatus.NOT_FOUND);
        } catch (Exception e) {
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            log.error("Failed to read static file: " + path, e);
        }
        return response;
    }

}
