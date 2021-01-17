package com.gitee.search.models;

import org.apache.lucene.document.Document;
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
    protected Relation enterprise;
    protected Relation project;
    protected Relation owner;
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
    public Repository(org.gitlab4j.api.models.Project p) {
        this.id = p.getId();
        this.name = p.getName();
        this.description = p.getDescription();
        this.url = p.getWebUrl();
        this.enterprise = Relation.EMPTY;
        this.project = Relation.EMPTY;
        this.owner = new Relation(p.getOwner().getId(), p.getOwner().getName(), p.getOwner().getWebUrl());
        this.setVisibility(p.getVisibility());
        this.setLicense(p.getLicense().getName());
        this.setLang(null);// TODO 编程语言如何获取
        this.setReadme(null); //读取 Readme 信息
        this.setFork((p.getForkedFromProject()!=null)?p.getForkedFromProject().getId():0);
        this.setTags(p.getTagList());
        this.setCreatedAt(p.getCreatedAt().getTime());
        this.setUpdatedAt((p.getLastActivityAt()!=null)?p.getLastActivityAt().getTime():0);
        this.setStarsCount(p.getStarCount());
        this.setForksCount(p.getForksCount());
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
                this.visibility = VISIBILITY_PRIVATE;
                break;
            case PUBLIC:
                this.visibility = VISIBILITY_PUBLIC;
                break;
            case INTERNAL:
                this.visibility = VISIBILITY_INTERNAL;
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
