package com.gitee.search.query;

/**
 * 查询工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueryHelper {

    public final static IQuery REPOSITORY = new RepoQuery();

    public final static IQuery ISSUE = new IssueQuery();

}
