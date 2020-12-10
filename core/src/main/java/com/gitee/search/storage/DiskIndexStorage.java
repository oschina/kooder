package com.gitee.search.storage;

import com.gitee.search.core.AnalyzerFactory;
import com.gitee.search.core.GiteeSearchConfig;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;

/**
 * 磁盘索引管理器
 * @author Winter Lau<javayou@gmail.com>
 */
public class DiskIndexStorage implements IndexStorage {

    private final static Logger log = LoggerFactory.getLogger(DiskIndexStorage.class);

    private Path indexBasePath;
    private Properties props;

    /**
     * 初始化磁盘索引存储
     * @param props
     * @throws IOException
     */
    public DiskIndexStorage(Properties props) throws IOException {
        this.props = props;
        String idxPath = props.getProperty("disk.path");
        this.indexBasePath = GiteeSearchConfig.getPath(idxPath);
        //路径已存在，但是不是一个目录，不可读写则报错
        if(Files.exists(indexBasePath) && (!Files.isDirectory(indexBasePath) || !Files.isReadable(indexBasePath) || !Files.isWritable(indexBasePath)))
            throw new FileSystemException("Path:" + idxPath + " isn't available.");
        //路径不存在，或者不是一个目录，则创建目录
        if(!Files.exists(indexBasePath) || !Files.isDirectory(indexBasePath)) {
            log.warn("Path '" + this.indexBasePath.toString()+"' for indexes not exists, created it!");
            Files.createDirectory(indexBasePath);
        }
    }

    @Override
    public IndexWriter getWriter(String type) throws IOException {
        IndexWriterConfig writerConfig = new IndexWriterConfig(AnalyzerFactory.INSTANCE);

        writerConfig.setUseCompoundFile(Boolean.valueOf(props.getProperty("disk.use_compound_file", "true")));
        writerConfig.setMaxBufferedDocs(NumberUtils.toInt(props.getProperty("disk.max_buffered_docs"), -1));
        writerConfig.setRAMBufferSizeMB(NumberUtils.toInt(props.getProperty("disk.ram_buffer_size_mb"), 16));
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        return new IndexWriter(FSDirectory.open(getIndexPath(type)), writerConfig);
    }

    @Override
    public IndexReader getReader(String type) throws IOException {
        return DirectoryReader.open(FSDirectory.open(getIndexPath(type)));
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
