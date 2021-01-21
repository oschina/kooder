package com.gitee.search.models;

import com.gitee.search.core.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.*;

import java.util.ArrayList;
import java.util.Arrays;
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
    protected Relation enterprise = Relation.EMPTY;
    protected Relation project = Relation.EMPTY;
    protected Relation repository = Relation.EMPTY;
    protected Relation owner = Relation.EMPTY ;

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

    public Issue(Document doc) {
        setDocument(doc);
    }

    /**
     * Read fields from document
     * @param doc
     */
    @Override
    public Searchable setDocument(Document doc) {
        this.id = NumberUtils.toInt(doc.get(Constants.FIELD_ID), 0);
        this.ident = doc.get(Constants.FIELD_IDENT);
        this.title = doc.get(Constants.FIELD_TITLE);
        this.description = doc.get(Constants.FIELD_DESC);
        this.url = doc.get(Constants.FIELD_URL);
        this.labels = new ArrayList<>();
        this.labels.addAll(Arrays.asList(StringUtils.split(doc.get(Constants.FIELD_TAGS), "\n")));
        this.updatedAt = NumberUtils.toLong(doc.get(Constants.FIELD_UPDATED_AT));
        this.createdAt = NumberUtils.toLong(doc.get(Constants.FIELD_CREATED_AT));
        this.closedAt = NumberUtils.toLong(doc.get(Constants.FIELD_CLOSED_AT));
        this.block = NumberUtils.toInt(doc.get(Constants.FIELD_BLOCK), Constants.REPO_BLOCK_NO);
        this.visibility = NumberUtils.toInt(doc.get(Constants.FIELD_VISIBILITY), Constants.VISIBILITY_PRIVATE);
        this.state = NumberUtils.toInt(doc.get(Constants.FIELD_STATUS), STATE_OPENED);
        this.enterprise.id = NumberUtils.toInt(doc.get(Constants.FIELD_ENTERPRISE_ID), 0);
        this.project.id = NumberUtils.toInt(doc.get(Constants.FIELD_PROGRAM_ID));
        this.repository.id = NumberUtils.toInt(doc.get(Constants.FIELD_REPO_ID));
        this.owner.id = NumberUtils.toInt(doc.get(Constants.FIELD_USER_ID));

        return this;
    }

    /**
     * generate lucene document
     * @return
     */
    @Override
    public Document getDocument() {
        Document doc = new Document();
        doc.add(new StringField(Constants.FIELD_ID,     String.valueOf(id),     Field.Store.YES));
        doc.add(new StringField(Constants.FIELD_IDENT,  ident,                  Field.Store.YES));
        doc.add(new TextField(Constants.FIELD_TITLE,    title,                  Field.Store.YES));
        doc.add(new TextField(Constants.FIELD_DESC,     description,            Field.Store.YES));
        doc.add(new StoredField(Constants.FIELD_URL,    url));
        doc.add(new TextField(Constants.FIELD_TAGS, String.join("\n", labels), Field.Store.NO));
        doc.add(new NumericDocValuesField(Constants.FIELD_UPDATED_AT, updatedAt));
        doc.add(new StoredField(Constants.FIELD_UPDATED_AT, updatedAt));
        doc.add(new NumericDocValuesField(Constants.FIELD_UPDATED_AT, updatedAt));
        doc.add(new StoredField(Constants.FIELD_UPDATED_AT, updatedAt));
        doc.add(new NumericDocValuesField(Constants.FIELD_CLOSED_AT, closedAt));
        doc.add(new StoredField(Constants.FIELD_CLOSED_AT, closedAt));

        doc.add(new NumericDocValuesField(Constants.FIELD_BLOCK, block));
        doc.add(new StoredField(Constants.FIELD_BLOCK, block));

        doc.add(new NumericDocValuesField(Constants.FIELD_VISIBILITY, visibility));
        doc.add(new StoredField(Constants.FIELD_VISIBILITY, visibility));

        doc.add(new NumericDocValuesField(Constants.FIELD_STATUS, state));
        doc.add(new StoredField(Constants.FIELD_STATUS, state));

        //enterprise info (just for gitee)
        enterprise:
        doc.add(new StringField(Constants.FIELD_ENTERPRISE_ID, String.valueOf(enterprise.id), Field.Store.YES));
        //program info (just for gitee)
        program:
        doc.add(new StringField(Constants.FIELD_PROGRAM_ID, String.valueOf(project.id), Field.Store.YES));
        //repository info (just for gitee)
        program:
        doc.add(new StringField(Constants.FIELD_REPO_ID, String.valueOf(repository.id), Field.Store.YES));
        //owner info
        owner:
        doc.add(new StringField(Constants.FIELD_USER_ID, String.valueOf(owner.id), Field.Store.YES));

        return doc;
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

    public void setState(org.gitlab4j.api.Constants.IssueState state) {
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
