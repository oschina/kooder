package com.gitee.search.action;

import org.apache.commons.lang.math.NumberUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Action 常用工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class ActionUtils {

    /**
     * 读取整数参数
     * @param params
     * @param name
     * @param defValue
     * @return
     */
    public static int getParam(Map<String, List<String>> params, String name, int defValue) {
        String value = params.getOrDefault(name, Arrays.asList(String.valueOf(defValue))).get(0);
        return NumberUtils.toInt(value, defValue);
    }

    /**
     * 读取字符串参数
     * @param params
     * @param name
     * @param defValue
     * @return
     */
    public static String getParam(Map<String, List<String>> params, String name, String defValue) {
        return params.getOrDefault(name, Arrays.asList(defValue)).get(0);
    }

    public static String getParam(Map<String, List<String>> params, String name) {
        List<String> values = params.get(name);
        return (values != null && values.size() > 0)?values.get(0):null;
    }

}
