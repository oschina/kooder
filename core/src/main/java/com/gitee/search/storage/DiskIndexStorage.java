package com.gitee.search.storage;

import com.gitee.search.core.GiteeSearchConfig;
import com.gitee.search.core.JcsegAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;

/**
 * 磁盘索引管理器
 * @author Winter Lau<javayou@gmail.com>
 */
public class DiskIndexStorage implements IndexStorage {

    private Path indexBasePath;

    public static void main(String[] args) throws IOException {
        DiskIndexStorage dis = new DiskIndexStorage(GiteeSearchConfig.getStoragePropertes());
        System.out.println(dis.indexBasePath);
        System.out.println(dis.indexBasePath.resolve("../hello").normalize());
        System.exit(0);
    }

    /**
     * 初始化磁盘索引存储
     * @param props
     * @throws IOException
     */
    public DiskIndexStorage(Properties props) throws IOException {
        String idxPath = props.getProperty("disk.path");
        this.indexBasePath = Paths.get(idxPath).normalize();
        if(!Files.exists(indexBasePath) || !Files.isDirectory(indexBasePath) || !Files.isReadable(indexBasePath) || !Files.isWritable(indexBasePath))
            throw new FileSystemException("Path:" + idxPath + " isn't available.");
    }

    @Override
    public IndexWriter getWriter(String type) throws IOException {
        FSDirectory dir = FSDirectory.open(getIndexPath(type));
        IndexWriterConfig writerConfig = new IndexWriterConfig(JcsegAnalyzer.INSTANCE);
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(dir, writerConfig);
    }

    @Override
    public IndexReader getReader(String type) throws IOException {
        FSDirectory dir = FSDirectory.open(getIndexPath(type));
        return DirectoryReader.open(dir);
    }

    @Override
    public IndexSearcher getSearcher(String type) throws IOException {
        return new IndexSearcher(getReader(type));
    }

    /**
     * 获取指定类型对象的索引目录
     * @param type
     * @return
     * @throws IOException
     */
    private Path getIndexPath(String type) throws IOException {
        String subPath = MAPPING_TYPES.getProperty(type, type);
        Path ipath = indexBasePath.resolve(subPath);
        if(!Files.exists(ipath) || !Files.isDirectory(ipath))
            Files.createDirectory(ipath);
        return ipath;
    }

    @Override
    public String name() {
        return "disk";
    }

}
