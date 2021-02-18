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
package com.gitee.kooder.index;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.models.Repository;
import com.gitee.kooder.models.Searchable;
import com.gitee.kooder.queue.QueueTask;
import com.gitee.kooder.storage.StorageFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gitee.kooder.core.Constants.FIELD_ID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * kooder index mananger
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexManager {

    private final static Logger log = LoggerFactory.getLogger(IndexManager.class);

    public final static int SEARCH_THREAD_COUNT = 10; //并发搜索线程数

    public final static String KEY_SCORE = "_score_"; //在 json 中存放文档的score值
    public final static String KEY_DOC_ID = "_id_"; //在 json 中存放文档的 id

    //private final static int maxNumberOfCachedQueries = 256;
    //private final static long maxRamBytesUsed = 50 * 1024L * 1024L; // 50MB
    // these cache and policy instances can be shared across several queries and readers
    // it is fine to eg. store them into static variables
    //private final static QueryCache queryCache = new LRUQueryCache(maxNumberOfCachedQueries, maxRamBytesUsed);
    //private final static QueryCachingPolicy defaultCachingPolicy = new UsageTrackingQueryCachingPolicy();

    public final static FacetsConfig facetsConfig = new FacetsConfig();

    /**
     * 写入索引库
     * @return
     * @exception
     */
    public static int write(QueueTask task) throws IOException {
        try (
            IndexWriter writer = StorageFactory.getIndexWriter(task.getType());
            TaxonomyWriter taxonomyWriter = StorageFactory.getTaxonomyWriter(task.getType());
        ) {
            return write(task, writer, taxonomyWriter);
        }
    }

    /**
     * 用于多线程环境下共享 IndexWriter 写入
     * @param task
     * @param i_writer
     * @param t_writer
     * @return
     * @throws IOException
     */
    public static int write(QueueTask task, IndexWriter i_writer, TaxonomyWriter t_writer) throws IOException {
        if(task.getObjects() == null || task.getObjects().size() == 0)
            return 0;
        switch (task.getAction()) {
            case QueueTask.ACTION_ADD:
            case QueueTask.ACTION_UPDATE:
                List<Document> docs = task.getObjects().stream().map(o -> o.getDocument()).collect(Collectors.toList());
                update(docs, i_writer, t_writer);
                break;
            case QueueTask.ACTION_DELETE:
                List<Long> objects = task.getObjects().stream().map(o -> o.getId()).collect(Collectors.toList());
                Query[] queries = objects.stream().map(id -> NumericDocValuesField.newSlowExactQuery(FIELD_ID, id)).toArray(Query[]::new);
                i_writer.deleteDocuments(queries);
                List<Long> repos = new ArrayList<>();
                for(Searchable obj : task.getObjects()) {
                    if(obj instanceof Repository) {
                        repos.add(obj.getId());
                    }
                }
                log.info("Documents['{}'] {} deleted.", task.getType(), objects);
                // Delete repository need to delete it's related issues and codes
                if(repos.size() > 0) {
                    // Delete issues of this repository
                    try (IndexWriter issues = StorageFactory.getIndexWriter(Constants.TYPE_ISSUE)) {
                        Query[] i_querys = repos.stream().map(id -> LongPoint.newExactQuery(Constants.FIELD_REPO_ID, id)).toArray(Query[]::new);
                        issues.deleteDocuments(i_querys);
                        log.info("Issues of repositories : {} deleted.", repos);
                    }
                    // Delete code repositories
                    try (IndexWriter codes = StorageFactory.getIndexWriter(Constants.TYPE_CODE)) {
                        Query[] i_querys = repos.stream().map(id -> LongPoint.newExactQuery(Constants.FIELD_REPO_ID, id)).toArray(Query[]::new);
                        codes.deleteDocuments(i_querys);
                        log.info("Codes of repositories : {} deleted.", repos);
                    }
                }
        }
        return task.getObjects().size();
    }

    /**
     * 添加文档
     * @param type
     * @param docs
     * @return
     * @throws IOException
     */
    public static long add(String type, List<Document> docs) throws IOException {
        if(docs != null && docs.size() > 0)
            try (
                IndexWriter writer = StorageFactory.getIndexWriter(type);
                TaxonomyWriter taxonomyWriter = StorageFactory.getTaxonomyWriter(type);
            ) {
                return update(docs, writer, taxonomyWriter);
            }
        return 0;
    }

    /**
     * 更新文档
     * @param docs
     * @param i_writer
     * @param t_writer
     * @return
     * @throws IOException
     */
    public static long update(List<Document> docs, IndexWriter i_writer, TaxonomyWriter t_writer) throws IOException {
        for(Document doc : docs) {
            try {
                i_writer.updateDocument(new Term(FIELD_ID, doc.get(FIELD_ID)), buildFacetDocument(t_writer, doc));
            } catch ( IllegalArgumentException e) {
                //FIXME 暂时先用这种方法来解决 jcseg 异常的问题
                doc.removeField(Constants.FIELD_DESC);
                i_writer.updateDocument(new Term(FIELD_ID, doc.get(FIELD_ID)), buildFacetDocument(t_writer, doc));
            }
        }
        return docs.size();
    }

    /**
     * 将普通 document 转成 facet 文档
     * @param taxonomyWriter
     * @param doc
     * @return
     */
    private static Document buildFacetDocument(TaxonomyWriter taxonomyWriter, Document doc) {
        try {
            return facetsConfig.build(taxonomyWriter, doc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
