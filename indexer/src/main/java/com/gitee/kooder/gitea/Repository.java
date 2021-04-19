package com.gitee.kooder.gitea;

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
    private String fullName;
    private String description;
    private Boolean empty;
    @JsonProperty("private")
    private Boolean isPrivate;
    private Boolean internal;
    private Boolean fork;
    private Boolean template;
    private Boolean mirror;
    private Boolean archived;
    private Boolean hasIssues;
    private Integer size;
    private String htmlUrl;
    private String sshUrl;
    private String cloneUrl;
    private String originalUrl;
    private String website;
    private Integer starsCount;
    private Integer forksCount;
    private Integer watchersCount;
    private Integer openIssuesCount;
    private Integer openPrCounter;
    private Integer releaseCounter;
    private String defaultBranch;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date updatedAt;
    private User owner;

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Boolean getInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public Boolean getFork() {
        return fork;
    }

    public void setFork(Boolean fork) {
        this.fork = fork;
    }

    public Boolean getTemplate() {
        return template;
    }

    public void setTemplate(Boolean template) {
        this.template = template;
    }

    public Boolean getMirror() {
        return mirror;
    }

    public void setMirror(Boolean mirror) {
        this.mirror = mirror;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public Boolean getHasIssues() {
        return hasIssues;
    }

    public void setHasIssues(Boolean hasIssues) {
        this.hasIssues = hasIssues;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getSshUrl() {
        return sshUrl;
    }

    public void setSshUrl(String sshUrl) {
        this.sshUrl = sshUrl;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

    public void setCloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Integer getStarsCount() {
        return starsCount;
    }

    public void setStarsCount(Integer starsCount) {
        this.starsCount = starsCount;
    }

    public Integer getForksCount() {
        return forksCount;
    }

    public void setForksCount(Integer forksCount) {
        this.forksCount = forksCount;
    }

    public Integer getWatchersCount() {
        return watchersCount;
    }

    public void setWatchersCount(Integer watchersCount) {
        this.watchersCount = watchersCount;
    }

    public Integer getOpenIssuesCount() {
        return openIssuesCount;
    }

    public void setOpenIssuesCount(Integer openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
    }

    public Integer getOpenPrCounter() {
        return openPrCounter;
    }

    public void setOpenPrCounter(Integer openPrCounter) {
        this.openPrCounter = openPrCounter;
    }

    public Integer getReleaseCounter() {
        return releaseCounter;
    }

    public void setReleaseCounter(Integer releaseCounter) {
        this.releaseCounter = releaseCounter;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
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

    public com.gitee.kooder.models.Repository toKooderRepository() {
        com.gitee.kooder.models.Repository repo = new com.gitee.kooder.models.Repository();
        repo.setId(this.getId());
        repo.setName(this.getName());
        repo.setDescription(this.getDescription());
        repo.setUrl(this.getCloneUrl());
        repo.setOwner(new Relation(this.getOwner().getId(), this.getOwner().getLogin(), ""));
        repo.setVisibility(this.getPrivate() ? Constants.VISIBILITY_PRIVATE : this.getInternal() ? Constants.VISIBILITY_INTERNAL : Constants.VISIBILITY_PUBLIC);
        repo.setLicense(null);
        repo.setReadme(null);
        repo.setFork(0);
        repo.setTags(Collections.emptyList());
        repo.setStarsCount(this.getStarsCount());
        repo.setForksCount(this.getForksCount());
        repo.setCreatedAt(this.getCreatedAt().getTime());
        repo.setUpdatedAt(this.getUpdatedAt().getTime());
        repo.setBlock(Constants.REPO_BLOCK_NO);
        repo.setEnterprise(new Relation(owner.getId(), owner.getUsername(), ""));
        return repo;
    }

}
