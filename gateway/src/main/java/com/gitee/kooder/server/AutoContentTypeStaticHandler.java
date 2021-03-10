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

import com.gitee.kooder.core.KooderConfig;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.StaticHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 自动生成静态文件对应的 Content-Type 信息，弥补 vertx 的不足
 * @author Winter Lau<javayou@gmail.com>
 */
public class AutoContentTypeStaticHandler extends StaticHandlerImpl {

    private final static Logger log = LoggerFactory.getLogger(AutoContentTypeStaticHandler.class);
    private final static Properties CONTENT_TYPES = new Properties();
    private final static Path webRoot;

    static {
        String sWebroot = KooderConfig.getProperty("http.webroot");
        webRoot = KooderConfig.getPath(sWebroot);
        try (InputStream stream = AutoContentTypeStaticHandler.class.getResourceAsStream("/mime-types.properties")) {
            CONTENT_TYPES.load(stream);
        } catch(IOException e) {
            log.error("Failed to loading mime-types.properties", e);
        }
    }

    public AutoContentTypeStaticHandler(){
        super();
        this.setAllowRootFileSystemAccess(true);
        this.setWebRoot(webRoot.toString());
    }

    @Override
    public void handle(RoutingContext context) {
        String path = context.request().path();
        int idx = path.lastIndexOf(".");
        String ctype = (idx > 0) ? CONTENT_TYPES.getProperty(path.substring(idx + 1).toLowerCase()) : null;
        if(ctype != null)
            context.response().putHeader("content-type", ctype);
        super.handle(context);
    }
}
