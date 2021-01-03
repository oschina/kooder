package com.gitee.search.code;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.search.core.Constants;
import com.searchcode.app.config.Values;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import spark.utils.StringUtils;

/**
 * 代码文档对象
 * @author Winter Lau<javayou@gmail.com>
 */
public class CodeIndexDocument {

    private String uuid;                //file unique identify
    private long   repoId;              //repository id, use this field to delete all files of repository
    private String repoName;
    private String repoURL;
    private String fileName;
    private String fileLocation;        // Path to file relative to repo location
    private String contents;
    private String sha1Hash;            //content hash
    private String codeOwner;
    private String language;

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
            document.add(new SortedSetDocValuesFacetField(Constants.FIELD_LANGUAGE,     this.getLanguage()));
        if (StringUtils.isNotBlank(repoName))
            document.add(new SortedSetDocValuesFacetField(Constants.FIELD_REPO_NAME,    this.getRepoName()));
        if (StringUtils.isNotBlank(codeOwner))
            document.add(new SortedSetDocValuesFacetField(Constants.FIELD_CODE_OWNER,   this.getCodeOwner()));
        if(StringUtils.isNotBlank(scm))
            document.add(new SortedSetDocValuesFacetField(Constants.FIELD_SCM,          this.getScm()));

        //仓库信息
        document.add(new NumericDocValuesField(Constants.FIELD_REPO_ID,       this.getRepoId()));
        document.add(new StoredField(Constants.FIELD_REPO_ID,       this.getRepoId()));
        document.add(new StringField(Constants.FIELD_REPO_NAME,     this.getRepoName(),         Field.Store.YES));
        document.add(new StringField(Constants.FIELD_REPO_URL,      this.getRepoURL(),          Field.Store.YES));

        //文件信息
        document.add(new TextField(Constants.FIELD_FILE_NAME,       this.getFileName(),         Field.Store.YES));
        document.add(new TextField(Constants.FIELD_FILE_LOCATION,   this.getFileLocation(),     Field.Store.YES));
        document.add(new TextField(Constants.FIELD_CONTENTS,        this.getContents(),         Field.Store.NO));

        //文件属性
        document.add(new StringField(Constants.FIELD_CODE_OWNER,    this.getCodeOwner(),        Field.Store.YES));
        document.add(new StringField(Constants.FIELD_LANGUAGE,      this.getLanguage(),         Field.Store.YES));
        document.add(new StoredField(Constants.FIELD_FILE_HASH,     this.getSha1Hash()));

        //文件统计信息
        document.add(new StoredField(Values.LINES,                  this.getLines()));
        document.add(new StoredField(Values.CODELINES,              this.getCodeLines()));
        document.add(new StoredField(Values.BLANKLINES,             this.getBlankLines()));
        document.add(new StoredField(Values.COMMENTLINES,           this.getCommentLines()));
        document.add(new StoredField(Values.COMPLEXITY,             this.getComplexity()));

        document.add(new StringField(Constants.FIELD_REVISION,      this.getRevision(),         Field.Store.YES));

        // Extra metadata in this case when it was last indexed
        document.add(new StoredField(Constants.FIELD_LAST_INDEX, System.currentTimeMillis()));

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
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        }catch(JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
