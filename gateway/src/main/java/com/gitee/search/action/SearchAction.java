package com.gitee.search.action;

import com.gitee.search.core.AnalyzerFactory;
import com.gitee.search.core.SearchHelper;
import com.gitee.search.index.IndexManager;
import com.gitee.search.queue.QueueTask;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import static com.gitee.search.action.ActionUtils.getParam;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索接口
 * @author Winter Lau<javayou@gmail.com>
 */
public class SearchAction {

    public final static int PAGE_SIZE = 20; //结果集每页显示的记录数

    public static void main(String[] args) throws Exception {

        StringBuilder json = repositories(new HashMap<String, List<String>>(){{
            put("q", Arrays.asList("红薯"));
        }}, null);
        System.out.println(json);

        /**
        NGramDistance ng = new NGramDistance();
        float score1 = ng.getDistance("Gorbachev", "Gorbechyov");
        System.out.println(score1);
        float score2 = ng.getDistance("girl", "girlfriend");
        System.out.println(score2);

        System.out.println(ng.getDistance("中华人民共和国","中华人民共和国"));
         */
    }

    /**
     * 仓库搜索
     * https://<search-server>/search/repositories?q=xxxx&sort=xxxx
     * @param params
     * @param body
     * @return
     */
    public static StringBuilder repositories(Map<String, List<String>> params, StringBuilder body)
            throws ActionException, IOException, ParseException
    {
        String q = getParam(params, "q");
        String sort = getParam(params, "sort");
        int page = getParam(params, "page", 1);
        String lang = getParam(params, "lang");
        String scope = getParam(params, "scope");

        q = SearchHelper.cleanupKey(q);

        String resultJson = IndexManager.search(QueueTask.TYPE_REPOSITORY,
                makeRepoQuery(q, lang, scope),
                new Sort(
                    SortField.FIELD_SCORE,
                    new SortField("created_at", SortField.Type.LONG, true)
                ),
                page,
                PAGE_SIZE);

        return new StringBuilder(resultJson);
    }

    /**
     * 仓库搜索的条件
     * @param q
     * @param lang
     * @param scope
     * @return
     */
    private static Query makeRepoQuery(String q, String lang, String scope) throws ParseException {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        //builder.setMinimumNumberShouldMatch(1);
        if(StringUtils.isNotBlank(lang))//编程语言
            builder.add(new TermQuery(new Term("lang", lang)), BooleanClause.Occur.MUST);

        if("gvp".equalsIgnoreCase(scope))//搜索范围
            builder.add(new TermQuery(new Term("recomm", "2")), BooleanClause.Occur.MUST);
        else if("recomm".equalsIgnoreCase(scope))
            builder.add(new TermQuery(new Term("recomm", "1")), BooleanClause.Occur.MUST);

        //BoostQuery
        BooleanQuery.Builder qbuilder = new BooleanQuery.Builder();
        qbuilder.add(makeBoostQuery("name", q, 50.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("description", q, 10.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("detail", q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("tags", q, 10.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("catalogs", q, 10.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery("owner.name", q, 10.0f), BooleanClause.Occur.SHOULD);

        builder.add(qbuilder.build(), BooleanClause.Occur.MUST);

        return builder.build();
    }

    private static BoostQuery makeBoostQuery(String field, String q, float boost) throws ParseException {
        return new BoostQuery(new QueryParser(field, AnalyzerFactory.INSTANCE_FOR_SEARCH).parse(q), boost);
    }

    /**
     * 代码搜索
     * @param params
     * @param body
     * @return
     */
    public static StringBuilder codes(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

    /**
     * issue 任务搜索
     * @param params
     * @param body
     * @return
     */
    public static StringBuilder issues(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

    /**
     * PR 搜索
     * @param params
     * @param body
     * @return
     */
    public static StringBuilder pullrequests(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

    /**
     * 文档搜索
     * @param params
     * @param body
     * @return
     */
    public static StringBuilder wiki(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

    /**
     * 代码提交搜索
     * @param params
     * @param body
     * @return
     */
    public static StringBuilder commits(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

    /**
     * 搜索用户
     * @param params
     * @param body
     * @return
     */
    public static StringBuilder users(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

}
