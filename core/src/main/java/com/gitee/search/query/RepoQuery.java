package com.gitee.search.query;

import com.gitee.search.core.AnalyzerFactory;
import com.gitee.search.core.Constants;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 仓库搜索
 * @author Winter Lau<javayou@gmail.com>
 */
public class RepoQuery extends QueryBase {

    public final static int SCORE_FACTOR = 6;

    private HashMap<String, Method> scoreMethods;
    private static Expression repoScoreExpr;

    public RepoQuery() {
        scoreMethods = new HashMap();
        try {
            scoreMethods.putAll(JavascriptCompiler.DEFAULT_FUNCTIONS);
            scoreMethods.put("gsort", getClass().getDeclaredMethod("sort", double.class, double.class, double.class, double.class));
            repoScoreExpr = JavascriptCompiler.compile("gsort($score,$recomm,$stars,$gindex)", scoreMethods, getClass().getClassLoader());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String type() {
        return Constants.TYPE_REPOSITORY;
    }

    /**
     * list facet names
     * @return
     */
    @Override
    protected List<String> listFacetFields() {
        return Arrays.asList(Constants.FIELD_LANGUAGE, Constants.FIELD_LICENSE);
    }

    /**
     * 构建查询对象
     *
     * @return
     */
    @Override
    protected Query buildUserQuery() {
        if(parseSearchKey) {
            QueryParser parser = new QueryParser("repo", AnalyzerFactory.getInstance(false));
            try {
                return parser.parse(searchKey);
            } catch (ParseException e) {
                throw new QueryException("Failed to parse \""+searchKey+"\"", e);
            }
        }
        String q = QueryParser.escape(searchKey);

        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        //只搜索公开仓库
        builder.add(NumericDocValuesField.newSlowExactQuery("type", Constants.REPO_TYPE_PUBLIC), BooleanClause.Occur.FILTER);
        //不搜索fork仓库
        builder.add(NumericDocValuesField.newSlowExactQuery("fork", Constants.REPO_FORK_NO), BooleanClause.Occur.FILTER);
        //不搜索被屏蔽的仓库
        builder.add(NumericDocValuesField.newSlowExactQuery("block", Constants.REPO_BLOCK_NO), BooleanClause.Occur.FILTER);

        String sRecomm = (facets.get("recomm")!=null&&facets.get("recomm").length>0)?facets.get("recomm")[0]:null;
        int recomm = NumberUtils.toInt(sRecomm, Constants.RECOMM_NONE);
        if(recomm >= Constants.RECOMM_GVP)//搜索范围
            builder.add(NumericDocValuesField.newSlowExactQuery("recomm", Constants.RECOMM_GVP), BooleanClause.Occur.FILTER);
        else if(recomm > Constants.RECOMM_NONE)
            builder.add(NumericDocValuesField.newSlowRangeQuery("recomm", Constants.RECOMM, Constants.RECOMM_GVP), BooleanClause.Occur.FILTER);

        //BoostQuery
        //如果调整  boost 就要调整 ScoreHelper 中的 SCORE_FACTOR
        BooleanQuery.Builder qbuilder = new BooleanQuery.Builder();
        qbuilder.add(makeBoostQuery("catalogs", q, 10.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("name", q, SCORE_FACTOR), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("description", q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("detail", q, 0.5f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("tags", q, 1.0f), BooleanClause.Occur.SHOULD);
        //qbuilder.add(makeBoostQuery("lang", q, 2.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("owner.name", q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("namespace.path", q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("namespace.name", q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.setMinimumNumberShouldMatch(1);

        builder.add(qbuilder.build(), BooleanClause.Occur.MUST);

        return new FunctionScoreQuery(builder.build(), getRepoScoreExpression());
    }

    /**
     * 对搜索加权
     * TODO 是否启用的仓库名的泛匹配
     * @param field
     * @param q
     * @param boost
     * @return
     */
    @Override
    protected BoostQuery makeBoostQuery(String field, String q, float boost) {
        /*
        if("name".equals(field)){
            List<String> keys = SearchHelper.splitKeywords(q);
            BooleanQuery.Builder qbuilder = new BooleanQuery.Builder();
            for(int i=0;i<keys.size();i++) {
                String key = keys.get(i);
                qbuilder.add(new WildcardQuery(new Term(field, "*"+key+"*")), BooleanClause.Occur.SHOULD);
            }
            Query query = qbuilder.build();
            return new BoostQuery(query, boost);
        }
        */
        return super.makeBoostQuery(field, q, boost);
    }

    /**
     * custom query score
     * @return
     */
    private DoubleValuesSource getRepoScoreExpression() {
        SimpleBindings bindings = new SimpleBindings();
        bindings.add("$score", DoubleValuesSource.SCORES);
        bindings.add("$recomm", DoubleValuesSource.fromIntField("recomm"));
        bindings.add("$stars", DoubleValuesSource.fromIntField("count.star"));
        bindings.add("$gindex", DoubleValuesSource.fromIntField("count.gindex"));
        return repoScoreExpr.getDoubleValuesSource(bindings);
    }

    /**
     * 构建排序对象
     * @return
     */
    @Override
    protected Sort buildSort() {
        if("stars".equals(sort))
            return new Sort(new SortedNumericSortField("count.star", SortField.Type.LONG, true));
        if("forks".equals(sort))
            return new Sort(new SortedNumericSortField("count.fork", SortField.Type.LONG, true));
        if("update".equals(sort))
            return new Sort(new SortedNumericSortField("last_push_at", SortField.Type.LONG, true));
        return Sort.RELEVANCE;
    }

    /**
     * 自定义仓库的搜索评分规则（该方法提供给 Lucene 调用，所以必须是 public static 方法）
     * @param score
     * @param recomm
     * @param stars
     * @param gindex
     * @return
     */
    public static double sort(double score, double recomm, double stars, double gindex) {
        if(score >= SCORE_FACTOR) {
            //官方推荐加权
            if(recomm > 0)
                score += (recomm * 20);
            else {
                //Star 数加权
                if (stars >= 100)
                    score += stars / 20;
                else if (stars > 0 && stars < 10)
                    score -= 10;
                else
                    score -= 20;
            }
            while(score < SCORE_FACTOR)
                score += 1;
        }
        return score;
    }

}
