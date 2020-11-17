package com.gitee.search.storage;

import com.gitee.search.queue.QueueTask;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;
import java.util.HashMap;
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
        setProperty(QueueTask.TYPE_REPOSITORY, "repositories");
        setProperty(QueueTask.TYPE_CODE, "code");
        setProperty(QueueTask.TYPE_COMMIT, "commits");
        setProperty(QueueTask.TYPE_ISSUE, "issues");
        setProperty(QueueTask.TYPE_PR, "pulls");
        setProperty(QueueTask.TYPE_WIKI, "wikis");
        setProperty(QueueTask.TYPE_USER, "users");
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
     * 获取读索引的入口
     * @param type
     * @return
     * @exception
     */
    IndexReader getReader(String type) throws IOException;

    /**
     * 获取索引的检索器
     * @param type
     * @return
     * @throws IOException
     */
    IndexSearcher getSearcher(String type) throws IOException;

}
