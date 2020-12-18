package com.gitee.search.server;

import com.gitee.search.core.SearchHelper;
import org.apache.commons.lang.StringUtils;

/**
 * 给 vm 模板提供一些工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class VelocityTool {

    /**
     * 搜索关键字高亮
     * @param text
     * @param key
     * @return
     */
    public StringBuffer highlight(String text, String key) {
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

}
