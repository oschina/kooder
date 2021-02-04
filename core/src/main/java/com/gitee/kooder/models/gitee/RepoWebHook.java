package com.gitee.kooder.models.gitee;

/**
 * @author zhanggx
 */
public class RepoWebHook {

    public static final String ACTION_CREATE = "create";
    public static final String ACTION_DESTROY = "destroy";

    private String action;

    private Repository repository;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

}
