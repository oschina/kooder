package com.gitee.search.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gitee.search.queue.QueueTask;
import com.gitee.search.storage.StorageFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * 索引管理器
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexManager {

    public final static int MAX_RESULT_COUNT = 1000;

    private final static Logger log = LoggerFactory.getLogger(IndexManager.class);

    /**
     * 执行搜索
     * @param type
     * @param query
     * @param sort
     * @param page
     * @param pageSize
     * @return
     * @throws IOException
     */
    public static String search(String type, Query query, Sort sort, int page, int pageSize) throws IOException {
        try (IndexReader reader = StorageFactory.getStorage().getReader(type)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode result = mapper.createObjectNode();
            IndexSearcher searcher = new IndexSearcher(reader);
            TopFieldDocs docs = searcher.search(query, MAX_RESULT_COUNT, sort);
            result.put("type", type);
            result.put("totalHits", docs.totalHits.value);
            result.put("pageIndex", page);
            result.put("pageSize", pageSize);
            ArrayNode objects = result.putArray("objects");
            for(int i = (page-1) * pageSize; i < page * pageSize && i < docs.totalHits.value ; i++) {
                Document doc = searcher.doc(docs.scoreDocs[i].doc);
                objects.addPOJO(ObjectMapping.doc2json(type, doc));
            }
            return result.toString();
        }
    }

    /**
     * 写入索引库
     * @exception
     */
    public static void write(QueueTask task) throws IOException {
        List<Document> docs = ObjectMapping.task2doc(task);
        if(docs.size() > 0) {
            try (IndexWriter writer = StorageFactory.getStorage().getWriter(task.getType())) {
                switch (task.getAction()) {
                    case QueueTask.ACTION_ADD:
                        writer.addDocuments(docs);
                        break;
                    case QueueTask.ACTION_UPDATE:
                        for (Document doc : docs) {
                            writer.updateDocument(new Term(ObjectMapping.FIELD_ID, doc.get(ObjectMapping.FIELD_ID)), doc);
                        }
                        break;
                    case QueueTask.ACTION_DELETE:
                        Term[] terms = docs.stream().map(d -> new Term(ObjectMapping.FIELD_ID, d.get(ObjectMapping.FIELD_ID))).toArray(Term[]::new);
                        writer.deleteDocuments(terms);
                }
            }
            log.info(docs.size() + " documents writed to index.");
        }
    }

}
