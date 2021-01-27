package com.gitee.search.models;

import com.gitee.search.core.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetField;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Visibility;

import java.util.List;

/**
 * Repository Info
 * @author Winter Lau<javayou@gmail.com>
 */
public final class Repository extends Searchable {

    protected String name;
    protected String displayName;   //nameWithNamespace
    protected String description;
    protected String url;
    protected Relation enterprise   = Relation.EMPTY;
    protected Relation project      = Relation.EMPTY;
    protected Relation owner        = Relation.EMPTY;
    protected int recomm;           //推荐级别
    protected int block;            //是否屏蔽 1屏蔽，0不屏蔽
    protected int visibility;       //0私有仓库，1内源仓库，2公开仓库
    protected String license;       //许可证
    protected String lang;          //编程语言
    protected String readme;        //Readme 内容
    protected long fork;            //fork自某个仓库的 id，如果没有则为 0
    protected List<String> tags;    //标签
    protected List<String> catalogs;//分类
    private long createdAt;
    private long updatedAt;

    protected int starsCount;
    protected int forksCount;

    public Repository() {}

    public Repository(Document doc) {
        setDocument(doc);
    }

    public Repository(Project p) {
        this.id = p.getId();
        this.name = p.getName();
        this.description = p.getDescription();
        this.url = p.getWebUrl();
        this.enterprise = Relation.EMPTY;
        this.project = Relation.EMPTY;
        if(p.getOwner() != null)
            this.owner = new Relation(p.getOwner().getId(), p.getOwner().getName(), p.getOwner().getWebUrl());
        this.setVisibility(p.getVisibility());
        if(p.getLicense() != null)
            this.setLicense(p.getLicense().getName());
        this.setLang(null);// 编程语言如何获取
        this.setReadme(null); //读取 Readme 信息
        this.setFork((p.getForkedFromProject()!=null)?p.getForkedFromProject().getId():0);
        this.setTags(p.getTagList());
        this.setCreatedAt(p.getCreatedAt().getTime());
        this.setUpdatedAt((p.getLastActivityAt()!=null)?p.getLastActivityAt().getTime():0);
        this.setStarsCount(p.getStarCount());
        this.setForksCount(p.getForksCount());
        this.setBlock(Constants.REPO_BLOCK_NO);
    }

    /**
     * generate lucene document
     * @return
     */
    @Override
    public Document getDocument() {
        Document doc = super.newDocument();
        doc.add(new TextField(Constants.FIELD_NAME,     this.getName(),         Field.Store.YES));
        if(StringUtils.isNotBlank(this.getDisplayName()))
            doc.add(new StringField(Constants.FIELD_DISPLAY_NAME, this.getDisplayName(), Field.Store.YES));
        if(StringUtils.isNotBlank(this.getDescription()))
            doc.add(new TextField(Constants.FIELD_DESC,     this.getDescription(),  Field.Store.YES));
        if(StringUtils.isNotBlank(this.getUrl()))
            doc.add(new StoredField(Constants.FIELD_URL,    this.getUrl()));

        doc.add(new NumericDocValuesField(Constants.FIELD_RECOMM, recomm));
        doc.add(new StoredField(Constants.FIELD_RECOMM, recomm));

        doc.add(new NumericDocValuesField(Constants.FIELD_BLOCK, block));
        doc.add(new StoredField(Constants.FIELD_BLOCK, block));

        doc.add(new NumericDocValuesField(Constants.FIELD_VISIBILITY, visibility));
        doc.add(new StoredField(Constants.FIELD_VISIBILITY, visibility));

        if(StringUtils.isNotBlank(license)) {
            doc.add(new FacetField(Constants.FIELD_LICENSE, license));
            doc.add(new StringField(Constants.FIELD_LICENSE, license, Field.Store.YES));
        }

        if(StringUtils.isNotBlank(lang)) {
            doc.add(new FacetField(Constants.FIELD_LANGUAGE, lang));
            doc.add(new StringField(Constants.FIELD_LANGUAGE, lang, Field.Store.YES));
        }

        if(StringUtils.isNotBlank(readme))
            doc.add(new TextField(Constants.FIELD_README, readme, Field.Store.NO));

        doc.add(new NumericDocValuesField(Constants.FIELD_FORK, fork));
        doc.add(new StoredField(Constants.FIELD_FORK, fork));

        doc.add(new NumericDocValuesField(Constants.FIELD_STAR_COUNT, starsCount));
        doc.add(new StoredField(Constants.FIELD_STAR_COUNT, starsCount));
        doc.add(new NumericDocValuesField(Constants.FIELD_FORK_COUNT, forksCount));
        doc.add(new StoredField(Constants.FIELD_FORK_COUNT, forksCount));
        doc.add(new NumericDocValuesField(Constants.FIELD_CREATED_AT, createdAt));
        doc.add(new StoredField(Constants.FIELD_CREATED_AT, createdAt));
        doc.add(new NumericDocValuesField(Constants.FIELD_UPDATED_AT, updatedAt));
        doc.add(new StoredField(Constants.FIELD_UPDATED_AT, updatedAt));

        //tags
        if(tags != null)
            doc.add(new TextField(Constants.FIELD_TAGS, String.join("\n", tags), Field.Store.NO));
        //catalogs
        if(catalogs != null)
            doc.add(new TextField(Constants.FIELD_CATALOGS, String.join("\n", catalogs), Field.Store.NO));

        //enterprise info (just for gitee)
        enterprise:
        doc.add(new StringField(Constants.FIELD_ENTERPRISE_ID, String.valueOf(enterprise.id), Field.Store.YES));
        if(StringUtils.isNotBlank(enterprise.name)) {
            doc.add(new FacetField(Constants.FIELD_ENTERPRISE_NAME, enterprise.name));
            doc.add(new TextField(Constants.FIELD_ENTERPRISE_NAME,  enterprise.name, Field.Store.YES));
        }
        if(StringUtils.isNotBlank(enterprise.url))
            doc.add(new StoredField(Constants.FIELD_ENTERPRISE_URL, enterprise.url));
        //program info (just for gitee)
        program:
        doc.add(new StringField(Constants.FIELD_PROGRAM_ID, String.valueOf(project.id), Field.Store.YES));
        if(StringUtils.isNotBlank(project.name)) {
            doc.add(new FacetField(Constants.FIELD_PROGRAM_NAME, project.name));
            doc.add(new TextField(Constants.FIELD_PROGRAM_NAME,  project.name, Field.Store.YES));
        }
        if(StringUtils.isNotBlank(project.url))
            doc.add(new StoredField(Constants.FIELD_PROGRAM_URL, project.url));
        //owner info
        owner:
        doc.add(new StringField(Constants.FIELD_USER_ID, String.valueOf(owner.id), Field.Store.YES));
        if(StringUtils.isNotBlank(owner.name)) {
            doc.add(new FacetField(Constants.FIELD_USER_NAME, owner.name));
            doc.add(new TextField(Constants.FIELD_USER_NAME,  owner.name, Field.Store.YES));
        }
        if(StringUtils.isNotBlank(owner.url))
            doc.add(new StoredField(Constants.FIELD_USER_URL, owner.url));

        return doc;
    }

