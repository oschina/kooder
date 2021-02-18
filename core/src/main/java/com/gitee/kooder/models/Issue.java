/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.models;

import com.gitee.kooder.core.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.*;
import org.gitlab4j.api.webhook.IssueEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        this.ident = issue.getProjectId() + "_" + issue.getId();
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

    public Issue(IssueEvent e) {
        this.id = e.getObjectAttributes().getId();
        this.ident = e.getProject().getId() + "_" + this.id;
        this.enterprise = Relation.EMPTY;
        this.project = Relation.EMPTY;
        this.repository = new Relation(e.getProject().getId(), e.getProject().getName(), e.getProject().getUrl());
        this.owner = new Relation(e.getObjectAttributes().getAuthorId(), e.getUser().getName(), e.getUser().getWebUrl());
        this.title = e.getObjectAttributes().getTitle();
        this.description = e.getObjectAttributes().getDescription();
        this.url = e.getObjectAttributes().getUrl();
        this.labels = e.getLabels().stream().map(l -> l.getTitle()).collect(Collectors.toList());
        this.createdAt = (e.getObjectAttributes().getCreatedAt() != null) ? e.getObjectAttributes().getCreatedAt().getTime() : 0L;
        this.updatedAt = (e.getObjectAttributes().getUpdatedAt() != null) ? e.getObjectAttributes().getUpdatedAt().getTime() : 0L;
        try {
            this.setState(org.gitlab4j.api.Constants.IssueState.valueOf(e.getObjectAttributes().getState()));
        }catch(Exception ee){}
    }

    public Issue(Document doc) {
        setDocument(doc);
    }

    /**
     * Read fields from document
     * @param doc
     */
    @Override
    public Issue setDocument(Document doc) {
        this.id = NumberUtils.toInt(doc.get(Constants.FIELD_ID), 0);
        this.ident = doc.get(Constants.FIELD_IDENT);
        this.title = doc.get(Constants.FIELD_TITLE);
        this.description = doc.get(Constants.FIELD_DESC);
        this.url = doc.get(Constants.FIELD_URL);
        this.labels = new ArrayList<>();
        String tags = doc.get(Constants.FIELD_TAGS);
        if(tags != null)
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
        Document doc = super.newDocument();

        doc.add(new StringField(Constants.FIELD_IDENT,  ident,                  Field.Store.YES));
        doc.add(new TextField(Constants.FIELD_TITLE,    title,                  Field.Store.YES));
        doc.add(new TextField(Constants.FIELD_DESC,     description,            Field.Store.YES));
        doc.add(new StoredField(Constants.FIELD_URL,    url));
        doc.add(new TextField(Constants.FIELD_TAGS, String.join("\n", labels), Field.Store.NO));

        super.addNumToDoc(doc, Constants.FIELD_CREATED_AT, createdAt);
        super.addNumToDoc(doc, Constants.FIELD_UPDATED_AT, updatedAt);
        super.addNumToDoc(doc, Constants.FIELD_CLOSED_AT, closedAt);

        super.addIntToDoc(doc, Constants.FIELD_BLOCK, block);
        super.addIntToDoc(doc, Constants.FIELD_VISIBILITY, visibility);
        super.addIntToDoc(doc, Constants.FIELD_STATUS, state);

        //enterprise info (just for gitee)
        enterprise:
        super.addLongToDoc(doc, Constants.FIELD_ENTERPRISE_ID, this.enterprise.id);
        //program info (just for gitee)
        program:
        super.addLongToDoc(doc, Constants.FIELD_PROGRAM_ID, this.project.id);
        //repository info (just for gitee)
        repository:
        super.addLongToDoc(doc, Constants.FIELD_REPO_ID, this.repository.id);
        //owner info
        owner:
        super.addLongToDoc(doc, Constants.FIELD_USER_ID, this.owner.id);

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
