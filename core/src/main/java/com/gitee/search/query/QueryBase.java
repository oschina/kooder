package com.gitee.search.query;

import com.gitee.search.core.AnalyzerFactory;
import com.gitee.search.index.IndexManager;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 搜索基类
 * @author Winter Lau<javayou@gmail.com>
 */
public abstract class QueryBase implements IQuery {

    protected String searchKey;
    protected String sort;
    protected int page = 1;
    protected int pageSize = 20;
    protected Map<String, String> facets = new HashMap();

    /**
     * 搜索
     * @return
     * @throws IOException
     */
    @Override
    public String search() throws IOException {
        Query query = buildQuery();
        Sort nSort = buildSort();
        return IndexManager.search(type(), query, facets, nSort, page, pageSize);
    }

    /**
     * 构建查询对象
     * @return
     */
    protected abstract Query buildQuery() ;

    /**
     * 构建排序对象
     * @return
     */
    protected abstract Sort buildSort();

    /**
     * 搜索关键字
     * @param key
     */
    @Override
    public IQuery setSearchKey(String key) {
        this.searchKey = key;
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

    /**
     * 扩展属性
     * @param key
     * @param value
     * @return
     */
    @Override
    public IQuery setFacets(String key, String value) {
        if(StringUtils.isBlank(value))
            this.facets.remove(key);
        else
            this.facets.put(key, value);
        return this;
    }

    /**
     * Build a query for one document field with boost
     * @param field
     * @param q
     * @param boost
     * @return
     */
    protected BoostQuery makeBoostQuery(String field, String q, float boost) {
        QueryParser parser = new QueryParser(field, AnalyzerFactory.getInstance(false));
        parser.setDefaultOperator(QueryParser.Operator.AND);
        try {
            return new BoostQuery(parser.parse(q), boost);
        } catch (ParseException e) {
            throw new QueryException(String.format("Failed to build field query(%s,%s,%.2f)", field, q, boost), e);
        }
    }

}
