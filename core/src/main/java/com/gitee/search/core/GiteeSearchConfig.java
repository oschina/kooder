package com.gitee.search.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            log.error("Failed to loading {}", CONFIG_NAME, e);
        }
    }

    /**
     * 队列配置
     * @return
     */
    public static Properties getQueueProperties() {
        return config.properties("queue");
    }

    /**
     * 存储配置
     * @return
     */
    public static Properties getStoragePropertes() {
        return config.properties("storage");
    }

    /**
     * HTTP 服务配置
     * @return
     */
    public static Properties getHttpProperties() {
        return config.properties("http");
    }

    /**
     * 索引器的配置
     * @return
     */
    public static Properties getIndexerProperties() {
        return config.properties("indexer");
    }

    public static String getHttpBind() {
        String bind =  config.getProperty("http.bind");
        if(bind != null && bind.trim().length() == 0)
            bind = null;
        return bind;
    }

    public static int getHttpPort() {
        return config.getIntProperty("http.port", 8080);
    }

    public static int getHttpMaxContentLength() {
        return config.getIntProperty("http.maxContentLength", 512 * 1024);
    }

    /**
     * 解析配置里的路径信息，转成 Path 对象，支持相对路径，绝对路径
     * @param spath
     * @return
     */
    public static Path getPath(String spath) {
        return Paths.get(spath).toAbsolutePath().normalize();
    }

    /***
     * read configuration from gitee-search.properties
     * @param name
     * @return
     */
    public static String getProperty(String name) {
        return config.getProperty(name);
    }

    /**
     * read configuration from gitee-search.properties with default value
     * @param name
     * @param defValue
     * @return
     */
    public static String getProperty(String name, String defValue) {
        return config.getProperty(name, defValue);
    }
 }
