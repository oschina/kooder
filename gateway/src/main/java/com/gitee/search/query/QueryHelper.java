package com.gitee.search.query;

import com.gitee.search.action.Constants;
import com.gitee.search.core.AnalyzerFactory;
import com.gitee.search.core.SearchHelper;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * 查询工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueryHelper {

    private final static HashMap<String, Method> scoreMethods = new HashMap();
    private static Expression repoScoreExpr;

    static {
        try {
            scoreMethods.putAll(JavascriptCompiler.DEFAULT_FUNCTIONS);
            scoreMethods.put("repo_sort", ScoreHelper.class.getDeclaredMethod("repoSort", double.class, double.class, double.class, double.class));
            repoScoreExpr = JavascriptCompiler.compile("repo_sort($score,$recomm,$stars,$gindex)", scoreMethods, ScoreHelper.class.getClassLoader());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建仓库搜索的排序方法
     * @param sortMethod
     * @return
     */
    public static Sort buildRepoSort(String sortMethod) {
        if("stars".equals(sortMethod))
            return new Sort(new SortedNumericSortField("count.star", SortField.Type.LONG, true));
        if("forks".equals(sortMethod))
            return new Sort(new SortedNumericSortField("count.fork", SortField.Type.LONG, true));
        if("update".equals(sortMethod))
            return new Sort(new SortedNumericSortField("last_push_at", SortField.Type.LONG, true));
        return Sort.RELEVANCE;
    }

    /**
     * 仓库搜索的条件
     * @param q
     * @param recomm
     * @return
     */
    public static Query buildRepoQuery(String q, int recomm) {
        q = QueryParser.escape(q);

        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        //只搜索公开仓库
        builder.add(NumericDocValuesField.newSlowExactQuery("type", Constants.REPO_TYPE_PUBLIC), BooleanClause.Occur.FILTER);
        //不搜索fork仓库
        builder.add(NumericDocValuesField.newSlowExactQuery("fork", Constants.REPO_FORK_NO), BooleanClause.Occur.FILTER);
        //不搜索被屏蔽的仓库
        builder.add(NumericDocValuesField.newSlowExactQuery("block", Constants.REPO_BLOCK_NO), BooleanClause.Occur.FILTER);

        if(recomm >= Constants.RECOMM_GVP)//搜索范围
            builder.add(NumericDocValuesField.newSlowExactQuery("recomm", Constants.RECOMM_GVP), BooleanClause.Occur.FILTER);
        else if(recomm > Constants.RECOMM_NONE)
            builder.add(NumericDocValuesField.newSlowRangeQuery("recomm", Constants.RECOMM, Constants.RECOMM_GVP), BooleanClause.Occur.FILTER);

        //BoostQuery
        //如果调整  boost 就要调整 ScoreHelper 中的 SCORE_FACTOR
        BooleanQuery.Builder qbuilder = new BooleanQuery.Builder();
        qbuilder.add(makeBoostQuery("catalogs", q, 10.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("name", q, ScoreHelper.SCORE_FACTOR), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("description", q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("detail", q, 0.5f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("tags", q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("lang", q, 2.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("owner.name", q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("namespace.path", q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("namespace.name", q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.setMinimumNumberShouldMatch(1);

        builder.add(qbuilder.build(), BooleanClause.Occur.MUST);

        //return builder.build();

        //custom query score
        SimpleBindings bindings = new SimpleBindings();
        bindings.add("$score", DoubleValuesSource.SCORES);
        bindings.add("$recomm", DoubleValuesSource.fromIntField("recomm"));
        bindings.add("$stars", DoubleValuesSource.fromIntField("count.star"));
        bindings.add("$gindex", DoubleValuesSource.fromIntField("count.gindex"));

        return new FunctionScoreQuery(builder.build(), repoScoreExpr.getDoubleValuesSource(bindings));
    }

    /**
     * 对搜索加权
     * @param field
     * @param q
     * @param boost
     * @return
     */
    private static BoostQuery makeBoostQuery(String field, String q, float boost) {
        try {
            QueryParser parser = new QueryParser(field, AnalyzerFactory.getInstance(false));
            Query query = parser.parse(q);
            return new BoostQuery(query, boost);
        } catch(ParseException e) {
            e.printStackTrace();
            return null;
        }
        /*
        List<String> keys = SearchHelper.splitKeywords(q);
        BooleanQuery.Builder qbuilder = new BooleanQuery.Builder();
        for(int i=0;i<keys.size();i++) {
            String key = keys.get(i);
            if("name".equals(field)) //TODO 提取该定制逻辑以支持不同的 type
                qbuilder.add(new WildcardQuery(new Term(field, "*"+key+"*")), BooleanClause.Occur.SHOULD);
            else
                qbuilder.add(new TermQuery(new Term(field, key)), BooleanClause.Occur.SHOULD);
        }
        return new BoostQuery(qbuilder.build(), boost);
         */
    }

}
