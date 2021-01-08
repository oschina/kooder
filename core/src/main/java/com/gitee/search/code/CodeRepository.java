package com.gitee.search.code;

import com.gitee.search.core.Constants;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

/**
 * 代码源的定义
 * @author Winter Lau<javayou@gmail.com>
 */
public class CodeRepository {

    public final static String SCM_GIT = "git";
    public final static String SCM_SVN = "svn";
    public final static String SCM_FILE = "file";

    private long   id;          //仓库编号
    private String scm;         //代码源类型：git/svn/file
    private String name;        //仓库名称
    private String url;         //仓库地址，ex: https://gitee.com/ld/J2Cache
    private String lastCommitId;//最后提交编号
    private String status;      //最后状态

    /**
     * 返回在仓库目录下的相对存储路径
     * @return
     */
    public String getRelativePath() {
        return String.format("%03d/%03d/%03d/%s_%d", id/1_000_000_000, id % 1_000_000_000 / 1_000_000, id % 1_000_000 / 1_000, name, id);
    }

    public long getId() {
        return id;
    }

    public String getIdAsString() {
        return String.valueOf(id);
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getScm() {
        return scm;
    }

    public void setScm(String scm) {
        this.scm = scm;
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

    public String getLastCommitId() {
        return lastCommitId;
    }

    public void setLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * generate lucene document
     * @return
     */
    public Document toDocument() {
        Document doc = new Document();
        doc.add(new StringField(Constants.FIELD_REPO_ID,        this.getIdAsString(), Field.Store.YES));
        doc.add(new StoredField(Constants.FIELD_REPO_URL,       this.getUrl()));
        doc.add(new StoredField(Constants.FIELD_REPO_NAME,      this.getName()));
        if(this.getLastCommitId() != null)
            doc.add(new StoredField(Constants.FIELD_REVISION,   this.getLastCommitId()));
        if(this.getScm() != null)
            doc.add(new StoredField(Constants.FIELD_SCM,        this.getScm()));
        if(this.getStatus() != null)
            doc.add(new StringField(Constants.FIELD_STATUS,     this.getStatus(),     Field.Store.YES));
        return doc;
    }

    /**
     * parse from lucene document
     * @param doc
     * @return
     */
    public static CodeRepository parse(Document doc) {
        CodeRepository repo = new CodeRepository();
        repo.setId(NumberUtils.toLong(doc.get(Constants.FIELD_REPO_ID), 0));
        repo.setName(doc.get(Constants.FIELD_REPO_NAME));
        repo.setUrl(doc.get(Constants.FIELD_REPO_URL));
        repo.setLastCommitId(doc.get(Constants.FIELD_REVISION));
        repo.setScm(doc.get(Constants.FIELD_SCM));
        repo.setStatus(doc.get(Constants.FIELD_STATUS));
        return repo;
    }

    @Override
    public String toString() {
        return String.format("CodeRepository(%d,%s,%s(%s),%s)", id, name, url, scm, this.getRelativePath());
    }
}
