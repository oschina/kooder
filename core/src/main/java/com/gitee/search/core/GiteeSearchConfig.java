package com.gitee.search.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * The global gitee search configuration
 * @author Winter Lau<javayou@gmail.com>
 */
public class GiteeSearchConfig {

    private final static Logger log = LoggerFactory.getLogger(GiteeSearchConfig.class);

    private final static String CONFIG_NAME = "/gitee-search.properties";
    private static Configuration config;

    static {
        try {
            config = Configuration.init(CONFIG_NAME);
        } catch (IOException e) {
            log.error("Failed to loading " + CONFIG_NAME, e);
        }
    }

    public static Properties getQueueProperties() {
        return config.properties("queue");
    }

    public static Properties getStoragePropertes() {
        return config.properties("storage");
    }

}
