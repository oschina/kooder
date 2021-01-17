package com.gitee.search.models;

import org.apache.lucene.document.Document;
import org.gitlab4j.api.Constants;

import java.util.List;

/**
 * Issue info
 * @author Winter Lau<javayou@gmail.com>
 */
public final class Issue extends Searchable {

    public final static int STATE_OPENED    = 0x01;
    public final static int STATE_CLOSED    = 0x02;
    public final static int STATE_REOPENED  = 0x03;

    protected String ident;
    protected Relation enterprise;
    protected Relation project;
    protected Relation repository;
    protected Relation owner;

    protected String title;
    protected String description;
    protected String url;
    protected List<String> labels;    //标签
    protected long createdAt;
    protected long updatedAt;
    protected long closedAt;
    protected int state;
    protected int block;
    protected int visibility;       //0私有仓库，1内源仓库，2公开仓库

    public Issue(){}
    public Issue(org.gitlab4j.api.models.Issue issue) {
        this.id = issue.getId();
        this.ident = issue.getExternalId();
        this.enterprise = Relation.EMPTY;
        this.project = Relation.EMPTY;
        this.repository = new Relation(issue.getProjectId(), null, null);
        this.owner = new Relation(issue.getAuthor().getId(), issue.getAuthor().getName(), issue.getAuthor().getWebUrl());
        this.title = issue.getTitle();
        this.description = issue.getDescription();
        this.url = issue.getWebUrl();
        this.labels = issue.getLabels();
        this.createdAt = issue.getCreatedAt().getTime();
        this.updatedAt = (issue.getUpdatedAt() != null) ? issue.getUpdatedAt().getTime() : 0;
        this.closedAt = (issue.getClosedAt() != null) ? issue.getClosedAt().getTime() : 0;
        this.setState(issue.getState());
    }

    /**
     * generate lucene document
     *
     * @return
     */
    @Override
    public Document getDocument() {
        return null;
    }

    /**
     * Read fields from document
     *
     * @param doc
     */
    @Override
    public Searchable setDocument(Document doc) {
        return this;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public Relation getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(Relation enterprise) {
        this.enterprise = enterprise;
    }

    public Relation getProject() {
        return project;
    }

    public void setProject(Relation project) {
        this.project = project;
    }

    public Relation getRepository() {
        return repository;
    }

    public void setRepository(Relation repository) {
        this.repository = repository;
    }

    public Relation getOwner() {
        return owner;
    }

    public void setOwner(Relation owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(long closedAt) {
        this.closedAt = closedAt;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setState(Constants.IssueState state) {
        switch(state){
            case OPENED:
                this.state = STATE_OPENED;
                break;
            case CLOSED:
                this.state = STATE_CLOSED;
                break;
            case REOPENED:
                this.state = STATE_REOPENED;
        }
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }
}
