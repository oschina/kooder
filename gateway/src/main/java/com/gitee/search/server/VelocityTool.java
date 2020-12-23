package com.gitee.search.server;

import com.gitee.search.core.SearchHelper;
import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

/**
 * 给 vm 模板提供一些工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class VelocityTool {

    /**
     * url 拼接
     * @param req
     * @param name
     * @param value
     * @return
     */
    public static StringBuffer uri(HttpServerRequest req, String name, Object value) {
        StringBuffer newUri = new StringBuffer();
        String path = req.path();
        newUri.append(path);
        req.params().forEach(e -> {
            String k = e.getKey();
            String v = e.getValue();
            if(!name.equals(k)) {
                newUri.append((newUri.length()==path.length())?'?':'&');
                newUri.append(encodeURL(k));
                newUri.append('=');
                newUri.append(encodeURL(v.toString()));
            }
        });
        newUri.append((newUri.length()==path.length())?'?':'&');
        newUri.append(encodeURL(name));
        newUri.append('=');
        newUri.append(encodeURL(value.toString()));
        return newUri;
    }

    /**
     * 搜索关键字高亮
     * @param text
     * @param key
     * @return
     */
    public static StringBuffer highlight(String text, String key) {
        return new StringBuffer(SearchHelper.highlight(text, key));
    }

    /**
     * HTML escape
     * @param content
     * @return
     */
    public final static String html(String content) {
        if (content == null) return "";
        String html = content;
        html = StringUtils.replace(html, "'", "&apos;");
        html = StringUtils.replace(html, "\"", "&quot;");
        html = StringUtils.replace(html, "\t", "&nbsp;&nbsp;");// 替换跳格
        html = StringUtils.replace(html, "<", "&lt;");
        html = StringUtils.replace(html, ">", "&gt;");
        return html;
    }

    /**
     * 日期格式化
     * @param fmt
     * @param d
     * @return
     */
    public final static String format(String fmt, Date d) {
        return DateFormatUtils.format(d, fmt);
    }

    public final static String format(String fmt, long millis) {
        return DateFormatUtils.format(millis, fmt);
    }

    public final static String encodeURL(String url) {
        if (StringUtils.isEmpty(url))
            return "";
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {}
        return url;
    }
}
