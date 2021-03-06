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
package com.gitee.kooder.storage;

import com.gitee.kooder.core.AnalyzerFactory;
import com.gitee.kooder.core.KooderConfig;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;
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

import static com.gitee.kooder.core.Constants.TYPE_CODE;

/**
 * store index in disk
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
        this.indexBasePath = KooderConfig.checkAndCreatePath(idxPath);
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
        if(!Files.exists(ipath) || !Files.isDirectory(ipath)) {
            synchronized (this) {
                if(!Files.exists(ipath) || !Files.isDirectory(ipath))
                    Files.createDirectory(ipath);
            }
        }
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
