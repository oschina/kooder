package com.gitee.search.code;

import com.gitee.search.core.Constants;
import com.gitee.search.index.IndexManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 处理代码文件索引
 * @author Winter Lau<javayou@gmail.com>
 */
public class CodeFileTraveler implements FileTraveler {

    private final static Logger log = LoggerFactory.getLogger(CodeFileTraveler.class);

    private IndexWriter writer;
    private TaxonomyWriter taxonomyWriter;

    public CodeFileTraveler(IndexWriter writer, TaxonomyWriter taxonomyWriter) {
        this.writer = writer;
        this.taxonomyWriter = taxonomyWriter;
    }

    /**
     * 更新源码文档（新文件、更改文件）
     *
     * @param codeid 文档信息
     * @return true: 继续下一个文档， false 不再处理下面文档
     */
    @Override
    public void updateDocument(CodeIndexDocument codeid) {
        //log.info("updateDocument:" + codeid);
        try {
            Document doc = buildFacetDocument(codeid.buildDocument());
            writer.updateDocument(new Term(Constants.FIELD_UUID,codeid.getUuid()), doc);
        } catch (IOException e) {
            log.error("Failed to update ducment<code> with uuid = " + codeid.getUuid(), e);
        }
    }

    /**
     * 删除文档
     *
     * @param codeid
     * @return
     */
    @Override
    public void deleteDocument(CodeIndexDocument codeid) {
        //log.info("deleteDocument:" + codeid);
        try {
            writer.deleteDocuments(new Term(Constants.FIELD_UUID, codeid.getUuid()));
        } catch (IOException e) {
            log.error("Failed to delete ducment<code> with uuid = " + codeid.getUuid(), e);
        }
    }

    /**
     * 清空仓库所有文件，以待重建
     *
     * @param repoId
     */
    @Override
    public void resetRepository(long repoId) {
        //log.info("resetRepository:" + repoId);
        try {
            writer.deleteDocuments(NumericDocValuesField.newSlowExactQuery(Constants.FIELD_REPO_ID, repoId));
        } catch (IOException e) {
            log.error("Failed to reset repository with id = " + repoId, e);
        }
    }

    private Document buildFacetDocument(Document doc) throws IOException {
        FacetsConfig facetsConfig = IndexManager.facetsConfig;
        return facetsConfig.build(taxonomyWriter, doc);
    }
}
