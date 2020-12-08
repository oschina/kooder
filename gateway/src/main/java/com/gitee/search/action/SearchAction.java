package com.gitee.search.action;

import com.gitee.search.core.SearchHelper;
import com.gitee.search.index.IndexManager;
import com.gitee.search.query.QueryHelper;
import com.gitee.search.queue.QueueTask;
import com.gitee.search.server.Request;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

import java.io.IOException;

/**
 * 搜索接口
 * @author Winter Lau<javayou@gmail.com>
 */
public class SearchAction {

    public final static int PAGE_SIZE = 20; //结果集每页显示的记录数

    /**
     * 仓库搜索
     * https://<search-server>/search/repositories?q=xxxx&sort=xxxx
     * @param request
     * @return
     */
    public static String repositories(Request request) throws IOException, ParseException
    {
        String q = request.param("q");
        String sort = request.param("sort");
        int page = request.param("page", 1);
        String lang = request.param("lang");
        String scope = request.param("scope");

        long ct = System.currentTimeMillis();
        q = SearchHelper.cleanupKey(q);

        System.out.println("q=" + q);

        Query query = QueryHelper.buildRepoQuery(q, lang, 0);
        System.out.println(query);

        return IndexManager.search(QueueTask.TYPE_REPOSITORY, query, Sort.RELEVANCE, page, PAGE_SIZE);
    }

    /**
     * 代码搜索
     * @param request
     * @return
     */
    public static String codes(Request request) throws ActionException {
        return null;
    }

    /**
     * issue 任务搜索
     * @param request
     * @return
     */
    public static String issues(Request request) throws ActionException {
        return null;
    }

    /**
     * PR 搜索
     * @param request
     * @return
     */
    public static String pullrequests(Request request) throws ActionException {
        return null;
    }

    /**
     * 文档搜索
     * @param request
     * @return
     */
    public static String wiki(Request request) throws ActionException {
        return null;
    }

    /**
     * 代码提交搜索
     * @param request
     * @return
     */
    public static String commits(Request request) throws ActionException {
        return null;
    }

    /**
     * 搜索用户
     * @param request
     * @return
     */
    public static String users(Request request) throws ActionException {
        return null;
    }

}
