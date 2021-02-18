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
package com.gitee.kooder.core;

import org.apache.commons.lang3.math.NumberUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

/**
 * 配置工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class Configuration {

    private static Map<String, Configuration> configs = new Hashtable<>();
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

    public String getProperty(String key) {
        return this.props.getProperty(key);
    }

    public String getProperty(String key, String defValue) {
        return this.props.getProperty(key, defValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        return NumberUtils.toInt(getProperty(key), defaultValue);
    }

    private Configuration() {
        this.props = new Properties();
    }

    /**
     * loading gitee search configuration
     * @param configName
     * @return
     */
    public final static Configuration init(String configName) throws IOException {
        Configuration cfg = configs.get(configName);
        if(cfg != null)
            return cfg;

        synchronized (Configuration.class) {
            cfg = configs.get(configName);
            if(cfg != null)
                return cfg;

            try (InputStream stream = getConfigStream(configName)) {
                cfg = new Configuration();
                cfg.props.load(stream);
                configs.put(configName, cfg);
            }
        }

        return cfg;
    }

    /**
     * get j2cache properties stream
     *
     * @return config stream
     */
    private static InputStream getConfigStream(String resource) throws IOException {
        InputStream configStream = Configuration.class.getResourceAsStream(resource);
        if (configStream == null) {
            configStream = Configuration.class.getClassLoader().getParent().getResourceAsStream(resource);
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
