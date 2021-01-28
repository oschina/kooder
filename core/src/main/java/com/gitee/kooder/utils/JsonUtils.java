package com.gitee.kooder.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * JSON 工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class JsonUtils {

    private final static ObjectMapper JSON = new ObjectMapper();

    static {
        JSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSON.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

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

    /**
     * 解析 JSON 到对象
     * @param content
     * @param valueType
     * @param <T>
     * @return
     */
    public static <T> T readValue(String content, Class<T> valueType) {
        try {
            return JSON.readValue(content, valueType);
        } catch(JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T readValue(String content, TypeReference<T> valueTypeRef) {
        try {
            return JSON.readValue(content, valueTypeRef);
        } catch(JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T readValue(InputStream src, TypeReference<T> valueTypeRef) {
        try {
            return JSON.readValue(src, valueTypeRef);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
