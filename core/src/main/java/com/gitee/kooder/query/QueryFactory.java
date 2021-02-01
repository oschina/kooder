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
    public final static RepoQuery REPO() {
        return new RepoQuery();
    }

    /**
     * Issue 任务查询器
     * @return
     */
    public final static IssueQuery ISSUE() {
        return new IssueQuery();
    }

    /**
     * 源码查询器
     * @return
     */
    public final static CodeQuery CODE() {
        return new CodeQuery();
    }

}
