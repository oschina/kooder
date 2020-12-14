package com.gitee.search.server;

import com.gitee.search.core.GiteeSearchConfig;
import org.apache.commons.lang.StringUtils;
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

    public final static String ENCODING = "utf-8";
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
                if(value instanceof String)
                    return html((String)value);
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
     * @return
     */
    public static String render(String vm, Map params) {
        VelocityContext context = new VelocityContext();
        context.attachEventCartridge(eventCartridge);
        if(params != null && params.size() > 0)
            params.forEach((k,v) -> context.put(k.toString(), v));
        StringWriter w = new StringWriter();
        Velocity.mergeTemplate(vm, ENCODING, context, w);
        return w.toString();
    }

    /**
     * HTML escape
     * @param content
     * @return
     */
    private final static String html(String content) {
        if (content == null) return "";
        String html = content;
        html = StringUtils.replace(html, "'", "&apos;");
        html = StringUtils.replace(html, "\"", "&quot;");
        html = StringUtils.replace(html, "\t", "&nbsp;&nbsp;");// 替换跳格
        html = StringUtils.replace(html, "<", "&lt;");
        html = StringUtils.replace(html, ">", "&gt;");
        return html;
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
