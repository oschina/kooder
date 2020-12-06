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
import java.util.List;
import java.util.Map;

/**
 * 搜索接口
 * @author Winter Lau<javayou@gmail.com>
 */
public class SearchAction {

    public final static int PAGE_SIZE = 20; //结果集每页显示的记录数

    /**
     * 仓库搜索
     * https://<search-server>/search/repositories?q=xxxx&sort=xxxx
     * @param params
     * @param body
     * @return
     */
    public static String repositories(Map<String, List<String>> params, StringBuilder body) throws IOException, ParseException
    {
        String q = getParam(params, "q");
        String sort = getParam(params, "sort");
        int page = getParam(params, "page", 1);
        String lang = getParam(params, "lang");
        String scope = getParam(params, "scope");

        long ct = System.currentTimeMillis();
        q = SearchHelper.cleanupKey(q);

        System.out.println("q=" + q);

        Query query = QueryHelper.buildRepoQuery(q, lang, 0);
        System.out.println(query);

        return IndexManager.search(QueueTask.TYPE_REPOSITORY, query, Sort.RELEVANCE, page, PAGE_SIZE);
    }

    /**
     * 代码搜索
     * @param params
     * @param body
     * @return
     */
    public static String codes(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

    /**
     * issue 任务搜索
     * @param params
     * @param body
     * @return
     */
    public static String issues(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

    /**
     * PR 搜索
     * @param params
     * @param body
     * @return
     */
    public static String pullrequests(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

    /**
     * 文档搜索
     * @param params
     * @param body
     * @return
     */
    public static String wiki(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

    /**
     * 代码提交搜索
     * @param params
     * @param body
     * @return
     */
    public static String commits(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

    /**
     * 搜索用户
     * @param params
     * @param body
     * @return
     */
    public static String users(Map<String, List<String>> params, StringBuilder body) throws ActionException {
        return null;
    }

}
