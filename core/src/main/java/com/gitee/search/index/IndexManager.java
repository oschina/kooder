package com.gitee.search.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gitee.search.queue.QueueTask;
import com.gitee.search.storage.StorageFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gitee.search.index.ObjectMapping.FIELD_ID;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 索引管理器
 * @author Winter Lau<javayou@gmail.com>
 */
public class IndexManager {

    private final static Logger log = LoggerFactory.getLogger(IndexManager.class);

    public final static int SEARCH_THREAD_COUNT = 10; //并发搜索线程数

    public final static String KEY_SCORE = "_score_"; //在 json 中存放文档的score值
    public final static String KEY_DOC_ID = "_id_"; //在 json 中存放文档的 id

    private final static int maxNumberOfCachedQueries = 256;
    private final static long maxRamBytesUsed = 50 * 1024L * 1024L; // 50MB
    // these cache and policy instances can be shared across several queries and readers
    // it is fine to eg. store them into static variables
    private final static QueryCache queryCache = new LRUQueryCache(maxNumberOfCachedQueries, maxRamBytesUsed);
    private final static QueryCachingPolicy defaultCachingPolicy = new UsageTrackingQueryCachingPolicy();

    private final static FacetsConfig facetsConfig = new FacetsConfig();

    /**
     * 执行搜索
     * @param type   search for what ?
     * @param query  search condictions
     * @param facets search facets
     * @param sort   sort type
     * @param page
     * @param pageSize
     * @return
     * @throws IOException
     */
    public static String search(String type, Query query, Map<String,String[]> facets, Sort sort, int page, int pageSize) throws IOException {

        long ct = System.currentTimeMillis();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        boolean needFacetQuery = (facets != null) && (facets.size() > 0);
        Query thisQuery = query;

        if ( needFacetQuery ) { // 避免二次执行 FunctionScoreQuery ，比较耗时
            if(query instanceof FunctionScoreQuery)
                thisQuery = ((FunctionScoreQuery)query).getWrappedQuery();
        }

        try (IndexReader reader = StorageFactory.getIndexReader(type)) {
            ExecutorService pool = Executors.newFixedThreadPool(SEARCH_THREAD_COUNT);//FIXED 似乎不起作用
            IndexSearcher searcher = new IndexSearcher(reader, pool);
            searcher.setQueryCache(queryCache);
            searcher.setQueryCachingPolicy(defaultCachingPolicy);

            TaxonomyReader taxoReader = StorageFactory.getTaxonomyReader(type);
            // Aggregates the facet values
            FacetsCollector fc = new FacetsCollector(false);
            //如果 n 传 0 ，则 search 方法 100% 报 ClassCastException 异常，这是 Lucene 的 bug
            TopDocs docs = FacetsCollector.search(searcher, thisQuery, page * pageSize, sort,true, fc); //fetch all facets

            if( needFacetQuery ) {
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                builder.add(query, BooleanClause.Occur.MUST);
                DrillDownQuery ddq = new DrillDownQuery(facetsConfig);
                facets.forEach((k, values) ->
                    Arrays.stream(values).forEach( v -> ddq.add(k,v))
                );
                builder.add(ddq, BooleanClause.Occur.MUST);
                thisQuery = builder.build();
                //TopDocs docs = FacetsCollector.search(searcher, thisQuery, page * pageSize, sort,true, new FacetsCollector(false));
                docs = searcher.search(thisQuery, page * pageSize, sort,true);
            }

            //log.info("{} documents find, search time: {}ms", docs.totalHits.value, (System.currentTimeMillis() - ct));
            result.put("type", type);
            result.put("totalHits", docs.totalHits.value);
            result.put("totalPages", (docs.totalHits.value + pageSize - 1) / pageSize);
            result.put("pageIndex", page);
            result.put("pageSize", pageSize);
            result.put("timeUsed", (System.currentTimeMillis() - ct));
            result.put("query", thisQuery.toString());

            ArrayNode objects = result.putArray("objects");
            for(int i = (page-1) * pageSize; i < page * pageSize && i < docs.totalHits.value ; i++) {
                Document doc = searcher.doc(docs.scoreDocs[i].doc);
                Map<String, Object> pojo = ObjectMapping.doc2json(type, doc);
                pojo.put(KEY_DOC_ID, docs.scoreDocs[i].doc);
                pojo.put(KEY_SCORE, docs.scoreDocs[i].score);
                objects.addPOJO(pojo);
            }

            readTaxonomy(type, result, taxoReader, fc);

            return result.toString();
        }
    }

