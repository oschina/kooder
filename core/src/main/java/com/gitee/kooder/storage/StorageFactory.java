package com.gitee.kooder.storage;

import com.gitee.kooder.core.GiteeSearchConfig;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 索引以及仓库存储管理工厂类
 * @author Winter Lau<javayou@gmail.com>
 */
public class StorageFactory {

    private final static Logger log = LoggerFactory.getLogger(StorageFactory.class);

    private static IndexStorage storage; //index storage
    private static Path repositoriesPath;
    private static int  repositoriesMaxSizeInGigabyte = 100;//unit:G

    static {
        Properties props = GiteeSearchConfig.getStoragePropertes();
        try {
            if("disk".equalsIgnoreCase(props.getProperty("type").trim())) {
                storage = new DiskIndexStorage(props);
            }
            String repoPath = props.getProperty("repositories.path");
            repositoriesPath = GiteeSearchConfig.checkAndCreatePath(repoPath);
        } catch (IOException e) {
            log.error("Failed to initialize storage manager.", e);
        }
        repositoriesMaxSizeInGigabyte = NumberUtils.toInt(props.getProperty("repositories.max_size_in_gigabyte"), 100);
    }

    /**
     * 返回仓库的存储路径
     * 1234/5678/J2Cache_12345678
     * @param path
     * @return
     */
    public static Path getRepositoryPath(String path) {
        return repositoriesPath.resolve(path);
    }

    /**
     * 获取索引更新的入口
     * @param type
     * @return
     * @exception
     */
    public static IndexWriter getIndexWriter(String type) throws IOException {
        return storage.getWriter(type);
    }

    /**
     * 获取读索引的入口
     * @param type
     * @return
     * @exception
     */
    public static IndexReader getIndexReader(String type) throws IOException {
        return storage.getReader(type);
    }

    /**
     * 获取分类数据写入入口
     * @param type
     * @return
     * @throws IOException
     */
    public static TaxonomyWriter getTaxonomyWriter(String type) throws IOException {
        return storage.getTaxonomyWriter(type);
    }

    /**
     * 获取分类索引的读取入口
     * @param type
     * @return
     * @throws IOException
     */
    public static TaxonomyReader getTaxonomyReader(String type) throws IOException {
        return storage.getTaxonomyReader(type);
    }
}
