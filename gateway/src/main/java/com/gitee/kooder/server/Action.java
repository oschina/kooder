/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.server;

import com.gitee.kooder.queue.QueueTask;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.math.NumberUtils;

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
     * @param context
     * @param vm
     * @param params
     */
    default void vm(RoutingContext context, String vm, Map params) {
        String content = TemplateEngine.render(vm, params, context);
        context.response().putHeader("Content-Type", "text/html; charset=UTF-8").send(content);
    }

    /**
     * Render velocity template
     * @param context
     * @param vm
     */
    default void vm(RoutingContext context, String vm) {
        vm(context, vm, null);
    }

    /**
     * Redirect to a new url
     * @param context
     * @param newUrl
     */
    default void redirect(RoutingContext context, String newUrl) {
        context.response().setStatusCode(HttpResponseStatus.FOUND.code()).putHeader("Location", newUrl);
    }

    /**
     * 从参数中解析对象类型字段，并判断值是否有效
     * @param context
     * @return
     */
    default String getType(RoutingContext context) {
        String type = param(context.request(), "type");
        return QueueTask.isAvailType(type)?type.toLowerCase():null;
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
