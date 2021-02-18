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
