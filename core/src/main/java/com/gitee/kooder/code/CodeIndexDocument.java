package com.gitee.kooder.code;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.utils.JsonUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetField;

/**
 * Source File of repository
 * @author Winter Lau<javayou@gmail.com>
 */
public class CodeIndexDocument {

    private String uuid;                // file unique identify
    private long   repoId;              // repository id, use this field to delete all files of repository
    private String repoName;            // repository name, for facet query
    private String repoURL;             // repository url
    private String fileName;            // source file name
    private String fileLocation;        // Path to file relative to repo location
    private String contents;            // file content
    private String sha1Hash;            // content hash
    private String codeOwner;           // file main developer name
    private String language;            // programming language

    private int lines;                  // How many lines in the file
    private int codeLines;              // How many lines are code
    private int commentLines;           // How many lines are comments
    private int blankLines;             // How many lines are blank
    private int complexity;             // Complexity calculation taken from scc

    private String revision;            //last commit id
    private String scm;                 //scm

    public CodeIndexDocument() {}

    public CodeIndexDocument(long repoId, String repoName, String fileLocation) {
        this.repoId = repoId;
        this.repoName = repoName;
        this.fileLocation = fileLocation;
        this.generateUuid();
    }

    /**
     * Builds a document ready to be indexed by lucene
     */
    public Document buildDocument() {
        Document document = new Document();

        // Uuid is the primary key for documents
        document.add(new StringField(Constants.FIELD_UUID, uuid, Field.Store.YES));

        //文档维度
        if (StringUtils.isNotBlank(language))
            document.add(new FacetField(Constants.FIELD_LANGUAGE,     this.getLanguage()));
        if (StringUtils.isNotBlank(repoName))
            document.add(new FacetField(Constants.FIELD_REPO_NAME,    this.getRepoName()));
        if (StringUtils.isNotBlank(codeOwner))
            document.add(new FacetField(Constants.FIELD_CODE_OWNER,   this.getCodeOwner()));
        if(StringUtils.isNotBlank(scm))
            document.add(new FacetField(Constants.FIELD_SCM,          this.getScm()));

        //仓库信息
        document.add(new NumericDocValuesField(Constants.FIELD_REPO_ID, this.getRepoId()));
        document.add(new StoredField(Constants.FIELD_REPO_ID,           this.getRepoId()));
        document.add(new StringField(Constants.FIELD_REPO_NAME,         this.getRepoName(),     Field.Store.YES));
        document.add(new StringField(Constants.FIELD_REPO_URL,          this.getRepoURL(),      Field.Store.YES));

        //文件信息
        document.add(new TextField(Constants.FIELD_FILE_NAME,       this.getFileName(),         Field.Store.YES));
        document.add(new StringField(Constants.FIELD_FILE_LOCATION, this.getFileLocation(),     Field.Store.YES));
        document.add(new TextField(Constants.FIELD_SOURCE,          this.getContents(),         Field.Store.YES));

        //文件属性
        document.add(new TextField(Constants.FIELD_CODE_OWNER,      this.getCodeOwner(),        Field.Store.YES));
        document.add(new StringField(Constants.FIELD_LANGUAGE,      this.getLanguage(),         Field.Store.YES));
        document.add(new StoredField(Constants.FIELD_FILE_HASH,     this.getSha1Hash()));

        //文件统计信息
        document.add(new StoredField(Constants.FIELD_LINES_TOTAL,   this.getLines()));
        document.add(new StoredField(Constants.FIELD_LINES_CODE,    this.getCodeLines()));
        document.add(new StoredField(Constants.FIELD_LINES_BLANK,   this.getBlankLines()));
        document.add(new StoredField(Constants.FIELD_LINES_COMMENT, this.getCommentLines()));
        document.add(new StoredField(Constants.FIELD_COMPLEXITY,    this.getComplexity()));

        document.add(new StringField(Constants.FIELD_REVISION,      this.getRevision(),         Field.Store.YES));

        // Extra metadata in this case when it was last indexed
        long indexTime = System.currentTimeMillis();
        document.add(new SortedNumericDocValuesField(Constants.FIELD_LAST_INDEX, indexTime));
        document.add(new StoredField(Constants.FIELD_LAST_INDEX, indexTime));

        return document;
    }

    public String generateUuid() {
        this.uuid = DigestUtils.sha1Hex(String.format("%d-%s-%s", getRepoId(), getRepoName().toLowerCase(), getFileLocation()));
        return this.uuid;
    }

    public String getUuid() {
        return this.uuid;
    }

    public CodeIndexDocument setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public long getRepoId() {
        return repoId;
    }

    public void setRepoId(long repoId) {
        this.repoId = repoId;
    }

    public String getRepoName() {
        return repoName;
    }

    public CodeIndexDocument setRepoName(String repoName) {
        this.repoName = repoName;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public CodeIndexDocument setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public CodeIndexDocument setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
        return this;
    }

    public String getSha1Hash() {
        return sha1Hash;
    }

    public CodeIndexDocument setSha1Hash(String sha1Hash) {
        this.sha1Hash = sha1Hash;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public CodeIndexDocument setLanguage(String language) {
        this.language = language;
        return this;
    }

    public int getLines() {
        return this.lines;
    }

    public CodeIndexDocument setLines(int lines) {
        this.lines = lines;
        return this;
    }

    public int getCodeLines() {
        return codeLines;
    }

    public CodeIndexDocument setCodeLines(int codeLines) {
        this.codeLines = codeLines;
        return this;
    }

    public int getBlankLines() {
        return this.blankLines;
    }

    public CodeIndexDocument setBlankLines(int blankLines) {
        this.blankLines = blankLines;
        return this;
    }

    public int getCommentLines() {
        return this.commentLines;
    }

    public CodeIndexDocument setCommentLines(int commentLines) {
        this.commentLines = commentLines;
        return this;
    }

    public int getComplexity() {
        return this.complexity;
    }

    public CodeIndexDocument setComplexity(int complexity) {
        this.complexity = complexity;
        return this;
    }

    public String getContents() {
        return contents;
    }

    public CodeIndexDocument setContents(String contents) {
        this.contents = contents;
        return this;
    }

    public String getRepoURL() {
        return repoURL;
    }

    public CodeIndexDocument setRepoURL(String repoURL) {
        this.repoURL = repoURL;
        return this;
    }

    public String getCodeOwner() {
        return codeOwner;
    }

    public CodeIndexDocument setCodeOwner(String codeOwner) {
        this.codeOwner = codeOwner;
        return this;
    }

    public String getRevision() {
        return revision;
    }

    public CodeIndexDocument setRevision(String revision) {
        this.revision = revision;
        return this;
    }

    public String getScm() {
        return scm;
    }

    public void setScm(String scm) {
        this.scm = scm;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }

}
