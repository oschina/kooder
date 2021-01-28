package com.gitee.kooder.query;

/**
 * 查询工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueryFactory {

    /**
     * 仓库查询器
     * @return
     */
    public final static IQuery REPO() {
        return new RepoQuery();
    }

    /**
     * Issue 任务查询器
     * @return
     */
    public final static IQuery ISSUE() {
        return new IssueQuery();
    }

    /**
     * 源码查询器
     * @return
     */
    public final static IQuery CODE() {
        return new CodeQuery();
    }

}
