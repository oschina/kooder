package com.gitee.search.server;

import com.gitee.search.core.GiteeSearchConfig;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.event.EventCartridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * webapp template engine
 * @author Winter Lau<javayou@gmail.com>
 */
public class TemplateEngine {

    private final static Logger log = LoggerFactory.getLogger(TemplateEngine.class);

    public final static String ENCODING     = "utf-8";
    public final static String VAR_CONTEXT  = "context";
    public final static String VAR_REQUEST  = "request";
    public final static String VAR_RESPONSE = "response";
    public final static String VAR_TOOL     = "tool";

    private final static String VAR_LAYOUT          = "layout";
    private final static String VAR_PAGE_TITLE      = "page_title";
    private final static String VAR_SCREEN_CONTENT  = "screen_content";

    private final static EventCartridge eventCartridge = new EventCartridge();

    static {
        //Initialize velocity engine
        try (InputStream stream = TemplateEngine.class.getResourceAsStream("/velocity.properties")) {
            Properties p = new Properties();
            p.load(stream);
            String sWebappPath = p.getProperty("resource.loader.file.path");
            if(sWebappPath != null) {
                Path webappPath = GiteeSearchConfig.getPath(sWebappPath);
                p.setProperty("resource.loader.file.path", webappPath.toString());
            }
            Velocity.init(p);

            eventCartridge.addReferenceInsertionEventHandler((context, reference, value) -> {
                if(value instanceof String) //自动对输出的文本进行 HTML 转义
                    return VelocityTool.html((String)value);
                return value;
            });
        } catch(IOException e) {
            log.error("Failed to loading velocity.properties", e);
        }
    }

    /**
     * execute velocity template
     * @param vm
     * @param params
     * @param routingContext
     * @return
     */
    public static String render(String vm, Map params, RoutingContext routingContext) {
        VelocityContext context = initContext(routingContext);
        if(params != null && params.size() > 0)
            params.forEach((k,v) -> context.put(k.toString(), v));
        StringWriter w = new StringWriter();
        if(Velocity.mergeTemplate(vm, ENCODING, context, w)) {
            String vm_layout = (String) context.get(VAR_LAYOUT);
            if (StringUtils.isNotBlank(vm_layout)) {
                vm_layout = "layout/" + vm_layout;
                context.put(VAR_SCREEN_CONTENT, w);
                StringWriter html = new StringWriter();
                Velocity.mergeTemplate(vm_layout, ENCODING, context, html);
                return html.toString();
            }
            return w.toString();
        }
        return null;
    }

    /**
     * 构造 Velocity 上下文
     * @return
     */
    private static VelocityContext initContext(RoutingContext routingContext) {
        VelocityContext context = new VelocityContext();
        context.attachEventCartridge(eventCartridge);
        context.put(VAR_TOOL,       new VelocityTool(routingContext));
        context.put(VAR_CONTEXT,    routingContext);
        context.put(VAR_REQUEST,    routingContext.request());
        context.put(VAR_RESPONSE,   routingContext.response());
        return context;
    }

    /**
     * just for test
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        VelocityContext context = new VelocityContext();
        context.attachEventCartridge(eventCartridge);

        context.put("name", "Velocity");
        context.put("project", "Jakarta");
        /* lets render a template */
        StringWriter w = new StringWriter();
        Velocity.mergeTemplate("index.vm", "utf-8", context, w );
        System.out.println( w );
    }

}
