package com.gitee.search.http;

import com.gitee.search.server.TemplateEngine;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action base
 * Eash request has one independent action instance
 * @author Winter Lau<javayou@gmail.com>
 */
public interface Action {

    int PAGE_SIZE = 20; //结果集每页显示的记录数

    /**
     * invoke velocity template
     * @param res
     * @param vm
     * @param params
     */
    default void vm(HttpServerResponse res, String vm, Map params) {
        String content = TemplateEngine.render(vm, params);
        res.putHeader("Content-Type", "text/html; charset=UTF-8").send(content);
    }

    /**
     * output json
     * @param res
     * @param json
     */
    default void json(HttpServerResponse res, String json) {
        res.putHeader("content-type","application/json; charset=utf-8").end(json);
    }

    default String param(HttpServerRequest req, String name, String...defValue) {
        String val = req.getParam(name);
        return (val!=null)?val:(defValue.length>0)?defValue[0]:null;
    }

    default int param(HttpServerRequest req, String name, int defValue) {
        String val = req.getParam(name);
        return NumberUtils.toInt(val, defValue);
    }

    default Map<String, Object> params(HttpServerRequest request) {
        Map<String, Object> params = new HashMap<>();
        MultiMap mm = request.params();
        for(String name : mm.names()){
            List<String> values = mm.getAll(name);
            if(values.size() == 1)
                params.put(name, values.get(0));
            else
                params.put(name, values.stream().toArray(String[]::new));
        }
        return Collections.unmodifiableMap(params);
    }

    default void error(HttpServerResponse res, int code, String...msg) {
        res.setStatusCode(code);
        if(msg != null && msg.length > 0)
            res.setStatusMessage(String.join("",msg));
    }

}