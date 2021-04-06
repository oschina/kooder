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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gitee.kooder.core.Constants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.*;

import java.util.List;

/**
 * Source File Object
 * @author Winter Lau<javayou@gmail.com>
 */
public final class SourceFile extends Searchable {

    private String vender;          // gitee,gitlab or gitea, using this field to indentify file url
    private String uuid;            // file unique identify
    private int enterprise;         // enterprise
    private Relation repository = Relation.EMPTY();    //repository, use this field to delete all files of repository

    private String branch;          // branch name
    private String name;            // file name
    private String url;             // absolute file url
    private String location;        // Path to file relative to repo location
    private String contents;
    private String hash;            // content sha1 hash
    private String codeOwner;
    private String language;

    private int lines;              // How many lines in the file
    private int codeLines;          // How many lines are code
    private int commentLines;       // How many lines are comments
    private int blankLines;         // How many lines are blank
    private int complexity;         // Complexity calculation taken from scc

    private String revision;        // last commit id

    private List<CodeLine> result;  // code lines with keyword highlight

    public SourceFile() {
    }
    public SourceFile(String vender) {
        this.vender = vender;
    }

    public SourceFile(long repoId, String repoName, String fileLocation) {
        this.repository.id = repoId;
        this.repository.name = repoName;
        this.location = fileLocation;
        this.generateUuid();
    }

    public String generateUuid() {
        this.uuid = DigestUtils.sha1Hex(String.format("%d-%s-%s", repository.getId(), repository.name.toLowerCase(), location));
        return this.uuid;
    }

    public void generateUrl() {
        String rurl = repository.getUrl();
        if(rurl.endsWith(".git"))
            rurl = rurl.substring(0, rurl.length() - 4);
        if(Constants.GITEA.equals(this.vender))
            this.setUrl(rurl + "/src/branch/" + this.getBranch() + "/" + this.getLocation());
        else
            this.setUrl(rurl + "/tree/" + this.getBranch() + "/" + this.getLocation());
    }

    /**
     * Read fields from document
     * @param doc
     */
    @Override
    @JsonIgnore
    public SourceFile setDocument(Document doc) {
        this.vender = doc.get(Constants.FIELD_VENDER);
        this.uuid = doc.get(Constants.FIELD_UUID);
        this.enterprise = NumberUtils.toInt(doc.get(Constants.FIELD_ENTERPRISE_ID), 0);
        this.repository.id = NumberUtils.toInt(doc.get(Constants.FIELD_REPO_ID));
        this.repository.name = doc.get(Constants.FIELD_REPO_NAME);
        this.repository.url = doc.get(Constants.FIELD_REPO_URL);
        this.name = doc.get(Constants.FIELD_FILE_NAME);
        this.url = doc.get(Constants.FIELD_URL);
        this.location = doc.get(Constants.FIELD_FILE_LOCATION);
        this.contents = doc.get(Constants.FIELD_SOURCE);
        this.hash = doc.get(Constants.FIELD_FILE_HASH);
        this.codeOwner = doc.get(Constants.FIELD_CODE_OWNER);
        this.language = doc.get(Constants.FIELD_LANGUAGE);
        this.revision = doc.get(Constants.FIELD_REVISION);

        this.lines = getIntField(doc, Constants.FIELD_LINES_TOTAL, 0);
        this.codeLines = getIntField(doc, Constants.FIELD_LINES_CODE, 0);
        this.commentLines = getIntField(doc, Constants.FIELD_LINES_COMMENT, 0);
        this.blankLines = getIntField(doc, Constants.FIELD_LINES_BLANK, 0);
        this.complexity = getIntField(doc, Constants.FIELD_COMPLEXITY);

        return this;
    }

    /**
     * generate lucene document
     * @return
     */
    @Override
    @JsonIgnore
    public Document getDocument() {
        Document document = new Document();

        document.add(new StoredField(Constants.FIELD_VENDER,    this.vender));
        // Uuid is the primary key for documents
        document.add(new StringField(Constants.FIELD_UUID,      this.uuid,   Field.Store.YES));

        if(StringUtils.isNotBlank(this.branch))
            document.add(new StringField(Constants.FIELD_BRANCH,this.branch, Field.Store.YES));
        document.add(new StoredField(Constants.FIELD_URL,       this.url));

        super.addLongToDoc(document, Constants.FIELD_ENTERPRISE_ID, this.enterprise);

        //repository info
        super.addLongToDoc(document, Constants.FIELD_REPO_ID,       this.repository.id);
        super.addFacetToDoc(document, Constants.FIELD_REPO_NAME,    this.repository.name);
        document.add(new StringField(Constants.FIELD_REPO_URL,      this.repository.url,    Field.Store.YES));

        //file meta
        if (StringUtils.isNotBlank(language))
            super.addFacetToDoc(document, Constants.FIELD_LANGUAGE,     this.language);

        if (StringUtils.isNotBlank(codeOwner))
            super.addFacetToDoc(document, Constants.FIELD_CODE_OWNER,   this.codeOwner);

        //file info
        document.add(new TextField(Constants.FIELD_FILE_NAME,       this.getName(),         Field.Store.YES));
        document.add(new StringField(Constants.FIELD_FILE_LOCATION, this.getLocation(),     Field.Store.YES));

        if(StringUtils.isNotBlank(this.getContents())) {
            document.add(new TextField(Constants.FIELD_SOURCE, this.getContents(), Field.Store.YES));
            //文件属性
            document.add(new StoredField(Constants.FIELD_FILE_HASH, this.getHash()));
        }

        //文件统计信息
        document.add(new StoredField(Constants.FIELD_LINES_TOTAL,   this.getLines()));
        document.add(new StoredField(Constants.FIELD_LINES_CODE,    this.getCodeLines()));
        document.add(new StoredField(Constants.FIELD_LINES_BLANK,   this.getBlankLines()));
        document.add(new StoredField(Constants.FIELD_LINES_COMMENT, this.getCommentLines()));
        document.add(new StoredField(Constants.FIELD_COMPLEXITY,    this.getComplexity()));

        document.add(new StringField(Constants.FIELD_REVISION,      this.getRevision(),     Field.Store.YES));

        // Extra metadata in this case when it was last indexed
        long indexTime = System.currentTimeMillis();
        super.addNumToDoc(document, Constants.FIELD_LAST_INDEX, indexTime);

        return document;
    }

    public String getVender() {
        return vender;
    }

    public void setVender(String vender) {
        this.vender = vender;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Relation getRepository() {
        return repository;
    }

    public void setRepository(Relation repository) {
        this.repository = repository;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getCodeOwner() {
        return codeOwner;
    }

    public void setCodeOwner(String codeOwner) {
        this.codeOwner = codeOwner;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public int getCodeLines() {
        return codeLines;
    }

    public void setCodeLines(int codeLines) {
        this.codeLines = codeLines;
    }

    public int getCommentLines() {
        return commentLines;
    }

    public void setCommentLines(int commentLines) {
        this.commentLines = commentLines;
    }

    public int getBlankLines() {
        return blankLines;
    }

    public void setBlankLines(int blankLines) {
        this.blankLines = blankLines;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public int getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(int enterprise) {
        this.enterprise = enterprise;
    }

    public List<CodeLine> getResult() {
        return result;
    }

    public void setResult(List<CodeLine> result) {
        this.result = result;
    }
}
