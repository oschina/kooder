package com.gitee.search.server;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Request
 * @author Winter Lau<javayou@gmail.com>
 */
@Deprecated
public class Request {

    private Map<String, Object> params;
    private String body;
    private HttpMethod method;
    private String path;
    private String uri;
    private HttpHeaders headers;
    private boolean keepAlive;

    /**
     * Turn netty request to gitee search request
     * @param req
     * @return
     */
    final static Request fromNettyHttpRequest(HttpRequest req) {
        Request request = new Request();
        QueryStringDecoder uri_decoder = new QueryStringDecoder(req.uri());
        LastHttpContent trailer = (LastHttpContent) req;
        Map<String, Object> tmpParams = new HashMap<>();
        uri_decoder.parameters().forEach((k,l) -> {
            if(l.size() == 1)
                tmpParams.put(k, l.get(0));
            else if (l.size() > 1)
                tmpParams.put(k, l.stream().toArray(String[]::new));
        });
        request.params = Collections.unmodifiableMap(tmpParams);
        request.body = formatBody(trailer).toString();
        request.method = req.method();
        request.path = uri_decoder.path();
        request.uri = req.uri();
        request.headers = req.headers();
        request.keepAlive = HttpUtil.isKeepAlive(req);
        return request;
    }

    public String getBody() {
        return body;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public boolean isPost() {
        return HttpMethod.POST.equals(this.method);
    }

    public boolean isGet() {
        return HttpMethod.GET.equals(this.method);
    }

    public String getPath() {
        return path;
    }

    public String getUri() {
        return uri;
    }

    public String uri(String name, Object value) {
        StringBuilder newUri = new StringBuilder();
        newUri.append(this.path);
        params.forEach((k,v) -> {
            if(!name.equals(k)) {
                newUri.append((newUri.length()==this.path.length())?'?':'&');
                newUri.append(encodeURL(k));
                newUri.append('=');
                newUri.append(encodeURL(v.toString()));
            }
        });
        newUri.append((newUri.length()==this.path.length())?'?':'&');
        newUri.append(encodeURL(name));
        newUri.append('=');
        newUri.append(encodeURL(value.toString()));
        return newUri.toString();
    }

    public boolean isKeepAlive() {
        return keepAlive;
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
        String value = param(name);
        return NumberUtils.toInt(value, defValue);
    }

    /**
     * 读取字符串参数
     * @param name
     * @param defValue
     * @return
     */
    public String param(String name, String defValue) {
        String v = param(name);
        return (v!=null)?v:defValue;
    }

    public String param(String name) {
        Object values = params.get(name);
        if(values instanceof String[])
            return ((String[])values)[0];
        return (String)params.get(name);
    }

    public String[] params(String name) {
        Object values = params.get(name);
        if(values instanceof String[])
            return (String[])values;
        return new String[]{values.toString()};
    }

    public Map<String, Object> params() {
        return params;
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

    private static String encodeURL(String url) {
        if (StringUtils.isEmpty(url))
            return "";
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {}
        return url;
    }
}
