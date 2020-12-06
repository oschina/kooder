package com.gitee.search.action;

import java.util.List;
import java.util.Map;

public class DefaultAction {

    /**
     * 测试输出
     * @param params
     * @param body
     * @return
     * @throws ActionException
     */
    public static String index(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        StringBuilder resp = new StringBuilder();
        params.forEach((k,v) -> resp.append("\r\n" + k + "="+String.join(",", v)));
        resp.append("\r\nBODY:" + body);
        return resp.toString();
    }

}
