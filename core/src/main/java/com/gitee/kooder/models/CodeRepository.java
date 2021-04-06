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

import com.gitee.kooder.code.RepositoryManager;
import com.gitee.kooder.core.Constants;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

/**
 * 代码源的定义
 * @author Winter Lau<javayou@gmail.com>
 */
public class CodeRepository extends Searchable {

    public final static String SCM_GIT = "git";
    public final static String SCM_SVN = "svn";
    public final static String SCM_FILE = "file";

    public final static String STATUS_DONE  = "indexed";
    public final static String STATUS_FETCH = "fetched";

    private int enterprise = 0; //所属企业id
    private String scm;         //代码源类型：git/svn/file
    private String vender;      // git vender name, for SourceFile
    private String name;        //仓库名称
    private String url;         //仓库地址，ex: https://gitee.com/ld/J2Cache
    private String lastCommitId;//最后提交编号
    private long   timestamp;   //状态最后更新时间
    private String status;      //最后状态

    public CodeRepository(){}

    public CodeRepository(Repository r) {
        this.id = r.getId();
        this.name = r.getName();
        this.url = r.getUrl();
        this.enterprise = (int)r.getEnterprise().getId();
    }

    /**
     * 返回在仓库目录下的相对存储路径
     * @return
     */
    public String getRelativePath() {
        return String.format("%03d/%03d/%03d/%s_%d", id/1_000_000_000, id % 1_000_000_000 / 1_000_000, id % 1_000_000 / 1_000, name, id);
    }

    public String getIdAsString() {
        return String.valueOf(id);
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

    public long getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(int enterprise) {
        this.enterprise = enterprise;
    }

    public String getVender() {
        return vender;
    }

    public void setVender(String vender) {
        this.vender = vender;
    }

    /**
     * generate lucene document
     *
     * @return
     */
    @Override
    public Document getDocument() {
        Document doc = new Document();
        doc.add(new StringField(Constants.FIELD_REPO_ID,        this.getIdAsString(), Field.Store.YES));
        doc.add(new StoredField(Constants.FIELD_ENTERPRISE_ID,  this.getEnterprise()));
        doc.add(new StoredField(Constants.FIELD_REPO_URL,       this.getUrl()));
        doc.add(new StoredField(Constants.FIELD_REPO_NAME,      this.getName()));
        if(this.getLastCommitId() != null)
            doc.add(new StoredField(Constants.FIELD_REVISION,   this.getLastCommitId()));
        if(this.getScm() != null)
            doc.add(new StoredField(Constants.FIELD_SCM,        this.getScm()));
        if(this.getStatus() != null)
            doc.add(new StringField(Constants.FIELD_STATUS,     this.getStatus(),     Field.Store.YES));
        doc.add(new StoredField(Constants.FIELD_TIMESTAMP,      System.currentTimeMillis()));
        return doc;
    }

    /**
     * Read fields from document
     *
     * @param doc
     */
    @Override
    public CodeRepository setDocument(Document doc) {
        this.setId(NumberUtils.toLong(doc.get(Constants.FIELD_REPO_ID), 0));
        this.setEnterprise(NumberUtils.toInt(doc.get(Constants.FIELD_ENTERPRISE_ID), 0));
        this.setName(doc.get(Constants.FIELD_REPO_NAME));
        this.setUrl(doc.get(Constants.FIELD_REPO_URL));
        this.setLastCommitId(doc.get(Constants.FIELD_REVISION));
        this.setScm(doc.get(Constants.FIELD_SCM));
        this.setStatus(doc.get(Constants.FIELD_STATUS));
        this.timestamp = NumberUtils.toLong(doc.get(Constants.FIELD_TIMESTAMP), 0);
        return this;
    }

    /**
     * 更新记录状态
     * @param newStatus
     */
    public void saveStatus(String newStatus) {
        this.status = newStatus;
        RepositoryManager.INSTANCE.save(this);
    }

    @Override
    public String toString() {
        return String.format("CodeRepository(%d,%s,%s(%s),%s)", id, name, url, scm, this.getRelativePath());
    }
}
