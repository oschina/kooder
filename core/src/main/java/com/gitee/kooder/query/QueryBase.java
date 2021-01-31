package com.gitee.kooder.query;

import com.gitee.kooder.core.AnalyzerFactory;
import com.gitee.kooder.core.Constants;
import com.gitee.kooder.models.QueryResult;
import com.gitee.kooder.models.Searchable;
import com.gitee.kooder.storage.StorageFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索基类
 * @author Winter Lau<javayou@gmail.com>
 */
public abstract class QueryBase implements IQuery {

    protected final static Logger log = LoggerFactory.getLogger(QueryBase.class);

    public final static FacetsConfig facetsConfig = new FacetsConfig();

    private int enterpriseId = 0;                           // Search in Enterprise
    private List<Integer> repositories = new ArrayList<>(); // Search in repositories
    protected String searchKey;                             // Search Keyword
    protected boolean parseSearchKey = false;               // Escape Search key ?
    protected String sort;                                  // Sort field name
    protected int page = 1;                                 // Search result page index
    protected int pageSize = 20;                            // Search result page size
    protected Map<String, String[]> facets = new HashMap(); // Search with facets
    protected List<Query> filters = new ArrayList<>();      // Search filters

    /**
     * Get max object indexed .
     * @return
     */
    public Searchable getLastestObject() {
        try (IndexReader reader = StorageFactory.getIndexReader(this.type())) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query thisQuery = new MatchAllDocsQuery();
            TopFieldDocs docs = searcher.search(thisQuery, 1, this.getLastestObjectSort());
            if (docs.totalHits.value > 0) {
                QueryResult result = new QueryResult(this.type());
                Document doc = searcher.doc(docs.scoreDocs[0].doc);
                result.addDocument(doc, docs.scoreDocs[0]);
                return result.getObjects().get(0);
            }
        }catch(IndexNotFoundException e) {
        }catch(Exception e) {
            log.error("Failed to get lastest object from index[" + type() + "]", e);
        }
        return null;
    }

    protected Sort getLastestObjectSort() {
        return new Sort(new SortField(Constants.FIELD_ID, SortField.Type.LONG, true));
    }

    /**
     * execute search
     * @return
     * @throws IOException
     */
    public final QueryResult execute() throws IOException {

        if(StringUtils.isBlank(searchKey))
            throw new IllegalArgumentException("SearchKey must not be empty");

        Query query = buildQuery();
        Sort sort = buildSort();

        QueryResult result = new QueryResult(this.type());

        boolean needFacetQuery = (facets != null) && (facets.size() > 0);
        Query thisQuery = query;

        if ( needFacetQuery ) { // 避免二次执行 FunctionScoreQuery ，比较耗时
            if(query instanceof FunctionScoreQuery)
                thisQuery = ((FunctionScoreQuery)query).getWrappedQuery();
        }

        long ct = System.currentTimeMillis();

        try (IndexReader reader = StorageFactory.getIndexReader(this.type())) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TaxonomyReader taxoReader = StorageFactory.getTaxonomyReader(this.type());
            // Aggregates the facet values
            FacetsCollector fc = new FacetsCollector(false);
            //如果 n 传 0 ，则 search 方法 100% 报 ClassCastException 异常，这是 Lucene 的 bug
            TopDocs docs = FacetsCollector.search(searcher, thisQuery, page * pageSize, sort,true, fc); //fetch all facets

            if( needFacetQuery ) {
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                builder.add(query, BooleanClause.Occur.MUST);
                DrillDownQuery ddq = new DrillDownQuery(facetsConfig);
                facets.forEach((k, values) ->
                        Arrays.stream(values).forEach(v -> ddq.add(k, v))
                );
                builder.add(ddq, BooleanClause.Occur.MUST);
                thisQuery = builder.build();
                //TopDocs docs = FacetsCollector.search(searcher, thisQuery, page * pageSize, sort,true, new FacetsCollector(false));
                docs = searcher.search(thisQuery, page * pageSize, sort,true);
            }

            int totalPages = (int) Math.ceil(docs.totalHits.value / (double) pageSize);

            //read objects
            result.setTotalHits((int)docs.totalHits.value);
            result.setTotalPages(totalPages);
            result.setPageIndex(page);
            result.setPageSize(pageSize);
            result.setTimeUsed(System.currentTimeMillis() - ct);
            result.setQuery(thisQuery.toString());

            for(int i = (page-1) * pageSize; i < page * pageSize && i < docs.totalHits.value ; i++) {
                Document doc = searcher.doc(docs.scoreDocs[i].doc);
                result.addDocument(doc, docs.scoreDocs[i]);
            }

            //read facets
            List<String> facetFields = this.listFacetFields();
            if(facetFields.size() > 0) {
                Facets facets = new FastTaxonomyFacetCounts(taxoReader, facetsConfig, fc);

                for (String facetField : facetFields) {
                    FacetResult facetResult = facets.getTopChildren(Integer.MAX_VALUE, facetField);
                    if (facetResult != null) {
                        for (LabelAndValue lav : facetResult.labelValues) {
                            result.addFacet(facetField, lav);
                        }
                    }
                }
            }

            return result;
        }
    }

    /**
     * Build query according to user key
     * @return
     */
    protected abstract Query buildUserQuery();

    /**
     * list facet names
     * @return
     */
    protected List<String> listFacetFields() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Build complete query with user query and filter query
     * @return
     */
    protected Query buildQuery() {
        Query query = this.buildUserQuery();

        if(filters.size() == 0 && enterpriseId <= 0 && repositories.size() == 0)
            return query;

        BooleanQuery.Builder fBuilder = new BooleanQuery.Builder();
        if(enterpriseId > 0)
            fBuilder.add(new TermQuery(new Term(Constants.FIELD_ENTERPRISE_ID, String.valueOf(this.getEnterpriseId()))), BooleanClause.Occur.FILTER);

        if(repositories.size() > 0)
            fBuilder.add(IntPoint.newSetQuery(Constants.FIELD_REPO_ID, repositories), BooleanClause.Occur.FILTER);

        for(Query filter : filters)
            fBuilder.add(filter, BooleanClause.Occur.FILTER);

        fBuilder.add(query, BooleanClause.Occur.MUST);

        return fBuilder.build();
    }

    /**
     * build sort according to user selection
     * @return
     */
    protected abstract Sort buildSort();

    /**
     * set search key
     * @param key
     */
    @Override
    public IQuery setSearchKey(String key) {
        return setSearchKey(key, parseSearchKey);
    }

    public IQuery setSearchKey(String key, boolean parseSearchKey) {
        this.searchKey = key;
        this.parseSearchKey = parseSearchKey;
        return this;
    }

    public IQuery setParseSearchKey(boolean parseSearchKey) {
        this.parseSearchKey = parseSearchKey;
        return this;
    }

    /**
     * 排序方法
     * @param sort
     * @return
     */
    @Override
    public IQuery setSort(String sort) {
        this.sort = sort;
        return this;
    }

    /**
     * 页码
     * @param page
     * @return
     */
    @Override
    public IQuery setPage(int page) {
        this.page = page;
        return this;
    }

    /**
     * 页大小
     * @param pageSize
     * @return
     */
    @Override
    public IQuery setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getEnterpriseId() {
        return enterpriseId;
    }

    public QueryBase setEnterpriseId(int enterpriseId) {
        this.enterpriseId = enterpriseId;
        return this;
    }

    public List<Integer> getRepositories() {
        return repositories;
    }

    public QueryBase addRepositories(List<Integer> repositories) {
        this.repositories.addAll(repositories);
        return this;
    }

    /**
     * 添加扩展属性
     * @param name
     * @param value
     * @return
     */
    public IQuery addFacets(String name, String value) {
        if(StringUtils.isBlank(value))
            return this;
        String[] values = facets.get(name);
        if(values == null)
            facets.put(name, new String[]{value});
        else {
            String[] newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
            facets.put(name, newValues);
        }
        return this;
    }

    /**
     * 获取扩展属性
     *
     * @return
     */
    @Override
    public Map<String, String[]> getFacets() {
        return facets;
    }

    /**
     * 添加过滤条件
     *
     * @param filterQueryString
     * @return
     */
    @Override
    public IQuery addFilter(String filterQueryString) {
        try {
            filters.add(new QueryParser(null, AnalyzerFactory.getSimpleAnalyzer()).parse(filterQueryString));
        } catch (ParseException e) {
            throw new QueryException("Failed to add filter: " + filterQueryString, e);
        }
        return this;
    }

    /**
     * 获取所有过滤条件
     *
     * @return
     */
    @Override
    public List<String> getFilters() {
        return filters.stream().map(q -> q.toString()).collect(Collectors.toList());
    }

    /**
     * Build a query for one document field with boost
     * @param field
     * @param q
     * @param boost
     * @return
     */
    protected BoostQuery makeBoostQuery(String field, String q, float boost) {
        QueryParser parser = new QueryParser(field, getAnalyzer(false));
        parser.setDefaultOperator(QueryParser.Operator.AND);
        try {
            return new BoostQuery(parser.parse(q), boost);
        } catch (ParseException e) {
            throw new QueryException(String.format("Failed to build field query(%s,%s,%.2f)", field, q, boost), e);
        }
    }

    /**
     * 自定义分词器
     * @param forIndex
     * @return
     */
    protected Analyzer getAnalyzer(boolean forIndex) {
        return AnalyzerFactory.getInstance(false);
    }

}
