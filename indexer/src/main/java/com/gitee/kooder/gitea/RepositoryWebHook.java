package com.gitee.kooder.gitea;

/**
 * @author zhanggx
 */
public class RepositoryWebHook {

    public static final String ACTION_CREATED = "created";
    public static final String ACTION_DELETED = "deleted";

    private String secret;
    private String action;
    private Repository repository;
    private User organization;
    private User sender;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

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

    public User getOrganization() {
        return organization;
    }

    public void setOrganization(User organization) {
        this.organization = organization;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

}
