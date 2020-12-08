package com.gitee.search.server;

import com.gitee.search.core.GiteeSearchConfig;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * webapp template engine
 * @author Winter Lau<javayou@gmail.com>
 */
public class TemplateEngine {

    public final static String ENCODING = "utf-8";

    static {
        String sWebappPath = GiteeSearchConfig.getProperty("http.webapp_path");
        Path webappPath = GiteeSearchConfig.getPath(sWebappPath);

        Properties p = new Properties();
        p.setProperty("resource.loader.file.path", webappPath.toString());
        Velocity.init(p);
    }

    /**
     * execute velocity template
     * @param vm
     * @param params
     * @return
     */
    public static String render(String vm, Map params) {
        VelocityContext context = new VelocityContext();
        if(params != null && params.size() > 0)
            params.forEach((k,v) -> context.put(k.toString(), v));
        StringWriter w = new StringWriter();
        Velocity.mergeTemplate(vm, ENCODING, context, w);
        return w.toString();
    }

    /**
     * just for test
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        VelocityContext context = new VelocityContext();
        context.put("name", "Velocity");
        context.put("project", "Jakarta");
        /* lets render a template */
        StringWriter w = new StringWriter();
        Velocity.mergeTemplate("index.vm", "utf-8", context, w );
        System.out.println( w );
    }

}
