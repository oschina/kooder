package com.gitee.kooder.query;

import com.gitee.kooder.core.AnalyzerFactory;
import com.gitee.kooder.core.Constants;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

/**
 * Issue 搜索
 * @author Winter Lau<javayou@gmail.com>
 */
public class IssueQuery extends QueryBase {
    /**
     * 索引类型
     *
     * @return
     */
    @Override
    public String type() {
        return Constants.TYPE_ISSUE;
    }

    /**
     * Last issue based on created_at field
     * @return
     */
    @Override
    protected Sort getLastestObjectSort() {
        return new Sort(new SortField(Constants.FIELD_CREATED_AT, SortField.Type.LONG, true));
    }

    /**
     * 构建查询对象
     *
     * @return
     */
    @Override
    protected Query buildUserQuery() {
        if(parseSearchKey) {
            QueryParser parser = new QueryParser("issue", AnalyzerFactory.getInstance(false));
            try {
                return parser.parse(searchKey);
            } catch (ParseException e) {
                throw new QueryException("Failed to parse \""+searchKey+"\"", e);
            }
        }
        String q = QueryParser.escape(searchKey);
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        //filter
        //search
        BooleanQuery.Builder qbuilder = new BooleanQuery.Builder();
        qbuilder.add(makeBoostQuery(Constants.FIELD_IDENT, q, 100.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery(Constants.FIELD_TITLE, q, 10.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery(Constants.FIELD_TAGS, q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery(Constants.FIELD_DESC, q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.setMinimumNumberShouldMatch(1);

        builder.add(qbuilder.build(), BooleanClause.Occur.MUST);

        return builder.build();
    }

    /**
     * 对搜索加权
     * @param field
     * @param q
     * @param boost
     * @return
     */
    @Override
    protected BoostQuery makeBoostQuery(String field, String q, float boost) {
        if("ident".equals(field))
            return new BoostQuery(new TermQuery(new Term("ident", q)), boost);
        return super.makeBoostQuery(field, q, boost);
    }

    /**
     * 构建排序对象
     *
     * @return
     */
    @Override
    protected Sort buildSort() {
        if("create".equals(sort))
            return new Sort(new SortedNumericSortField("created_at", SortField.Type.LONG, true));
        if("update".equals(sort))
            return new Sort(new SortedNumericSortField("updated_at", SortField.Type.LONG, true));
        return Sort.RELEVANCE;
    }

}
