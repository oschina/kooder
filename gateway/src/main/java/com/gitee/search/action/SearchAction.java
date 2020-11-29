package com.gitee.search.action;

import com.gitee.search.core.SearchHelper;
import com.gitee.search.index.IndexManager;
import com.gitee.search.query.QueryHelper;
import com.gitee.search.queue.QueueTask;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

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

        for(int i=0;i<10;i++) {
            long ct = System.currentTimeMillis();

            StringBuilder json = repositories(new HashMap<String, List<String>>() {{
                put("q", Arrays.asList("红薯"));
            }}, null);
            //System.out.println(json);

            System.out.println("total time: " + (System.currentTimeMillis() - ct) + " ms");
        }
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
    public static StringBuilder repositories(Map<String, List<String>> params, StringBuilder body) throws IOException, ParseException
    {
        String q = getParam(params, "q");
        String sort = getParam(params, "sort");
        int page = getParam(params, "page", 1);
        String lang = getParam(params, "lang");
        String scope = getParam(params, "scope");

        long ct = System.currentTimeMillis();
        q = SearchHelper.cleanupKey(q);

        Query query = QueryHelper.buildRepoQuery(q, lang, 0);
        System.out.println((System.currentTimeMillis() - ct) + "ms:"+query);

        String resultJson = IndexManager.search(QueueTask.TYPE_REPOSITORY, query, Sort.RELEVANCE, page, PAGE_SIZE);

        return new StringBuilder(resultJson);
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
