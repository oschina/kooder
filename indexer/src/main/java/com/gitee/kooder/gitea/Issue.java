package com.gitee.kooder.gitea;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gitee.kooder.models.Relation;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhanggx
 */
public class Issue {

    public static final String STATE_OPEN = "open";
    public static final String STATE_CLOSED = "closed";

    private Integer id;
    private String url;
    private String htmlUrl;
    private Integer number;
    private User user;
    private String originalAuthor;
    private Integer originalAuthorId;
    private String title;
    private String body;
    private String ref;
    private List<Label> labels;
    private String state;
    private Integer comments;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date updatedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date closedAt;

    /**
     * Turn to kooder issue
     *
     * @return
     */
    public com.gitee.kooder.models.Issue toKooderIssue(Repository repository) {
        com.gitee.kooder.models.Issue issue = new com.gitee.kooder.models.Issue();
        issue.setId(this.getId());
        issue.setIdent(repository.getId() + "_" + this.getId());
        issue.setRepository(new Relation(repository.getId(), repository.getName(), repository.getHtmlUrl()));
        issue.setOwner(new Relation(this.getUser().getId(), this.getUser().getUsername(), ""));
        issue.setTitle(this.getTitle());
        issue.setDescription(this.getBody());
        issue.setUrl(this.getHtmlUrl());
        issue.setLabels(this.labels.stream().map(Label::getName).collect(Collectors.toList()));
        issue.setCreatedAt(this.getCreatedAt().getTime());
        issue.setUpdatedAt(this.getUpdatedAt().getTime());
        issue.setState(STATE_OPEN.equals(this.getState()) ? com.gitee.kooder.models.Issue.STATE_OPENED : com.gitee.kooder.models.Issue.STATE_CLOSED);
        issue.setEnterprise(new Relation(repository.getOwner().getId(), repository.getOwner().getUsername(), ""));
        return issue;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOriginalAuthor() {
        return originalAuthor;
    }

    public void setOriginalAuthor(String originalAuthor) {
        this.originalAuthor = originalAuthor;
    }

    public Integer getOriginalAuthorId() {
        return originalAuthorId;
    }

    public void setOriginalAuthorId(Integer originalAuthorId) {
        this.originalAuthorId = originalAuthorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    public static class Label {
        private Integer id;
        private String name;
        private String color;
        private String description;
        private String url;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
