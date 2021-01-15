package com.gitee.search.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * JSON 工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class JsonUtils {

    private final static ObjectMapper JSON = new ObjectMapper();

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

    /**
     * 对象转 JSON 字符串
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        try {
            return JSON.writeValueAsString(obj);
        } catch(JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
