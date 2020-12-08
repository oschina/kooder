package com.gitee.search.server;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP Request
 * @author Winter Lau<javayou@gmail.com>
 */
public class Request {

    private Map<String, List<String>> params = new HashMap<>();
    private String body;
    private HttpMethod method;
    private String path;
    private String uri;
    private HttpHeaders headers;

    /**
     * Turn netty request to gitee search request
     * @param req
     * @return
     */
    final static Request fromNettyHttpRequest(HttpRequest req) {
        Request request = new Request();
        QueryStringDecoder uri_decoder = new QueryStringDecoder(req.uri());
        LastHttpContent trailer = (LastHttpContent) req;
        request.params.putAll(uri_decoder.parameters());
        request.body = formatBody(trailer).toString();
        request.method = req.method();
        request.path = uri_decoder.path();
        request.uri = req.uri();
        request.headers = ((LastHttpContent) req).trailingHeaders();
        return request;
    }

    public String getBody() {
        return body;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getUri() {
        return uri;
    }

    /**
     * Get request header
     * @param name
     */
    public String getHeader(String name) {
        return (headers!=null)?headers.get(name):null;
    }

    /**
     * 读取整数参数
     * @param name
     * @param defValue
     * @return
     */
    public int param(String name, int defValue) {
        String value = params.getOrDefault(name, Arrays.asList(String.valueOf(defValue))).get(0);
        return NumberUtils.toInt(value, defValue);
    }

    /**
     * 读取字符串参数
     * @param name
     * @param defValue
     * @return
     */
    public String param(String name, String defValue) {
        return params.getOrDefault(name, Arrays.asList(defValue)).get(0);
    }

    public String param(String name) {
        List<String> values = params.get(name);
        return (values != null && values.size() > 0)?values.get(0):null;
    }

    public List<String> params(String name) {
        return params.get(name);
    }

    /**
     * 提取 HTTP 请求中的 Body 内容
     * @param httpContent
     * @return
     */
    private final static StringBuilder formatBody(HttpContent httpContent) {
        StringBuilder responseData = new StringBuilder();
        ByteBuf content = httpContent.content();
        if (content.isReadable()) {
            responseData.append(content.toString(CharsetUtil.UTF_8));
            responseData.append("\r\n");
        }
        return responseData;
    }

}
