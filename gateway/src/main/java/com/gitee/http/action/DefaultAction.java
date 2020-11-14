package com.gitee.http.action;

import java.util.List;
import java.util.Map;

public class DefaultAction {

    public static StringBuilder index(Map<String, List<String>> params, StringBuilder body) {
        StringBuilder resp = new StringBuilder();
        params.forEach((k,v) -> resp.append("\r\n" + k + "="+String.join(",", v)));
        resp.append("\r\nBODY:" + body);
        return resp;
    }

}
