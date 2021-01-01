package com.gitee.search.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.math.NumberUtils;

/**
 * JSON 工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class JsonUtils {

    /**
     * 读取文本json节点数据
     * @param node
     * @param fn
     * @return
     */
    public static String jsonAttr(JsonNode node, String fn) {
        JsonNode f = node.get(fn);
        return (f != null) ? f.textValue() : null;
    }

    /**
     * 读取数值 json 节点数据
     * @param node
     * @param fn
     * @param defaultValue
     * @return
     */
    public static long jsonAttr(JsonNode node, String fn, long defaultValue) {
        JsonNode f = node.get(fn);
        return (f!=null)?NumberUtils.toLong(f.textValue(), defaultValue):defaultValue;
    }
}