    /**
     * Read fields from document
     *
     * @param doc
     */
    @Override
    public Repository setDocument(Document doc) {
        this.id = NumberUtils.toInt(doc.get(Constants.FIELD_ID), 0);
        this.name = doc.get(Constants.FIELD_NAME);
        this.displayName = doc.get(Constants.FIELD_DISPLAY_NAME);
        this.description = doc.get(Constants.FIELD_DESC);
        this.url = doc.get(Constants.FIELD_URL);
        this.recomm = NumberUtils.toInt(doc.get(Constants.FIELD_RECOMM), Constants.RECOMM_NONE);
        this.block = NumberUtils.toInt(doc.get(Constants.FIELD_BLOCK), Constants.REPO_BLOCK_YES);//如果没有block字段，为安全计，默认是屏蔽状态
        this.visibility = NumberUtils.toInt(doc.get(Constants.FIELD_VISIBILITY), Constants.VISIBILITY_PRIVATE);
        this.fork = NumberUtils.toInt(doc.get(Constants.FIELD_FORK), 0);
        this.license = doc.get(Constants.FIELD_LICENSE);
        this.lang = doc.get(Constants.FIELD_LANGUAGE);
        this.starsCount = NumberUtils.toInt(doc.get(Constants.FIELD_STAR_COUNT), 0);
        this.forksCount = NumberUtils.toInt(doc.get(Constants.FIELD_FORK_COUNT), 0);
        this.createdAt = NumberUtils.toLong(doc.get(Constants.FIELD_CREATED_AT), 0);
        this.updatedAt = NumberUtils.toLong(doc.get(Constants.FIELD_UPDATED_AT), 0);

        //enterprise
        this.enterprise.id = NumberUtils.toInt(doc.get(Constants.FIELD_ENTERPRISE_ID), 0);
        this.enterprise.name = doc.get(Constants.FIELD_ENTERPRISE_NAME);
        this.enterprise.url = doc.get(Constants.FIELD_ENTERPRISE_URL);
        //program
        this.project.id = NumberUtils.toInt(doc.get(Constants.FIELD_PROGRAM_ID), 0);
        this.project.name = doc.get(Constants.FIELD_PROGRAM_NAME);
        this.project.url = doc.get(Constants.FIELD_PROGRAM_URL);
        //owner
        this.owner.id = NumberUtils.toInt(doc.get(Constants.FIELD_USER_ID), 0);
        this.owner.name = doc.get(Constants.FIELD_USER_NAME);
        this.owner.url = doc.get(Constants.FIELD_USER_URL);

        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Relation getOwner() {
        return owner;
    }

    public void setOwner(Relation owner) {
        this.owner = owner;
    }

    public int getRecomm() {
        return recomm;
    }

    public void setRecomm(int recomm) {
        this.recomm = recomm;
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

    public void setVisibility(Visibility visibility) {
        switch(visibility) {
            case PRIVATE:
                this.visibility = Constants.VISIBILITY_PRIVATE;
                break;
            case PUBLIC:
                this.visibility = Constants.VISIBILITY_PUBLIC;
                break;
            case INTERNAL:
                this.visibility = Constants.VISIBILITY_INTERNAL;
        }
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }

    public long getFork() {
        return fork;
    }

    public void setFork(long fork) {
        this.fork = fork;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getCatalogs() {
        return catalogs;
    }

    public void setCatalogs(List<String> catalogs) {
        this.catalogs = catalogs;
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

    public int getStarsCount() {
        return starsCount;
    }

    public void setStarsCount(int starsCount) {
        this.starsCount = starsCount;
    }

    public int getForksCount() {
        return forksCount;
    }

    public void setForksCount(int forksCount) {
        this.forksCount = forksCount;
    }
}
