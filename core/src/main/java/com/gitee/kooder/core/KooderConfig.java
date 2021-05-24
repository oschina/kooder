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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * The global gitee search configuration
 * @author Winter Lau<javayou@gmail.com>
 */
public class KooderConfig {

    private final static Logger log = LoggerFactory.getLogger(KooderConfig.class);

    private final static String CONFIG_NAME = "kooder.properties";
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

    /**
     * 解析配置里的路径信息，转成 Path 对象，支持相对路径，绝对路径
     * @param spath
     * @return
     */
    public static Path getPath(String spath) {
        return Paths.get(spath).toAbsolutePath().normalize();
    }

    /**
     * Check and Initialize directory, if no exists, create it!
     * @param spath
     * @throws IOException
     */
    public static Path checkAndCreatePath(String spath) throws IOException {
        return checkAndCreatePath(getPath(spath));
    }

    /**
     * Check and Initialize directory, if no exists, create it!
     * @param p
     * @throws IOException
     */
    public static Path checkAndCreatePath(Path p) throws IOException {
        //路径已存在，但是不是一个目录，不可读写则报错
        if(Files.exists(p) && (!Files.isDirectory(p) || !Files.isReadable(p) || !Files.isWritable(p)))
            throw new FileSystemException("Path:" + p + " isn't available.");
        //路径不存在，或者不是一个目录，则创建目录
        if(!Files.exists(p) || !Files.isDirectory(p)) {
            log.warn("Path '" + p + "' for indexes not exists, created it!");
            Files.createDirectory(p);
        }
        return p;
    }

    /***
     * read configuration from kooder.properties
     * @param name
     * @return
     */
    public static String getProperty(String name) {
        return config.getProperty(name);
    }

    /**
     * read configuration from kooder.properties with default value
     * @param name
     * @param defValue
     * @return
     */
    public static String getProperty(String name, String defValue) {
        return config.getProperty(name, defValue);
    }

 }
