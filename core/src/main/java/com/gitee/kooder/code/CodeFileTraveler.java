package com.gitee.kooder.code;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.index.IndexManager;
import com.gitee.kooder.models.SourceFile;
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
 * Travel all file in repository and build index for it
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
     * update source file index
     *
     * @param codeid document object
     */
    @Override
    public void updateDocument(SourceFile codeid) {
        //log.info("updateDocument:" + codeid);
        try {
            Document doc = buildFacetDocument(codeid.getDocument());
            writer.updateDocument(new Term(Constants.FIELD_UUID, codeid.getUuid()), doc);
        } catch (IllegalArgumentException e) {
            log.error(codeid.getContents(), e);
            throw e;
        } catch (IOException e) {
            log.error("Failed to update ducment<code> with uuid = " + codeid.getUuid(), e);
        }
    }

    /**
     * Delete single file document
     *
     * @param codeid
     * @return
     */
    @Override
    public void deleteDocument(SourceFile codeid) {
        //log.info("deleteDocument:" + codeid);
        try {
            writer.deleteDocuments(new Term(Constants.FIELD_UUID, codeid.getUuid()));
        } catch (IOException e) {
            log.error("Failed to delete ducment<code> with uuid = " + codeid.getUuid(), e);
        }
    }

    /**
     * Clear all file document belong to one repository
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

    /**
     * Building facet document
     * @param doc
     * @return
     * @throws IOException
     */
    private Document buildFacetDocument(Document doc) throws IOException {
        FacetsConfig facetsConfig = IndexManager.facetsConfig;
        return facetsConfig.build(taxonomyWriter, doc);
    }
}
