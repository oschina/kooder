package com.gitee.kooder.gitee;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gitee.kooder.core.Constants;
import com.gitee.kooder.models.Relation;

import java.util.Collections;
import java.util.Date;

/**
 * @author zhanggx
 */
public class Repository {

    private Integer id;
    private String name;
    private String url;
    private String htmlUrl;
    private String gitHttpUrl;
    private String description;

    /**
     * 私有仓库
     */
    @JsonProperty("private")
    private Boolean isPrivate;

    /**
     * 内源仓库
     */
    @JsonProperty("internal")
    private Boolean isInternal;

    /**
     * 公开仓库
     */
    @JsonProperty("public")
    private Boolean isPublic;
    private String license;
    private String language;
    private Integer stargazersCount;
    private Integer forksCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    private Date updatedAt;

    private User owner;

    /**
     * Turn to kooder repository
     * @return
     */
    public com.gitee.kooder.models.Repository toKooderRepository() {
        com.gitee.kooder.models.Repository repo = new com.gitee.kooder.models.Repository();
        repo.setId(this.getId());
        repo.setName(this.getName());
        repo.setDescription(this.getDescription());
        repo.setUrl(this.getGitHttpUrl() == null ? this.getHtmlUrl() : this.getGitHttpUrl());
        repo.setProject(Relation.EMPTY);
        repo.setOwner(new Relation(this.getOwner().getId(), this.getOwner().getName(), this.getOwner().getHtmlUrl()));
        repo.setVisibility(this.getPrivate() ? Constants.VISIBILITY_PRIVATE : this.getInternal() ? Constants.VISIBILITY_INTERNAL : Constants.VISIBILITY_PUBLIC);
        repo.setLicense(this.getLicense());
        repo.setLang(this.getLanguage());
        repo.setReadme(null);
        repo.setFork(0);
        repo.setTags(Collections.emptyList());
        repo.setStarsCount(this.getStargazersCount());
        repo.setForksCount(this.getForksCount());
        repo.setCreatedAt(this.getCreatedAt().getTime());
        repo.setUpdatedAt(this.getUpdatedAt().getTime());
        repo.setBlock(Constants.REPO_BLOCK_NO);
        return repo;
    }


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

    public String getGitHttpUrl() {
        return gitHttpUrl;
    }

    public void setGitHttpUrl(String gitHttpUrl) {
        this.gitHttpUrl = gitHttpUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Boolean getInternal() {
        return isInternal;
    }

    public void setInternal(Boolean internal) {
        isInternal = internal;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getStargazersCount() {
        return stargazersCount;
    }

    public void setStargazersCount(Integer stargazersCount) {
        this.stargazersCount = stargazersCount;
    }

    public Integer getForksCount() {
        return forksCount;
    }

    public void setForksCount(Integer forksCount) {
        this.forksCount = forksCount;
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

}
