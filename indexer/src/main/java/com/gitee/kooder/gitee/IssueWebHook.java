package com.gitee.kooder.gitee;

/**
 * @author zhanggx
 */
public class IssueWebHook {

    private Issue issue;

    private Repository repository;

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

}
