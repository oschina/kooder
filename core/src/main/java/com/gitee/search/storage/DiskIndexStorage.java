package com.gitee.search.storage;

import com.gitee.search.core.AnalyzerFactory;
import com.gitee.search.core.GiteeSearchConfig;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;

import static com.gitee.search.core.Constants.TYPE_CODE;

/**
 * 磁盘索引管理器
 * @author Winter Lau<javayou@gmail.com>
 */
public class DiskIndexStorage implements IndexStorage {

    private final static Logger log = LoggerFactory.getLogger(DiskIndexStorage.class);

    private Path indexBasePath;
    private Properties props;
    private boolean isWindows = false;

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
        isWindows = SystemUtils.IS_OS_WINDOWS;
    }

    private FSDirectory getDirectory(String type, boolean taxonomy) throws IOException {
        Path path = getIndexPath(type, taxonomy);
        return isWindows?FSDirectory.open(path):NIOFSDirectory.open(path);
    }

    @Override
    public IndexWriter getWriter(String type) throws IOException {
        return new IndexWriter(getDirectory(type, false), getWriterConfig(type));
    }

    @Override
    public IndexReader getReader(String type) throws IOException {
        return DirectoryReader.open(getDirectory(type, false));
    }

    /**
     * 获取分类数据写入入口
     *
     * @param type
     * @return
     * @throws IOException
     */
    @Override
    public TaxonomyWriter getTaxonomyWriter(String type) throws IOException {
        return new DirectoryTaxonomyWriter(getDirectory(type, true));
    }

    /**
     * 获取分类索引的读取入口
     *
     * @param type
     * @return
     * @throws IOException
     */
    @Override
    public TaxonomyReader getTaxonomyReader(String type) throws IOException {
        return new DirectoryTaxonomyReader(getDirectory(type, true));
    }

    /**
     * 获取指定类型对象的索引目录
     * @param type
     * @parma taxonomy  是否为分类数据
     * @return
     * @throws IOException
     */
    private Path getIndexPath(String type, boolean taxonomy) throws IOException {
        String subPath = MAPPING_TYPES.getProperty(type, type);
        subPath += taxonomy?"_taxo":"_idxs";
        Path ipath = indexBasePath.resolve(subPath);
        if(!Files.exists(ipath) || !Files.isDirectory(ipath))
            Files.createDirectory(ipath);
        return ipath;
    }

    /**
     * 索引配置
     * @return
     */
    private IndexWriterConfig getWriterConfig(String type) {
        Analyzer analyzer = TYPE_CODE.equals(type)?AnalyzerFactory.getCodeAnalyzer():AnalyzerFactory.getInstance(true);
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        writerConfig.setUseCompoundFile(Boolean.valueOf(props.getProperty("disk.use_compound_file", "true")));
        writerConfig.setMaxBufferedDocs(NumberUtils.toInt(props.getProperty("disk.max_buffered_docs"), -1));
        writerConfig.setRAMBufferSizeMB(NumberUtils.toInt(props.getProperty("disk.ram_buffer_size_mb"), 16));
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return writerConfig;
    }

    @Override
    public String name() {
        return "disk";
    }

}
