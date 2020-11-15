package com.gitee.http.server;

import com.gitee.search.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Gateway configurations
 * @author Winter Lau<javayou@gmail.com>
 */
public class GatewayConfig {

    private final static Logger log = LoggerFactory.getLogger(GatewayConfig.class);

    private final static String CONFIG_NAME = "/gateway.properties";
    private static Configuration config;

    static {
        try {
            config = Configuration.init(CONFIG_NAME);
        } catch (IOException e) {
            log.error("Failed to loading " + CONFIG_NAME, e);
        }
    }

    public static String getBind() {
        String bind =  config.getProperty("http.bind");
        if(bind != null && bind.trim().length() == 0)
            bind = null;
        return bind;
    }

    public static int getPort() {
        return config.getIntProperty("http.port", 8080);
    }

    public static int getMaxContentLength() {
        return config.getIntProperty("http.maxContentLength", 524288);
    }
}