    /**
     * search after document
     * @param type
     * @param after
     * @param query
     * @param numHits
     * @return
     * @throws IOException
     * @deprecated 暂时不用此方法
     */
    @Deprecated
    public static String searchAfter(String type, ScoreDoc after, Query query, int numHits) throws IOException {

        long ct = System.currentTimeMillis();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        try (IndexReader reader = StorageFactory.getIndexReader(type)) {
            ExecutorService pool = Executors.newFixedThreadPool(SEARCH_THREAD_COUNT);//FIXED 似乎不起作用
            IndexSearcher searcher = new IndexSearcher(reader, pool);
            searcher.setQueryCache(queryCache);
            searcher.setQueryCachingPolicy(defaultCachingPolicy);

            TaxonomyReader taxoReader = StorageFactory.getTaxonomyReader(type);
            // Aggregates the facet values
            FacetsCollector fc = new FacetsCollector(false);
            TopDocs docs = FacetsCollector.searchAfter(searcher, after, query, numHits, fc);

            //log.info("{} documents find, search time: {}ms", docs.totalHits.value, (System.currentTimeMillis() - ct));
            result.put("type", type);
            result.put("totalHits", docs.totalHits.value);
            result.put("totalPages", (docs.totalHits.value + numHits - 1) / numHits);
            result.put("timeUsed", (System.currentTimeMillis() - ct));
            if(after != null) {
                result.put("afterDoc", after.doc);
                result.put("afterScore", after.score);
            }
            result.put("query", query.toString());

            ArrayNode objects = result.putArray("objects");
            for(ScoreDoc sdoc : docs.scoreDocs) {
                Document doc = searcher.doc(sdoc.doc);
                Map<String, Object> pojo = ObjectMapping.doc2json(type, doc);
                pojo.put(KEY_DOC_ID, sdoc.doc);
                pojo.put(KEY_SCORE, sdoc.score);
                objects.addPOJO(pojo);
            }

            readTaxonomy(type, result, taxoReader, fc);

            return result.toString();
        }
    }

    /**
     * 读取分类信息
     * @param type
     * @param json
     * @param taxoReader
     * @param  fc
     */
    private static void readTaxonomy(String type, ObjectNode json, TaxonomyReader taxoReader, FacetsCollector fc) throws IOException {
        List<String> facetFields = IndexMapping.get(type).listFacetFields();
        if(facetFields.size() == 0)
            return ;

        ObjectNode taxs = json.putObject("facets");
        Facets facets = new FastTaxonomyFacetCounts(taxoReader, facetsConfig, fc);
        for(String facetField : facetFields) {
            FacetResult facetResult = facets.getTopChildren(Integer.MAX_VALUE, facetField);
            if(facetResult != null) {
                ArrayNode taxs2 = taxs.putArray(facetField);
                for (LabelAndValue lav : facetResult.labelValues) {
                    taxs2.addPOJO(lav);
                }
            }
        }
    }

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
        List<Document> docs = ObjectMapping.task2doc(task);
        if(docs == null || docs.size() == 0)
            return 0;
        switch (task.getAction()) {
            case QueueTask.ACTION_ADD:
                update(docs, i_writer, t_writer);
                break;
            case QueueTask.ACTION_UPDATE:
                update(docs, i_writer, t_writer);
                break;
            case QueueTask.ACTION_DELETE:
                deleteByDoc(docs, i_writer);
        }
        return docs.size();
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
        //update documents
        Query[] queries = docs.stream().map(d -> NumericDocValuesField.newSlowExactQuery(FIELD_ID, d.getField(FIELD_ID).numericValue().longValue())).toArray(Query[]::new);
        i_writer.deleteDocuments(queries);
        //re-add documents
        return i_writer.addDocuments(docs.stream().map(d -> buildFacetDocument(t_writer, d)).collect(Collectors.toList()));
    }

    /**
     * 删除文档
     * @param docs
     * @param i_writer
     * @return
     * @throws IOException
     */
    public static long deleteById(List<Long> docs, IndexWriter i_writer) throws IOException {
        Query[] queries = docs.stream().map(id -> NumericDocValuesField.newSlowExactQuery(FIELD_ID, id)).toArray(Query[]::new);
        return i_writer.deleteDocuments(queries);
    }

    /**
     * 删除文档
     * @param docs
     * @param i_writer
     * @return
     * @throws IOException
     */
    public static long deleteByDoc(List<Document> docs, IndexWriter i_writer) throws IOException {
        Query[] queries = docs.stream().map(doc -> NumericDocValuesField.newSlowExactQuery(FIELD_ID, doc.getField(FIELD_ID).numericValue().longValue())).toArray(Query[]::new);
        return i_writer.deleteDocuments(queries);
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
