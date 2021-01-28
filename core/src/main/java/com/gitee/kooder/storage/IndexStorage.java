package com.gitee.kooder.storage;

import com.gitee.kooder.core.Constants;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.Properties;

/**
 * 索引存储接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface IndexStorage {

    /**
     * 对象类型和存储目录的对应关系
     */
    Properties MAPPING_TYPES = new Properties(){{
        setProperty(Constants.TYPE_REPOSITORY,  "repos");
        setProperty(Constants.TYPE_CODE,        "code");
        setProperty(Constants.TYPE_COMMIT,      "commits");
        setProperty(Constants.TYPE_ISSUE,       "issues");
        setProperty(Constants.TYPE_PR,          "pulls");
        setProperty(Constants.TYPE_WIKI,        "wikis");
        setProperty(Constants.TYPE_USER,        "users");
    }};

    /**
     * 存储的唯一名称
     * @return
     */
    String name();

    /**
     * 获取索引更新的入口
     * @param type
     * @return
     * @exception
     */
    IndexWriter getWriter(String type) throws IOException;

    /**
     * 获取分类数据写入入口
     * @param type
     * @return
     * @throws IOException
     */
    TaxonomyWriter getTaxonomyWriter(String type) throws IOException;

    /**
     * 获取读索引的入口
     * @param type
     * @return
     * @exception
     */
    IndexReader getReader(String type) throws IOException;

    /**
     * 获取分类索引的读取入口
     * @param type
     * @return
     * @throws IOException
     */
    TaxonomyReader getTaxonomyReader(String type) throws IOException;

}
