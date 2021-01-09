package com.gitee.search.server;

import com.gitee.search.code.CodeLine;
import com.gitee.search.core.SearchHelper;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
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

    public int param(String name, int defValue) {
        return NumberUtils.toInt(context.request().getParam(name), defValue);
    }

    /**
     * 显示页码
     * @param totalPage
     * @param currentPage
     * @param baseNum
     * @return
     */
    public int[] pages(int totalPage, int currentPage, int baseNum) {
        int base = ((currentPage - 1) / baseNum) * baseNum;
        int from = base + 1;
        int to = Math.min(from + baseNum, totalPage);
        int count = Math.max(0, to - from);
        if(count == 0)
            return null;
        int[] pages = new int[count];
        for(int i=from;i<to;i++){
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
     * 搜索关键字高亮
     * @param text
     * @param key
     * @return
     */
    public static StringBuffer highlight(String text, String key) {
        return new StringBuffer(SearchHelper.highlight(text, key));
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
        return DateFormatUtils.format(millis * 1000, fmt);
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
