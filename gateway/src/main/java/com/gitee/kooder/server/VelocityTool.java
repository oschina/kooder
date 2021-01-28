package com.gitee.kooder.server;

import com.gitee.kooder.code.CodeLine;
import com.gitee.kooder.core.GiteeSearchConfig;
import com.gitee.kooder.core.SearchHelper;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

/**
 * 给 vm 模板提供一些工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class VelocityTool {

    private RoutingContext context;

    public VelocityTool(RoutingContext context) {
        this.context = context;
    }

    public RoutingContext context() {
        return context;
    }

    /**
     * 静态资源自动增加时间戳参数
     * @param uri
     * @return
     */
    public String static_with_timestamp(String uri) {
        StringBuffer url = new StringBuffer();
        url.append(uri);
        Path path = GiteeSearchConfig.getPath("gateway/src/main/webapp/" + uri);
        if(Files.exists(path)) {
            try {
                url.append("?timestamp=");
                url.append(Files.getLastModifiedTime(path).toMillis());
            } catch (IOException e0) {}
        }
        return url.toString();
    }

    /**
     * 读取 HTTP 参数
     * @param name
     * @param defValue
     * @return
     */
    public int param(String name, int defValue) {
        return NumberUtils.toInt(context.request().getParam(name), defValue);
    }

    /**
     * 读取 HTTP 参数
     * @param name
     * @param defValue
     * @return
     */
    public String param(String name, String...defValue) {
        String value = context.request().getParam(name);
        return (value != null) ? value : ((defValue!=null&&defValue.length>0)?defValue[0]:null);
    }

    public boolean is_empty(String value) {
        return StringUtils.isBlank(value);
    }

    /**
     * 显示页码
     * @param totalPage
     * @param currentPage
     * @param baseNum
     * @return
     */
    public static int[] pages(int totalPage, int currentPage, int baseNum) {
        int from = currentPage - ( currentPage % baseNum ) + 1;
        int to = Math.min(from + baseNum - 1, totalPage);
        int count = Math.max(0, to - from + 1);
        if(count == 0)
            return null;
        int[] pages = new int[count];
        for(int i=from;i<=to;i++){
            pages[i-from] = i;
        }
        return pages;
    }

    /**
     * url 拼接
     * @param name
     * @param value
     * @return
     */
    public StringBuffer uri(String name, Object value) {
        HttpServerRequest req = context.request();
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
     * 删除 URL 中的某个参数
     * @param name
     * @return
     */
    public StringBuffer remove_uri_param(String name) {
        HttpServerRequest req = context.request();
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
     * 搜索关键字高亮
     * @param text
     * @param key
     * @param maxLen
     * @return
     */
    public static StringBuffer highlight(String text, String key, int maxLen) {
        return new StringBuffer(SearchHelper.highlight(text, key, maxLen));
    }

    /**
     * 源码高亮，由于使用不同的 Analyzer ，所以需要不同的方法
     * @param code
     * @param key
     * @return
     */
    public static StringBuffer hlcode(String code, String key) {
        return new StringBuffer(SearchHelper.hlcode(code, key));
    }

    /**
     * 高亮标识出源码中的关键字
     * @param code
     * @param key
     * @param maxLines
     * @return
     */
    public static List<CodeLine> hl_lines(String code, String key, int maxLines) {
        return SearchHelper.hl_lines(code, key, maxLines);
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
