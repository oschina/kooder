package com.gitee.search.query;

import com.gitee.search.core.AnalyzerFactory;
import com.gitee.search.core.Constants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 源码搜索
 * @author Winter Lau (javayou@gmail.com)
 */
public class CodeQuery extends QueryBase {

    /**
     * 索引类型
     *
     * @return
     */
    @Override
    public String type() {
        return Constants.TYPE_CODE;
    }

    /**
     * 构建查询对象
     * @return
     */
    @Override
    protected Query buildUserQuery() {
        String q = QueryParser.escape(searchKey);
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(makeBoostQuery(Constants.FIELD_FILE_NAME,  q, 10.0f), BooleanClause.Occur.SHOULD);
        builder.add(makeBoostQuery(Constants.FIELD_SOURCE,     q, 1.0f), BooleanClause.Occur.SHOULD);
        builder.setMinimumNumberShouldMatch(1);
        return builder.build();
    }

    /**
     * 构建排序对象
     *
     * @return
     */
    @Override
    protected Sort buildSort() {
        if("update".equals(sort))
            return new Sort(new SortedNumericSortField(Constants.FIELD_LAST_INDEX, SortField.Type.LONG, true));
        return Sort.RELEVANCE;
    }

    /**
     * list facet names
     * @return
     */
    @Override
    protected List<String> listFacetFields() {
        return Arrays.asList(Constants.FIELD_LANGUAGE, Constants.FIELD_REPO_NAME, Constants.FIELD_CODE_OWNER);
    }

    /**
     * 自定义分词器
     * @param forIndex
     * @return
     */
    @Override
    protected Analyzer getAnalyzer(boolean forIndex) {
        return AnalyzerFactory.getCodeAnalyzer();
    }

}
