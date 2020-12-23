package com.gitee.search.http;

import com.gitee.search.core.GiteeSearchConfig;
import com.gitee.search.server.StaticFileService;
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

    private final static Logger log = LoggerFactory.getLogger(StaticFileService.class);
    private final static Properties CONTENT_TYPES = new Properties();
    private final static Path webRoot;

    static {
        String sWebroot = GiteeSearchConfig.getProperty("http.webroot");
        webRoot = GiteeSearchConfig.getPath(sWebroot);
        try (InputStream stream = AutoContentTypeStaticHandler.class.getResourceAsStream("/mime-types.properties")) {
            CONTENT_TYPES.load(stream);
        } catch(IOException e) {
            log.error("Failed to loading mime-types.properties", e);
        }
    }

    public AutoContentTypeStaticHandler(String webRoot){
        super();
        this.setAllowRootFileSystemAccess(true);
        this.setWebRoot(webRoot);
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
