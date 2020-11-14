package com.gitee.search.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The global gitee search configuration
 * @author Winter Lau<javayou@gmail.com>
 */
public class GiteeSearchConfig {

    private final static String CONFIG_NAME = "/gitee-search.properties";
    private static GiteeSearchConfig config;
    private Properties props ;

    /**
     * read sub properties by prefix
     *
     * @param i_prefix prefix of config
     * @return properties without prefix
     */
    public Properties properties(String i_prefix) {
        if(i_prefix == null)
            return props;

        Properties sub_props = new Properties();
        final String prefix = i_prefix + '.';
        props.forEach((k, v) -> {
            String key = (String) k;
            if (key.startsWith(prefix)) {
                sub_props.setProperty(key.substring(prefix.length()), trim((String) v));
            }
        });
        return sub_props;
    }

    private GiteeSearchConfig() {
        this.props = new Properties();
    }

    /**
     * loading gitee search configuration
     * @return
     */
    public final static GiteeSearchConfig init() throws IOException {
        if(config != null)
            return config;

        synchronized (GiteeSearchConfig.class) {
            if(config != null)
                return config;

            try (InputStream stream = getConfigStream(CONFIG_NAME)) {
                config = new GiteeSearchConfig();
                config.props.load(stream);
            }
        }

        return config;
    }

    /**
     * get j2cache properties stream
     *
     * @return config stream
     */
    private static InputStream getConfigStream(String resource) throws IOException {
        InputStream configStream = GiteeSearchConfig.class.getResourceAsStream(resource);
        if (configStream == null) {
            configStream = GiteeSearchConfig.class.getClassLoader().getParent().getResourceAsStream(resource);
        }
        if (configStream == null) {
            throw new FileNotFoundException("Cannot find " + resource + " !!!");
        }
        return configStream;
    }

    private static String trim(String str) {
        return (str != null) ? str.trim() : null;
    }
}
