package com.gitee.search.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gitee.search.queue.QueueTask;
import com.gitee.search.storage.StorageFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gitee.search.index.ObjectMapping.FIELD_ID;

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
        try (IndexReader reader = StorageFactory.getReader(type)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode result = mapper.createObjectNode();
            IndexSearcher searcher = new IndexSearcher(reader);
            long ct = System.currentTimeMillis();
            TopFieldDocs docs = searcher.search(query, MAX_RESULT_COUNT, sort, true);
            log.info("{} documents find, search time: {}ms", docs.totalHits.value, (System.currentTimeMillis() - ct));
            result.put("type", type);
            result.put("totalHits", docs.totalHits.value);
            result.put("pageIndex", page);
            result.put("pageSize", pageSize);
            ArrayNode objects = result.putArray("objects");
            for(int i = (page-1) * pageSize; i < page * pageSize && i < docs.totalHits.value ; i++) {
                Document doc = searcher.doc(docs.scoreDocs[i].doc);
                objects.addPOJO(ObjectMapping.doc2json(type, doc));
                log.info("id:{},score:{},repo:{}/{},name:{},type:{},stars:{},recomm:{},fork:{}",
                        doc.get("id"),
                        docs.scoreDocs[i].score,
                        doc.get("namespace.path"),
                        doc.get("path"),
                        doc.get("name"),
                        doc.get("type"),
                        doc.get("count.star"),
                        doc.get("recomm"),
                        doc.get("fork")
                );
            }
            return result.toPrettyString();//.toString();
        }
    }

    /**
     * 写入索引库
     * @return
     * @exception
     */
    public static int write(QueueTask task) throws IOException {
        List<Document> docs = ObjectMapping.task2doc(task);
        if(docs != null && docs.size() > 0) {
            try (IndexWriter writer = StorageFactory.getWriter(task.getType())) {
                switch (task.getAction()) {
                    case QueueTask.ACTION_ADD:
                        long ct = System.currentTimeMillis();
                        writer.addDocuments(docs);
                        log.info("{} documents writed to index. {}ms", docs.size(), (System.currentTimeMillis()-ct));
                        break;
                    case QueueTask.ACTION_UPDATE:
                        //update documents
                        ct = System.currentTimeMillis();
                        Query[] queries = docs.stream().map(d -> NumericDocValuesField.newSlowExactQuery(FIELD_ID, d.getField(FIELD_ID).numericValue().longValue())).toArray(Query[]::new);
                        writer.deleteDocuments(queries);
                        //re-add documents
                        writer.addDocuments(docs);
                        log.info("{} documents updated to index, {}ms", docs.size(), (System.currentTimeMillis()-ct));
                        break;
                    case QueueTask.ACTION_DELETE:
                        ct = System.currentTimeMillis();
                        queries = docs.stream().map(d -> NumericDocValuesField.newSlowExactQuery(FIELD_ID, d.getField(FIELD_ID).numericValue().longValue())).toArray(Query[]::new);
                        writer.deleteDocuments(queries);
                        log.info("{} documents deleted from index, {}ms", docs.size(), (System.currentTimeMillis()-ct));
                }
            }

        }
        return (docs!=null)?docs.size():0;
    }

    public static void main(String[] args) throws IOException {
        QueueTask task = new QueueTask();
        task.setType(QueueTask.TYPE_REPOSITORY);
        //test delete
        //task.setAction(QueueTask.ACTION_DELETE);
        //task.setBody("{\"objects\":[{\"id\":1016657},{\"id\":1495600}]}");
        //test update
        task.setAction(QueueTask.ACTION_UPDATE);
        task.setBody("{\"objects\":[{\"id\":91902,\"recomm\":333}]}");
        write(task);
    }

}
