package com.gitee.search.storage;

import com.gitee.search.core.GiteeSearchConfig;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * 索引存储管理工厂类
 * @author Winter Lau<javayou@gmail.com>
 */
public class StorageFactory {

    private final static Logger log = LoggerFactory.getLogger(StorageFactory.class);

    static IndexStorage storage;

    static {
        Properties props = GiteeSearchConfig.getStoragePropertes();
        if("disk".equalsIgnoreCase(props.getProperty("type").trim())) {
            try {
                storage = new DiskIndexStorage(props);
            } catch (IOException e) {
                log.error("Failed to initialize storage manager.", e);
            }
        }
    }

    public final static IndexStorage getStorage() {
        return storage;
    }

    /**
     * 获取索引更新的入口
     * @param type
     * @return
     * @exception
     */
    public static IndexWriter getWriter(String type) throws IOException {
        return storage.getWriter(type);
    }

    /**
     * 获取读索引的入口
     * @param type
     * @return
     * @exception
     */
    public static IndexReader getReader(String type) throws IOException {
        return storage.getReader(type);
    }

}
