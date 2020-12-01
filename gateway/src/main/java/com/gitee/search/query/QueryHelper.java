package com.gitee.search.query;

import com.gitee.search.action.SearchObject;
import com.gitee.search.core.AnalyzerFactory;
import com.gitee.search.core.SearchHelper;
import org.apache.commons.lang.StringUtils;
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

    public static void main(String[] args ) {
    }

    /**
     * 仓库搜索的条件
     * @param q
     * @param lang
     * @param recomm
     * @return
     */
    public static Query buildRepoQuery(String q, String lang, int recomm) throws ParseException {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        //只搜索公开仓库
        builder.add(NumericDocValuesField.newSlowExactQuery("type", 2), BooleanClause.Occur.MUST);
        //不搜索fork仓库
        builder.add(NumericDocValuesField.newSlowExactQuery("fork", 0), BooleanClause.Occur.MUST);
        if(StringUtils.isNotBlank(lang))//编程语言
            builder.add(new TermQuery(new Term("lang", lang)), BooleanClause.Occur.MUST);
        if(recomm >= SearchObject.RECOMM_GVP)//搜索范围
            builder.add(NumericDocValuesField.newSlowExactQuery("recomm", SearchObject.RECOMM_GVP), BooleanClause.Occur.MUST);
        else if(recomm > SearchObject.RECOMM_NONE)
            builder.add(NumericDocValuesField.newSlowRangeQuery("recomm", SearchObject.RECOMM, SearchObject.RECOMM_GVP), BooleanClause.Occur.MUST);

        //BoostQuery
        BooleanQuery.Builder qbuilder = new BooleanQuery.Builder();
        qbuilder.add(makeBoostQuery("name", q, 20.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("description", q, 10.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("detail", q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("tags", q, 10.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("catalogs", q, 10.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("owner.name", q, 2.0f), BooleanClause.Occur.SHOULD);
        qbuilder.setMinimumNumberShouldMatch(1);

        builder.add(qbuilder.build(), BooleanClause.Occur.MUST);

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
     * @throws ParseException
     */
    private static BoostQuery makeBoostQuery(String field, String q, float boost) throws ParseException {
        List<String> keys = SearchHelper.splitKeywords(q);
        BooleanQuery.Builder qbuilder = new BooleanQuery.Builder();
        for(int i=0;i<keys.size();i++) {
            String key = keys.get(i);
            qbuilder.add(new TermQuery(new Term(field, key)), (i==0)?BooleanClause.Occur.MUST:BooleanClause.Occur.SHOULD);
        }
        return new BoostQuery(qbuilder.build(), boost);
    }


}
